package org.jboss.ide.eclipse.as.core.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Server;
import org.jboss.tools.jmx.core.IMemento;
import org.jboss.tools.jmx.core.util.XMLMemento;

public class DeploymentPreferenceLoader {
	public static final String DEPLOYMENT_PREFERENCES_KEY = "org.jboss.ide.eclipse.as.core.util.deploymentPreferenceKey"; //$NON-NLS-1$
	
	public static DeploymentPreferences loadPreferencesFromFile(IServer server) {
		File f = getFile(server);
		InputStream is = null;
		try {
			if( f.exists())
				is = new FileInputStream(f);
		} catch( IOException ioe) {}
		return new DeploymentPreferences(is);
	}
	
	public static DeploymentPreferences loadPreferencesFromServer(IServer server) {
		String xml = ((Server)server).getAttribute(DEPLOYMENT_PREFERENCES_KEY, (String)null);
		ByteArrayInputStream bis = null;
		if( xml != null ) {
			bis = new ByteArrayInputStream(xml.getBytes());
		}
		return new DeploymentPreferences(bis);
	}

	public static void savePreferences(IServer server, DeploymentPreferences prefs) throws IOException {
		File f = getFile(server);
		prefs.getMemento().saveToFile(f.getAbsolutePath());
	}

	public static void savePreferences(OutputStream os, DeploymentPreferences prefs) {
		try {
			prefs.getMemento().save(os);
		} catch(IOException ioe) {}
	}
	
	protected static File getFile(IServer server) {
		IPath loc = ServerUtil.getServerStateLocation(server);
		return loc.append("deploymentPreferences.xml").toFile(); //$NON-NLS-1$
	}
	
	public static class DeploymentPreferences {
		private HashMap<String, DeploymentTypePrefs> children;
		private XMLMemento memento;
		public DeploymentPreferences(InputStream is) {
			children = new HashMap<String, DeploymentTypePrefs>();
			if( is != null) {
					memento = XMLMemento.createReadRoot(is);
					String[] deploymentTypes = memento.getChildNames();
					for( int i = 0; i < deploymentTypes.length; i++ )
						children.put(deploymentTypes[i], 
								new DeploymentTypePrefs(deploymentTypes[i], 
										memento.getChild(deploymentTypes[i])));
			} else {
				memento = XMLMemento.createWriteRoot("deployment"); //$NON-NLS-1$
			}
		}

		public DeploymentTypePrefs getPreferences(String deploymentType) {
			return children.get(deploymentType);
		}
		
		public DeploymentTypePrefs getOrCreatePreferences(String deploymentType) {
			if( children.get(deploymentType) == null ) {
				children.put(deploymentType, 
						new DeploymentTypePrefs(deploymentType, 
								memento.createChild(deploymentType)));
			}
			return children.get(deploymentType);
		}

		public String[] getDeploymentTypes() {
			Set<String> s = children.keySet();
			return (String[]) s.toArray(new String[s.size()]);
		}
		
		protected XMLMemento getMemento() {
			return memento;
		}
	}
	
	public static class DeploymentTypePrefs {
		private String type;
		private HashMap<String, DeploymentModulePrefs> children;
		private IMemento memento;
		public DeploymentTypePrefs(String type, IMemento memento) {
			this.type = type;
			this.memento = memento;
			this.children = new HashMap<String, DeploymentModulePrefs>();
			IMemento[] mementos = memento.getChildren("module"); //$NON-NLS-1$
			for( int i = 0; i < mementos.length; i++ ) {
				String id = mementos[i].getString("id"); //$NON-NLS-1$
				this.children.put(id, new DeploymentModulePrefs(id, mementos[i]));
			}
			// TODO properties? 
		}
		
		public DeploymentModulePrefs getModulePrefs(IModule module) {
			return children.get(module.getId());
		}
		public DeploymentModulePrefs getOrCreateModulePrefs(IModule module) {
			if( children.get(module.getId()) == null ) {
				IMemento childMemento = memento.createChild("module"); //$NON-NLS-1$
				childMemento.putString("id", module.getId()); //$NON-NLS-1$
				children.put(module.getId(), 
						new DeploymentModulePrefs(module.getId(), 
								childMemento));
			}
			return children.get(module.getId());
		}
	}
	public static class DeploymentModulePrefs {
		private String id;
		private IMemento memento;
		private HashMap<String, String> properties;
		public DeploymentModulePrefs(String id, IMemento memento) {	
			this.id = id;
			this.memento = memento;
			properties = new HashMap<String, String>();
			IMemento[] children = memento.getChildren("property"); //$NON-NLS-1$
			String key, val;
			for( int i = 0; i < children.length; i++ ) {
				key = children[i].getString("key"); //$NON-NLS-1$
				val = children[i].getString("value"); //$NON-NLS-1$
				properties.put(key,val);
			}
		}
		
		public String getProperty(String key) {
			return properties.get(key);
		}
		
		public void setProperty(String key, String val) {
			properties.put(key, val);
			IMemento[] children = memento.getChildren("property"); //$NON-NLS-1$
			for( int i = 0; i < children.length; i++ ) {
				if( key.equals(children[i].getString("key"))) { //$NON-NLS-1$
					children[i].putString("key", key); //$NON-NLS-1$
					children[i].putString("value", val);//$NON-NLS-1$
					return;
				}
			}
			// not found
			IMemento child = memento.createChild("property"); //$NON-NLS-1$
			child.putString("key", key);//$NON-NLS-1$
			child.putString("value", val);//$NON-NLS-1$
		}
	}
}
