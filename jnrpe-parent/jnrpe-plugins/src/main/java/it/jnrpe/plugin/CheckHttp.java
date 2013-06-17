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
import it.jnrpe.Status;
import it.jnrpe.plugin.utils.HttpUtils;
import it.jnrpe.plugin.utils.Utils;
import it.jnrpe.plugins.Metric;
import it.jnrpe.plugins.MetricGatheringException;
import it.jnrpe.plugins.PluginBase;
import it.jnrpe.utils.BadThresholdException;
import it.jnrpe.utils.thresholds.ThresholdsEvaluatorBuilder;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

/**
 *  This plugin tests the HTTP service on the specified host. It can test normal (http) and secure (https) servers, 
 *  follow redirects, search for strings and regular expressions on page results and check connection times.
 *
 * @author Frederico Campos
 *
 */

public class CheckHttp extends PluginBase {

	/**
	 * default values
	 */
	private final String DEFAULT_PORT = "80";

	private final String DEFAULT_SSL_PORT = "443";

	private final int DEFAULT_TIMEOUT = 30;

	private final String DEFAULT_PATH = "/";

	private final String DEFAULT_METHOD = "GET";

	private final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.94 Safari/537.36";

	@Override
	public void configureThresholdEvaluatorBuilder(ThresholdsEvaluatorBuilder thrb, ICommandLine cl) throws BadThresholdException {
		thrb.withLegacyThreshold("time", null, cl.getOptionValue("warning"), cl.getOptionValue("critical"));
		if (cl.hasOption("regex")){
			if (cl.hasOption("invert-regex")){
				// invert-regex: CRITICAL value if regex is found (true = 1)
				thrb.withLegacyThreshold("invert-regex", null, null, "1");
			}else{
				//WARNING if regex not found (false = 0)
				thrb.withLegacyThreshold("regex", null, null, "0");
			}
		}
		if (cl.hasOption("expect")){
			// WARNING if expected string not found (false = 0) 
			thrb.withLegacyThreshold("expect", null, "0", null);
		}
		if (cl.hasOption("string")){
			// WARNING if expected string not found (false = 0) 
			thrb.withLegacyThreshold("string", null, "0", null);
		}
		if (cl.hasOption("onredirect")){
			String redirect = cl.getOptionValue("onredirect").toUpperCase();
			if ("OK".equals(redirect)) {
				thrb.withLegacyThreshold("onredirect", "1:", null, null);
			}else if ("CRITICAL".equals(redirect)) {
				thrb.withLegacyThreshold("onredirect", null, null, "1:");
			}else if ("WARNING".equals(redirect)) {
				thrb.withLegacyThreshold("onredirect", null, "1:", null);
			}
		}
		if (cl.hasOption("certificate")){
			String ok = cl.getOptionValue("certificate");
			thrb.withLegacyThreshold("certificate", ok, null, null);
//			String critical = cl.getOptionValue("critical");
//			String warning = cl.getOptionValue("warning");
		}
	}

	/**
	 * Execute the plugin
	 */
	public Collection<Metric> gatherMetrics(ICommandLine cl)
			throws MetricGatheringException {
		List<Metric> metrics = new ArrayList<Metric>();
		String hostname = cl.getOptionValue("hostname");
		String port = cl.hasOption("port") ? cl.getOptionValue("port") : "" + DEFAULT_PORT;
		String path = cl.hasOption("url") ? cl.getOptionValue("url") : DEFAULT_PATH;
		String method = cl.hasOption("method") ? cl.getOptionValue("method").toUpperCase() : DEFAULT_METHOD;
		int timeout = DEFAULT_TIMEOUT;
		if (cl.hasOption("post")){
			method = "POST";
		}		
		boolean ssl = false;
		if (cl.hasOption("ssl") || cl.getOptionValue("certificate") != null) {
			if (cl.getOptionValue("ssl") != null){
				port = cl.getOptionValue("ssl");
			}else{
				port = DEFAULT_SSL_PORT;
			}
			ssl = true;
		}
		if (cl.getOptionValue("timeout") != null){
			try{
				timeout = Integer.parseInt(cl.getOptionValue("timeout"));
			}catch(NumberFormatException e){
				throw new MetricGatheringException("Invalid numeric value for timeout.", Status.CRITICAL, e);
			}
		}
		if (!path.startsWith("/")){
			path = "/" + path;
		}
		if (hostname.endsWith("/")){
			hostname = hostname.substring(0, hostname.length() -1);
		}

		long then = System.currentTimeMillis();
		
		String response = getHttpResponse(cl, hostname, port, method, path, timeout, ssl, metrics);
		int ellapsed = (int)Utils.milliToSec(System.currentTimeMillis() - then);
		metrics.addAll(analyzeResponse(cl, response, ellapsed));
		return metrics;
	}

