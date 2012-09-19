/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.util;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.Server;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;
import org.jboss.ide.eclipse.as.wtp.core.Messages;

/**
 * This class just opens up the get and set attributes for a server,
 * and provides saving mechanisms.
 * @author rstryker@redhat.com
 *
 */
public class ServerAttributeHelper {

	public static ServerAttributeHelper createHelper(IServer iserver) {
		if( iserver instanceof Server ) {
			IServerWorkingCopy copy = ((Server)iserver).createWorkingCopy();
			if( copy != null ) {
				return new ServerAttributeHelper(iserver, copy);
			}
		}
		return null;
	}
	
	private IServerWorkingCopy wch;
	private IServer server;
	public ServerAttributeHelper(IServer server, IServerWorkingCopy copy) {
		this.wch = copy;
		this.server = server;
	}

	
	public IServer getServer() {
		return server;
	}
	
	public void setAttribute(String attributeName, int value) {
		wch.setAttribute(attributeName, value);
	}

	public void setAttribute(String attributeName, boolean value) {
		wch.setAttribute(attributeName, value);
	}

	public void setAttribute(String attributeName, String value) {
		wch.setAttribute(attributeName, value);
	}

	public void setAttribute(String attributeName, List value) {
		wch.setAttribute(attributeName, value);
	}

	public void setAttribute(String attributeName, Map value) {
		wch.setAttribute(attributeName, value);
	}
	
	public String getAttribute(String attributeName, String defaultValue) {
		return wch.getAttribute(attributeName, defaultValue);
	}

	public int getAttribute(String attributeName, int defaultValue) {
		return wch.getAttribute(attributeName, defaultValue);
	}

	public boolean getAttribute(String attributeName, boolean defaultValue) {
		return wch.getAttribute(attributeName, defaultValue);
	}
	
	public List getAttribute(String attributeName, List defaultValue) {
		return wch.getAttribute(attributeName, defaultValue);
	}
	
	public Map getAttribute(String attributeName, Map defaultValue) {
		return wch.getAttribute(attributeName, defaultValue);
	}

	public IServerWorkingCopy getWorkingCopy() {
		return this.wch;
	}
	public boolean isDirty() {
		return wch.isDirty();
	}

	public IServer save(boolean force, IProgressMonitor monitor) throws CoreException {
		return wch.save(force, monitor);
	}
	
	public IServer save() {
		try {
			return save(false, new NullProgressMonitor());
		} catch( CoreException e ) {
			ASWTPToolsPlugin.log(IStatus.ERROR, Messages.ServerSaveFailed, e);
		}
		return null;
	}	
}
