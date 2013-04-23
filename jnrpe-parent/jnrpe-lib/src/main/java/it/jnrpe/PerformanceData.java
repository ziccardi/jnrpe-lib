package it.jnrpe;

import it.jnrpe.ReturnValue.UnitOfMeasure;

import java.math.BigDecimal;
import java.text.DecimalFormat;

class PerformanceData
{
    private final String m_sLabel;
    private final BigDecimal m_Value;
    private final UnitOfMeasure m_uom;
    private final String m_sWarningRange;
    private final String m_sCriticalRange;
    private final BigDecimal m_MinimumValue;
    private final BigDecimal m_MaximumValue;
    
    private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.000000");
    
    public PerformanceData(String sLabel, BigDecimal sValue, UnitOfMeasure uom, String sWarningRange, String sCriticalRange, BigDecimal sMinimumValue, BigDecimal sMaximumValue)
    {
        m_sLabel = sLabel;
        m_Value = sValue;
        m_uom = uom;
        m_sWarningRange = sWarningRange;
        m_sCriticalRange = sCriticalRange;
        m_MinimumValue = sMinimumValue;
        m_MaximumValue = sMaximumValue;
    }
    
    public String toPerformanceString()
    {
        StringBuffer res = new StringBuffer()
                    .append(quote(m_sLabel))
                    .append("=")
                    .append(DECIMAL_FORMAT.format(m_Value));
        
        if (m_uom != null)
        {
            switch(m_uom)
            {
            case milliseconds:
                res.append("ms");
                break;
            case microseconds:
                res.append("us");
                break;
            case seconds:
                res.append("s");
                break;
            case bytes:
                res.append("B");
                break;
            case kilobytes:
                res.append("KB");
                break;
            case megabytes:
                res.append("MB");
                break;
            case gigabytes:
                res.append("GB");
                break;
            case terabytes:
                res.append("TB");
                break;
            case percentage:
                res.append("%");
                break;
            case counter:
                res.append("c");
                break;
            }
        }
        
        res.append(";");
        if (m_sWarningRange != null)
            res.append(m_sWarningRange);
        res.append(";");
        if (m_sCriticalRange != null)
            res.append(m_sCriticalRange);
        res.append(";");
        if (m_MinimumValue != null)
            res.append(DECIMAL_FORMAT.format(m_MinimumValue));
        res.append(";");
        if (m_MaximumValue != null)
            res.append(DECIMAL_FORMAT.format(m_MaximumValue));
        
        while (res.charAt(res.length() - 1) == ';')
            res.deleteCharAt(res.length() - 1);
        
        return res.toString();
    }

    private String quote(String sLabel)
    {
        if (sLabel.indexOf(' ') == -1)
            return sLabel;
        
        return new StringBuffer("'").append(sLabel).append("'").toString();
    }
}
