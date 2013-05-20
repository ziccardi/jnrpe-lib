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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The CheckTomcat plugin
 * This plugin does a HTTP GET of the tomcat status page, by default: /manager/status?XML=true
 * 
 * @author Frederico Campos
 *
 */
public class CheckTomcat extends PluginBase {

	public CheckTomcat(){
		
	}
	/**
	 * default values
	 */
	public final String DEFAULT_PORT = "8080";
	public final String DEFAULT_URI = "/manager/status?XML=true";
	public final int DEFAULT_TIMEOUT = 10;
	
	/* (non-Javadoc)
	 * @see it.jnrpe.plugins.IPluginInterface#execute(it.jnrpe.ICommandLine)
	 */
	public ReturnValue execute(ICommandLine cl) throws BadThresholdException {
		String username = cl.getOptionValue("username");
		String password = cl.getOptionValue("password");
		String hostname = cl.getOptionValue("hostname");
		String port = cl.getOptionValue("port") != null ? cl.getOptionValue("port") : DEFAULT_PORT;
		String uri =  cl.getOptionValue("uri") != null ? cl.getOptionValue("port") : DEFAULT_URI;
		String warning = cl.getOptionValue("warning");
		String critical = cl.getOptionValue("critical");
		int timeout = cl.getOptionValue("timeout") != null ? Integer.parseInt(cl.getOptionValue("timeout")) : DEFAULT_TIMEOUT;
		
		if (!uri.startsWith("/")){
			uri = "/" + uri;
		}
		String url = (cl.getOptionValue("ssl") != null ? "https://" :"http://") 
				+ username + ":" + password + "@" + hostname + ":" + port + uri;
		String encoded = new sun.misc.BASE64Encoder().encode((username+ ":" + password).getBytes());
		Properties props = new Properties();
		props.put("Authorization", "Basic " + encoded);
		String response = null;
		String errmsg = null;
		try {
			response = Utils.getUrl(new URL(url), props, timeout * 1000);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			errmsg = e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
			errmsg = e.getMessage();
		}
		
		if (response == null){
			 return new ReturnValue(Status.WARNING, errmsg); 
		}
				
		return analyseStatus(response, warning, critical);
	}
	
	/**
	 * Parse xml data and return status
	 * @param xml
	 * @param warning
	 * @param critical
	 * @return
	 * ReturnValue
	 */
	private ReturnValue analyseStatus(String xml, String warning, String critical) {
		StringBuffer buff = new StringBuffer();
		System.out.println(xml);
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
			Element root = (Element)xpath.compile("//status").evaluate(doc.getDocumentElement(), XPathConstants.NODE);
			Element memory = (Element)xpath.compile("//status/jvm/memory").evaluate(doc.getDocumentElement(), XPathConstants.NODE);
			
			int free = Integer.parseInt(memory.getAttribute("free"));
			int total = Integer.parseInt(memory.getAttribute("total"));
			int max = Integer.parseInt(memory.getAttribute("max"));
			int available = free + max - total;
			
			if (critical != null && critical.length() == 2 && !"".equals(crit[1])){
				int criticalMemory = getValue(crit[1], max) * 1024 * 1024;
				if (available < criticalMemory){
					return new ReturnValue(Status.CRITICAL, "Free memory critical: " + available + " MB available");
				}
			}	
			
			if (warning != null && warn.length == 2 && !"".equals(warn[1])){
				int warnMemory = getValue(warn[1], max) * 1024 * 1024;
				if (available < warnMemory){
					return new ReturnValue(Status.WARNING, "Free memory low: " + available + " MB available");
				}
			}
			
			int memUse = (max - available);
			buff.append("JVM memory use " + Utils.formatSize(memUse) + " ");
			buff.append("Free: " + Utils.formatSize(free) + ", Total: " + Utils.formatSize(total) + ", Max: " + Utils.formatSize(max) + " ");
			NodeList connectors = root.getElementsByTagName("connector");
			int threadWarn = -1;
			int threadCrit =-1;
			if (warn != null && !"".equals(warn[0])) {
				threadWarn = Integer.parseInt(warn[0]);
			}
			if (crit != null && !"".equals(crit[0])) {
				threadCrit = Integer.parseInt(crit[0]);
			}
			
			for (int i = 0; i < connectors.getLength(); i++){
				Element connector = (Element)connectors.item(i);
				String connectorName = connector.getAttribute("name");
				Element threadInfo = (Element)connector.getElementsByTagName("threadInfo").item(0);
				int maxThreads = Integer.parseInt(threadInfo.getAttribute("maxThreads"));
				int currentThreadCount = Integer.parseInt(threadInfo.getAttribute("currentThreadCount"));
				int currentThreadsBusy = Integer.parseInt(threadInfo.getAttribute("currentThreadsBusy"));
				int threadsAvailable = maxThreads - currentThreadsBusy;
				if (threadsAvailable <= threadCrit){
					return new ReturnValue(Status.CRITICAL, "Free " + connectorName + " threads: " + threadsAvailable);
				}
				if (threadsAvailable <= threadWarn){
					return new ReturnValue(Status.WARNING, "Free " + connectorName + " threads: " + threadsAvailable);
				}
				
				buff.append(connectorName + " - thread count: " + currentThreadCount + ", current threads busy: " + currentThreadsBusy + ", max threads: " + maxThreads + " ");
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
	 * Extract numeric value, even if it's a %
	 * 
	 * @param value
	 * @param factor
	 * @return
	 * int
	 */
	private int getValue(String value, int factor){
		int val = 0;
		if (value != null){
			if (value.contains("%")){
				val = factor * Integer.parseInt(value.replace("%", "")) * 100;
			}else{
				val = Integer.parseInt(value);
			}
		}
		return val;
	}
	
}
