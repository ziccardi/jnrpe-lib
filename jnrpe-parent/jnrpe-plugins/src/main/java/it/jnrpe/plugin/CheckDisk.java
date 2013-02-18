/*
 * Copyright (c) 2011 Massimiliano Ziccardi
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
import it.jnrpe.plugins.IPluginInterface;
import it.jnrpe.utils.ThresholdUtil;

import java.io.File;

/**
 * Checks the disk space
 * 
 * @author Massimiliano Ziccardi
 *
 */
public class CheckDisk implements IPluginInterface
{

    private final static long KB = 1024;
    private final static long MB = KB << 10;
    
    /**
     * Compute the percent values
     * 
     * @param val The value to be represented in percent
     * @param total The total value
     * @return The percent of val/total
     */
    private int percent(long val, long total)
    {
        if (total == 0)
            return 100;
        
        if (val == 0)
            return 0;
        
        double dVal = (double) val;
        double dTotal = (double) total;
        
        return (int) (dVal / dTotal * 100);
    }
    
    /**
     * Executes the check
     * 
     * @param cl the command line
     * @return the check return code
     */
    public ReturnValue execute(ICommandLine cl)
    {
        String sPath = cl.getOptionValue("path");
        String sWarning = cl.getOptionValue("warning");
        String sCritical = cl.getOptionValue("critical");
        
        File f = new File(sPath);
        
        long lBytes = f.getFreeSpace();
        long lTotalSpace = f.getTotalSpace();
        
        String sFreeSpace = format(lBytes);
        String sUsedSpace = format(lTotalSpace - lBytes); 
        
        int iFreePercent = percent(lBytes, lTotalSpace);
        
        String sFreePercent = "" + iFreePercent + "%";
        String sUsedPercent = "" + percent(lTotalSpace - lBytes, lTotalSpace) + "%";
        
        if (ThresholdUtil.isValueInRange(sCritical, iFreePercent))
        {
            return new ReturnValue(Status.CRITICAL, 
                    "CHECK_DISK CRITICAL - Used: " + sUsedSpace + "(" + sUsedPercent + ") Free: " + sFreeSpace + "(" + sFreePercent + ")");
        }

        if (ThresholdUtil.isValueInRange(sWarning, iFreePercent))
        {
            return new ReturnValue(Status.WARNING, 
                    "CHECK_DISK WARNING - Used: " + sUsedSpace + "(" + sUsedPercent + ") Free: " + sFreeSpace + "(" + sFreePercent + ")");
        }

        return new ReturnValue(Status.OK, 
                "CHECK_DISK OK - Used: " + sUsedSpace + "(" + sUsedPercent + ") Free: " + sFreeSpace + "(" + sFreePercent + ")");
    }

    /**
     * Format the size returning it as MB or KB.
     * 
     * @param lBytes The size to be formatted
     * @return The formatted size
     */
    private String format(long lBytes)
    {
        if (lBytes > MB)
        {
            return "" + (lBytes / MB) + " MB";
        }
        return "" + (lBytes / KB) + " KB";
    }

}
