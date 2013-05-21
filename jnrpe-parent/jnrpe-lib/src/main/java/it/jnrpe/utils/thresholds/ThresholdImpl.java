package it.jnrpe.utils.thresholds;

/**
 * The threshold object.
 *
 * @author Massimiliano Ziccardi
 */
class ThresholdImpl extends ThresholdConfig {

    /**
     *
     * @param threshold The threshold to be parsed
     * @throws BadThresholdSyntaxException -
     */
    public ThresholdImpl(final String threshold)
            throws BadThresholdSyntaxException {
        parse(threshold);
    }

    /**
     *
     * @param threshold The threshold to be parsed
     * @throws BadThresholdSyntaxException -
     */
    private void parse(final String threshold)
            throws BadThresholdSyntaxException {
        new ThresholdParser().parse(threshold, this);
    }
}
