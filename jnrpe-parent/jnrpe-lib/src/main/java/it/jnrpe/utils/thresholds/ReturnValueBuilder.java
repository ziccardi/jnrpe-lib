/*
 * Copyright (c) 2013 Massimiliano Ziccardi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.jnrpe.utils.thresholds;

import it.jnrpe.ReturnValue;
import it.jnrpe.Status;

import java.math.BigDecimal;

/**
 * This object takes the responsability to build and configure the return value
 * object and the performance data. The plugin has only the responsability to
 * gain the metrics and pass them to the builder: both status and performance
 * data will be generated.
 *
 * @author Massimiliano Ziccardi
 */
public class ReturnValueBuilder {
    /**
     * The return value that we are configuring.
     */
    private ReturnValue retVal = new ReturnValue();

    /**
     * The thresholds that must be used to compute the Status result.
     */
    private ThresholdsEvaluator thresholds = null;

    /**
     * The status.
     */
    private Status status = Status.OK;

    /**
     * Contructs the object passing the thresholds.
     *
     * @param thr
     *            The thresholds.
     */
    public ReturnValueBuilder(final ThresholdsEvaluator thr) {
        thresholds = thr;
    }

    /**
     * Configure the {@link ReturnValue} we are building with the specified
     * value.
     *
     * @param metric
     *            The metric name.
     * @param value
     *            The value.
     * @param minValue
     *            The maximum value (can be null)
     * @param maxValue
     *            The minimum value (can be null)
     * @return this
     */
    public final ReturnValueBuilder withValue(final String metric,
            final BigDecimal value,
            final BigDecimal minValue, final BigDecimal maxValue) {
        if (thresholds.isMetricRequired(metric)) {
            Status newStatus = thresholds.evaluate(metric, value);
            if (newStatus.getSeverity() > status.getSeverity()) {
                status = newStatus;
            }

            Threshold thr = thresholds.getThreshold(metric);

            retVal.withPerformanceData(metric, value, thr.getFormattedUnit(),
                    thr.getRangesAsString(Status.WARNING),
                    thr.getRangesAsString(Status.CRITICAL), minValue, maxValue);
        }
        return this;
    }

    /**
     * Configures the message to be returned to Nagios.
     *
     * @param message
     *            The message
     * @return this
     */
    public final ReturnValueBuilder withMessage(final String message) {
        retVal.withMessage(message);
        return this;
    }

    /**
     * Builds the configured {@link ReturnValue} object.
     * @return The {@link ReturnValue} object
     */
    public final ReturnValue create() {
        if (retVal.getMessage() == null) {
            throw new IllegalArgumentException(
                    "Return value message can't be null");
        }
        return retVal.withStatus(status);
    }
}
