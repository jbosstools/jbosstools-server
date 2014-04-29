/******************************************************************************* 
 * Copyright (c) 2010-2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 

package org.jboss.ide.eclipse.as.core.server.bean;

import java.io.File;


public class ServerBean {
	
	public static final String EMPTY = ""; //$NON-NLS-1$

	private String location=EMPTY;
	private ServerBeanType type = ServerBeanType.UNKNOWN;
	private String name = EMPTY;
	private String version = EMPTY;
	private String fullVersion = EMPTY;
	
	@Deprecated 
	public ServerBean() {
	}
	
	public ServerBean(String location, String name, ServerBeanType type,
			String fullVersion) {
		super();
		this.location = location;
		this.name = name;
		this.type = type;
		this.fullVersion = fullVersion;
		this.version = ServerBeanLoader.getMajorMinorVersion(fullVersion);
	}
	
	public ServerBean(ServerBean bean) {
		this(bean.getLocation(),bean.getName(), bean.getBeanType(),bean.getVersion());
	}

	public String getServerAdapterTypeId() {
		return getBeanType().getServerAdapterTypeId(version);
	}
	
	public String getLocation() {
		return location;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	/**
	 * This method has been deprecated as the 
	 * superclass set has been expanded. We prefer
	 * to return a higher class now. 
	 * @return
	 */
	@Deprecated
	public JBossServerType getType() {
		return type instanceof JBossServerType ? (JBossServerType)type : JBossServerType.UNKNOWN;
	}
	
	public ServerBeanType getBeanType() {
		return type;
	}

	public String getUnderlyingTypeId() {
		return getBeanType().getUnderlyingTypeId(new File(location));
	}
	
	public void setType(ServerBeanType type) {
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getVersion() {
		return version;
	}
	
	public String getFullVersion() {
		return fullVersion;
	}
	
	public String toString() {
		return name + "," + type + "," + version + "," + location; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if(this == obj) return true;
		return this.toString().equals(obj.toString());
	}
}