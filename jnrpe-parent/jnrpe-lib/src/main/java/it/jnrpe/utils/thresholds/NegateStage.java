package it.jnrpe.utils.thresholds;

/**
 * This stage handles the negate character ('^').
 *
 * Example Input : ^(0..100
 *
 * Produced Output : (0..100 and calls the
 * {@link ThresholdConfig#setNegate(boolean)} passing <code>true</code>
 *
 * @author Massimiliano Ziccardi
 */
class NegateStage extends Stage {

    /**
     *
     */
    protected NegateStage() {
        super("negate");
    }

    /**
     * Parses the threshold to remove the matched '^' char.
     *
     * @param threshold
     *            The threshold chunk to be parsed
     * @param tc
     *            The threshold config object. This object will be populated
     *            according to the passed in threshold.
     * @return the remaining part of the threshold
     */
    @Override
    public String parse(final String threshold, final ThresholdConfig tc) {
        if (canParse(threshold)) {
            tc.setNegate(true);
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
    public boolean canParse(final String threshold) {
        return threshold.startsWith("^");
    }

    /**
     * This method is used to generate the exception message.
     *
     * @return the token that this stage is waiting for.
     */
    @Override
    public String expects() {
        return "^";
    }
}
