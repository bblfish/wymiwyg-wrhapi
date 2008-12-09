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


import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.service.component.ComponentContext;
import org.wymiwyg.wrhapi.Handler;
import org.wymiwyg.wrhapi.ServerBinding;
import org.wymiwyg.wrhapi.WebServer;
import org.wymiwyg.wrhapi.WebServerFactory;
import org.osgi.service.cm.ConfigurationAdmin;
import org.wymiwyg.wrhapi.util.pathmapttings.PathMappingHandler;

/**
 * This is intended to run in an an OSGi-DS environment.
 * This activates wrhapi if there is a {@link WebServerFactory} and a
 * {@link Handler} available.
 * 
 * @author reto
 * @scr.component immediate="true" name="org.wymiwyg.wrhapi.activator.Activator"
 * @service.description start the web server
 * @scr.reference name="handler" interface="org.wymiwyg.wrhapi.Handler"
 *				  cardinality="1..n" 
 * @scr.property name="port" value="8282"
 * @scr.property name="mappings"
 *               values.name=""
 * 
 */
public class Activator {

	final private Logger log = LoggerFactory.getLogger(Activator.class);
	private WebServer webServer;
	/**
	 * @scr.reference
	 */
	private ConfigurationAdmin configurationAdmin;
	private Set<ServiceReference> handlerRefs = new HashSet<ServiceReference>();
	private Map<String, Handler> nameServiceMap = new HashMap<String, Handler>();
	/**
	 * @scr.reference
	 */
	private WebServerFactory webServerFactory;
	//port (configurable, but set default 8282)
	private int port = 8282;

	public Activator() {
		log.info("constructing activator");
	}

	public void setPort(int port) throws Exception {
		this.port = port;
	}

	protected void bindHandler(ServiceReference handlerRef) {
		log.info("binding: " + handlerRef);
		handlerRefs.add(handlerRef);
		final BundleContext bundleContext = handlerRef.getBundle().getBundleContext();
		String name = (String) handlerRef.getProperty("service.pid");
		Handler handler = (Handler) bundleContext.getService(handlerRef);
		if (handler == null) {
			log.error("Could not get service for "+name);
		} else {
			nameServiceMap.put(name, handler);
		}
	/*try {
	final BundleContext bundleContext = handlerRef.getBundle().getBundleContext();
	ServiceReference adminRef = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
	ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) bundleContext.getService(adminRef);
	
	Configuration config = configurationAdmin.getConfiguration(Activator.class.getName());
	Dictionary properties = config.getProperties();
	String[] mappings;
	if (properties == null) {
	properties = new Hashtable();
	mappings = new String[0];
	} else {
	mappings = (String[]) properties.get("mappings");
	}
	String[] newValues = new String[mappings.length + 1];
	for (int i = 0; i < mappings.length; i++) {
	newValues[i] = mappings[i];
	}
	newValues[mappings.length] = "activated at " + new Date();
	properties.put("mappings", newValues);
	config.update(properties);
	} catch (IOException ex) {
	java.util.logging.Logger.getLogger(Activator.class.getName()).log(Level.SEVERE, null, ex);
	}*/

	}

	protected void unbindHandler(ServiceReference handlerRef) {
		handlerRefs.remove(handlerRef);
	}

	protected void activate(ComponentContext context) throws Exception {


		log.info("Activating WRHAPI (" + configurationAdmin + ")");
		try {
			String portStr =
					(String) context.getProperties().get("port");
			port = Integer.parseInt(portStr);
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		Map<String, String> mappingMap = getMappings((String[]) context.getProperties().get("mappings"));
		PathMappingHandler mappingHandler = new PathMappingHandler();
		for (Map.Entry<String, Handler> entry : nameServiceMap.entrySet()) {
			String pid = entry.getKey();
			mappingHandler.addHandler(entry.getValue(),
					mappingMap.get(pid), pid, true);
		}
		try {
			log.info("Starting webserver at port " + port);
			webServer = webServerFactory.startNewWebServer(mappingHandler,
					new ServerBinding() {

						@Override
						public InetAddress getInetAddress() {
							return null;
						}

						@Override
						public int getPort() {
							return port;
						}
					});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}


	}

	public void deactivate(ComponentContext context) throws Exception {
		webServer.stop();
		webServer = null;
	}

	/**
	 *
	 * @param string
	 * @return a mapping from servicename to path
	 */
	private Map<String, String> getMappings(String[] mappings) throws IOException {
		Map<String, String> result = new HashMap<String, String>();
		Set<String> knownServiceNames = new HashSet<String>(nameServiceMap.keySet());
		for (String mapping : mappings) {
			String[] parts = mapping.split("=");
			if (parts.length != 2) {
				log.warn("invalid mapping " + mapping);
				continue;
			}
			result.put(parts[0], parts[1]);
			knownServiceNames.remove(parts[0]);
		}
		if (knownServiceNames.size() > 0) {
			Configuration config = configurationAdmin.getConfiguration(
					Activator.class.getName());
			Dictionary properties = config.getProperties();
			if (properties == null) {
				properties = new Hashtable();
			}
			String[] newValues = new String[mappings.length + knownServiceNames.size()];
			int i;
			for (i = 0; i < mappings.length; i++) {
				newValues[i] = mappings[i];

			}
			for (String string : knownServiceNames) {
				newValues[i++] = string + "=/";
				result.put(string, "/");
			}
			properties.put("mappings", newValues);
			config.update(properties);
		}
		return result;
	}
}
