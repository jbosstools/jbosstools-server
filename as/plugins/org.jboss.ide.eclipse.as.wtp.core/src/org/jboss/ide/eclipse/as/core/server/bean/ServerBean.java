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

package org.jboss.ide.eclipse.as.core.server.bean;


public class ServerBean {
	
	public static final String EMPTY = ""; //$NON-NLS-1$
	
	public ServerBean() {
		
	}
	
	public ServerBean(String location, String name, JBossServerType type,
			String version) {
		super();
		this.location = location;
		this.name = name;
		this.type = type;
		this.version = version;
	}
	
	public ServerBean(ServerBean bean) {
		this(bean.getLocation(),bean.getName(), bean.getType(),bean.getVersion());
	}

	private String location=EMPTY;
	private JBossServerType type = JBossServerType.UNKNOWN;
	private String name = EMPTY;
	private String version = EMPTY;
	
	public String getLocation() {
		return location;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	public JBossServerType getType() {
		return type;
	}
	
	public void setType(JBossServerType type) {
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
	
	public void setVersion(String version) {
		this.version = version;
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