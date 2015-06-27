/******************************************************************************* 
 * Copyright (c) 2015 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.management.core.service;

import java.util.HashMap;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.management.core.IAS7ManagementDetails;

/**
 * This class represents a wrapper around an IAS7ManagementDetails
 * object for the purpose of setting or overriding properties
 * when using other services. 
 */
public class DelegatingDetails implements IAS7ManagementDetails {
	private IAS7ManagementDetails delegate;
	private HashMap<String, Object> propertiesOverride;
	public DelegatingDetails(IAS7ManagementDetails delegate) {
		this.delegate = delegate;
		propertiesOverride = new HashMap<String, Object>();
	}
	
	public void overrideProperty(String key, Object val) {
		propertiesOverride.put(key, val);
	}
	
	@Override
	public String getHost() {
		return delegate.getHost();
	}
	@Override
	public int getManagementPort() {
		return delegate.getManagementPort();
	}
	@Override
	public String getManagementUsername() {
		return delegate.getManagementUsername();
	}
	@Override
	public String getManagementPassword() {
		return delegate.getManagementPassword();
	}
	@Override
	public String[] handleCallbacks(String[] prompts) throws UnsupportedOperationException {
		return delegate.handleCallbacks(prompts);
	}
	@Override
	public IServer getServer() {
		return delegate.getServer();
	}
	@Override
	public Object getProperty(String key) {
		if( propertiesOverride.get(key) != null ) {
			return propertiesOverride.get(key);
		}
		return delegate.getProperty(key);
	}
}