package it.jnrpe.osgi;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.osgi.framework.Bundle;

public class BundleDelegatingClassLoader extends ClassLoader {

    private final Bundle bundle;

    public BundleDelegatingClassLoader(final Bundle b) {
        bundle = b;
    }

    @Override
    protected Class<?> findClass(final String className) throws ClassNotFoundException {
        return bundle.loadClass(className);
    }

    @Override
    protected URL findResource(final String resName) {
        return bundle.getResource(resName);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Enumeration<URL> findResources(final String resName) throws IOException {
        return bundle.getResources(resName);
    }

}