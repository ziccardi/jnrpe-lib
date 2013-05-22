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
 * Parses a negative infinity (+inf). The '+' is optional. See
 * http://nagiosplugins.org/rfc/new_threshold_syntax
 *
 * Example Input : 50..+inf
 *
 * {@link RangeConfig#setPositiveInfinity(boolean)} gets called passing
 * <code>true</code>
 *
 * Positive infinity can only be at the end of a range.
 *
 * @author Massimiliano Ziccardi
 */
class PositiveInfinityStage  extends Stage {

    /**
     * The infinity sign.
     */
    private static final  String INFINITY = "inf";

    /**
     * The negative infinity sign.
     */
    private static final String POS_INFINITY = "+inf";

    /**
     *
     */
    protected PositiveInfinityStage() {
        super("positiveinfinity");
    }

    @Override
    public String parse(final String threshold, final RangeConfig tc) {

        if (canParse(threshold)) {
            tc.setPositiveInfinity(true);

            if (threshold.startsWith(INFINITY)) {
                return threshold.substring(INFINITY.length());
            }

            if (threshold.startsWith(POS_INFINITY)) {
                return threshold.substring(POS_INFINITY.length());
            }
        }

        return threshold;
    }

    @Override
    public boolean canParse(final String threshold) {
        return threshold.startsWith("inf") || threshold.startsWith("+inf");
    }

    @Override
    public String expects() {
        return "[+]inf";
    }
}
