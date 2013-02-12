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
package it.jnrpe.utils;

public class JNRPEClientException extends Exception {

	public JNRPEClientException() {
		super();
	}

	public JNRPEClientException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JNRPEClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public JNRPEClientException(String message) {
		super(message);
	}

	public JNRPEClientException(Throwable cause) {
		super(cause);
	}

}
