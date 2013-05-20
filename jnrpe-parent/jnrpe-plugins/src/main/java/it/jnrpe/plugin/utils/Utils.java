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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Properties;

/**
 * Utility class for various operations.
 *
 * @author Frederico
 *
 */
public final class Utils {

    /**
     * Base multiplier.
     */
    private static final double BASE = 1024;
    /**
     * KB value.
     */
    private static final double KB = BASE;
    /**
     * MB value.
     */
    private static final double MB = KB * BASE;
    /**
     * GB value.
     */
    private static final double GB = MB * BASE;

    /**
     *
     */
    private Utils() {
    }

    /**
     * Get contents from an URL.
     *
     * @param url
     *            The url
     * @param requestProps
     *            The request props
     * @param timeout
     *            The timeout
     * @return Requested value
     * @throws IOException
     *             -
     */
    public static String getUrl(final URL url, final Properties requestProps,
            final Integer timeout) throws IOException {
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        if (requestProps != null) {
            for (Object key : requestProps.keySet()) {
                httpConn.setRequestProperty(key + "", requestProps.get(key)
                        + "");
            }
        }
        if (timeout != null) {
            httpConn.setConnectTimeout(timeout);
        }
        httpConn.setRequestMethod("GET");
        BufferedReader in =
                new BufferedReader(new InputStreamReader(
                        httpConn.getInputStream()));
        StringBuffer buff = new StringBuffer();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            buff.append(inputLine);
        }
        in.close();
        return buff.toString();
    }

    /**
     * Get contents from an URL.
     *
     * @param url
     *            The url
     * @return -
     * @throws IOException
     *             -
     */
    public static String getUrl(final String url) throws IOException {
        return getUrl(new URL(url), null, null);
    }

    /**
     * Returns formatted size of a file size.
     *
     * @param value
     *            The value
     * @return String
     */
    public static String formatSize(final long value) {
        double size = new Double(value);
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
