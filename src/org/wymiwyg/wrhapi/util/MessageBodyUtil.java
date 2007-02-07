/*
 * Copyright  2002-2006 WYMIWYG (http://wymiwyg.org)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wymiwyg.wrhapi.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.channels.Channels;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.wymiwyg.wrhapi.MessageBody;

/**
 * @author reto
 *
 */
public class MessageBodyUtil {
	/**
	 * @author reto
	 *
	 */
	public static class KeyValuePair {
		String key, value;

		private KeyValuePair(String key, String value) {
			super();
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}

	}

	/** decodes a form encoded following application/x-www-form-urlencoded as defined in 17.13.4 of HTML 4.01 
	 * 
	 * @param messageBody
	 * @return
	 */
	public static Iterator<KeyValuePair> parseFormUrlencoded(MessageBody messageBody) {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		try {
			messageBody.writeTo(Channels.newChannel(byteOut));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		String bodyString = new String(byteOut.toByteArray());
		final StringTokenizer tokens = new StringTokenizer(bodyString, "&");
		return new Iterator<KeyValuePair>() {

			public boolean hasNext() {
				return tokens.hasMoreTokens();
			}

			public KeyValuePair next() {
				String currentToken = tokens.nextToken();
				int indexOfEquals = currentToken.indexOf('=');
				String key = currentToken.substring(0, indexOfEquals);
				String value  = currentToken.substring(indexOfEquals+1);
				try {
					return new KeyValuePair(URLDecoder.decode(key, "utf-8"), URLDecoder.decode(value, "utf-8"));
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		};
		
	}

}
