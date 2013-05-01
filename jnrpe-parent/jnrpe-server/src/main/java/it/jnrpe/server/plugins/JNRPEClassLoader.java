/*
 * Copyright (c) 2011 Massimiliano Ziccardi
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package it.jnrpe.server.plugins;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.jar.JarFile;

/**
 * A parent-last classloader that will try the child classloader first and then
 * the parent. This takes a fair bit of doing because java really prefers
 * parent-first.
 * 
 * For those not familiar with class loading trickery, be wary
 */
class JNRPEClassLoader extends ClassLoader
{
    private List<URL> m_vUrls;
    private ChildURLClassLoader childClassLoader;

    /**
     * This class allows me to call findClass on a classloader
     */
    private static class FindClassClassLoader extends ClassLoader
    {
        public FindClassClassLoader(ClassLoader parent)
        {
            super(parent);
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException
        {
            return super.findClass(name);
        }
    }

    /**
     * This class delegates (child then parent) for the findClass method for a
     * URLClassLoader. We need this because findClass is protected in
     * URLClassLoader
     */
    private static class ChildURLClassLoader extends URLClassLoader
    {
        private FindClassClassLoader realParent;

        public ChildURLClassLoader(URL[] urls, FindClassClassLoader realParent)
        {
            super(urls, null);

            this.realParent = realParent;
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException
        {
            try
            {
                // first try to use the URLClassLoader findClass
                return super.findClass(name);
            }
            catch (ClassNotFoundException e)
            {
                // if that fails, we ask our real parent classloader to load the
                // class (we give up)
                return realParent.loadClass(name);
            }
        }
    }

    public JNRPEClassLoader(List<URL> classpath)
    {
        super(Thread.currentThread().getContextClassLoader());
        m_vUrls = classpath;

        final URL[] urls = classpath.toArray(new URL[classpath.size()]);

        final ClassLoader parent = getParent();
        
        childClassLoader = AccessController.doPrivileged(
                new PrivilegedAction<ChildURLClassLoader>()
                {
                    public ChildURLClassLoader run() {
                        return new ChildURLClassLoader(urls,
                                new FindClassClassLoader(parent));
                    }

                }
                );
//        childClassLoader = new ChildURLClassLoader(urls,
//                new FindClassClassLoader(this.getParent()));
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException
    {
        try
        {
            // first we try to find a class inside the child classloader
            return childClassLoader.findClass(name);
        }
        catch (ClassNotFoundException e)
        {
            // didn't find it, try the parent
            return super.loadClass(name, resolve);
        }
    }

    @Override
    protected URL findResource(String name)
    {
        
        JarFile jf = null;
        try
        {
            for (URL u : m_vUrls)
            {
                jf = new JarFile(u.getFile());
                String resourceUrl = "jar:" + new File(u.getFile()).toURI() + "!/" + name;
                if (jf.getEntry("plugin.xml") != null)
                    return new URL(resourceUrl);
            }
            return null;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
        finally
        {
            if (jf != null)
            {
                try
                {
                    jf.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
