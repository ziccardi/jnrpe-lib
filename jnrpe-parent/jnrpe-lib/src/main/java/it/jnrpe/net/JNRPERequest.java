/*
 * Copyright (c) 2008 Massimiliano Ziccardi Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package it.jnrpe.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

import org.apache.commons.lang.StringUtils;

/**
 * This object represent a generic request packet.
 *
 * @author Massimiliano Ziccardi
 */
public class JNRPERequest extends JNRPEProtocolPacket
{
    /**
     * This constructor initializes the object with the data read from the given.
     * input stream.
     *
     * @param in
     *            The stream containing the data to be parsed
     * @throws IOException
     *             On any IO exception
     * @throws BadCRCException
     *             If the CRC can't be validated
     */
    public JNRPERequest(final InputStream in) throws IOException,
            BadCRCException
    {
        fromInputStream(in);

        validate();
    }

    public JNRPERequest(String sCommand, String...arguments)
    {
    	String sCommandBytes;
    	
    	if (arguments != null)
    	{
    		String[] ary = new String[arguments.length + 1];
    		
    		for (int i = 0; i < arguments.length; i++)
    		{
    			if (arguments[i].indexOf('!') == -1)
    				ary[i+1] = arguments[i];
    			else
    				ary[i+1] = "'" + arguments[i] + "'";
    		}
    		//System.arraycopy(arguments, 0, ary, 1, arguments.length);
    		ary[0] = sCommand;
    		
    		sCommandBytes = StringUtils.join(ary, '!');
    	}
    	else
    		sCommandBytes = sCommand;
    	
    	
    	
    	setPacketVersion(PacketVersion.VERSION_2);
    	super.setPacketType(PacketType.QUERY);
    	super.initRandomBuffer();
    	super.setDataBuffer(sCommandBytes);
    	updateCRC();
    }
    
    /**
     * Updates the CRC value.
     */
    // TODO : move into base class....
    private void updateCRC()
    {
        setCRC(0);
        int iCRC = 0;

        CRC32 crcAlg = new CRC32();
        crcAlg.update(toByteArray());

        iCRC = (int) crcAlg.getValue();

        setCRC(iCRC);
    }
    
}
