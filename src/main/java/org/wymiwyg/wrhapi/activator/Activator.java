/*
 * Copyright 2008 WYMIWYG (http://wymiwyg.org)
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
package org.wymiwyg.wrhapi.activator;

import java.io.IOException;
import java.net.InetAddress;

import org.osgi.service.component.ComponentContext;
import org.wymiwyg.wrhapi.Handler;
import org.wymiwyg.wrhapi.ServerBinding;
import org.wymiwyg.wrhapi.WebServer;
import org.wymiwyg.wrhapi.WebServerFactory;

/**
 * This is intended to run in an an OSGi-DS environment.
 * This activates wrhapi if there is a {@link WebServerFactory} and a
 * {@link Handler} available.
 * 
 * @author reto
 * @scr.component immediate="true"
 * @service.vendor trialox
 * @service.description start the web server
 * @scr.property name="port" field="port" value="8282" method="setPort"
 * 
 */
public class Activator {

	private WebServer webServer;

	/**
	 * @scr.reference
	 */
	private WebServerFactory webServerFactory;
	//port (configurable, but set default 8282)

	private int port = 8282;

	public Activator() {
		System.out.println("constructing activator");
	}

	public void setPort(int port) throws Exception {
		this.port = port;
	}

	protected void activate(ComponentContext context) throws Exception {

		final ComponentContext ctx = context;

		System.out.println("Activating WRHAPI");
		if (webServer == null) {
			try {
				System.out.println("binding webserver factory");
				webServer = webServerFactory.startNewWebServer(
						new ServerBinding() {

							@Override
							public InetAddress getInetAddress() {
								return null;
							}

							@Override
							public int getPort() {
								int port = 8282;

								try {
									String portStr =
											(String) ctx.getProperties().get("port");
									port = Integer.parseInt(portStr);
								} catch (Exception e) {
									System.out.println(e.toString());
								}

								return port;
							}
						});
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

	public void deactivate(ComponentContext context) throws Exception {
		webServer.stop();

	}
}
