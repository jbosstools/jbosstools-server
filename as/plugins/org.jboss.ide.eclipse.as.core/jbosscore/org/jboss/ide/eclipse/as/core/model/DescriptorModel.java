package org.jboss.ide.eclipse.as.core.model;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.runtime.JBossRuntimeConfiguration;

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
		
		public ServerDescriptorModel(String id) {
			this.serverId = id;
		}
		
		public int getJNDIPort() {
			try {
				JBossRuntimeConfiguration rc = getJBossServer().getRuntimeConfiguration();
				String configPath = rc.getConfigurationPath();
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
		
	}
	
	
}
