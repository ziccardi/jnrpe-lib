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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.CRC32;

/**
 * This class represent a generic NRPE protocol packet.
 *
 * @author Massimiliano Ziccardi
 */
class JNRPEProtocolPacket
{
    /**
     * Max amount of data we'll send in one query/response.
     */
    private static final int MAX_PACKETBUFFER_LENGTH = 1024;

    /**
     * The CRC value.
     */
    private int m_iCRC = 0;

    /**
     * The packet type.
     */
    private int m_iPacketType = 0;

    /**
     * The packet version.
     */
    private int m_iPacketVersion = 0;

    /**
     * The result code.
     */
    private int m_iResultCode = 0;

    /**
     * The packet buffer.
     */
    private byte[] m_vBuffer = new byte[MAX_PACKETBUFFER_LENGTH];

    /**
     * Dummy bytes.
     */
    private byte[] m_vDummy = new byte[2];

    /**
     * Returns the packet CRC value.
     *
     * @return the CRC value
     */
    public int getCRC()
    {
        return m_iCRC;
    }

    /**
     * Returns the packet type.
     *
     * @return The packet type
     */
    public PacketType getPacketType()
    {
        return PacketType.fromIntValue(m_iPacketType);
    }

    /**
     * Returns the packet version.
     *
     * @return The packet version
     */
    public PacketVersion getPacketVersion()
    {
        return PacketVersion.fromIntValue(m_iPacketVersion);
    }

    /**
     * Sets the CRC value.
     *
     * @param iCRC
     *            The new CRC value
     */
    public void setCRC(final int iCRC)
    {
        m_iCRC = iCRC;
    }

    /**
     * Sets the packet type.
     *
     * @param packetType
     *            The new packet type
     */
    protected void setPacketType(final PacketType packetType)
    {
        m_iPacketType = packetType.intValue();
    }

    /**
     * Sets the packet version.
     *
     * @param version
     *            The packet version
     */
    public void setPacketVersion(final PacketVersion version)
    {
        m_iPacketVersion = version.intValue();
    }

    /**
     * Returns the result code.
     *
     * @return The result code
     */
    public int getResultCode()
    {
        return m_iResultCode;
    }

    /**
     * Sets the result code.
     *
     * @param status
     *            The new result code
     */
    public void setResultCode(final int status)
    {
        m_iResultCode = status;
    }

    /**
     * Initialize the object reading the data from the input stream.
     *
     * @param in The stream to be read
     * @throws IOException On any I/O error
     */
    protected void fromInputStream(final InputStream in) throws IOException
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
     * Validates the packet CRC.
     *
     * @throws BadCRCException If the CRC can't be validated
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
            {
                throw new BadCRCException("Bad CRC");
            }
        }
        catch (IOException e)
        {
            // Should never happen...
        }
    }

    /**
     * Converts the packet object to its byte array representation.
     *
     * @return The byte array representation of this packet.
     */
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

    /**
     * Returns the string message.
     *
     * @return The string message
     */
    public String getStringMessage()
    {
        int iZeroIndex = MAX_PACKETBUFFER_LENGTH - 1;

        // find the first 0 byte
        for (int i = 0; i < MAX_PACKETBUFFER_LENGTH; i++)
        {
            if (m_vBuffer[i] == 0)
            {
                iZeroIndex = i;
                break;
            }
        }

        return new String(m_vBuffer, 0, iZeroIndex);
    }

    /**
     * Sets the packet message. If the message is longer than.
     * {@link IJNRPEConstants#MAX_PACKETBUFFER_LENGTH} than it gets truncated to
     * {@link IJNRPEConstants#MAX_PACKETBUFFER_LENGTH} bytes.
     *
     * @param sMessage
     *            The message
     */
    protected void _setMessage(final String sMessage)
    {
        if (sMessage == null)
        {
            m_vBuffer[0] = 0;
            return;
        }
        System.arraycopy(sMessage.getBytes(), 0, m_vBuffer, 0, Math.min(
                sMessage.length(), MAX_PACKETBUFFER_LENGTH));

        if (sMessage.length() < MAX_PACKETBUFFER_LENGTH)
        {
            m_vBuffer[sMessage.length()] = 0;
        }
    }

    /**
     * Initializes the arrays with random data. Not sure it is really needed...
     */
    protected void initRandomBuffer()
    {
        Random r = new Random(System.currentTimeMillis());

        r.nextBytes(m_vBuffer);
        r.nextBytes(m_vDummy);
    }
    
    /**
     * Write the command name inside the JNRPE packet.
     * 
     * @param sCommand The command name
     */
    protected void setDataBuffer(final String sCommand)
    {
    	if (sCommand == null)
    	{
    		throw new IllegalArgumentException("Buffer can't be null");
    	}
    	
    	m_vBuffer = Arrays.copyOf(sCommand.getBytes(), MAX_PACKETBUFFER_LENGTH);
    }
}
