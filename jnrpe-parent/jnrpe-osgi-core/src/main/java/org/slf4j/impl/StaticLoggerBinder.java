package org.slf4j.impl;

import org.slf4j.ILoggerFactory;

public class StaticLoggerBinder
{
    private static OSGILogFactory s_factory = new OSGILogFactory();

    /**
     * The unique instance of this class.
     * 
     * @deprecated Please use the {@link #getSingleton()} method instead of
     *             accessing this field directly. In future versions, this field
     *             will become private.
     */
    @Deprecated
    public static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    /**
     * Return the singleton of this class.
     * 
     * @return the StaticLoggerBinder singleton
     */
    public static final StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    /**
     * Declare the version of the SLF4J API this implementation is compiled against.
     * The value of this field is usually modified with each release.
     */
    // to avoid constant folding by the compiler, this field must *not* be final
    public static String REQUESTED_API_VERSION = "1.7.5";  // !final

    private StaticLoggerBinder()
    {
    }

    public ILoggerFactory getLoggerFactory()
    {
        return s_factory;
    }

    public String getLoggerFactoryClassStr() {
        return null;
    }

}
