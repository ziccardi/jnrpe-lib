package it.jnrpe.utils.thresholds;

/**
 * When the threshold parser is executing this stage it means it is expecting an
 * open or closed bracket.
 *
 * @author Massimiliano Ziccardi
 */
class BracketStage extends Stage {

    /**
     * The bracket to search for (open or closed).
     */
    private final String bracket;

    /**
     * Builds the bracket stage.
     *
     * @param stageName
     *            The name of the stage
     * @param matchedBracket
     *            The bracket to search for
     */
    protected BracketStage(final String stageName, final char matchedBracket) {
        super(stageName);
        bracket = "" + matchedBracket;
    }

    /**
     * Parses the threshold to remove the matched braket.
     *
     * @param threshold
     *            The threshold to parse
     * @param tc
     *            The threshold config object. This object will be populated
     *            according to the passed in threshold.
     * @return the remaining part of the threshold
     */
    @Override
    public String parse(final String threshold, final ThresholdConfig tc) {
        if (canParse(threshold)) {
            tc.setRightInclusive(false);
            return threshold.substring(1);
        }

        return threshold;
    }

    /**
     * Tells the parser if this stage is able to parse the current remaining
     * threshold part.
     *
     * @param threshold
     *            The threshold part to be parsed.
     * @return <code>true</code> if this object can consume a part of the
     *         threshold
     */
    @Override
    public boolean canParse(final String threshold) {
        return threshold.startsWith(bracket);
    }

    /**
     * This method is used to generate the exception message.
     *
     * @return the token that this stage is waiting for.
     */
    @Override
    public String expects() {
        return bracket;
    }

    /**
     * This class implements a 'closed brace stage'. It consumes, if present, a
     * closed brace at the beginning of the received threshold chunk.
     *
     * @author Massimiliano Ziccardi
     */
    public static class ClosedBracketStage extends BracketStage {

        /**
         *
         */
        public ClosedBracketStage() {
            super("closedbracket", ')');
        }
    }

    /**
     * This class implements a 'open brace stage'. It consumes, if present, an
     * open brace at the beginning of the received threshold chunk.
     *
     * Example Input : (0..100
     *
     * Produced Output : 0..100 and calls the
     * {@link ThresholdConfig#setLeftInclusive(boolean)} passing
     * <code>true</code>
     *
     * @author Massimiliano Ziccardi
     */
    public static class OpenBracketStage extends BracketStage {

        /**
         *
         */
        public OpenBracketStage() {
            super("openbracket", '(');
        }
    }

}
