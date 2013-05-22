package it.jnrpe.utils.thresholds;

import it.jnrpe.utils.BadThresholdException;

/**
 * Base class for the range parsing exceptions.
 *
 * @author Massimiliano Ziccardi
 */
public class RangeException extends BadThresholdException {

    /**
     *
     */
    private static final long serialVersionUID = 8370789724508683948L;

    /**
     * The whole range string.
     */
    private String wholeRangeString = null;

    /**
     * The stage that raised the exception.
     */
    private final Stage failedStage;


    /**
     * The erroneous tokens.
     */
    private final String badString;

    /**
     * Builds the exception specifying the stage, the bad tokens and the whole
     * string.
     *
     * @param stage
     *            The stage that caused the error.
     * @param found
     *            The string that caused the error.
     * @param wholeString
     *            The whole range string.
     */
    public RangeException(final Stage stage, final String found,
            final String wholeString) {
        super();
        failedStage = stage;
        badString = found;
        wholeRangeString = wholeString;
    }

    /**
     * @return The whole range string
     */
    protected final String getWholeRangeString() {
        return wholeRangeString;
    }

    /**
     * Sets the whole range string.
     * @param rangeString The whole range string
     */
    final void setWholeRangeString(final String rangeString) {
        this.wholeRangeString = rangeString;
    }

    /**
     * @return The parser stage that failed
     */
    protected final Stage getFailedStage() {
        return failedStage;
    }

    /**
     * @return the erroneous tokens
     */
    protected final String getBadString() {
        return badString;
    }

    /**
     * Utility method for error messages.
     *
     * @param stage
     *            the stage to ask for expected tokens.
     * @return The list of expected tokens
     */
    private static String parseExpecting(final Stage stage) {
        StringBuffer expected = new StringBuffer();

        for (String key : stage.getTransitionNames()) {
            expected.append(",").append(
                    stage.getTransition(key).expects());
        }

        return expected.substring(1);
    }

    /**
     * Returns the expected token for the failed stage.
     * @return the expected token for the failed stage.
     */
    protected final String getExpectedTokens() {
        return parseExpecting(failedStage);
    }
}
