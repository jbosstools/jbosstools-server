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

import org.jboss.tools.foundation.core.xml.IMemento;

/**
 * Replacement class for class inner class DeploymentModulePrefs 
 * inside org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader
 */
public class DeploymentModulePrefs {
	private static final String PROPERTY = "property"; //$NON-NLS-1$
	private static final String KEY = "key"; //$NON-NLS-1$
	private static final String VALUE = "value"; //$NON-NLS-1$

	
	
	private IMemento memento;
	private HashMap<String, String> properties;
	public DeploymentModulePrefs(IMemento memento) {	
		this.memento = memento;
		properties = new HashMap<String, String>();
		IMemento[] children = memento.getChildren(PROPERTY);
		String key, val;
		for( int i = 0; i < children.length; i++ ) {
			key = children[i].getString(KEY);
			val = children[i].getString(VALUE); 
			properties.put(key,val);
		}
	}
	
	public String getProperty(String key) {
		return properties.get(key);
	}
	
	public void setProperty(String key, String val) {
		properties.put(key, val);
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