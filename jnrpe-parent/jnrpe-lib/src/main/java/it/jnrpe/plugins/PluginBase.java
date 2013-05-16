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
public abstract class PluginBase implements IPluginInterfaceEx {
    /**
     * The list of listener registered for the events raised by this plugin.
     */
    private Set<IJNRPEEventListener> listenersSet =
            new HashSet<IJNRPEEventListener>();

    protected final Logger log = new Logger();

    /**
     * This class represent a generic logger that will be used by plugins
     * extending the {@link PluginBase} to write logs.
     *
     * @author Massimiliano Ziccardi
     *
     */
    protected class Logger {

        /**
         * Writes a trace message.
         *
         * @param message
         *            the message
         */
        public final void trace(final String message) {
            EventsUtil.sendEvent(listenersSet, PluginBase.this, LogEvent.TRACE,
                    message);
        }

        /**
         * Writes a trace message and logs the exception.
         *
         * @param message
         *            the message
         * @param exc
         *            the exception to be logged
         */
        public final void trace(final String message, final Throwable exc) {
            EventsUtil.sendEvent(listenersSet, PluginBase.this, LogEvent.TRACE,
                    message, exc);
        }

        /**
         * Writes a debug message.
         *
         * @param message
         *            the message
         */
        public final void debug(final String message) {
            EventsUtil.sendEvent(listenersSet, PluginBase.this, LogEvent.DEBUG,
                    message);
        }

        /**
         * Writes a debug message and logs the exception.
         *
         * @param message
         *            the message
         * @param exc
         *            the exception to be logged
         */
        public final void debug(final String message, final Throwable exc) {
            EventsUtil.sendEvent(listenersSet, PluginBase.this, LogEvent.DEBUG,
                    message, exc);
        }

        /**
         * Writes an info message.
         *
         * @param message
         *            the message
         */
        public final void info(final String message) {
            EventsUtil.sendEvent(listenersSet, PluginBase.this, LogEvent.INFO,
                    message);
        }

        /**
         * Writes an info message and logs the exception.
         *
         * @param message
         *            the message
         * @param exc
         *            the exception to be logged
         */
        public final void info(final String message, final Throwable exc) {
            EventsUtil.sendEvent(listenersSet, PluginBase.this, LogEvent.INFO,
                    message, exc);
        }

        /**
         * Writes a warning message.
         *
         * @param message
         *            the message
         */
        public final void warn(final String message) {
            EventsUtil.sendEvent(listenersSet, PluginBase.this,
                    LogEvent.WARNING, message);
        }

        /**
         * Writes a warning message and logs the exception.
         *
         * @param message
         *            the message
         * @param exc
         *            the exception to be logged
         */
        public final void warn(final String message, final Throwable exc) {
            EventsUtil.sendEvent(listenersSet, PluginBase.this,
                    LogEvent.WARNING, message,
                    exc);
        }

        /**
         * Writes an error message.
         *
         * @param message
         *            the message
         */
        public final void error(final String message) {
            EventsUtil.sendEvent(listenersSet, PluginBase.this, LogEvent.ERROR,
                    message);
        }

        /**
         * Writes an error message and logs the exception.
         *
         * @param message
         *            the message
         * @param exc
         *            the exception to be logged
         */
        public final void error(final String message, final Throwable exc) {
            EventsUtil.sendEvent(listenersSet, PluginBase.this, LogEvent.ERROR,
                    message,
                    exc);
        }

        /**
         * Writes a fatal message.
         *
         * @param message
         *            the message
         */
        public final void fatal(final String message) {
            EventsUtil.sendEvent(listenersSet, PluginBase.this, LogEvent.FATAL,
                    message);
        }

        /**
         * Writes a fatal message and logs the exception.
         *
         * @param message
         *            the message
         * @param exc
         *            the exception to be logged
         */
        public final void fatal(final String message, final Throwable exc) {
            EventsUtil.sendEvent(listenersSet, PluginBase.this, LogEvent.FATAL,
                    message,
                    exc);
        }
    }

    /**
     * Adds a new listener to the list of objects that will receive the messages
     * sent by this class.
     *
     * @param listener
     *            The new listener
     */
    public final void addListener(final IJNRPEEventListener listener) {
        listenersSet.add(listener);
    }

    /**
     * Adds a new collection of listeners.
     *
     * @param listeners
     *            The collection of listeners to be added
     */
    public final void addListeners(
            final Collection<IJNRPEEventListener> listeners) {
        if (listeners == null) {
            return;
        }

        listenersSet.addAll(listeners);
    }

    /**
     * Sends an event.
     *
     * @param evt
     *            The event type
     * @param message
     *            The message
     */
    public final void sendEvent(final LogEvent evt, final String message) {
        EventsUtil.sendEvent(listenersSet, this, evt, message);
    }

    /**
     * Sends an event.
     *
     * @param evt
     *            The event type
     * @param message
     *            The message
     * @param exc
     *            The exception to be attached to the event
     */
    public final void sendEvent(final LogEvent evt, final String message,
            final Exception exc) {
        EventsUtil.sendEvent(listenersSet, this, evt, message, exc);
    }

    /**
     * Sends a custom event.
     *
     * @param customEventName
     *            The custom event identifier
     * @param paramsAry
     *            The parameter of the event. Can be null.
     */
    public final void sendEvent(final String customEventName,
            final EventParam... paramsAry) {
        EventsUtil.sendEvent(listenersSet, this, customEventName, paramsAry);
    }

    /**
     * Returns all the registered listeners.
     *
     * @return All the listeners
     */
    protected final Set<IJNRPEEventListener> getListeners() {
        return listenersSet;
    }
}
