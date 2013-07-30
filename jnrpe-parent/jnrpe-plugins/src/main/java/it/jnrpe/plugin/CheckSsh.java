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
import it.jnrpe.plugins.Metric;
import it.jnrpe.plugins.MetricGatheringException;
import it.jnrpe.plugins.PluginBase;
import it.jnrpe.plugins.annotations.Option;
import it.jnrpe.plugins.annotations.Plugin;
import it.jnrpe.plugins.annotations.PluginOptions;
import it.jnrpe.utils.BadThresholdException;
import it.jnrpe.utils.thresholds.ThresholdsEvaluatorBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * Plugin to check ssh connections on a remote host.
 * 
 * @author Frederico Campos
 *
 */

@Plugin(
		name = "CHECK_SSH",
		description = "Try to connect to an SSH server at specified server and port.\n" +
				"EXAMPLES:\n" +
				"The example will be based upon the following command definition (ini file)\n\n" +
			    "check_ssh : CHECK_SSH --hostname $ARG1$ --port $ARG2$ --password $ARG3$ \n"+
				"check_nrpe -H myjnrpeserver -c check_ssh -a myhostname 22 password")
@PluginOptions({
	@Option(
		shortName="h",
		longName="hostname",
		description="IP or hostname",
		required=true,
		hasArgs=true,
		argName="hostname",
		optionalArgs=false,
		option="hostname"
	),
	@Option(
		shortName="p",
		longName="port",
		description="Port number. Default is 22.",
		required=false,
		hasArgs=true,
		argName="port",
		optionalArgs=false,
		option="port"
	),
	@Option(
		shortName="u",
		longName="username",
		description="Username.",
		required=false,
		hasArgs=true,
		argName="username",
		optionalArgs=false,
		option="username"
	),
	@Option(
		shortName="P",
		longName="password",
		description="Password.",
		required=false,
		hasArgs=true,
		argName="hostname",
		optionalArgs=false,
		option="password"
	),
	@Option(
		shortName="t",
		longName="timeout",
		description="Seconds before connection times out (default: 10)",
		required=false,
		hasArgs=true,
		argName="timeout",
		optionalArgs=false,
		option="timeout"
	)
})

public class CheckSsh extends PluginBase {


	/**
	 * @TODO 
	 * 
	 * - ssh key authentication
	 *
	 * - remote-version check option
	 *
	 */
	
    /**
     * Default timeout.
     */
    private static final int DEFAULT_TIMEOUT = 10;
    
    /**
     * Default HTTP port.
     */
    private static final int DEFAULT_PORT = 22;

	@Override
	protected String getPluginName() {
		return "CHECK_SSH";
	}

	@Override
	public final void configureThresholdEvaluatorBuilder(
			final ThresholdsEvaluatorBuilder thrb, final ICommandLine cl)
					throws BadThresholdException {
		thrb.withLegacyThreshold("connected", "1:", null, "0");
	}

	@Override
	public final Collection<Metric> gatherMetrics(final ICommandLine cl)
			throws MetricGatheringException {
		List<Metric> metrics = new ArrayList<Metric>();
		Session session = null;
		try {
			session = getSession(cl);
			Channel channel=session.openChannel("shell");
			channel.setInputStream(System.in);

			channel.setOutputStream(System.out);
			channel.connect();
			metrics.add(new Metric("connected", "", new BigDecimal(1), null, null));
			channel.disconnect();
			session.disconnect();
			
		} catch (Exception e) {
			metrics.add(new Metric("connected", e.getMessage(), new BigDecimal(0), null, null));
			log.debug(e.getMessage(), e);
		}
		return metrics;
	}
	
	/**
	 * Starts an ssh session
	 * 
	 * @param cl
	 * @return
	 * @throws MetricGatheringException
	 * @throws Exception
	 * Session
	 */
	protected Session getSession(final ICommandLine cl) throws MetricGatheringException, Exception {
		JSch jsch = new JSch();
		Session session = null;
		int timeout = DEFAULT_TIMEOUT;
        int port = cl.hasOption("port") ? Integer.parseInt(cl.getOptionValue("port")) :  + DEFAULT_PORT;
        String hostname = cl.getOptionValue("hostname");
        String username = cl.getOptionValue("username");
        String password = cl.getOptionValue("password");
        String key = cl.getOptionValue("key");
        if (cl.getOptionValue("timeout") != null) {
            try {
                timeout = Integer.parseInt(cl.getOptionValue("timeout"));
            } catch (NumberFormatException e) {
                throw new MetricGatheringException("Invalid numeric value for timeout.", Status.CRITICAL, e);
            }
        }
        session = jsch.getSession(username, hostname, port);
        if (key == null) {
        	session.setConfig("StrictHostKeyChecking", "no");
        	session.setPassword(password);
        }else{
        	jsch.addIdentity(key);
        }
        session.connect(timeout * 1000);
        return session;
	}
}