	/**
	 * Do the actual http request and return the response string
	 * 
	 * @param cl
	 * @param hostname
	 * @param port
	 * @param method
	 * @param path
	 * @param timeout
	 * @return
	 * @throws MetricGatheringException
	 * String
	 */
	private String getHttpResponse(ICommandLine cl, 
			String hostname, 
			String port, 
			String method,
			String path,
			int timeout,
			boolean ssl,
			List<Metric> metrics) throws MetricGatheringException{
		Properties props = getRequestProperties(cl, method);
		String response = null;
		String redirect = cl.getOptionValue("onredirect");
		boolean ignoreBody = false;
		try{
			String data = null;
			if ("POST".equals(method)) {
				data = getPostData(cl);
			}
			
			if (cl.hasOption("no-body")){
				ignoreBody = true;				
			}				
			String urlString = hostname + ":" + port + path;
			if (cl.getOptionValue("authorization") != null){
				urlString = cl.getOptionValue("authorization") + "@";
			}else if (cl.getOptionValue("proxy-authorization") != null){
				urlString = cl.getOptionValue("proxy-authorization") + "@";
			}
			if (ssl) {
				urlString = "https://" + urlString;
			}else{
				urlString = "http://" + urlString;
			}		
			URL url = new URL(urlString);
			if (cl.getOptionValue("certificate") != null){
				checkCertificateExpiryDate(url, metrics);
			}else if (redirect != null) {
				response = checkRedirectResponse(url, method, timeout, props, data, redirect, ignoreBody, metrics);
			}else{
				try {
					if ("GET".equals(method)){
						response = HttpUtils.doGET(url, props, timeout, true, ignoreBody);
					}else if ("POST".equals(method)){						
						response = HttpUtils.doPOST(url, props, null, data, true, ignoreBody);
					}else if ("HEAD".equals(method)){
						response = HttpUtils.doHEAD(url, props, timeout, true, ignoreBody);
					}
					// @TODO complete for other http methods

				} catch (MalformedURLException e) {
					e.printStackTrace();
					log.error("Bad url", e);
					throw new MetricGatheringException("Bad url string : " + urlString, Status.CRITICAL, e);
				}
			}

		}catch(Exception e){
			e.printStackTrace();
			log.error("Exception: " + e.getMessage(), e);
			throw new MetricGatheringException(e.getClass().getName() + ": " + e.getMessage(), Status.CRITICAL, e);
		}
		return response;		
	}

	/**
	 * Apply the logic to check for url redirects
	 * @param url
	 * @param method
	 * @param timeout
	 * @param metrics
	 * @return
	 * String
	 */
	private String checkRedirectResponse(URL url, 
			String method, 
			Integer timeout, 
			Properties props, 
			String postData,
			String redirect,
			boolean ignoreBody,
			List<Metric> metrics) throws Exception {
		// @todo handle sticky/port and follow param options		
		
		String response = null;
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod(method);
		HttpUtils.setRequestProperties(props, conn, timeout);
		String initialUrl = conn.getURL() + "";
		String redirectedUrl = null;		
		if (method.equals("POST")){
			HttpUtils.sendPostData(conn, postData);
		}
		response = HttpUtils.parseHttpResponse(conn, false, ignoreBody);
		redirectedUrl = conn.getURL() + "";
		
		if (!redirectedUrl.equals(initialUrl)){
			Metric metric = new Metric("onredirect", "", new BigDecimal(1), null, null);
			metrics.add(metric);
		}
		return response;
	}

