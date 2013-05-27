package it.jnrpe.utils.thresholds;

import it.jnrpe.utils.BadThresholdException;

/**
 * Builds a {@link ThresholdsEvaluator} object.
 *
 * @author Massimiliano Ziccardi
 *
 */
public class ThresholdsEvaluatorBuilder {

    /**
     * The threshold evaluator instance.
     */
    private ThresholdsEvaluator thresholds = new ThresholdsEvaluator();

    /**
     * Default constructor.
     */
    public ThresholdsEvaluatorBuilder() {
    }

    /**
     * Adds a threshold to the threshold evaluator object.
     *
     * @param threshold
     *            The threhsold text
     * @return this
     * @throws BadThresholdException
     *             -
     */
    public final ThresholdsEvaluatorBuilder
            withThreshold(final String threshold)
                    throws BadThresholdException {
        thresholds.addThreshold(new Threshold(threshold));
        return this;
    }

    /**
     * @return the threshold evaluator.
     */
    public final ThresholdsEvaluator create() {
        return thresholds;
    }
}
