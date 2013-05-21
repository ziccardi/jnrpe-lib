package it.jnrpe.utils.thresholds;

import java.util.ArrayList;
import java.util.List;

/**
 * The threshold interface. This object must be used to verify if a value falls
 * inside one of the ok, warning or critical ranges.
 *
 * According to nagios specifications, the evaluation order is the following:
 *
 * <ul>
 * <li>If no levels are specified, return OK
 * <li>If an ok level is specified and value is within range, return OK
 * <li>If a critical level is specified and value is within range, return
 * CRITICAL
 * <li>If a warning level is specified and value is within range, return WARNING
 * <li>If an ok level is specified, return CRITICAL
 * <li>Otherwise return OK
 * </ul>
 *
 * @author ziccardi
 *
 */
public class Threshold {

    /**
     * According to the nagios specifications:
     *
     * <blockquote> the unit can be specified with plugins that do not know
     * about the type of value returned (SNMP, Windows performance counters,
     * etc.). </blockquote>
     */
    private enum units {
        yotta, zetta, exa, peta, tera, giga, mega, kilo, hecto, deca, deci,
        centi, milli, micro, nano, pico, femto, atto, zepto, yocto
    };

    /**
     * According to the nagios specifications:
     *
     * <blockquote> the prefix is used to multiply the input range and possibly
     * for display data. The prefixes allowed are defined by NIST:
     * <ul>
     * <li>http://physics.nist.gov/cuu/Units/prefixes.html
     * <li>http://physics.nist.gov/cuu/Units/binary.html
     * </ul>
     * </blockquote>
     */
    private enum prefixes {
        kibi, mebi, gibi, tebi, pebi, exbi
    };

    /**
     * The name of the metric attached to this threshold.
     */
    private String metricName = null;

    /**
     * The list of ok ranges.
     */
    private List<ThresholdImpl> okThresholdList =
            new ArrayList<ThresholdImpl>();
    /**
     * The list of warning ranges.
     */
    private List<ThresholdImpl> warningThresholdList =
            new ArrayList<ThresholdImpl>();
    /**
     * The list of critical ranges.
     */
    private List<ThresholdImpl> criticalThresholdList =
            new ArrayList<ThresholdImpl>();

    private units unit = null;
    private prefixes prefix = null;

    /**
     * Build a threshold object parsing the string received. A threshold can be
     * in the format: <blockquote>
     * metric={metric},ok={range},warn={range},crit={
     * range},unit={unit}prefix={SI prefix} </blockquote>
     *
     * Where :
     * <ul>
     * <li>ok, warn, crit are called "levels"
     * <li>any of ok, warn, crit, unit or prefix are optional
     * <li>if ok, warning and critical are not specified, then ok is always
     * returned
     * <li>the unit can be specified with plugins that do not know about the
     * type of value returned (SNMP, Windows performance counters, etc.)
     * <li>the prefix is used to multiply the input range and possibly for
     * display data. The prefixes allowed are defined by NIST:
     * <ul>
     * <li>http://physics.nist.gov/cuu/Units/prefixes.html
     * <li>http://physics.nist.gov/cuu/Units/binary.html
     * </ul>
     * <li>ok, warning or critical can be repeated to define an additional
     * range. This allows non-continuous ranges to be defined
     * <li>warning can be abbreviated to warn or w
     * <li>critical can be abbreviated to crit or c
     * </ul>
     *
     * @param definition
     *            The threshold string
     * @throws BadThresholdSyntaxException
     *             -
     */
    public Threshold(final String definition)
            throws BadThresholdSyntaxException {
        parse(definition);
    }

    /**
     * Parses a threshold definition.
     *
     * @param definition
     *            The threshold definition
     * @throws BadThresholdSyntaxException
     *             -
     */
    private void parse(final String definition)
            throws BadThresholdSyntaxException {
        String[] thresholdComponentAry = definition.split(",");

        for (String thresholdComponent : thresholdComponentAry) {
            String[] nameValuePair = thresholdComponent.split("=");

            if (nameValuePair[0].equalsIgnoreCase("metric")) {
                metricName = nameValuePair[1];
                continue;
            }
            if (nameValuePair[0].equalsIgnoreCase("ok")) {

                ThresholdImpl thr = new ThresholdImpl(nameValuePair[1]);

                okThresholdList.add(thr);
                continue;
            }
            if (nameValuePair[0].equalsIgnoreCase("warning")
                    || nameValuePair[0].equalsIgnoreCase("warn")
                    || nameValuePair[0].equalsIgnoreCase("w")) {
                ThresholdImpl thr = new ThresholdImpl(nameValuePair[1]);
                warningThresholdList.add(thr);
                continue;
            }
            if (nameValuePair[0].equalsIgnoreCase("critical")
                    || nameValuePair[0].equalsIgnoreCase("crit")
                    || nameValuePair[0].equalsIgnoreCase("c")) {
                ThresholdImpl thr = new ThresholdImpl(nameValuePair[1]);
                criticalThresholdList.add(thr);
                continue;
            }
            if (nameValuePair[0].equalsIgnoreCase("unit")) {
                unit = units.valueOf(nameValuePair[1].toLowerCase());
                continue;
            }
            if (nameValuePair[0].equalsIgnoreCase("prefix")) {
                prefix = prefixes.valueOf(nameValuePair[1].toLowerCase());
                continue;
            }

            // Threshold specification error
        }
    }

    /**
     * Returns the metric attached with this threshold.
     * @return The metric
     */
    public final String getMetric() {
        return metricName;
    }
}
