package it.jnrpe.utils.thresholds;

/**
 * This class is involved when we reached the separator
 * between the left and the right boundary ('..').
 *
 * @author Massimiliano Ziccardi
 */
class SeparatorStage extends Stage {

    /**
     *
     */
    protected SeparatorStage() {
        super("separator");
    }

    @Override
    public String parse(final String threshold, final ThresholdConfig tc)
            throws BadThresholdSyntaxException {
        if (canParse(threshold)) {
            return threshold.substring(2);
        }

        return threshold;
    }

    @Override
    public boolean canParse(final String threshold) {
        return threshold.startsWith("..");
    }

    @Override
    public String expects() {
        return "..";
    }
}
