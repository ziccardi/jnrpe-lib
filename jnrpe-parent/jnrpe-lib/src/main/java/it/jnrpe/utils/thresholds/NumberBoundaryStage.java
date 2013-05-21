package it.jnrpe.utils.thresholds;

import java.math.BigDecimal;

/**
 * Base class for the number parsing stages.
 *
 * @author Massimiliano Ziccardi
 */
abstract class NumberBoundaryStage extends Stage {

    /**
     * @param stageName
     *            The name of this stage
     */
    protected NumberBoundaryStage(final String stageName) {
        super(stageName);
    }

    @Override
    public String parse(final String threshold, final ThresholdConfig tc)
            throws BadThresholdSyntaxException {
        if (canParse(threshold)) {
            StringBuffer numberString = new StringBuffer();
            for (int i = 0; i < threshold.length(); i++) {
                if (Character.isDigit(threshold.charAt(i))) {
                    numberString.append(threshold.charAt(i));
                    continue;
                }
                if (threshold.charAt(i) == '.') {
                    if (numberString.toString().endsWith(".")) {
                        numberString.deleteCharAt(numberString.length() - 1);
                        break;
                    } else {
                        numberString.append(threshold.charAt(i));
                        continue;
                    }
                }
                if (threshold.charAt(i) == '+' || threshold.charAt(i) == '-') {
                    if (numberString.length() == 0) {
                        numberString.append(threshold.charAt(i));
                        continue;
                    } else {
                        throw new BadThresholdSyntaxException(
                                threshold.substring(numberString.length()));
                    }
                }

            }
            if (numberString.length() != 0) {
                BigDecimal bd = new BigDecimal(numberString.toString());
                setBoundary(tc, bd);
                return threshold.substring(numberString.length());
            } else {
                throw new BadThresholdSyntaxException(threshold);
            }
        }

        return threshold;
    }

    @Override
    public boolean canParse(final String threshold) {
        switch (threshold.charAt(0))
        {
        case '+':
        case '-':
            return !(threshold.startsWith("-inf") || threshold
                    .startsWith("+inf"));
        default:
            return Character.isDigit(threshold.charAt(0));
        }
    }

    @Override
    public String expects() {
        return "+-[0-9]";
    }

    /**
     * This object can be used to set both left or right boundary of a range. It
     * is left to the implementing class to set the right boundary inside the
     * {@link ThresholdConfig} object;
     *
     * @param tc
     *            The threshold configuration
     * @param boundary
     *            The boundary value
     */
    public abstract void setBoundary(final ThresholdConfig tc,
            final BigDecimal boundary);

    /**
     * This class represent a left numeric boundary of a range.
     *
     * In the -10..+inf range it represent the '-10' value.
     *
     * @author Massimiliano Ziccardi
     *
     */
    public static class LeftBoundaryStage extends NumberBoundaryStage {

        /**
         *
         */
        protected LeftBoundaryStage() {
            super("startboundary");
        }

        @Override
        public void setBoundary(final ThresholdConfig tc,
                final BigDecimal boundary) {
            tc.setLeftBoundary(boundary);
        }
    }

    /**
     * This class represent a right numeric boundary of a range.
     *
     * In the range -10..+100.34 it represent the '100.34' value.
     *
     * @author Massimiliano Ziccardi
     *
     */
    public static class RightBoundaryStage extends NumberBoundaryStage {

        /**
         *
         */
        protected RightBoundaryStage() {
            super("rightboundary");
        }

        @Override
        public void setBoundary(final ThresholdConfig tc,
                final BigDecimal boundary) {
            tc.setRightBoundary(boundary);
        }
    }
}
