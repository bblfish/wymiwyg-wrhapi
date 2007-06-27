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

import junit.framework.TestCase;

import org.wymiwyg.commons.mediatypes.MimeType;
import org.wymiwyg.wrhapi.util.MessageBody2Read;
import org.wymiwyg.wrhapi.util.parameterparser.FormFile;
import org.wymiwyg.wrhapi.util.parameterparser.KeyValuePair;
import org.wymiwyg.wrhapi.util.parameterparser.MultiPartBody;
import org.wymiwyg.wrhapi.util.parameterparser.MultiPartBodyImpl;
import org.wymiwyg.wrhapi.util.parameterparser.ParameterValue;
import org.wymiwyg.wrhapi.util.parameterparser.URLEncodedParameterCollection;

/**
 * @author reto
 * 
 */
public class MessageBodyUtilTest extends TestCase {

	/**
	 * test parseFormUrlencoded with an empty body
	 */
	public void testParseFormUrlencodedEmpty() {
		Iterator<KeyValuePair<ParameterValue>> paramIter = new URLEncodedParameterCollection(
				new MessageBody2Read() {

					public ReadableByteChannel read() throws IOException {
						return Channels.newChannel(new ByteArrayInputStream(""
								.getBytes("utf-8")));
					}

				}).iterator();
		assertFalse(paramIter.hasNext());
	}

	/**
	 * Tests MultiPartBodyImpl using the InputStream/MimeType constructor
	 * 
	 * @throws Exception
	 */
	public void testMultiPartBody() throws Exception {
		String contentType = "multipart/form-data; boundary=---------------------------21400068493715374902128087183";
		String fileContentString = "Hello World!\n"
			+ "\n";
		String bodyString = "-----------------------------21400068493715374902128087183\r\n"
				+ "Content-Disposition: form-data; name=\"file\"; filename=\"hello.txt\"\r\n"
				+ "Content-Type: text/plain\r\n"
				+ "\r\n"
				+ fileContentString
				+ "\r\n"
				+ "-----------------------------21400068493715374902128087183\r\n"
				+ "Content-Disposition: form-data; name=\"text\"\r\n"
				+ "\r\n"
				+ "\r\n"
				+ "-----------------------------21400068493715374902128087183\r\n"
				+ "Content-Disposition: form-data; name=\"location\"\r\n"
				+ "\r\n"
				+ "http://gvs.hpl.hp.com/documenation/images/persistent-store\r\n"
				+ "-----------------------------21400068493715374902128087183\r\n"
				+ "Content-Disposition: form-data; name=\"submit\"\r\n"
				+ "\r\n"
				+ "Upload\r\n"
				+ "-----------------------------21400068493715374902128087183--";
		MimeType mimeType = new MimeType(contentType);
		MultiPartBody multiPartBody = new MultiPartBodyImpl(
				new ByteArrayInputStream(bodyString.getBytes("utf-8")),
				mimeType);
		String submitParamValue = multiPartBody
				.getTextParameterValues("submit")[0];
		assertEquals("Upload", submitParamValue);
		String locationParamValue = multiPartBody.getParameteValues("location")[0].toString();
		assertEquals("http://gvs.hpl.hp.com/documenation/images/persistent-store", locationParamValue);
		FormFile fileParameterValue  = (FormFile) multiPartBody.getParameteValues("file")[0];
		String fileParameterContent = new String(fileParameterValue.getContent(), "utf-8");
		assertEquals(fileContentString, fileParameterContent);
	}

}
