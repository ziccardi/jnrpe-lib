/*
 * Copyright (c) 2012 Massimiliano Ziccardi Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package it.jnrpe.events;

import java.util.HashMap;
import java.util.Map;

/**
 * A basic, very simple, general purpose event.
 *
 * @author Massimiliano Ziccardi
 */
class SimpleEvent implements IJNRPEEvent
{
    /**
     * The log event associated with this object.
     */
    private LogEvent m_evt = null;

    /**
     * The parameters of the vent.
     */
    private Map<String, Object> m_mParams = new HashMap<String, Object>();

    /**
     * The custom event type (if it is not a log event).
     */
    private String m_sCustEvtType = null;

    /**
     * Builds the {@link SimpleEvent} object with a LogEvent.
     *
     * @param evt
     *            The Log Event
     * @param vParams
     *            The Log Event parameters
     */
    public SimpleEvent(final LogEvent evt, final Object[] vParams)
    {
        if (evt == null)
        {
            throw new NullPointerException("Event type can't be null");
        }
        m_evt = evt;
        for (int i = 0; vParams != null && i < vParams.length; i += 2)
        {
            m_mParams.put((String) vParams[i], vParams[i + 1]);
        }
    }

    /**
     * Builds the {@link SimpleEvent} object with a custom event type.
     *
     * @param sCustEvtType
     *            The Custom Event Type
     * @param vParams
     *            The Event parameters
     */
    public SimpleEvent(final String sCustEvtType, final Object[] vParams)
    {
        if (sCustEvtType == null)
        {
            throw new NullPointerException("Event type can't be null");
        }

        m_sCustEvtType = sCustEvtType;
        for (int i = 0; vParams != null && i < vParams.length; i += 2)
        {
            EventParam param = (EventParam) vParams[i];
            m_mParams.put(param.getName(), param.getValue());
        }
    }

    /**
     * Returns the event name.
     */
    public String getEventName()
    {
        if (m_sCustEvtType != null)
        {
            return m_sCustEvtType;
        }
        return m_evt.name();
    }

    /**
     * Returns the LogEvent type (<code>null</code> if it is not a log event).
     *
     * @return
     */
    LogEvent getLogEvent()
    {
        return m_evt;
    }

    /**
     * Returns the event parameters.
     */
    public Map<String, Object> getEventParams()
    {
        return m_mParams;
    }

}
