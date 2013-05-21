package it.jnrpe.utils.thresholds;

/**
 * The root of the parsing state machine.
 *
 * @author Massimiliano Ziccardi
 */
class StartStage extends Stage {

    /**
     *
     */
    protected StartStage() {
        super("root");
    }

    /**
     * The current stage during the parsing.
     */
    private Stage currentStage = this;

    /**
     * Try to move to the next stage according to the content of the received
     * threshold.
     *
     * @param threshold
     *            The threshold
     * @param tc
     *            The threshold configuration
     * @return The remaining part of the threshold. If there are no errors,
     *         returns an empty string.
     * @throws BadThresholdSyntaxException -
     */
    private String moveNext(final String threshold, final ThresholdConfig tc)
            throws BadThresholdSyntaxException {
        for (String transitionName : currentStage.getTransitionNames()) {
            Stage transition = currentStage.getTransition(transitionName);
            if (transition.canParse(threshold)) {
                String res = transition.parse(threshold, tc);
                currentStage = transition;
                return res;
            }
        }

        throw new BadThresholdSyntaxException("Found '" + threshold
                + "' while expecting one of " + parseExpecting());
    }

    @Override
    public boolean canParse(final String threshold) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * Utility method for error messages.
     * @return The list of expected tokens
     */
    private String parseExpecting() {
        StringBuffer expected = new StringBuffer();

        for (String key : currentStage.getTransitionNames()) {
            expected.append(",").append(
                    currentStage.getTransition(key).expects());
        }

        return expected.substring(1);
    }

    @Override
    public String parse(final String threshold, final ThresholdConfig tc)
            throws BadThresholdSyntaxException {
        String parsedThreshold = threshold;

        while (parsedThreshold.length() != 0) {
            parsedThreshold = moveNext(parsedThreshold, tc);
        }

        if (!currentStage.isLeaf()) {
            throw new BadThresholdSyntaxException(
                    "Premature end of range. Expected one of : "
                            + parseExpecting());
        }

        return parsedThreshold;
    }

    @Override
    public String expects() {
        return null;
    }

    /**
     * Resets the parser.
     */
    void reset() {
        currentStage = this;
    }
}
