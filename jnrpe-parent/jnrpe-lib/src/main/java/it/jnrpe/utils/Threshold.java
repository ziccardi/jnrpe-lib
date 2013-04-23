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
package it.jnrpe.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.math.BigDecimal;

/**
 * Utility class for evaluating thresholds. This class represent a Threshold.
 *
 * @author Massimiliano Ziccardi
 */
class Threshold
{
    /**
     * When the current state is 'MINVAL', that means that the value we are
     * parsing is the definition of the minimum value.
     */
    private static final int MINVAL = 0;

    /**
     * When the current state is 'MAXVAL', that means that the value we are is
     * the definition of the maximum value.
     */
    private static final int MAXVAL = 1;

    /**
     * When the current state is 'END', that means that we have finished
     * parsing the threshold definition.
     */
    private static final int END = 99;

    /**
     * The mimimum value as parsed from the threshold definition.
     */
    private BigDecimal m_iMinVal = null;

    /**
     * The maximum value as parsed from the threshold definition.
     */
    private BigDecimal m_iMaxVal = null;

    /**
     * <code>true</code> if the threshold is negated.
     */
    private boolean m_bNegate = false;

    /**
     * The current state of the threshold parser.
     */
    private int m_iCurState = MINVAL;

    /**
     * Builds the object with the specified range.
     *
     * @param sRange
     *            The range
     * @throws BadThresholdException 
     */
    Threshold(final String sRange) throws BadThresholdException
    {
        parseRange(sRange);
    }

    /**
     * Parses the range definition to evaluate the minimum and maximum
     * thresholds.
     *
     * @param sRange
     *            The range
     */
    private void parseRange(final String sRange) throws BadThresholdException
    {
        byte[] vBytes = sRange.getBytes();
        ByteArrayInputStream bin = new ByteArrayInputStream(vBytes);
        PushbackInputStream pb = new PushbackInputStream(bin);

        StringBuffer sbCurrentParsed = new StringBuffer();
        
        byte b = 0;

        try
        {
            while ((b = (byte) pb.read()) != -1)
            {
                sbCurrentParsed.append(b);
                if (b == '@')
                {
                    if (m_iCurState != MINVAL)
                    {
                        throw new BadThresholdException("Unparsable threshold '" + sRange + "'. Error at char " + sbCurrentParsed.length() + ": the '@' should not be there.");
                    }
                    m_bNegate = true;
                    continue;
                }
                if (b == ':')
                {

                    switch (m_iCurState)
                    {
                        case MINVAL:
                            if (m_iMinVal == null)
                            {
                                m_iMinVal = new BigDecimal(0);
                            }
                            m_iCurState = MAXVAL;
                            continue;
                        case MAXVAL:
                            throw new BadThresholdException("Unparsable threshold '" + sRange + "'. Error at char " + sbCurrentParsed.length() + ": the ':' should not be there.");
                            //m_iCurState = END;
                            //continue;
                        default:
                            m_iCurState = MAXVAL;
                    }
                }
                if (b == '~')
                {
                    switch (m_iCurState)
                    {
                        case MINVAL:
                            m_iMinVal = new BigDecimal(Integer.MIN_VALUE);
                            // m_iCurState = MAXVAL;
                            continue;
                        case MAXVAL:
                            m_iMaxVal = new BigDecimal(Integer.MAX_VALUE);
                            m_iCurState = END;
                            continue;
                        default:
                    }

                }

                StringBuffer sNumBuffer = new StringBuffer();

                // while (i < vBytes.length &&
                // Character.isDigit((char)vBytes[i]))

                do
                {
                    sNumBuffer.append((char) b);
                }
                while (((b = (byte) pb.read()) != -1)
                        && (Character.isDigit((char) b) 
                                || b == '+' || b == '-'));

                if (b != -1)
                {
                    pb.unread((int) b);
                }

                String sNum = sNumBuffer.toString();
                if (sNum.trim().length() == 0)
                {
                    throw new BadThresholdException("A number was expected after '" + sbCurrentParsed.toString() + "', but an empty string was found");
                }

                switch (m_iCurState)
                {
                    case MINVAL:
                        m_iMinVal = new BigDecimal(sNum.trim());
                        continue;
                    case MAXVAL:
                        m_iMaxVal = new BigDecimal(sNum.trim());
                        continue;
                    default:
                        m_iCurState = END;
                }
                // if (i < vBytes.length)
                // i-=2;
            }
        }
        catch (IOException ioe)
        {

        }

        if (m_iCurState == MINVAL)
        {
            m_iMaxVal = m_iMinVal;
            m_iMinVal = new BigDecimal(0);
        }
    }

    /**
     * Returns <code>true</code> if the value falls inside the range.
     *
     * @param iVal
     *            The value
     * @return <code>true</code> if the value falls inside the range.
     *         <code>false</code> otherwise.
     */
    public boolean isValueInRange(final int iVal)
    {
        return isValueInRange(new BigDecimal(iVal));
    }

    /**
     * Returns <code>true</code> if the value falls inside the range.
     *
     * @param val
     *            The value
     * @return <code>true</code> if the value falls inside the range.
     *         <code>false</code> otherwise.
     */
    public boolean isValueInRange(final BigDecimal val)
    {
        boolean bRes = true;
        // Sets the minimum value of the range
        if (m_iMinVal != null)
        {
            bRes = bRes && (val.compareTo(m_iMinVal) >= 0);
        }
        // Sets the maximum value of the range
        if (m_iMaxVal != null)
        {
            bRes = bRes && (val.compareTo(m_iMaxVal) <= 0);
        }
        if (m_bNegate)
        {
            return !bRes;
        }
        return bRes;
    }

}
