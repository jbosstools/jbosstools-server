/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.as.core.internal.modules;

import java.util.HashMap;

import org.eclipse.wst.server.core.IModule;
import org.jboss.tools.foundation.core.xml.IMemento;

/**
 * Replacement class for class inner class DeploymentTypePrefs 
 * inside org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader
 * 
 * This class is internal
 */
public class DeploymentTypePrefs {
	private static final String MODULE = "module"; //$NON-NLS-1$
	private static final String ID = "id"; //$NON-NLS-1$
	private static final String PROPERTY = "property"; //$NON-NLS-1$
	private static final String KEY = "key"; //$NON-NLS-1$
	private static final String VALUE = "value"; //$NON-NLS-1$
	
	
	
	private HashMap<String, DeploymentModulePrefs> children;
	private IMemento memento;
	public DeploymentTypePrefs(IMemento memento) {
		this.memento = memento;
		this.children = new HashMap<String, DeploymentModulePrefs>();
		IMemento[] mementos = memento.getChildren(MODULE);
		for( int i = 0; i < mementos.length; i++ ) {
			String id = mementos[i].getString(ID);
			this.children.put(id, new DeploymentModulePrefs(mementos[i]));
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
			IMemento childMemento = memento.createChild(MODULE);
			childMemento.putString(ID, id);
			children.put(id, 
					new DeploymentModulePrefs(childMemento));
		}
		return children.get(id);
	}
	
	public String getProperty(String key) {
		IMemento[] children = memento.getChildren(PROPERTY); 
		for( int i = 0; i < children.length; i++ ) {
			if( key.equals(children[i].getString(KEY))) { 
				return children[i].getString(VALUE); 
			}
		}
		return null;
	}
	
	public void setProperty(String key, String val) {
		IMemento[] children = memento.getChildren(PROPERTY);
		for( int i = 0; i < children.length; i++ ) {
			if( key.equals(children[i].getString(KEY))) { 
				children[i].putString(KEY, key); 
				children[i].putString(VALUE, val);
				return;
			}
		}
		// not found
		IMemento child = memento.createChild(PROPERTY);
		child.putString(KEY, key);
		child.putString(VALUE, val);
	}
}