/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.core.model;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.util.ASDebug;

/**
 * This class is intended to represent the actual mbeans, 
 * configuration descriptors, jars, etc enabled, disabled, or 
 * available in a server's on-disk configuration.
 * 
 * By configuration, here, I mean the directory under jboss' 
 * install directory which contains deployed jars and -service.xml files,
 * mbeans, etc. 
 * 
 * This is not yet implemented. All code here is forward-looking.
 * 
 * @author rstryker
 *
 */

public class DescriptorModel {
	private static DescriptorModel model = null;
	
	public static DescriptorModel getDefault() {
		if( model == null ) {
			model = new DescriptorModel();
		}
		return model;
	}
	
	private HashMap map;
	
	public DescriptorModel() {
		map = new HashMap();
	}
	
	public ServerDescriptorModel getServerModel(IServer server) {
		String key = server.getId();
		Object o = map.get(key);
		if( o == null ) {
			o = createEntity(server);
		}
		return (ServerDescriptorModel)o;
	}
	
	private ServerDescriptorModel createEntity(IServer server) {
		if( server.getServerType().getId().startsWith("org.jboss.ide.eclipse.as.")) {
			String id = server.getId();
			ServerDescriptorModel val = new ServerDescriptorModel(id);
			map.put(id, val);
			return val;
		}
		return null;
	}
	
	
	/*
	 * MUST BE FIXED, or rather, actually implemented
	 */
	public class ServerDescriptorModel {
		private String serverId;
		private long lastChecked;
		private String configPath;
		
		public ServerDescriptorModel(String id) {
			this.serverId = id;
			ServerAttributeHelper helper = getJBossServer().getAttributeHelper();
			configPath = helper.getConfigurationPath();
			
		}
		
		public int getJNDIPort() {
			try {
				String jbossServicePath = configPath + File.separator + "conf" + 
						File.separator + "jboss-service.xml";
				URL jbossServiceURL = new File(jbossServicePath).toURL();
				SAXReader reader = new SAXReader();
				Document document = reader.read(jbossServiceURL);
				List l = document.selectNodes("/server/mbean[@name='jboss:service=Naming']/attribute[@name='Port']");
				if( l.size() == 1 ) {
					DefaultElement el = (DefaultElement)l.get(0);
					int jndi = Integer.parseInt(el.getText());
					return jndi;
				}
			} catch( Exception e ) {
				
			}
			return 1099;
		}
		
		public JBossServer getJBossServer() {
			return JBossServerCore.getServer(ServerCore.findServer(serverId));
		}
		
		private File[] getAllDescriptors() {
			ArrayList list = new ArrayList();
			File config = new File(configPath);
			getAllDescriptorsRecurse(config, list);
			File[] ret = new File[list.size()];
			list.toArray(ret);
			return ret;
		}
		
		private void getAllDescriptorsRecurse(File parent, ArrayList collector) {
			if( parent.isDirectory() ) {
				File[] children = parent.listFiles();
				for( int i = 0; i < children.length; i++ ) {
					if( children[i].isDirectory()) {
						getAllDescriptorsRecurse(children[i], collector);
					} else if( children[i].getAbsolutePath().endsWith(".xml")) {
						collector.add(children[i]);
					}
				}
			}
		}
	}
	
	
}
