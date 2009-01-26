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


import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
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
import org.wymiwyg.wrhapi.filter.Filter;
import org.wymiwyg.wrhapi.filter.impl.FilterRunner;
import org.wymiwyg.wrhapi.util.pathmapttings.PathMappingHandler;

/**
 * This is intended to run in an an OSGi-DS environment.
 * This activates wrhapi if there is a {@link WebServerFactory} and at least one
 * {@link Handler} available.
 * 
 * @author reto
 * @scr.component immediate="true" name="org.wymiwyg.wrhapi.activator.Activator"
 * @service.description start the web server
 * @scr.reference name="handler" interface="org.wymiwyg.wrhapi.Handler"
 *				  cardinality="1..n" target="(!(org.wymiwyg.wrhapi.nobind=true))"
 * @scr.reference name="filter" interface="org.wymiwyg.wrhapi.filter.Filter"
 *				  cardinality="0..n"
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
	private Map<String, Handler> nameServiceMap = new HashMap<String, Handler>();
	private List<Filter> filters = new ArrayList<Filter>();
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
		//String noBind = (String) handlerRef.getProperty(
		final BundleContext bundleContext = handlerRef.getBundle().
				getBundleContext();
		String name = (String) handlerRef.getProperty("service.pid");
		Handler handler = (Handler) bundleContext.getService(handlerRef);
		if (handler == null) {
			log.error("Could not get service for " + name);
		} else {
			nameServiceMap.put(name, handler);
		}

	}

	protected void unbindHandler(ServiceReference handlerRef) {
		String name = (String) handlerRef.getProperty("service.pid");
		nameServiceMap.remove(name);
	}

	protected void bindFilter(Filter filter) {
		filters.add(filter);
	}

	protected void unbindFilter(Filter filter) {
		filters.remove(filter);
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
		Map<String, ParameterizedPath> mappingMap = getMappings((String[]) context.getProperties().
				get("mappings"));
		PathMappingHandler mappingHandler = new PathMappingHandler();
		for (Map.Entry<String, Handler> entry : nameServiceMap.entrySet()) {
			String pid = entry.getKey();
			ParameterizedPath parameterizedPath = mappingMap.get(pid);
			mappingHandler.addHandler(entry.getValue(),
					parameterizedPath.getPath(), pid, parameterizedPath.
					removePrefix(), parameterizedPath.isOverlay());
		}
		Handler handler = mappingHandler;
		if (filters.size() > 0) {
			log.info("Activating WRHAPI with "+filters.size()+" filters");
			handler = new FilterRunner(
					filters.toArray(new Filter[filters.size()]), handler);
		}
		try {
			log.info("Starting webserver at port " + port);
			webServer = webServerFactory.startNewWebServer(handler,
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
	 * @return a mapping from each registeres servicename to  a ParameterizedPath
	 */
	private Map<String, ParameterizedPath> getMappings(String[] mappings) throws IOException {
		Map<String, ParameterizedPath> result =
				new HashMap<String, ParameterizedPath>();
		Set<String> knownServiceNames = new HashSet<String>(nameServiceMap.
				keySet());
		for (String mapping : mappings) {
			int firstEqualsPos = mapping.indexOf('=');
			if (firstEqualsPos == -1) {
				log.warn("invalid mapping " + mapping);
				continue;
			}
			String key = mapping.substring(0, firstEqualsPos);
			String value = mapping.substring(firstEqualsPos+1);
			result.put(key, new ParameterizedPath(value));
			knownServiceNames.remove(key);
		}
		if (knownServiceNames.size() > 0) {
			Configuration config = configurationAdmin.getConfiguration(
					Activator.class.getName());
			Dictionary properties = config.getProperties();
			if (properties == null) {
				properties = new Hashtable();
			}
			String[] newValues = new String[mappings.length + knownServiceNames.
					size()];
			int i;
			for (i = 0; i < mappings.length; i++) {
				newValues[i] = mappings[i];

			}
			for (String string : knownServiceNames) {
				newValues[i++] = string + "=/";
				result.put(string, new ParameterizedPath("/"));
			}
			properties.put("mappings", newValues);
			config.update(properties);
		}
		return result;
	}

	/**
	 * represents an optionally  parameterized path, paramters are separated
	 * from the path and from each other with a semicolon (';') and expressed
	 * as key-value pairs in the form key=value.
	 */
	private static class ParameterizedPath {

		String path;
		boolean removePrefix = true;
		boolean overlay = false;

		ParameterizedPath(String string) {
			String[] parts = string.split(";");
			path = parts[0];
			for (int i = 1; i < parts.length; i++) {
				String currentPart = parts[i];
				String[] keyValue = currentPart.split("=");
				if (keyValue[0].equals("removePrefix")) {
					removePrefix = parseBoolean(keyValue[1]);
				} else {
					if (keyValue[0].equals("overlay")) {
						overlay = parseBoolean(keyValue[1]);
					} else {
						throw new RuntimeException(
								"unrecognised path parameter: " + keyValue[0]);
					}
				}
			}
		}

		String getPath() {
			return path;
		}

		boolean isOverlay() {
			return overlay;
		}

		boolean removePrefix() {
			return removePrefix;
		}
		
		private boolean parseBoolean(String string) {
			if (string.equals("true")) {
				return true;
			}
			if (string.equals("false")) {
				return false;
			}
			throw new RuntimeException("boolean paramter value must be \"true\" or \"false\" (case sensitive)");
		}

		
	}
}
