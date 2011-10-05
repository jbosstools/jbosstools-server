/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethodType;

public class ServerPublishMethodType implements IJBossServerPublishMethodType {

	private String typeId, name;
	private String[] serverTypes;
	private IConfigurationElement element;
	private String publisherClassAttribute;
	public ServerPublishMethodType(String id, String name, String serverTypes2, 
			IConfigurationElement element, String publisherAttribute) {
		this.typeId = id;
		this.name = name;
		String tmp = element.getAttribute("serverTypes"); //$NON-NLS-1$
		serverTypes = serverTypes2.split(","); //$NON-NLS-1$
		for( int i = 0; i < serverTypes.length; i++ ) 
			serverTypes[i] = serverTypes[i].trim();
		this.element = element;
		this.publisherClassAttribute = publisherAttribute;
	}

	public ServerPublishMethodType(IConfigurationElement element) {
		this.element = element;
		IContributor c = element.getContributor();
		this.typeId = element.getAttribute("id"); //$NON-NLS-1$
		this.name = element.getAttribute("name"); //$NON-NLS-1$
		String tmp = element.getAttribute("serverTypes"); //$NON-NLS-1$
		serverTypes = tmp.split(","); //$NON-NLS-1$
		// clean
		for( int i = 0; i < serverTypes.length; i++ ) 
			serverTypes[i] = serverTypes[i].trim();
		publisherClassAttribute = "class"; //$NON-NLS-1$
	}
	
	public String getId() {
		return typeId;
	}

	public String getName() {
		return this.name;
	}
	
	public boolean accepts(String serverTypeId) {
		return Arrays.asList(serverTypes).contains(serverTypeId);
	}

	public IJBossServerPublishMethod createPublishMethod() {
		try {
			return (IJBossServerPublishMethod) element.createExecutableExtension(publisherClassAttribute);
		} catch( CoreException ce ) {
		}
		return null;
	}
}
