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
package it.jnrpe.plugin;

import it.jnrpe.ICommandLine;
import it.jnrpe.ReturnValue;
import it.jnrpe.Status;
import it.jnrpe.plugin.utils.Utils;
import it.jnrpe.plugins.PluginBase;
import it.jnrpe.utils.BadThresholdException;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The CheckTomcat plugin. This plugin does a HTTP GET of the tomcat status
 * page, by default: /manager/status?XML=true
 *
 * @author Frederico Campos
 *
 */
public class CheckTomcat extends PluginBase {

    /**
     * Default Tomcat http port.
     */
    public static final String DEFAULT_PORT = "8080";

    /**
     * Default Tomcat manager URL.
     */
    public static final String DEFAULT_URI = "/manager/status?XML=true";

    /**
     * Default timeout.
     */
    public static final String DEFAULT_TIMEOUT = "10";

    /**
     * Executes the check.
     *
     * @param cl
     *            The command line
     * @return the result of the check
     * @throws BadThresholdException
     *             if the threshold could not be parsed
     */
    public final ReturnValue execute(final ICommandLine cl)
            throws BadThresholdException {
        String username = cl.getOptionValue("username");
        String password = cl.getOptionValue("password");
        String hostname = cl.getOptionValue("hostname");

        String port = cl.getOptionValue("port", DEFAULT_PORT);
        String uri = cl.getOptionValue("uri", DEFAULT_URI);
        String warning = cl.getOptionValue("warning");
        String critical = cl.getOptionValue("critical");

        int timeout =
                Integer.parseInt(cl.getOptionValue("timeout", DEFAULT_TIMEOUT));

        // http(s)://user[:pwd]@hostname:port/uri
        //String urlPattern = "{0}://{1}@{2}:{3}{4}";

        if (!uri.startsWith("/")) {
            uri = "/" + uri;
        }

        String protocol;
        String credentials;

        if (cl.hasOption("ssl")) {
            protocol = "https://";
        } else {
            protocol = "http://";
        }

        if (password != null) {
            credentials = username + ":" + password;
        } else {
            credentials = username + ":";
        }

        String url = protocol + credentials + "@" + hostname + ":" + port + uri;

        // String url =
        // (cl.hasOption("ssl") ? "https://" : "http://")
        // + username + ":" + password + "@" + hostname + ":"
        // + port + uri;
        String encoded =
                Base64.encodeBase64String((username + ":" + password)
                        .getBytes());
        Properties props = new Properties();
        props.put("Authorization", "Basic " + encoded);
        String response = null;
        String errmsg = null;
        try {
            response = Utils.getUrl(new URL(url), props, timeout * 1000);
        } catch (MalformedURLException e) {
            // e.printStackTrace();
            log.info("Bad plugin URL configuration : " + e.getMessage());
            errmsg = e.getMessage();
        } catch (IOException e) {
            log.info("Plugin execution failed : " + e.getMessage(), e);
            errmsg = e.getMessage();
        }

        if (response == null) {
            return new ReturnValue(Status.WARNING, errmsg);
        }

        return analyseStatus(response, warning, critical);
    }

    /**
     * Parse xml data and return status.
     *
     * @param xml
     *            The XML to be analyzed
     * @param warning
     *            The warning range
     * @param critical
     *            The critical range
     * @return ReturnValue The reesult
     */
    private ReturnValue analyseStatus(final String xml, final String warning,
            final String critical) {
        StringBuffer buff = new StringBuffer();

        String[] warn = warning != null ? warning.split(",") : null;
        String[] crit = critical != null ? critical.split(",") : null;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        InputSource is = new InputSource(new StringReader(xml));
        try {
            Document doc = builder.parse(is);
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            Element root =
                    (Element) xpath.compile("//status").evaluate(
                            doc.getDocumentElement(), XPathConstants.NODE);
            Element memory =
                    (Element) xpath.compile("//status/jvm/memory").evaluate(
                            doc.getDocumentElement(), XPathConstants.NODE);

            int free = Integer.parseInt(memory.getAttribute("free"));
            int total = Integer.parseInt(memory.getAttribute("total"));
            int max = Integer.parseInt(memory.getAttribute("max"));
            int available = free + max - total;

            if (critical != null && critical.length() == 2
                    && !"".equals(crit[1])) {
                int criticalMemory = getValue(crit[1], max) * 1024 * 1024;
                if (available < criticalMemory) {
                    return new ReturnValue(Status.CRITICAL,
                            "Free memory critical: " + available
                                    + " MB available");
                }
            }

            if (warning != null && warn.length == 2 && !"".equals(warn[1])) {
                int warnMemory = getValue(warn[1], max) * 1024 * 1024;
                if (available < warnMemory) {
                    return new ReturnValue(Status.WARNING, "Free memory low: "
                            + available + " MB available");
                }
            }

            int memUse = (max - available);
            buff.append("JVM memory use " + Utils.formatSize(memUse) + " ");
            buff.append("Free: " + Utils.formatSize(free) + ", Total: "
                    + Utils.formatSize(total) + ", Max: "
                    + Utils.formatSize(max) + " ");
            NodeList connectors = root.getElementsByTagName("connector");
            int threadWarn = -1;
            int threadCrit = -1;
            if (warn != null && !"".equals(warn[0])) {
                threadWarn = Integer.parseInt(warn[0]);
            }
            if (crit != null && !"".equals(crit[0])) {
                threadCrit = Integer.parseInt(crit[0]);
            }

            for (int i = 0; i < connectors.getLength(); i++) {
                Element connector = (Element) connectors.item(i);
                String connectorName = connector.getAttribute("name");
                Element threadInfo =
                        (Element) connector.getElementsByTagName("threadInfo")
                                .item(0);
                int maxThreads =
                        Integer.parseInt(threadInfo.getAttribute("maxThreads"));
                int currentThreadCount =
                        Integer.parseInt(threadInfo
                                .getAttribute("currentThreadCount"));
                int currentThreadsBusy =
                        Integer.parseInt(threadInfo
                                .getAttribute("currentThreadsBusy"));
                int threadsAvailable = maxThreads - currentThreadsBusy;
                if (threadsAvailable <= threadCrit) {
                    return new ReturnValue(Status.CRITICAL, "Free "
                            + connectorName + " threads: " + threadsAvailable);
                }
                if (threadsAvailable <= threadWarn) {
                    return new ReturnValue(Status.WARNING, "Free "
                            + connectorName + " threads: " + threadsAvailable);
                }

                buff.append(connectorName + " - thread count: "
                        + currentThreadCount + ", current threads busy: "
                        + currentThreadsBusy + ", max threads: " + maxThreads
                        + " ");
            }

        } catch (XPathExpressionException e) {
            e.printStackTrace();

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ReturnValue(Status.OK, buff.toString());
    }

    /**
     * Extract numeric value, even if it's a %.
     *
     * @param value
     *            The value
     * @param factor
     *            The factor
     * @return int The numeric value
     */
    private int getValue(final String value, final int factor) {
        int val = 0;
        if (value != null) {
            if (value.contains("%")) {
                val = factor * Integer.parseInt(value.replace("%", "")) * 100;
            } else {
                val = Integer.parseInt(value);
            }
        }
        return val;
    }

}
