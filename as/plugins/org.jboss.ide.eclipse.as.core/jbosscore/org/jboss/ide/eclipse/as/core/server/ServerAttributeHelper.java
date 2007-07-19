/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.core.server;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;

/**
 * This class just opens up the get and set attributes for a server,
 * and provides saving mechanisms.
 * @author rstryker@redhat.com
 *
 */
public class ServerAttributeHelper {

	private ServerWorkingCopy wch;
	private IServer server;
	public ServerAttributeHelper(IServer server, IServerWorkingCopy copy) {
		this.wch = (ServerWorkingCopy)copy;
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

	public boolean isDirty() {
		return wch.isDirty();
	}

	public IServer save(boolean force, IProgressMonitor monitor) throws CoreException {
		return wch.save(force, monitor);
	}
	
	public IServer save() {
		try {
			return save(false, new NullProgressMonitor());
		} catch( Exception e ) {}
		return null;
	}	
}
