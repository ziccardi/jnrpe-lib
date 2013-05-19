/**
 * 
 */
package it.jnrpe.plugin.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Properties;

/**
 * Utility class for various operations
 * 
 * @author Frederico
 *
 */
public class Utils {
	
	/**
	 * Get contents from an URL
	 * @param url
	 * @param requestProps
	 * @return
	 * @throws IOException
	 * String
	 */
	public static String getUrl(URL url, Properties requestProps, Integer timeout) throws IOException {
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		if (requestProps != null){
			for (Object key: requestProps.keySet()) {
				httpConn.setRequestProperty(key + "", requestProps.get(key) + "");				
			}
		}
		if (timeout != null){
			httpConn.setConnectTimeout(timeout);
		}
		httpConn.setRequestMethod("GET");        
		BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
		StringBuffer buff = new StringBuffer();
        String inputLine;
        while ((inputLine = in.readLine()) != null){
            buff.append(inputLine);
        }
        in.close();
        return buff.toString();		
	}
	
	/**
	 * Get contents from an URL
	 * @param url
	 * @return
	 * @throws IOException
	 * String
	 */
	public static String getUrl(String url) throws IOException{
		return getUrl(new URL(url), null, null);
	}
	
	/**
	 * Returns formatted size of a file size
	 * @param value
	 * @return
	 * String
	 */
	public static String formatSize(long value) {
        double size = new Double(value);
        double BASE = 1024;
        double KB = BASE;
        double MB = KB * BASE;
        double GB = MB * BASE;
        DecimalFormat df = new DecimalFormat("#.##");
        if (size >= GB) {
            return df.format(size / GB) + " GB";
        }
        if (size >= MB) {
            return df.format(size / MB) + " MB";
        }
        if (size >= KB) {
            return df.format(size / KB) + " KB";
        }
        return "" + (int) size + " bytes";
    }
}
