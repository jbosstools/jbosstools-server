/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jboss.tools.jmx.core.providers;

import java.io.Serializable;

/**
 * @author Mitko Kolev
 *
 */
public class MBeanServerConnectionDescriptor implements Serializable {

    private static final long serialVersionUID = -8358701879017195518L;

    private String id;
    private String url;
    private String userName;
	private String password;

    public MBeanServerConnectionDescriptor(
    		String id, String url,
            String userName, String password) {
        this.id = id;
        this.url = url;
        this.userName = userName;
        this.password = password;
    }

	public void setId(String id) {
		this.id = id;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

    public String getID() {
        return id;
    }

    public String getURL() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

}
