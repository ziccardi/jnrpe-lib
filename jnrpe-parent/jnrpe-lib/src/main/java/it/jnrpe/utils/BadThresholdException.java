package it.jnrpe.utils;

public class BadThresholdException extends Exception
{

    public BadThresholdException()
    {
        super();
    }

    public BadThresholdException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BadThresholdException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BadThresholdException(String message)
    {
        super(message);
    }

    public BadThresholdException(Throwable cause)
    {
        super(cause);
    }


}
