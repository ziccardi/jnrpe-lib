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
package it.jnrpe.net;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.zip.CRC32;

/**
 * This class represent a generic NRPE packet.
 * 
 * @author Massimiliano Ziccardi
 *
 */
class JNRPEProtocolPacket
{
	private int m_iCRC = 0;
	private int m_iPacketType = 0;
	private int m_iPacketVersion = 0;
	private int m_iResultCode = 0;
	private byte[] m_vBuffer = new byte[IJNRPEConstants.MAX_PACKETBUFFER_LENGTH];
	private byte[] m_vDummy = new byte[2];

	public int getCRC()
	{
		return m_iCRC;
	}

	public int getPacketType()
	{
		return m_iPacketType;
	}

	public int getPacketVersion()
	{
		return m_iPacketVersion;
	}

	public void setCRC(int iCRC)
	{
		m_iCRC = iCRC;
	}

	protected void setPacketType(int iPacketType)
	{
		m_iPacketType = iPacketType;
	}

	public void setPacketVersion(int iPacketVersion)
	{
		m_iPacketVersion = iPacketVersion;
	}

	public int getResultCode()
	{
		return m_iResultCode;
	}

	public void setResultCode(int iResultCode)
	{
		m_iResultCode = iResultCode;
	}

	/**
	 * Initialize the object reading the data from the input stream
	 * @param in
	 * @throws IOException
	 */
	protected void fromInputStream(InputStream in) throws IOException
	{
		DataInputStream din = new DataInputStream(in);
		m_iPacketVersion = din.readShort();
		m_iPacketType = din.readShort();
		m_iCRC = din.readInt();
		m_iResultCode = din.readShort();
        din.readFully(m_vBuffer);
        din.readFully(m_vDummy);
	}

	/**
	 * Validates the packet CRC
	 * 
	 * @throws BadCRCException
	 */
	public void validate() throws BadCRCException
	{
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(bout);

		try
		{
			dout.writeShort(m_iPacketVersion);
			dout.writeShort(m_iPacketType);
			dout.writeInt(0); // NO CRC
			dout.writeShort(m_iResultCode);
			dout.write(m_vBuffer);
			dout.write(m_vDummy);

			dout.close();

			byte[] vBytes = bout.toByteArray();

			CRC32 crcAlg = new CRC32();
			crcAlg.update(vBytes);

			if (!(((int) crcAlg.getValue()) == m_iCRC))
				throw new BadCRCException("Bad CRC");
		}
		catch (IOException e)
		{
			// Should never happen...
		}
	}

	public byte[] toByteArray()
	{
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(bout);

		try
		{
			dout.writeShort(m_iPacketVersion);
			dout.writeShort(m_iPacketType);
			dout.writeInt(m_iCRC);
			dout.writeShort(m_iResultCode);
			dout.write(m_vBuffer);
			dout.write(m_vDummy);

			dout.close();

			
		}
		catch (IOException e)
		{
			// Should never happen...
		}
		return bout.toByteArray();
	}
	
	public String getStringMessage()
	{
		int iZeroIndex = IJNRPEConstants.MAX_PACKETBUFFER_LENGTH - 1;
		
		// find the first 0 byte
		for (int i = 0; i < IJNRPEConstants.MAX_PACKETBUFFER_LENGTH; i++)
			if (m_vBuffer[i] == 0)
			{
				iZeroIndex = i;
				break;
			}
		
		return new String(m_vBuffer, 0, iZeroIndex);
	}

	protected void _setMessage(String sMessage)
	{
		if (sMessage == null)
			sMessage = "";
		System.arraycopy(sMessage.getBytes(), 0, m_vBuffer, 0, Math.min(sMessage.length(), IJNRPEConstants.MAX_PACKETBUFFER_LENGTH));
		
		if (sMessage.length() < IJNRPEConstants.MAX_PACKETBUFFER_LENGTH)
			m_vBuffer[sMessage.length()] = 0;
	}
	
	// Not sure this is really needed...
	protected void initRandomBuffer()
	{
		Random r = new Random(System.currentTimeMillis());

		r.nextBytes(m_vBuffer);
		r.nextBytes(m_vDummy);
	}
}
