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
package it.jnrpe.plugins;

import it.jnrpe.events.EventParam;
import it.jnrpe.events.EventsUtil;
import it.jnrpe.events.IJNRPEEventListener;
import it.jnrpe.events.LogEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * If your plugin needs to send events and you don't mind extending this class,
 * you can save some coding using this as a base.
 * 
 * @author Massimiliano Ziccardi
 */
public abstract class PluginBase implements IPluginInterfaceEx
{
    /**
     * The list of listener registered for the events raised by this plugin.
     */
    private Set<IJNRPEEventListener> m_vListeners 
        = new HashSet<IJNRPEEventListener>();

    /**
     * Adds a new listener to the list of objects that will receive the
     * messages sent by this class.
     * 
     * @param listener
     *            The new listener
     */
    public final void addListener(final IJNRPEEventListener listener)
    {
        m_vListeners.add(listener);
    }

    /**
     * Adds a new collection of listeners.
     * 
     * @param listeners
     *            The collection of listeners to be added
     */
    public final void addListeners(
            final Collection<IJNRPEEventListener> listeners)
    {
        if (listeners == null)
        {
            return;
        }

        m_vListeners.addAll(listeners);
    }

    /**
     * Sends an event.
     * 
     * @param evt
     *            The event type
     * @param sMessage
     *            The message
     */
    public final void sendEvent(final LogEvent evt, final String sMessage)
    {
        EventsUtil.sendEvent(m_vListeners, this, evt, sMessage);
    }

    /**
     * Sends an event.
     * 
     * @param evt
     *            The event type
     * @param sMessage
     *            The message
     * @param exc
     *            The exception to be attached to the event
     */
    public final void sendEvent(final LogEvent evt, final String sMessage,
            final Exception exc)
    {
        EventsUtil.sendEvent(m_vListeners, this, evt, sMessage, exc);
    }

    /**
     * Sends a custom event.
     * 
     * @param sCustomEvent
     *            The custom event identifier
     * @param vParams
     *            The parameter of the event. Can be null.
     */
    public final void sendEvent(final String sCustomEvent,
            final EventParam... vParams)
    {
        EventsUtil.sendEvent(m_vListeners, this, sCustomEvent, vParams);
    }

    /**
     * Returns all the registered listeners.
     * 
     * @return All the listeners
     */
    protected final Set<IJNRPEEventListener> getListeners()
    {
        return m_vListeners;
    }
}
