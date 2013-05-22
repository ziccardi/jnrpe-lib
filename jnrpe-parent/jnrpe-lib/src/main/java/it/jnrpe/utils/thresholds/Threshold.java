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

import it.jnrpe.ReturnValue.UnitOfMeasure;
import it.jnrpe.utils.BadThresholdException;

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
 * @author Massimiliano Ziccardi
 *
 */
public class Threshold {

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
        yotta, zetta, exa, peta, tera, giga, mega, kilo, hecto, deca, deci,
        centi, milli, micro, nano, pico, femto, atto, zepto, yocto, kibi, mebi, gibi, tebi, pebi, exbi
    };

    /**
     * The name of the metric attached to this threshold.
     */
    private String metricName = null;

    /**
     * The list of ok ranges.
     */
    private List<Range> okThresholdList =
            new ArrayList<Range>();
    /**
     * The list of warning ranges.
     */
    private List<Range> warningThresholdList =
            new ArrayList<Range>();
    /**
     * The list of critical ranges.
     */
    private List<Range> criticalThresholdList =
            new ArrayList<Range>();

    private UnitOfMeasure unit = null;
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
     * @throws BadThresholdException
     *             -
     */
    public Threshold(final String definition)
            throws BadThresholdException {
        parse(definition);
    }

    /**
     * Parses a threshold definition.
     *
     * @param definition
     *            The threshold definition
     * @throws BadThresholdException
     *             -
     */
    private void parse(final String definition)
            throws BadThresholdException {
        String[] thresholdComponentAry = definition.split(",");

        for (String thresholdComponent : thresholdComponentAry) {
            String[] nameValuePair = thresholdComponent.split("=");

            if (nameValuePair[0].equalsIgnoreCase("metric")) {
                metricName = nameValuePair[1];
                continue;
            }
            if (nameValuePair[0].equalsIgnoreCase("ok")) {

                Range thr = new Range(nameValuePair[1]);

                okThresholdList.add(thr);
                continue;
            }
            if (nameValuePair[0].equalsIgnoreCase("warning")
                    || nameValuePair[0].equalsIgnoreCase("warn")
                    || nameValuePair[0].equalsIgnoreCase("w")) {
                Range thr = new Range(nameValuePair[1]);
                warningThresholdList.add(thr);
                continue;
            }
            if (nameValuePair[0].equalsIgnoreCase("critical")
                    || nameValuePair[0].equalsIgnoreCase("crit")
                    || nameValuePair[0].equalsIgnoreCase("c")) {
                Range thr = new Range(nameValuePair[1]);
                criticalThresholdList.add(thr);
                continue;
            }
            if (nameValuePair[0].equalsIgnoreCase("unit")) {
                unit = UnitOfMeasure.valueOf(nameValuePair[1].toLowerCase());
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