	/**
	 * Apply logic to the http response and build metrics
	 * 
	 * @param opt
	 * @param response
	 * @param ellapsed
	 * @return
	 * @throws MetricGatheringException
	 * List<Metric>
	 */
	private List<Metric> analyzeResponse(ICommandLine opt, String response, int ellapsed) throws MetricGatheringException {
		log.debug("response");

		List<Metric> metrics = new ArrayList<Metric>();
		metrics.add(new Metric("time", "", new BigDecimal(ellapsed), null, null));

		if (opt.getOptionValue("certificate") == null 
				&& !opt.hasOption("certificate")) {			
			if (opt.getOptionValue("string") != null && !opt.getOptionValue("string").equals("")){
				boolean found = false;
				String string = opt.getOptionValue("string");
				found = response.contains(string);
				metrics.add(new Metric("string", "", new BigDecimal(Utils.getIntValue(found)), null, null));
			}
			if (opt.getOptionValue("expect") != null){
				int count = 0;
				String[] values = opt.getOptionValue("expect").split(",");
				for (String value: values){
					if (response.contains(value)){
						count++;
					}
				}
				metrics.add(new Metric("expect", "" + count + " times. ", new BigDecimal(count), null, null));
			}
			if (opt.getOptionValue("regex") != null){
				String regex = opt.getOptionValue("regex");
				Pattern p = null;
				int flags = 0;
				if (opt.hasOption("eregi")){
					flags = Pattern.CASE_INSENSITIVE;
				}
				if (opt.hasOption("linespan")){
					flags = flags | Pattern.MULTILINE;
				}
				p = Pattern.compile(regex, flags);	
				boolean found = p.matcher(response).find();	    
				if (opt.hasOption("invert-regex")){
					metrics.add(new Metric("invert-regex", "" + found, new BigDecimal(Utils.getIntValue(found)), null, null));
				}else{
					metrics.add(new Metric("regex", "" + found, new BigDecimal(Utils.getIntValue(found)), null, null));
				}
			} 
		}
		return metrics;
	}

	/**
	 * Set the http request properties and headers 
	 * @param cl
	 * @param method
	 * @return
	 * Properties
	 */
	private Properties getRequestProperties(ICommandLine cl, String method){
		Properties props = new Properties();
		if (cl.getOptionValue("useragent") != null){
			props.put("User-Agent", cl.getOptionValue("useragent"));
		}else{
			props.put("User-Agent", DEFAULT_USER_AGENT);
		}
		if (cl.getOptionValue("content-type") != null && method.toUpperCase().equals("POST")){
			props.put("Content-Type", cl.getOptionValue("content-type"));
		}
		if (cl.getOptionValues("header") != null){
			List headers = cl.getOptionValues("header");
			for (Object obj: headers){
				String header = (String)obj;
				String key = header.split(":")[0].trim();
				String value = header.split(":")[1].trim();
				props.put(key, value);
			}
		}
		String auth = null;
		String encoded = null;
		if (cl.getOptionValue("authorization") != null){
			encoded = Base64.encodeBase64String(cl.getOptionValue("authorization").getBytes());	
			auth = "Authorization";	    
		}else if (cl.getOptionValue("proxy-authorization") !=null){
			encoded = Base64.encodeBase64String(cl.getOptionValue("proxy-authorization").getBytes());	        
			auth = "Proxy-Authorization";
		}
		if (auth != null && encoded != null){
			props.put(auth, "Basic " + encoded);
		}	
		return props;
	}

	/**
	 * Returns encoded post data
	 * 
	 * @param cl
	 * @return
	 * @throws UnsupportedEncodingException
	 * String
	 */
	private String getPostData(ICommandLine cl) throws Exception {
		String encoded = "";
		String data = cl.getOptionValue("post");
		if (data == null){
			return null;
		}
		String[] values = data.split("&");
		for (String value: values){
			String[] splitted = value.split("=");
			String key = splitted[0];
			String val = "";
			if (splitted.length > 1){
				val = splitted[1];
			}
			encoded += key + "=" + URLEncoder.encode(val, "UTF-8") + "&";
		}	
		if (encoded.endsWith("&")){
			StringUtils.removeEnd(encoded, "&");
		}
		return encoded;
	}


	/* (non-Javadoc)
	 * @see it.jnrpe.plugins.PluginBase#getPluginName()
	 */
	@Override
	protected String getPluginName() {
		return "CHECK_HTTP";
	}

	
	// stuff for checking certificate
	private void checkCertificateExpiryDate(URL url, List<Metric> metrics) throws Exception {
		SSLContext ctx = SSLContext.getInstance("TLS");
		ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
		SSLContext.setDefault(ctx);
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();	
		conn.setHostnameVerifier(new HostnameVerifier() {	    
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		});
		List<Date> expiryDates = new ArrayList<Date>();
		conn.getResponseCode();
		Certificate[] certs = conn.getServerCertificates();
		for (Certificate cert :certs){
			X509Certificate x509 = (X509Certificate)cert;
			Date expiry = x509.getNotAfter();
			expiryDates.add(expiry);
		}	
			
		conn.disconnect();
		Date today = new Date();                
		for (Date date:expiryDates){
			int diffInDays = (int)( (date.getTime() - today.getTime()) / (1000 * 60 * 60 * 24) );
			metrics.add(new Metric("certificate", "", new BigDecimal(diffInDays), null, null));
		}
	}

	private static class DefaultTrustManager implements X509TrustManager {

		public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

		}

		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

		}

		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}

}
