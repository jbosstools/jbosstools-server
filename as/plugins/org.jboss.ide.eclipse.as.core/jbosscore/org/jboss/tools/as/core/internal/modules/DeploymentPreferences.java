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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Set;

import org.jboss.tools.foundation.core.xml.IMemento;
import org.jboss.tools.foundation.core.xml.XMLMemento;

/**
 * Replacement class for class inner class DeploymentPreferences 
 * inside org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader
 * 
 * This class is internal
 */
public class DeploymentPreferences {
	// prefs are all in "local" now, even for rse stuff. 
	private static final String LOCAL_PUBLISH_METHOD = "local";  //$NON-NLS-1$
	private static final String DEPLOYMENT = "deployment"; //$NON-NLS-1$

	private static final String PROPERTY = "property"; //$NON-NLS-1$
	private static final String KEY = "key"; //$NON-NLS-1$
	private static final String VALUE = "value"; //$NON-NLS-1$

	
	private HashMap<String, DeploymentTypePrefs> children;
	private XMLMemento memento;
	public DeploymentPreferences(InputStream is) {
		children = new HashMap<String, DeploymentTypePrefs>();
		if( is != null) {
				memento = XMLMemento.createReadRoot(is);
				String[] deploymentTypes = memento.getChildNames();
				for( int i = 0; i < deploymentTypes.length; i++ )
					children.put(deploymentTypes[i], 
							new DeploymentTypePrefs( 
									memento.getChild(deploymentTypes[i])));
		} else {
			memento = XMLMemento.createWriteRoot(DEPLOYMENT);
		}
	}
	
	public DeploymentTypePrefs getOrCreatePreferences() {
		return getOrCreatePreferences(LOCAL_PUBLISH_METHOD);
	}
	
	private DeploymentTypePrefs getOrCreatePreferences(String deploymentType) {
		if( children.get(deploymentType) == null ) {
			children.put(deploymentType, 
					new DeploymentTypePrefs( 
							memento.createChild(deploymentType)));
		}
		return children.get(deploymentType);
	}
	
	protected XMLMemento getMemento() {
		return memento;
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