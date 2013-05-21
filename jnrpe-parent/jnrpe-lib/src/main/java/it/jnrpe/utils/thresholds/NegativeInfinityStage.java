package it.jnrpe.utils.thresholds;

/**
 * Parses a negative infinity (-inf). The '-' is optional: if the infinity is at
 * the left side of the range, than it is assumed to be negative infinity. See
 * {@link http://nagiosplugins.org/rfc/new_threshold_syntax}
 *
 * Example Input : -inf..100
 *
 * Produced Output : ..100 and calls the
 * {@link ThresholdConfig#setNegativeInfinity(boolean)} passing
 * <code>true</code>
 *
 * @author Massimiliano Ziccardi
 */
class NegativeInfinityStage extends Stage {

    /**
     *
     */
    protected NegativeInfinityStage() {
        super("negativeinfinity");
    }

    /**
     * Parses the threshold to remove the matched '-inf' or 'inf' string.
     *
     * @param threshold
     *            The threshold chunk to be parsed
     * @param tc
     *            The threshold config object. This object will be populated
     *            according to the passed in threshold.
     * @see ThresholdConfig#setNegativeInfinity(boolean)
     * @return the remaining part of the threshold
     */
    @Override
    public String parse(final String threshold, final ThresholdConfig tc) {

        if (canParse(threshold)) {
            tc.setNegativeInfinity(true);

            if (threshold.startsWith("inf")) {
                return threshold.substring(3);
            }

            if (threshold.startsWith("-inf")) {
                return threshold.substring(4);
            }
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
        return threshold.startsWith("inf") || threshold.startsWith("-inf");
    }

    /**
     * This method is used to generate the exception message.
     *
     * @return the token that this stage is waiting for.
     */
    @Override
    public String expects() {
        return "[-]inf";
    }
}
