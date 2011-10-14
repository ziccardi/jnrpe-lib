/*
 * Copyright (c) 2008 Massimiliano Ziccardi
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
package it.jnrpe;

import it.jnrpe.commands.CommandInvoker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * Thread that listen on a given IP:PORT.
 * 
 * @author Massimiliano Ziccardi
 */
class JNRPEListenerThread extends Thread
{
    private ServerSocket m_serverSocket = null;
    
    private List<InetAddress> m_vAcceptedHosts = new ArrayList<InetAddress>();
    private ThreadFactory m_threadFactory = null;

    private final String m_sBindingAddress;
    private final int m_iBindingPort;
    private final CommandInvoker m_commandInvoker;
    
    private boolean m_bSSL = false;
    
    private int m_iCommandExecutionTimeout = 20000;
    
    JNRPEListenerThread(String sBindingAddress, int iBindingPort, CommandInvoker commandInvoker)
    {
        m_sBindingAddress = sBindingAddress;
        m_iBindingPort = iBindingPort;
        m_commandInvoker = commandInvoker;
//        try
//        {
//            init();
//        }
//        catch (Exception e)
//        {
//            throw new BindException(e.getMessage());
//        }
    }

    public void enableSSL()
    {
        m_bSSL = true;
    }
    
    
    /**
     * Returns the SSL factory to be used to create the Server Socket
     * @throws KeyStoreException 
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws CertificateException 
     * @throws UnrecoverableKeyException 
     * @throws KeyManagementException 
     * 
     * @see it.intesa.fi2.client.network.ISSLObjectsFactory#getSSLSocketFactory(String, String, String)
     */
    public SSLServerSocketFactory getSSLSocketFactory(
        String sKeyStoreFile,
        String sKeyStorePwd,
        String sKeyStoreType) throws KeyStoreException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException, KeyManagementException 
    {
        if (sKeyStoreFile == null)
            throw new KeyStoreException("KEYSTORE HAS NOT BEEN SPECIFIED");
        if (!new File(sKeyStoreFile).exists())
            throw new KeyStoreException("COULD NOT FIND KEYSTORE '" + sKeyStoreFile + "'");

        if (sKeyStorePwd == null)
            throw new KeyStoreException("KEYSTORE PASSWORD HAS NOT BEEN SPECIFIED");
        
        SSLContext ctx;
        KeyManagerFactory kmf;

        try
        {
            ctx = SSLContext.getInstance("SSLv3");
            
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            
            //KeyStore ks = getKeystore(sKeyStoreFile, sKeyStorePwd, sKeyStoreType);
            KeyStore ks = KeyStore.getInstance(sKeyStoreType);
            ks.load(new FileInputStream(sKeyStoreFile), sKeyStorePwd.toCharArray());
            
            char[] passphrase = sKeyStorePwd.toCharArray();
            kmf.init(ks, passphrase);
            ctx.init(kmf.getKeyManagers(), null, new java.security.SecureRandom());           
            
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new SSLException ("Unable to initialize SSLSocketFactory.\n" + e.getMessage());
        }
        
        return ctx.getServerSocketFactory();
    }
    
    
    private void init() throws IOException, KeyManagementException, KeyStoreException, CertificateException, UnrecoverableKeyException
    {
        InetAddress addr = InetAddress.getByName(m_sBindingAddress);
        ServerSocketFactory sf = null;
        
        if (m_bSSL)
        {
            // TODO: configurazione keystore
            //sf = getSSLSocketFactory(m_Binding.getKeyStoreFile(), m_Binding.getKeyStorePassword(), "JKS");
        }
        else
            sf = ServerSocketFactory.getDefault();

        m_serverSocket = sf.createServerSocket(m_iBindingPort, 0, addr);
        if (m_serverSocket instanceof SSLServerSocket)
            ((SSLServerSocket)m_serverSocket).setEnabledCipherSuites(((SSLServerSocket) m_serverSocket).getSupportedCipherSuites());
        
        // Init the thread factory
        m_threadFactory = new ThreadFactory(m_iCommandExecutionTimeout, m_commandInvoker);
    }

    public void addAcceptedHosts(String sHost) throws UnknownHostException
    {
        InetAddress addr = InetAddress.getByName(sHost);
        m_vAcceptedHosts.add(addr);
    }

    public void run()
    {
        try
        {
            init();
            
            while (true)
            {
                Socket clientSocket = m_serverSocket.accept();
                if (!canAccept(clientSocket.getInetAddress()))
                {
                    clientSocket.close();
                    continue;
                }

                JNRPEServerThread kk = m_threadFactory.createNewThread(clientSocket);
                kk.start();
            }
        }
        catch (SocketException se)
        {
            // This exception is thrown when the server socket is closed.
            // Ignoring
        }
        catch (Exception e)
        {
        }

        exit();
    }

    private synchronized void exit()
    {
        notify();
    }

    public synchronized void close()
    {
 
        try
        {
            m_serverSocket.close();
            wait();
        }
        catch (InterruptedException ie)
        {

        }
        catch (IOException e)
        {
        }
    }

    private boolean canAccept(InetAddress inetAddress)
    {
        for (InetAddress addr : m_vAcceptedHosts)
        {
            if (addr.equals(inetAddress))
                return true;
        }

        System.out.println ("Refusing connection to " + inetAddress);
        
        return false;
    }
}
