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

import it.jnrpe.net.IJNRPEConstants;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is just a container for the plugin result.
 * 
 * @author Massimiliano Ziccardi
 */
public final class ReturnValue
{
    public enum UnitOfMeasure
    {
        microseconds, milliseconds, seconds, percentage, bytes, kilobytes, megabytes, gigabytes, terabytes, counter
    };

    private List<PerformanceData> m_vPerformanceData = new ArrayList<PerformanceData>();

    /**
     * The raw return code.
     */
    private Status m_returnCode;

    /**
     * The message.
     */
    private String m_sMessage;

    /**
     * Initializes an empty return value.
     */
    public ReturnValue()
    {

    }

    /**
     * Initializes the return value object with the given message and with the
     * {@link Status.OK} state.
     * 
     * @param sMessage
     *            The message
     */
    public ReturnValue(final String sMessage)
    {
        m_returnCode = Status.OK;
        m_sMessage = sMessage;
    }

    /**
     * Initializes the return value object with the given state and the given
     * message.
     * 
     * @param iReturnCode
     *            The state
     * @param sMessage
     *            The message
     * @deprecated Use {@link #ReturnValue(Status, String)} instead
     */
    public ReturnValue(final int iReturnCode, final String sMessage)
    {
        m_returnCode = Status.fromIntValue(iReturnCode);
        m_sMessage = sMessage;
    }

    /**
     * Initializes the return value object with the given state and the given
     * message.
     * 
     * @param status
     *            The status to be returned
     * @param sMessage
     *            The message to be returned
     */
    public ReturnValue(final Status status, final String sMessage)
    {
        m_returnCode = status;
        m_sMessage = sMessage;
    }

    /**
     * Sets the return code and returns 'this' so that the calls can be
     * cascaded.
     * 
     * @param iReturnCode
     *            The return code
     * @return this
     * @deprecated Use {@link #withStatus(Status)} instead.
     */
    public ReturnValue withReturnCode(final int iReturnCode)
    {
        m_returnCode = Status.fromIntValue(iReturnCode);
        return this;
    }

    /**
     * Sets the return code and returns 'this' so that the calls can be
     * cascaded.
     * 
     * @param status
     *            The status to be returned to Nagios
     * @return this
     */
    public ReturnValue withStatus(final Status status)
    {
        if (status == null)
        {
            throw new IllegalArgumentException("Status cannot be null");
        }

        m_returnCode = status;
        return this;
    }

    /**
     * Sets the message and returns 'this' so that the calls can be cascaded.
     * 
     * @param sMessage
     *            The message to be returned
     * @return this
     */
    public ReturnValue withMessage(final String sMessage)
    {
        m_sMessage = sMessage;
        return this;
    }

    /**
     * Returns the status.
     * 
     * @return The state
     * @deprecated Use {@link #getStatus()} instead.
     */
    public int getReturnCode()
    {
        return m_returnCode.intValue();
    }

    /**
     * Returns the status.
     * 
     * @return The status
     */
    public Status getStatus()
    {
        return m_returnCode;
    }

    /**
     * Returns the message. If the performance data has been passed in, they are
     * attached at the end of the message accordingly to the Nagios
     * specifications
     * 
     * @return The message and optionally the performance data
     */
    public String getMessage()
    {
        if (m_vPerformanceData.isEmpty()) return m_sMessage;
        StringBuffer res = new StringBuffer(m_sMessage).append("|");
        for (PerformanceData pd : m_vPerformanceData)
        {
            res.append(pd.toPerformanceString()).append(' ');
        }
        return res.toString();
    }

    /**
     * Adds performance data to the plugin result. Thos data will be added to
     * the output formatted as specified in Nagios specifications
     * (http://nagiosplug.sourceforge.net/developer-guidelines.html#AEN201)
     * 
     * @param sLabel
     *            The label of the performance data we are adding
     * @param value
     *            The performance data value
     * @param uom
     *            The Unit Of Measure
     * @param sWarningRange
     *            The warning threshold used to check this metric (can be null)
     * @param sCriticalRange
     *            The critical threshold used to check this value (can be null)
     * @param minimumValue
     *            The minimum value for this metric (can be null if not
     *            applicable)
     * @param maximumValue
     *            The maximum value for this metric (can be null if not
     *            applicable)
     * @return this
     */
    public ReturnValue withPerformanceData(final String sLabel,
            final Long value, final UnitOfMeasure uom,
            final String sWarningRange, final String sCriticalRange,
            final Long minimumValue, final Long maximumValue)
    {
        BigDecimal bdValue = null;
        BigDecimal bdMin = null;
        BigDecimal bdMax = null;

        if (value != null) bdValue = new BigDecimal(value);
        if (minimumValue != null) bdMin = new BigDecimal(minimumValue);
        if (maximumValue != null) bdMax = new BigDecimal(maximumValue);

        m_vPerformanceData.add(new PerformanceData(sLabel, bdValue, uom,
                sWarningRange, sCriticalRange, bdMin, bdMax));
        return this;
    }

    /**
     * Adds performance data to the plugin result. Thos data will be added to
     * the output formatted as specified in Nagios specifications
     * (http://nagiosplug.sourceforge.net/developer-guidelines.html#AEN201)
     * 
     * @param sLabel
     *            The label of the performance data we are adding
     * @param value
     *            The performance data value
     * @param uom
     *            The Unit Of Measure
     * @param sWarningRange
     *            The warning threshold used to check this metric (can be null)
     * @param sCriticalRange
     *            The critical threshold used to check this value (can be null)
     * @param minimumValue
     *            The minimum value for this metric (can be null if not
     *            applicable)
     * @param maximumValue
     *            The maximum value for this metric (can be null if not
     *            applicable)
     * @return this
     */
    public ReturnValue withPerformanceData(final String sLabel,
            final BigDecimal value, final UnitOfMeasure uom,
            final String sWarningRange, final String sCriticalRange,
            final BigDecimal minimumValue, final BigDecimal maximumValue)
    {
        m_vPerformanceData.add(new PerformanceData(sLabel, value, uom,
                sWarningRange, sCriticalRange, minimumValue, maximumValue));
        return this;
    }
}
