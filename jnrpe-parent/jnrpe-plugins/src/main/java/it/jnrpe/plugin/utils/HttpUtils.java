/*
 * Copyright (c) 2008 Massimiliano Ziccardi
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
package it.jnrpe.plugin.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

/**
 * Utility class for various HTTP operations.
 *
 * @author Frederico Campos
 *
 */
public class HttpUtils {

	/**
	 * Do a http get request and return response
	 * @param url
	 * @param requestProps
	 * @param timeout
	 * @param includeHeaders
	 * @param ignoreBody
	 * @return
	 * @throws Exception
	 */
	public static String doGET(final URL url, 
			final Properties requestProps,
			final Integer timeout,
			boolean includeHeaders,
			boolean ignoreBody			
			) throws Exception {
		return doRequest(url, requestProps, timeout, includeHeaders, ignoreBody, "GET");
	}
	
	/**
	 * Do a http head request and return response
	 *  
	 * @param url
	 * @param requestProps
	 * @param timeout
	 * @param includeHeaders
	 * @param ignoreBody
	 * @return
	 * @throws Exception
	 */
	public static String doHEAD(final URL url, 
			final Properties requestProps,
			final Integer timeout,
			boolean includeHeaders,
			boolean ignoreBody			
			) throws Exception {
		return doRequest(url, requestProps, timeout, includeHeaders, ignoreBody, "HEAD");
	}

	/**
	 * Do a http post request and return response
	 * 
	 * @param url
	 * @param requestProps
	 * @param timeout
	 * @param encodedData
	 * @param includeHeaders
	 * @param ignoreBody
	 * @return
	 * @throws IOException
	 */
	public static String doPOST(final URL url, 
			final Properties requestProps,
			final Integer timeout,
			final String encodedData,
			boolean includeHeaders,
			boolean ignoreBody) throws IOException {		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		setRequestProperties(requestProps, conn, timeout);
		sendPostData(conn, encodedData);
		return parseHttpResponse(conn, includeHeaders, ignoreBody);
	} 

	/**
	 * Submits http post data to an HttpURLConnection
	 *
	 * @param conn
	 * @param encodedData
	 * @throws IOException
	 */
	public static void sendPostData(HttpURLConnection conn, String encodedData) throws IOException {
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		if (conn.getRequestProperty("Content-Type") == null) {
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		}
		if (encodedData != null){
			if (conn.getRequestProperty("Content-Length") == null) {
				conn.setRequestProperty("Content-Length", "" + encodedData.getBytes().length);
			}
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			out.write(encodedData.getBytes());
			out.close();
		}    	
	}
	
	private static String doRequest(final URL url, 
			final Properties requestProps,
			final Integer timeout,
			boolean includeHeaders,
			boolean ignoreBody,
			String method) throws Exception {
		if (method.toUpperCase().equals("POST")){
			throw new Exception("use it.jnrpe.plugin.utils.HttpUtils.doPOST instead.");
		}
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		setRequestProperties(requestProps, conn, timeout);
		conn.setRequestMethod("GET");
		String response =  parseHttpResponse(conn, includeHeaders, ignoreBody);
		return response;
	}
	
	/**
	 * Sets request headers for an http connection
	 * 
	 * @param props
	 * @param conn
	 * @param timeout
	 */
	public static void setRequestProperties(final Properties props,
			HttpURLConnection conn,
			Integer timeout){
		if (props != null) {
			if (props.get("User-Agent") == null){
				conn.setRequestProperty("User-Agent", "Java"); 
			}
			for (Object key : props.keySet()) {
				conn.setRequestProperty(key + "", props.get(key) + "");
			}
		}
		
		
		if (timeout != null) {
			conn.setConnectTimeout(timeout * 1000);
		}
		
	}
	
	/**
	 * Parses an http request response
	 * 
	 * @param conn
	 * @param includeHeaders
	 * @param ignoreBody
	 * @return
	 * @throws IOException
	 */
	public static String parseHttpResponse(HttpURLConnection conn, 
			boolean includeHeaders, 
			boolean ignoreBody) throws IOException {
		StringBuffer buff = new StringBuffer();
		if (includeHeaders){
			buff.append(conn.getResponseCode() + " " + conn.getResponseMessage()+"\n");
			int idx = (conn.getHeaderFieldKey(0) == null) ? 1 : 0;
			while (true) {
				String key = conn.getHeaderFieldKey(idx);
				if (key == null){
					break;
				}
				buff.append(key + ": " + conn.getHeaderField(idx) + "\n");
				++idx;
			}			
		}
		if (!ignoreBody){
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				buff.append(inputLine);
			}
			in.close();
		}
		return buff.toString();
	}

}
