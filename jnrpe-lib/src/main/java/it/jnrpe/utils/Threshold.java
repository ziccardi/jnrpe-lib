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
package it.jnrpe.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.math.BigDecimal;

/**
 * Utility class for evaluating thresholds
 * This class represent a Threshold
 * 
 * @author Massimiliano Ziccardi
 */
class Threshold
{
    private static final int MINVAL = 0;

    private static final int MAXVAL = 1;

    private static final int END = 99;

    private BigDecimal m_iMinVal = null;

    private BigDecimal m_iMaxVal = null;

    private boolean m_bNegate = false;

    private int m_iCurState = MINVAL;

    /**
     * Builds the object with the specified range 
     * @param sRange
     */
    Threshold(String sRange)
    {
        parseRange(sRange);
    }

    private void parseRange(String sRange)
    {
        byte[] vBytes = sRange.getBytes();
        ByteArrayInputStream bin = new ByteArrayInputStream(vBytes);
        PushbackInputStream pb = new PushbackInputStream(bin);

        byte b = 0;
        byte[] a_r = new byte[1];
        a_r[0] = 0;

        try
        {
            while ((b = (byte) pb.read()) != -1)
            {

                if (b == '@')
                {
                    if (m_iCurState != MINVAL)
                        System.err.println("PARSE ERROR");
                    m_bNegate = true;
                    continue;
                }
                if (b == ':')
                {

                    switch (m_iCurState)
                    {
                    case MINVAL:
                        if (m_iMinVal == null)
                            m_iMinVal = new BigDecimal(0);
                        m_iCurState = MAXVAL;
                        continue;
                    case MAXVAL:
                        System.err.println("PARSE ERROR");
                        m_iCurState = END;
                        continue;
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
                        && (Character.isDigit((char) b) || b == '+' || b == '-'));

                if (b != -1)
                    pb.unread((int) b);

                String sNum = sNumBuffer.toString();
                if (sNum.trim().length() == 0)
                    System.err.println("PARSE ERROR");

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
     * Returns <code>true</code> if the value falls int the range
     * @param iVal The value
     * @return
     */
    public boolean isValueInRange(int iVal)
    {
        return isValueInRange(new BigDecimal(iVal));
    }
    
    public boolean isValueInRange(BigDecimal val)
    {
        boolean bRes = true;
        // imposta il valore min del range
        if (m_iMinVal != null)
        {
            bRes = bRes && ( val.compareTo(m_iMinVal) >= 0);
        }
        // imposta il valore max del range
        if (m_iMaxVal != null)
        {
            bRes = bRes && (val.compareTo(m_iMaxVal) <= 0);
        }
        if (m_bNegate)
            return !bRes;
        return bRes;
    }
    
}
