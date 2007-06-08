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
package org.wymiwyg.wrhapi.filter.impl.test;

import junit.framework.TestCase;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wymiwyg.commons.util.Util;

import org.wymiwyg.wrhapi.Handler;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.MessageBody;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.ResponseStatus;
import org.wymiwyg.wrhapi.ServerBinding;
import org.wymiwyg.wrhapi.WebServer;
import org.wymiwyg.wrhapi.WebServerFactory;
import org.wymiwyg.wrhapi.filter.Filter;
import org.wymiwyg.wrhapi.filter.impl.FilterRunner;
import org.wymiwyg.wrhapi.util.MessageBody2Read;
import org.wymiwyg.wrhapi.util.MessageBody2Write;
import org.wymiwyg.wrhapi.util.ResponseWrapper;

import sun.misc.Regexp;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * @author reto
 * 
 */
public class FilterTests extends TestCase {
	/**
	 * @author reto
	 *
	 */
	public class SpellCheckedResponse extends ResponseWrapper {
		
		final Map<String, String> corrections = new HashMap<String, String>();
		{
			corrections.put("Hullo", "Hello");
		}

		/**
		 * @param response
		 */
		public SpellCheckedResponse(Response response) {
			super(response);
		}

		/* (non-Javadoc)
		 * @see org.wymiwyg.wrhapi.Response#setBody(org.wymiwyg.wrhapi.MessageBody)
		 */
		public void setBody(final MessageBody body) throws HandlerException {
			super.setBody(new MessageBody2Write() {

				public void writeTo(WritableByteChannel out) throws IOException {
					Reader reader = Channels.newReader(body.read(), "utf-8");
					Writer writer = Channels.newWriter(out, "utf-8");
					StringWriter wordWriter = null;
					for (int ch = reader.read(); ch != -1; ch = reader.read()) {
						if (Character.isLetterOrDigit(ch)) {
							if (wordWriter == null) {
								wordWriter = new StringWriter();
							}
							wordWriter.write(ch);
						} else {
							if (wordWriter != null) {
								String word = wordWriter.toString();
								if (corrections.containsKey(word)) {
									writer.write(corrections.get(word));
								} else {
									writer.write(word);
								}
								wordWriter = null;
							}
							writer.write(ch);
							writer.flush();
						}
					}
					if (wordWriter != null) {
						String word = wordWriter.toString();
						if (corrections.containsKey(word)) {
							writer.write(corrections.get(word));
						} else {
							writer.write(word);
						}
					}
					writer.flush();
				}
				
			});

		}


	}

	private static final Log log = LogFactory.getLog(FilterTests.class);

	ServerBinding serverBinding = new ServerBinding() {
		public InetAddress getInetAddress() {
			try {
				return InetAddress.getByName("localhost");
			} catch (UnknownHostException e) {
				throw new RuntimeException(e);
			}
		}

		public int getPort() {
			return 8686;
		}
	};

	protected WebServerFactory createServer() {
		return WebServerFactory.newInstance();
	}

	/**
	 * this tests with a cookie-setting and a spell-checking filter.
	 */
	public void testMultipleFilters() throws Exception {
		final String body = "Hullo World";
		Filter[] filters = new Filter[2];
		Handler terminator = new Handler() {
			public void handle(Request request, Response response)
					throws HandlerException {
				response.setBody(new MessageBody2Write() {
					public void writeTo(WritableByteChannel out)
							throws IOException {
						out.write(ByteBuffer.wrap(body.getBytes("utf-8")));
					}
				});

			}
		};
		filters[0] = new Filter() {

			public void handle(Request request, Response response, Handler rest) throws HandlerException {
				response.setHeader(HeaderName.COOKIE, "foo=bar");
				rest.handle(request, response);
			}
			
		};
		
		filters[1] = new Filter() {

			public void handle(Request request, Response response, Handler rest) throws HandlerException {
				rest.handle(request, new SpellCheckedResponse(response));
			}
			
		};
		
		WebServer webServer = createServer().startNewWebServer(
				new FilterRunner(filters, terminator), serverBinding);

		try {
			URL serverURL = new URL("http://"
					+ serverBinding.getInetAddress().getHostAddress() + ":"
					+ serverBinding.getPort() + "/");
			Reader reader = new InputStreamReader(serverURL.openStream());
			StringWriter stringWriter = new StringWriter();

			for (int ch = reader.read(); ch != -1; ch = reader.read()) {
				stringWriter.write(ch);
			}

			assertEquals("Hello World", stringWriter.toString());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			webServer.stop();
		}
	}
}
