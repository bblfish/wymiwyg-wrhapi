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
package org.wymiwyg.wrhapi.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;

import org.wymiwyg.wrhapi.util.MessageBody2Read;
import org.wymiwyg.wrhapi.util.bodyparser.KeyValuePair;
import org.wymiwyg.wrhapi.util.bodyparser.MessageBodyUtil;

import junit.framework.TestCase;

/**
 * @author reto
 *
 */
public class MessageBodyUtilTest extends TestCase {
	
	/**
	 *  test parseFormUrlencoded with an empty body
	 */
	public void testParseFormUrlencodedEmpty() {
		Iterator<KeyValuePair> paramIter = MessageBodyUtil.parseFormUrlencoded(new MessageBody2Read() {

			public ReadableByteChannel read() throws IOException {
				return Channels.newChannel(new ByteArrayInputStream("".getBytes("utf-8")));
			}
			
		});
		assertFalse(paramIter.hasNext());
	}

}
