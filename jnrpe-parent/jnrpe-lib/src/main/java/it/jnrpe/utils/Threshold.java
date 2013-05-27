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
class Threshold {
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
     * When the current state is 'END', that means that we have finished parsing
     * the threshold definition.
     */
    private static final int END = 99;

    /**
     * The mimimum value as parsed from the threshold definition.
     */
    private BigDecimal minVal = null;

    /**
     * The maximum value as parsed from the threshold definition.
     */
    private BigDecimal maxVal = null;

    /**
     * <code>true</code> if the threshold is negated.
     */
    private boolean negateThreshold = false;

    /**
     * The current state of the threshold parser.
     */
    private int curState = MINVAL;

    /**
     * Builds the object with the specified range.
     *
     * @param thresholdString
     *            The range
     * @throws BadThresholdException
     *             -
     */
    Threshold(final String thresholdString) throws BadThresholdException {
        parseRange(thresholdString);
    }

    /**
     * Parses the range definition to evaluate the minimum and maximum
     * thresholds.
     *
     * @param thresholdString
     *            The range
     * @throws BadThresholdException
     *             -
     */
    private void parseRange(final String thresholdString)
            throws BadThresholdException {
        byte[] bytesAry = thresholdString.getBytes();
        ByteArrayInputStream bin = new ByteArrayInputStream(bytesAry);
        PushbackInputStream pb = new PushbackInputStream(bin);

        StringBuffer currentParsedBuffer = new StringBuffer();

        byte b = 0;

        try {
            while ((b = (byte) pb.read()) != -1) {
                currentParsedBuffer.append((char)b);
                if (b == '@') {
                    if (curState != MINVAL) {
                        throw new BadThresholdException(
                                "Unparsable threshold '" + thresholdString
                                        + "'. Error at char "
                                        + currentParsedBuffer.length()
                                        + ": the '@' should not be there.");
                    }
                    negateThreshold = true;
                    continue;
                }
                if (b == ':') {

                    switch (curState) {
                    case MINVAL:
                        if (minVal == null) {
                            minVal = new BigDecimal(0);
                        }
                        curState = MAXVAL;
                        continue;
                    case MAXVAL:
                        throw new BadThresholdException(
                                "Unparsable threshold '" + thresholdString
                                        + "'. Error at char "
                                        + currentParsedBuffer.length()
                                        + ": the ':' should not be there.");
                        // m_iCurState = END;
                        // continue;
                    default:
                        curState = MAXVAL;
                    }
                }
                if (b == '~') {
                    switch (curState) {
                    case MINVAL:
                        minVal = new BigDecimal(Integer.MIN_VALUE);
                        // m_iCurState = MAXVAL;
                        continue;
                    case MAXVAL:
                        maxVal = new BigDecimal(Integer.MAX_VALUE);
                        curState = END;
                        continue;
                    default:
                    }

                }

                StringBuffer numberBuffer = new StringBuffer();

                // while (i < vBytes.length &&
                // Character.isDigit((char)vBytes[i]))

                do {
                    numberBuffer.append((char) b);
                } while (((b = (byte) pb.read()) != -1)
                        && (Character.isDigit((char) b)
                                || b == '+' || b == '-'));

                if (b != -1) {
                    pb.unread((int) b);
                }

                String numberString = numberBuffer.toString();
                if (numberString.trim().length() == 0
                        || numberString.equals("+")
                        || numberString.equals("-")) {
                    throw new BadThresholdException(
                            "A number was expected after '"
                                    + currentParsedBuffer.toString()
                                    + "', but an empty string was found");
                }

                switch (curState) {
                case MINVAL:
                    minVal = new BigDecimal(numberString.trim());
                    continue;
                case MAXVAL:
                    maxVal = new BigDecimal(numberString.trim());
                    continue;
                default:
                    curState = END;
                }
                // if (i < vBytes.length)
                // i-=2;
            }
        } catch (IOException ioe) {

        }

        if (curState == MINVAL) {
            maxVal = minVal;
            minVal = new BigDecimal(0);
        }

        if (curState == MAXVAL && maxVal == null
                && thresholdString.startsWith(":")) {
            throw new BadThresholdException(
                 "At least one of maximum or minimum value must me specified.");
        }

    }

    /**
     * Returns <code>true</code> if the value falls inside the range.
     *
     * @param value
     *            The value
     * @return <code>true</code> if the value falls inside the range.
     *         <code>false</code> otherwise.
     */
    public boolean isValueInRange(final int value) {
        return isValueInRange(new BigDecimal(value));
    }

    /**
     * Returns <code>true</code> if the value falls inside the range.
     *
     * @param val
     *            The value
     * @return <code>true</code> if the value falls inside the range.
     *         <code>false</code> otherwise.
     */
    public boolean isValueInRange(final BigDecimal val) {
        boolean bRes = true;
        // Sets the minimum value of the range
        if (minVal != null) {
            bRes = bRes && (val.compareTo(minVal) >= 0);
        }
        // Sets the maximum value of the range
        if (maxVal != null) {
            bRes = bRes && (val.compareTo(maxVal) <= 0);
        }
        if (negateThreshold) {
            return !bRes;
        }
        return bRes;
    }

}
