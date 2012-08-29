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

/**
 * This class is just a container for the plugin result.
 *
 * @author Massimiliano Ziccardi
 */
public final class ReturnValue
{
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
     * {@link IJNRPEConstants#STATE_OK} state.
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
     * @param iReturnCode The return code
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
     * @param status The status to be returned to Nagios
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
     * @param sMessage The message to be returned
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
     * Returns the message.
     *
     * @return The message
     */
    public String getMessage()
    {
        return m_sMessage;
    }
}
