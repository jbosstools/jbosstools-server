/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.Server;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethodType;
import org.jboss.ide.eclipse.as.core.server.internal.BehaviourModel;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;

public class DeploymentPreferenceLoader {
	public static final String DEPLOYMENT_PREFERENCES_KEY = "org.jboss.ide.eclipse.as.core.util.deploymentPreferenceKey"; //$NON-NLS-1$
	public static final String CURRENT_METHOD_PROPERTY = "publishMethod"; //$NON-NLS-1$	
	
	/**
	 * This method can return null and is only meant to show what deployment method
	 * is currently stored in a server's deployment preferences.
	 * 
	 * To get a more accurate version (with a default) please use
	 * {@link DeployableServerBehavior#createPublishMethod()#getPublishMethodType()}
	 * 
	 * @param server
	 * @return
	 */
	public static IJBossServerPublishMethodType getCurrentDeploymentMethodType(IServer server) {
		return getCurrentDeploymentMethodType(server, null);
	}

	public static IJBossServerPublishMethodType getCurrentDeploymentMethodType(IServer server, String defaultType) {
		return BehaviourModel.getPublishMethodType(server, defaultType);
	}

	public static String getCurrentDeploymentMethodTypeId(IServerAttributes server) {
		return getCurrentDeploymentMethodTypeId(server, null);
	}
	
	public static String getCurrentDeploymentMethodTypeId(IServerAttributes server, String defaultType) {
		return 	server.getAttribute(IDeployableServer.SERVER_MODE, defaultType);
	}

	public static DeploymentPreferences loadPreferencesFromServer(IServerAttributes server) {
		String xml = ((Server)server).getAttribute(DEPLOYMENT_PREFERENCES_KEY, (String)null);
		ByteArrayInputStream bis = null;
		if( xml != null ) {
			bis = new ByteArrayInputStream(xml.getBytes());
		}
		return new DeploymentPreferences(bis);
	}

	public static void savePreferences(OutputStream os, DeploymentPreferences prefs) throws IOException {
		prefs.getMemento().save(os);
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
		
		// prefs are all in "local" now, even for rse stuff. 
		public static final String LOCAL_PUBLISH_METHOD = "local";  //$NON-NLS-1$

		public DeploymentTypePrefs getOrCreatePreferences() {
			return getOrCreatePreferences(LOCAL_PUBLISH_METHOD);
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
		public String getProperty(String key) {
			IMemento[] children = memento.getChildren("property"); //$NON-NLS-1$
			for( int i = 0; i < children.length; i++ ) {
				if( key.equals(children[i].getString("key"))) { //$NON-NLS-1$
					return children[i].getString("value"); //$NON-NLS-1$
				}
			}
			return null;
		}
		
		public void setProperty(String key, String val) {
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
		}
		
		public DeploymentModulePrefs getModulePrefs(IModule module) {
			return getModulePrefs(module.getId());
		}
		public DeploymentModulePrefs getModulePrefs(String id) {
			return children.get(id);
		}
		public DeploymentModulePrefs getOrCreateModulePrefs(IModule module) {
			return getOrCreateModulePrefs(module.getId());
		}
		public DeploymentModulePrefs getOrCreateModulePrefs(String id) {
			if( children.get(id) == null ) {
				IMemento childMemento = memento.createChild("module"); //$NON-NLS-1$
				childMemento.putString("id", id); //$NON-NLS-1$
				children.put(id, 
						new DeploymentModulePrefs(id, 
								childMemento));
			}
			return children.get(id);
		}
		
		public String getProperty(String key) {
			IMemento[] children = memento.getChildren("property"); //$NON-NLS-1$
			for( int i = 0; i < children.length; i++ ) {
				if( key.equals(children[i].getString("key"))) { //$NON-NLS-1$
					return children[i].getString("value"); //$NON-NLS-1$
				}
			}
			return null;
		}
		
		public void setProperty(String key, String val) {
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
	
	public static void savePreferencesToServerWorkingCopy(ServerAttributeHelper helper, DeploymentPreferences prefs) {
		savePreferencesToServerWorkingCopy(helper.getWorkingCopy(), prefs);
	}
	
	public static void savePreferencesToServerWorkingCopy(IServerWorkingCopy wc, DeploymentPreferences prefs) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DeploymentPreferenceLoader.savePreferences(bos, prefs);
			String asXML = new String(bos.toByteArray());
			wc.setAttribute(DeploymentPreferenceLoader.DEPLOYMENT_PREFERENCES_KEY, asXML);
		} catch(IOException ioe) {
			// Should never happen since this is a simple byte array output stream
			JBossServerCorePlugin.log(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
				"Could not save module deployment preferences to server " + wc.getOriginal().getName(), ioe)); //$NON-NLS-1$
		}
	}

}
