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
package org.jboss.ide.eclipse.as.core.util;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerPort;
import org.jboss.ide.eclipse.as.core.server.IServerWorkingCopyProvider;

/**
 * This class is to primarily replace or enhance the ability of 
 * {@link ServerAttributeHelper} which did not implement {@link IServerAttributes}
 * and made it difficult to liberalize the signatures of several methods. 
 * 
 * This class is nothing more than a wrapper of {@link IServerWorkingCopy},
 * which allows it to be extended for the case where an {@link IServerWorkingCopyProvider}
 * may change the working copy periodically and new ones must be used. 
 */
public class ServerWorkingCopyWrapper implements IServerWorkingCopy {
	private IServerWorkingCopyProvider provider;
	private IServerWorkingCopy wc;
	public ServerWorkingCopyWrapper(IServerWorkingCopyProvider provider) {
		this.provider = provider;
	}
	public ServerWorkingCopyWrapper(IServerWorkingCopy workingCopy) {
		this.wc = workingCopy;
	}
	
	public IServerWorkingCopy getWorkingCopy() {
		if( provider != null )
			return provider.getServer();
		return wc;
	}
	
	public void setName(String name) {
		getWorkingCopy().setName(name);
	}
	public void setReadOnly(boolean readOnly) {
		getWorkingCopy().setReadOnly(readOnly);
	}
	public boolean isDirty() {
		return getWorkingCopy().isDirty();
	}
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		getWorkingCopy().addPropertyChangeListener(listener);
	}
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		getWorkingCopy().removePropertyChangeListener(listener);
	}
	public void setServerConfiguration(IFolder configuration) {
		getWorkingCopy().setServerConfiguration(configuration);
	}
	public String getName() {
		return getWorkingCopy().getName();
	}
	public String getId() {
		return getWorkingCopy().getId();
	}
	public void delete() throws CoreException {
		getWorkingCopy().delete();
	}
	public IServer getOriginal() {
		return getWorkingCopy().getOriginal();
	}
	public boolean isReadOnly() {
		return getWorkingCopy().isReadOnly();
	}
	public boolean isWorkingCopy() {
		return getWorkingCopy().isWorkingCopy();
	}
	public IServer save(boolean force, IProgressMonitor monitor)
			throws CoreException {
		return getWorkingCopy().save(force, monitor);
	}
	public Object getAdapter(Class adapter) {
		return getWorkingCopy().getAdapter(adapter);
	}
	public Object loadAdapter(Class adapter, IProgressMonitor monitor) {
		return getWorkingCopy().loadAdapter(adapter, monitor);
	}
	public String getHost() {
		return getWorkingCopy().getHost();
	}
	public IServer saveAll(boolean force, IProgressMonitor monitor)
			throws CoreException {
		return getWorkingCopy().saveAll(force, monitor);
	}
	public IRuntime getRuntime() {
		return getWorkingCopy().getRuntime();
	}
	public IServerType getServerType() {
		return getWorkingCopy().getServerType();
	}
	public IFolder getServerConfiguration() {
		return getWorkingCopy().getServerConfiguration();
	}
	public void setRuntime(IRuntime runtime) {
		getWorkingCopy().setRuntime(runtime);
	}
	public IServerWorkingCopy createWorkingCopy() {
		return getWorkingCopy().createWorkingCopy();
	}
	public void setAttribute(String attributeName, int value) {
		getWorkingCopy().setAttribute(attributeName, value);
	}
	public void setAttribute(String attributeName, boolean value) {
		getWorkingCopy().setAttribute(attributeName, value);
	}
	public void setAttribute(String attributeName, String value) {
		getWorkingCopy().setAttribute(attributeName, value);
	}
	public void setAttribute(String attributeName, List<String> value) {
		getWorkingCopy().setAttribute(attributeName, value);
	}
	public IModule[] getModules() {
		return getWorkingCopy().getModules();
	}
	public void setAttribute(String attributeName, Map value) {
		getWorkingCopy().setAttribute(attributeName, value);
	}
	public IStatus canModifyModules(IModule[] add, IModule[] remove,
			IProgressMonitor monitor) {
		return getWorkingCopy().canModifyModules(add, remove, monitor);
	}
	public void setHost(String host) {
		getWorkingCopy().setHost(host);
	}
	public void modifyModules(IModule[] add, IModule[] remove,
			IProgressMonitor monitor) throws CoreException {
		getWorkingCopy().modifyModules(add, remove, monitor);
	}
	public int getAttribute(String attributeName, int defaultValue) {
		return getWorkingCopy().getAttribute(attributeName, defaultValue);
	}
	public boolean getAttribute(String attributeName, boolean defaultValue) {
		return getWorkingCopy().getAttribute(attributeName, defaultValue);
	}
	public String getAttribute(String attributeName, String defaultValue) {
		return getWorkingCopy().getAttribute(attributeName, defaultValue);
	}
	public List<String> getAttribute(String attributeName,
			List<String> defaultValue) {
		return getWorkingCopy().getAttribute(attributeName, defaultValue);
	}
	public Map getAttribute(String attributeName, Map defaultValue) {
		return getWorkingCopy().getAttribute(attributeName, defaultValue);
	}
	public IModule[] getChildModules(IModule[] module, IProgressMonitor monitor) {
		return getWorkingCopy().getChildModules(module, monitor);
	}
	public IModule[] getRootModules(IModule module, IProgressMonitor monitor)
			throws CoreException {
		return getWorkingCopy().getRootModules(module, monitor);
	}
	public ServerPort[] getServerPorts(IProgressMonitor monitor) {
		return getWorkingCopy().getServerPorts(monitor);
	}
}
