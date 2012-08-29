package it.jnrpe;

public enum PacketType
{
    /**
     * Id code for a packet containing a query
     */
    QUERY(1),
    /**
     * Id code for a packet containing a response
     */
    RESPONSE(2);

    private final int m_iIntValue;

    PacketType(int iValue)
    {
        m_iIntValue = iValue;
    }

    public int intValue()
    {
        return m_iIntValue;
    }

    public static PacketType fromIntValue(int iIntValue)
    {
        switch (iIntValue)
        {
            case 1:
                return QUERY;
            case 2:
                return RESPONSE;
            default:
                return null;
        }
    }
}
