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

/**
 * In this interface are initialized all needed constants
 * 
 * @author Massimiliano Ziccardi
 */
public interface IJNRPEConstants
{
	public final String VERSION = "0.1/2.0";
	
	public final int QUERY_PACKET = 1;       // id code for a packet containing a query 
	public final int RESPONSE_PACKET = 2;       // id code for a packet containing a response 

	public final int NRPE_PACKET_VERSION_2 = 2;       // packet version identifier 
	public final int NRPE_PACKET_VERSION_1 = 1;    // older packet version identifiers (no longer supported)

	public final int MAX_PACKETBUFFER_LENGTH = 1024; // max amount of data we'll send in one query/response

	
	public final int STATE_UNKNOWN = 3;       /* service state return codes */
	public final int STATE_CRITICAL = 2;
	public final int STATE_WARNING = 1;
	public final int STATE_OK = 0;
	
	
}
