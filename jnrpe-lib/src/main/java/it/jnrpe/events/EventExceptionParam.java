package it.jnrpe.events;

/**
 * This object represent an 'EXCEPTION' parameter.
 * It is usually used with the LogEvents to pass the exception
 * to be logged.
 * 
 * @author Massimiliano Ziccardi
 */
public class EventExceptionParam extends EventParam
{
    /**
     * Builds and initializes the exception parameter
     * @param exc The exception.
     */
    public EventExceptionParam(final Throwable exc)
    {
        super("EXCEPTION", exc);
    }
}
