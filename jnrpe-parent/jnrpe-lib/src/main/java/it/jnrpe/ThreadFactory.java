/*
 * Copyright (c) 2008 Massimiliano Ziccardi Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package it.jnrpe;

import it.jnrpe.commands.CommandInvoker;

import java.net.Socket;

/**
 * This class implements a simple thread factory. Each binding has its own
 * thread factory.
 * 
 * @author Massimiliano Ziccardi
 * 
 */
class ThreadFactory
{
    /**
     * How many milliseconds to wait for a thread to stop.
     */
    private static final int     DEFAULT_THREAD_STOP_TIMEOUT = 5000;

    /**
     * Timeout handler.
     */
    private ThreadTimeoutWatcher m_watchDog                  = null;

    /**
     * The invoker object.
     */
    private final CommandInvoker m_commandInvoker;

    /**
     * Constructs a new thread factory.
     * 
     * @param iThreadTimeout
     *            The thread timeout
     * @param commandInvoker
     *            The command invoker
     */
    public ThreadFactory(final int iThreadTimeout,
            final CommandInvoker commandInvoker)
    {
        m_commandInvoker = commandInvoker;

        m_watchDog = new ThreadTimeoutWatcher();
        m_watchDog.setThreadTimeout(iThreadTimeout);
        m_watchDog.start();
    }

    /**
     * Asks the system level thread factory for a new thread.
     * 
     * @param s
     *            The socket to be served by the thread
     * @return The newly created thread
     */
    public JNRPEServerThread createNewThread(final Socket s)
    {
        JNRPEServerThread t = JNRPEServerThreadFactory.getInstance(
                m_commandInvoker).createNewThread(s);
        m_watchDog.watch(t);
        return t;
    }

    /**
     * Stops all the created threads and stops the timeout watcher.
     */
    public void shutdown()
    {
        try
        {
            m_watchDog.stopWatching();
            // Waits for the thread to stop.
            m_watchDog.join(DEFAULT_THREAD_STOP_TIMEOUT);
        }
        catch (InterruptedException ie)
        {
            // This should never happen...
        }
    }
}
