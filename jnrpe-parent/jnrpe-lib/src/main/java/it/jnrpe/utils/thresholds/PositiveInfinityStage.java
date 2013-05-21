package it.jnrpe.utils.thresholds;

/**
 * Parses a negative infinity (+inf). The '+' is optional. See
 * http://nagiosplugins.org/rfc/new_threshold_syntax
 *
 * Example Input : 50..+inf
 *
 * {@link ThresholdConfig#setPositiveInfinity(boolean)} gets called passing
 * <code>true</code>
 *
 * Positive infinity can only be at the end of a range.
 *
 * @author Massimiliano Ziccardi
 */
class PositiveInfinityStage  extends Stage {

    /**
     *
     */
    protected PositiveInfinityStage() {
        super("positiveinfinity");
    }

    @Override
    public String parse(final String threshold, final ThresholdConfig tc) {

        if (canParse(threshold)) {
            tc.setPositiveInfinity(true);

            if (threshold.startsWith("inf")) {
                return threshold.substring(3);
            }

            if (threshold.startsWith("+inf")) {
                return threshold.substring(4);
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