/**
 * 
 */
package it.jnrpe.plugin;

import it.jnrpe.ICommandLine;
import it.jnrpe.plugins.Metric;
import it.jnrpe.plugins.MetricGatheringException;
import it.jnrpe.plugins.PluginBase;
import it.jnrpe.plugins.annotations.Option;
import it.jnrpe.plugins.annotations.Plugin;
import it.jnrpe.plugins.annotations.PluginOptions;
import it.jnrpe.utils.BadThresholdException;
import it.jnrpe.utils.thresholds.ThresholdsEvaluatorBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Checks system processes and does check against metrics.
 * 
 * @author Frederico Campos
 * 
 */
@Plugin(name = "CHECK_PROCS", description = "Checks system processes and does check against metrics. Default metrics is number of processes.")
@PluginOptions({
	
	@Option(shortName = "w", longName = "warning", description = "Warning value if metric is out of range", required = false, hasArgs = true, argName = "warning", optionalArgs = false, option = "warning"),
	@Option(shortName = "c", longName = "critical", description = "Critical value if metric is out of range", required = false, hasArgs = true, argName = "critical", optionalArgs = false, option = "critical"),
	@Option(shortName = "m", longName = "metric", description = "Metric type. Valid values are: PROCS - number of processes"
			+ "; VSZ - virtual memory size; RSS -  resident set memory size; CPU - CPU percentage; ELAPSED - elapsed time in seconds", required = false, hasArgs = true, argName = "metric", optionalArgs = false, option = "metric"),
	@Option(shortName = "T", longName = "traditional", description = "Filter own process the traditional way by PID instead of /proc/pid/exe", required = false, hasArgs = false, argName = "traditional", optionalArgs = false, option = "traditional"),
	@Option(shortName = "t", longName = "timeout", description = "Seconds before connection times out (default: 10)", required = false, hasArgs = true, argName = "timeout", optionalArgs = false, option = "timeout"),
	@Option(shortName = "a", longName = "argument-array", description = "Only scan for processes with args that contain STRING.", required = false, hasArgs = true, argName = "timeout", optionalArgs = false, option = "timeout"),
	@Option(shortName = "c", longName = "command", description = "Only scan for exact matches of COMMAND (without path).", required = false, hasArgs = true, argName = "timeout", optionalArgs = false, option = "timeout")
	
})
public class CheckProcs extends PluginBase {
	
	private final static int DEFAULT_TIMEOUT = 10;
	
	private final static String DEFAULT_WINDOWS_CMD = "tasklist.exe /FO CSV";
	
	private final static String DEFAULT_UNIX_CMD = "ps -ef";
	
	@Override
	protected String getPluginName() {
		return "CHECK_PROCS";
	}

	@Override
	public final void configureThresholdEvaluatorBuilder(
			final ThresholdsEvaluatorBuilder thrb, final ICommandLine cl)
					throws BadThresholdException {
		if (cl.getOptionValue("metrics") != null){
			
		}else{
			thrb.withThreshold("total_procs");
		}		 
	}
	
	/**
	 * Execute and gather metrics.
	 * 
	 * @param cl
	 * @return
	 * @throws MetricGatheringException
	 * Collection<Metric>
	 */
	public final Collection<Metric> gatherMetrics(final ICommandLine cl) throws MetricGatheringException {
		List<Metric> metrics = new ArrayList<Metric>();
		String os = System.getProperty("os.name").toLowerCase();
		String command = null;
		boolean unix = true;
        if (os.contains("windows")) {
            command = DEFAULT_WINDOWS_CMD;
            unix = false;
        }else{
        	command = DEFAULT_UNIX_CMD;
        }        
        String output = exec(command);
        List<Map<String,String>> result = unix ? parseUnixOutput(output) : parseWindowsOutput(output); 
        
        metrics.add(new Metric("total_procs", "", new BigDecimal(result.size()), null, null));
        
		if (cl.hasOption("command")){
			metrics.add(new Metric("total_procs", "", new BigDecimal(result.size()), null, null));
		}
		return metrics;
	}
	
	
	/**
	 * Execute a system command and return output.
	 * 
	 * @param command
	 * @return
	 * String
	 */
	private String exec(String command) {
        String line = null;		
        StringBuffer lines = new StringBuffer();
        
	    try {
	        Process p = Runtime.getRuntime().exec(command);
	        BufferedReader input =
	                new BufferedReader(new InputStreamReader(p.getInputStream()));
	        while ((line = input.readLine()) != null) {
	            lines.append(line);
	        }
	        input.close();
	    } catch (Exception err) {
	        err.printStackTrace();
	    }
	    return lines.toString();
	}	
	
	/**
	 * Parse command output for windows environment
	 * 
	 * @param output
	 * @return
	 * List<Map<String,String>>
	 */
	private List<Map<String,String>> parseWindowsOutput(String output) {
		List<Map<String,String>> info = new ArrayList<Map<String,String>>();
		String[] lines = output.split("\n");
		for (String l: lines){
			Map<String, String> values = new HashMap<String,String>();
			String[] line = l.replace("\"", "").split(",");
			values.put("command", line[0]);
			values.put("pid", line[1]);
			values.put("memory", line[4].replace("K", "").trim());			
		}		
		return info;
	}
	
	/**
	 * Parse command output for unix environment
	 * 
	 * @param output
	 * @return
	 * List<Map<String,String>>
	 */
	private List<Map<String,String>> parseUnixOutput(String output) {
		return null;
	}
}
