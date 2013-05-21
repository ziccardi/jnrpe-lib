package it.jnrpe.utils.thresholds;

/**
 * This exception is throwns if a badly formatted threshold/range is passed to
 * the threshold parser.
 *
 * @author Massimiliano Ziccardi
 */
public class BadThresholdSyntaxException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 2036144202685590610L;

    /**
     * Default exception contructor.
     */
    public BadThresholdSyntaxException() {
        super();
    }

    /**
     * Builds and initializes the exception.
     *
     * @param message
     *            The exception message
     * @param cause
     *            the cause
     */
    public BadThresholdSyntaxException(final String message,
            final Throwable cause) {
        super(message, cause);
    }

    /**
     * Builds and initializes the exception.
     *
     * @param message
     *            The exception message
     */
    public BadThresholdSyntaxException(final String message) {
        super(message);
    }

    /**
     * Builds and initializes the exception.
     *
     * @param cause
     *            the cause
     */
    public BadThresholdSyntaxException(final Throwable cause) {
        super(cause);
    }

}
