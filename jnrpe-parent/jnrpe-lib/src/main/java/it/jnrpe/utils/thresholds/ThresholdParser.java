package it.jnrpe.utils.thresholds;

/**
 * The configured threshold parser. Different threads must use different
 * instances of the object.
 *
 * @author Massimiliano Ziccardi
 */
class ThresholdParser {

    /**
     * The root of the parsing state machine.
     */
    private Stage startStage;

    /**
     * Builds and configure the parser.
     */
    public ThresholdParser() {
        startStage = new StartStage();
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
    }

    /**
     * Parses the threshold.
     *
     * @param range
     *            The threshold to be parsed
     * @param tc
     *            The configuration
     * @throws BadThresholdSyntaxException
     *             -
     */
    public final void parse(final String range, final ThresholdConfig tc)
            throws BadThresholdSyntaxException {
        startStage.parse(range, tc);
    }
}
