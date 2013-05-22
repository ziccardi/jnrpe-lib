/*
 * Copyright (c) 2013 Massimiliano Ziccardi
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
package it.jnrpe.utils.thresholds;

/**
 * Builds the range object parsing the range passed inside the threshold
 * definition.
 *
 * A range can have the following format:<p>
 *
 * [^](start..end)<p>
 *
 * Where:
 * <ul>
 * <li>start and end must be defined
 * <li>start and end match the regular expression /^[+-]?[0-9]+\.?[0-9]*$|^inf$/
 * (ie, a numeric or "inf")
 * <li>start &lt= end
 * <li>if start = "inf", this is negative infinity. This can also be written as
 * "-inf"
 * <li>if end = "inf", this is positive infinity
 * <li>endpoints are excluded from the range if () are used, otherwise endpoints
 * are included in the range
 * <li>alert is raised if value is within start and end range, unless ^ is used,
 * in which case alert is raised if outside the range
 * </ul>
 *
 * @author Massimiliano Ziccardi
 */
class Range extends RangeConfig {

    /**
     *
     * @param range
     *            The range to be parsed
     * @throws RangeException
     *             -
     */
    public Range(final String range)
            throws RangeException {
        parse(range);
    }

    /**
     *
     * @param range
     *            The range to be parsed
     * @throws RangeException
     *             -
     */
    private void parse(final String range)
            throws RangeException {
        new RangeStringParser().parse(range, this);
    }
}
