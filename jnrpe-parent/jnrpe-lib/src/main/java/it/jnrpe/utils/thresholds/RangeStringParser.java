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
 * The configured threshold parser.
 *
 * @author Massimiliano Ziccardi
 */
class RangeStringParser {

    /**
     * The root of the parsing state machine.
     */
    private static final Stage ROOT_STAGE = configureParser();

    /**
     * Builds and configure the parser.
     */
    public RangeStringParser() {

    }

    /**
     * Configures the state machine.
     * @return The configured state machine.
     */
    private static Stage configureParser() {
        Stage startStage = new StartStage();
        Stage negativeInfinityStage = new NegativeInfinityStage();
        Stage positiveInfinityStage = new PositiveInfinityStage();
        NegateStage negateStage = new NegateStage();
        BracketStage.OpenBracketStage openBraceStage =
                new BracketStage.OpenBracketStage();
        NumberBoundaryStage.LeftBoundaryStage startBoundaryStage =
                new NumberBoundaryStage.LeftBoundaryStage();
        NumberBoundaryStage.RightBoundaryStage rightBoundaryStage =
                new NumberBoundaryStage.RightBoundaryStage();
        SeparatorStage separatorStage = new SeparatorStage();
        BracketStage.ClosedBracketStage closedBraketStage =
                new BracketStage.ClosedBracketStage();

        startStage.addTransition(negateStage);
        startStage.addTransition(openBraceStage);
        startStage.addTransition(negativeInfinityStage);
        startStage.addTransition(startBoundaryStage);

        negateStage.addTransition(negativeInfinityStage);
        negateStage.addTransition(openBraceStage);
        negateStage.addTransition(startBoundaryStage);

        openBraceStage.addTransition(startBoundaryStage);
        startBoundaryStage.addTransition(separatorStage);
        negativeInfinityStage.addTransition(separatorStage);

        separatorStage.addTransition(positiveInfinityStage);
        separatorStage.addTransition(rightBoundaryStage);

        positiveInfinityStage.addTransition(closedBraketStage);
        rightBoundaryStage.addTransition(closedBraketStage);

        return startStage;
    }

    /**
     * Parses the threshold.
     *
     * @param range
     *            The threshold to be parsed
     * @param tc
     *            The configuration
     * @throws RangeException
     *             -
     */
    public final void parse(final String range, final RangeConfig tc)
            throws RangeException {
        ROOT_STAGE.parse(range, tc);
    }
}
