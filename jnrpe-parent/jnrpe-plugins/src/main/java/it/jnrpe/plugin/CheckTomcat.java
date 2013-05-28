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
import it.jnrpe.ReturnValue.UnitOfMeasure;
import it.jnrpe.Status;
import it.jnrpe.plugin.utils.Utils;
import it.jnrpe.plugins.PluginBase;
import it.jnrpe.utils.BadThresholdException;
import it.jnrpe.utils.ThresholdUtil;

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
    	log.debug("check_tomcat");
        String username = cl.getOptionValue("username");
        String password = cl.getOptionValue("password");
        String hostname = cl.getOptionValue("hostname");

        String port = cl.getOptionValue("port", DEFAULT_PORT);
        String uri = cl.getOptionValue("uri", DEFAULT_URI);
        String warning = cl.getOptionValue("warning") != null ? cl.getOptionValue("warning") : null;
        String critical = cl.getOptionValue("critical") != null ? cl.getOptionValue("critical") : null;

        int timeout =
                Integer.parseInt(cl.getOptionValue("timeout", DEFAULT_TIMEOUT));

        // http(s)://user[:pwd]@hostname:port/uri
        //String urlPattern = "{0}://{1}@{2}:{3}{4}";
//{[-h (--hostname) [<hostname>]]=[127.0.0.1], [-a (--password) [<password>]]=[tomcat], [-w (--warning) [<warning>]]=[1:], [-p (--port) [<port>]]=[8080], [-l (--username) [<username>]]=[tomcat]}
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
        
        boolean checkThreads = cl.hasOption("threads");
        boolean checkMemory = cl.hasOption("memory");
        
        // can only have one check at a time
        if (checkThreads && checkMemory){
        	 throw new BadThresholdException("Either --memory or --threads allowed in command.");
        }
        return analyseStatus(response, warning, critical, checkMemory, checkThreads);
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
            final String critical, boolean checkMemory, boolean checkThreads) throws BadThresholdException {
        StringBuffer buff = new StringBuffer();
        log.debug("checkThreads " + checkThreads);
        log.debug("checkMemory " + checkMemory);
        log.debug("critical " + critical);
        log.debug("warning	 " + warning);
        
        ReturnValue retVal = new ReturnValue(Status.OK, null);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        int freeMem = 0;
        int totalMem = 0;
        int availableMem = 0;
        int maxMem = 0;
        int memUse = 0;
        int maxMemMb = 0;
        int availableMemMb = 0;
        int currentThreadCount = 0;
        int currentThreadsBusy = 0;
        int threadsAvailable = 0;
        int maxThreads = 0;
        
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

            // check memory
            freeMem = Integer.parseInt(memory.getAttribute("free"));
            totalMem = Integer.parseInt(memory.getAttribute("total"));
            maxMem = Integer.parseInt(memory.getAttribute("max"));
            availableMem = freeMem + maxMem - totalMem;
            
            maxMemMb = (int)(maxMem / (1024*1024));
            availableMemMb = (int)(availableMem / (1024*1024));
            memUse = (maxMem - availableMem);
            buff.append("JVM memory use " + Utils.formatSize(memUse) + " ");
            buff.append("Free: " + Utils.formatSize(freeMem) + ", Total: "
                    + Utils.formatSize(totalMem) + ", Max: "
                    + Utils.formatSize(maxMem) + " ");          
            
            if (checkMemory) {
            	String warn = warning != null ? getRangeValue(warning, maxMem, true) : null;
                String crit = critical != null ? getRangeValue(critical, maxMem, true) : null;

                if (crit != null && ThresholdUtil.isValueInRange(crit, availableMemMb)) {
                	return new ReturnValue(Status.CRITICAL, "Free memory critical: " + availableMemMb + " MB available").
                			withPerformanceData("memory", new Long(maxMemMb), 
                					!critical.contains("%") ? UnitOfMeasure.megabytes : UnitOfMeasure.percentage, 
                					warning, 
                					critical, 
                					0L, 
                					new Long(maxMem));
                }
                if (warn != null && ThresholdUtil.isValueInRange(warn, availableMemMb)){
                	return new ReturnValue(Status.WARNING, "Free memory low: "
                			+ availableMem / (1024*1024)+ " MB available / " + buff.toString()).withPerformanceData("memory", new Long(maxMemMb), 
                					!warning.contains("%") ? UnitOfMeasure.megabytes : UnitOfMeasure.percentage, 
                					warning, 
                					critical, 
                					0L, 
                					new Long(maxMem));
                }
            }            
            
            // check threads
            NodeList connectors = root.getElementsByTagName("connector");
            for (int i = 0; i < connectors.getLength(); i++) {
                Element connector = (Element) connectors.item(i);
                String connectorName = connector.getAttribute("name");
                
                Element threadInfo =
                        (Element) connector.getElementsByTagName("threadInfo")
                                .item(0);
                maxThreads =
                        Integer.parseInt(threadInfo.getAttribute("maxThreads"));
                currentThreadCount =
                        Integer.parseInt(threadInfo
                                .getAttribute("currentThreadCount"));
                currentThreadsBusy =
                        Integer.parseInt(threadInfo
                                .getAttribute("currentThreadsBusy"));
                threadsAvailable = maxThreads - currentThreadsBusy;
                log.debug("Connector " + connectorName +
                		" maxThreads: " + maxThreads + 
                		", currentThreadCount:" + currentThreadCount + 
                		", currentThreadsBusy: "+ currentThreadsBusy);
                
                String msg = connectorName + " - thread count: "
                        + currentThreadCount + ", current threads busy: "
                        + currentThreadsBusy + ", max threads: " + maxThreads;
                        
                if (checkThreads){
                	String warn = warning != null ? getRangeValue(warning, maxThreads, false) : null;
                    String crit = critical != null ? getRangeValue(critical, maxThreads, false) : null;
                    
					if (critical != null && ThresholdUtil.isValueInRange(crit, threadsAvailable)) {
					    return new ReturnValue(Status.CRITICAL, "CRITICAL - Free "
					            + connectorName + " threads: " + threadsAvailable).withMessage(msg).
					            withPerformanceData(connectorName + " threads", new Long(threadsAvailable), 
					            		!critical.contains("%") ? UnitOfMeasure.counter : UnitOfMeasure.percentage, 
					            		warning,
					            		critical,
					            		0L, 
					            		new Long(maxThreads));
					}
					if (warning != null && ThresholdUtil.isValueInRange(warn, threadsAvailable)) {
						return new ReturnValue(Status.WARNING, "WARNING - Free "
					           + connectorName + " threads: " + threadsAvailable + ", " + msg).
					           withPerformanceData(connectorName + " threads", 
					        		   new Long(threadsAvailable),
					        		   !warning.contains("%") ? UnitOfMeasure.counter : UnitOfMeasure.percentage,
					        		   warning, 
					        		   critical, 
					        		   0L, 
					        		   new Long(maxThreads));
					}
					
					//retVal.withPerformanceData(connectorName + " threads", new Long(threadsAvailable),null, warning, critical, 0L, new Long(maxThreads));

                }                
                buff.append(msg);
            }

            retVal.withMessage(buff.toString());
        } catch (XPathExpressionException e) {
            e.printStackTrace();

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        
        return retVal;
    }

    /**
     * Extract numeric value, even if it's a percentage sign.
     *
     * @param value
     *            The value
     * @param factor
     *            The factor
     * @return int The numeric value
     */
    private long getValue(String value, final int factor, boolean memory) {    	
        long val = 0;
        if (value != null) {
            if (value.contains("%")) {
            	val = (long) (((double)factor * Double.parseDouble(value.replace(":","").replace("%", ""))) / 100);
                if (memory){
                	val = val / (1024 * 1024); // MB
                }
            } else {
                val = Long.parseLong(value.replace(":",""));
            }
        }
        return val;
        
    }
    
    private String getRangeValue(String value, int factor, boolean memory){
    	boolean hadRangeStart = false;
    	boolean hadRangeEnd = false;
    	if (value.endsWith(":")){
    		hadRangeEnd = true;
    		value = value.substring(0, value.length() - 1);
    	}
    	if (value.startsWith(":")){
    		hadRangeStart = true;
    		value = value.substring(1, value.length());
    	}
    	String val = "" + getValue(value, factor, memory);
        
        if (hadRangeStart){
        	val = ":" + val;
        	
        }
        if (hadRangeEnd){
        	val += ":";
        }
        
    	return val;
    }

}
