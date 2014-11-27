/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.editor.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerPort;
import org.eclipse.wst.server.core.internal.Trace;


/**
 * For use in the change-profile wizard, in order to help cache / delay 
 * persistance of changes into the real working copy until asked to do so.
 * 
 * This is not intended to be used by other clients
 */
public class DelayedServerWorkingCopy implements IServerWorkingCopy {
	private IServerWorkingCopy original;
	private Object rt;
	private Object configFolder;
	private Map<String, Object> map = new HashMap<String, Object>();
	private Object myNull = new Object();
	private transient List<PropertyChangeListener> propertyListeners;
	private boolean dirty;
	
	public DelayedServerWorkingCopy(IServerWorkingCopy original) {
		this.original = original;
		rt = null;
		configFolder = null;
		propertyListeners = new ArrayList<PropertyChangeListener>();
	}
	@Override
	public void setName(String name) {
		setAttribute("name", name);
	}
	@Override
	public String getName() {
		return getAttribute("name", "");
	}
	@Override
	public String getId() {
		return getAttribute("id", "");
	}
	@Override
	public void delete() throws CoreException {
	}
	@Override
	public boolean isReadOnly() {
		return false;
	}
	@Override
	public boolean isWorkingCopy() {
		return true;
	}
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}
	@Override
	public Object loadAdapter(Class adapter, IProgressMonitor monitor) {
		return null;
	}
	
	@Override
	public void setHost(String host) {
		setAttribute("hostname", host);
	}

	@Override
	public String getHost() {
		return getAttribute("hostname", "localhost");
	}
	@Override
	public IServerType getServerType() {
		return original.getServerType();
	}
	@Override
	public IServerWorkingCopy createWorkingCopy() {
		return null;
	}
	@Override
	public IModule[] getModules() {
		return original.getModules();
	}
	
	@Override
	public IStatus canModifyModules(IModule[] add, IModule[] remove,
			IProgressMonitor monitor) {
		return Status.CANCEL_STATUS;
	}
	
	public IModule[] getChildModules(IModule[] module, IProgressMonitor monitor) {
		return original.getChildModules(module, monitor);
	}
	@Override
	public IModule[] getRootModules(IModule module, IProgressMonitor monitor)
			throws CoreException {
		return original.getRootModules(module, monitor);
	}
	@Override
	public ServerPort[] getServerPorts(IProgressMonitor monitor) {
		return original.getServerPorts(monitor);
	}
	@Override
	public void setReadOnly(boolean readOnly) {
	}
	
	@Override
	public IFolder getServerConfiguration() {
		if( this.configFolder == null )
			return original.getServerConfiguration();
		if( this.configFolder == myNull )
			return null;
		return (IFolder)configFolder;
	}

	@Override
	public void setServerConfiguration(IFolder configuration) {
		this.configFolder = (configuration == null ? myNull : configuration);
		setAttribute("configuration-id", configuration == null ? (String)null : configuration.getFullPath().toString());
	}
	@Override
	public IServer getOriginal() {
		return original.getOriginal();
	}
	@Override
	public IServer save(boolean force, IProgressMonitor monitor)
			throws CoreException {
		return null;
	}
	
	
	@Override
	public IServer saveAll(boolean force, IProgressMonitor monitor)
			throws CoreException {
		return null;
	}
	
	@Override
	public IRuntime getRuntime() {
		if( rt == null )
			return original.getRuntime();
		if( rt == myNull )
			return null;
		return (IRuntime)rt;
	}

	
	@Override
	public void setRuntime(IRuntime runtime) {
		this.rt = runtime;
		setAttribute("runtime-id", runtime == null ? runtime.getId() : (String)null);
	}
	@Override
	public void modifyModules(IModule[] add, IModule[] remove,
			IProgressMonitor monitor) throws CoreException {
	}
	
	@Override
	public boolean isDirty() {
		return dirty;
	}
	
	@Override
	public int getAttribute(String attributeName, int defaultValue) {
		Object obj = map.get(attributeName);
		if( obj == null )
			return original.getAttribute(attributeName, defaultValue);
		if( obj == myNull )
			return defaultValue;
		return Integer.parseInt((String)obj);
	}
	
	@Override
	public boolean getAttribute(String attributeName, boolean defaultValue) {
		Object obj = map.get(attributeName);
		if( obj == null )
			return original.getAttribute(attributeName, defaultValue);
		if( obj == myNull )
			return defaultValue;
		return Boolean.valueOf((String)obj);
	}
	
	@Override
	public String getAttribute(String attributeName, String defaultValue) {
		Object obj = map.get(attributeName);
		if( obj == null )
			return original.getAttribute(attributeName, defaultValue);
		if( obj == myNull )
			return defaultValue;
		return (String)obj;
	}
	
	@Override
	public List<String> getAttribute(String attributeName,
			List<String> defaultValue) {
		try {
			Object obj = map.get(attributeName);
			if (obj == null)
				return original.getAttribute(attributeName, defaultValue);
			if( obj == myNull )
				return defaultValue;
			return (List<String>) obj;
		} catch (Exception e) {
			// ignore
		}
		return defaultValue;
	}
	@Override
	public Map getAttribute(String attributeName, Map defaultValue) {
		try {
			Object obj = map.get(attributeName);
			if (obj == null)
				return original.getAttribute(attributeName, defaultValue);
			if( obj == myNull )
				return defaultValue;
			return (Map) obj;
		} catch (Exception e) {
			// ignore
		}
		return defaultValue;
	}
	
	
	@Override
	public void setAttribute(String attributeName, int value) {
		int current = getAttribute(attributeName, 0);
		map.put(attributeName, Integer.toString(value));
		firePropertyChangeEvent(attributeName, new Integer(current), new Integer(value));
		dirty = true;
	}
	@Override
	public void setAttribute(String attributeName, boolean value) {
		boolean current = getAttribute(attributeName, value);
		map.put(attributeName, Boolean.toString(value));
		firePropertyChangeEvent(attributeName, new Boolean(current), new Boolean(value));
		dirty = true;
	}
	@Override
	public void setAttribute(String attributeName, String value) {
		String current = getAttribute(attributeName, value);
		map.put(attributeName, value);
		firePropertyChangeEvent(attributeName, current, value);
		dirty = true;
	}
	@Override
	public void setAttribute(String attributeName, List<String> value) {
		List<String> current = getAttribute(attributeName, value);
		map.put(attributeName, value);
		firePropertyChangeEvent(attributeName, current, value);
		dirty = true;
	}
	@Override
	public void setAttribute(String attributeName, Map value) {
		Map current = getAttribute(attributeName, value);
		map.put(attributeName, value);
		firePropertyChangeEvent(attributeName, current, value);
		dirty = true;
	}
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyListeners.add(listener);
	}
	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyListeners.remove(listener);
	}
	
	
	/**
	 * Fire a property change event.
	 * 
	 * @param propertyName a property name
	 * @param oldValue the old value
	 * @param newValue the new value
	 */
	public void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
		if (propertyListeners == null)
			return;
	
		PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
		try {
			Iterator iterator = propertyListeners.iterator();
			while (iterator.hasNext()) {
				try {
					PropertyChangeListener listener = (PropertyChangeListener) iterator.next();
					listener.propertyChange(event);
				} catch (Exception e) {
					if (Trace.SEVERE) {
						Trace.trace(Trace.STRING_SEVERE, "Error firing property change event", e);
					}
				}
			}
		} catch (Exception e) {
			if (Trace.SEVERE) {
				Trace.trace(Trace.STRING_SEVERE, "Error in property event", e);
			}
		}
	}


	public void saveToOriginalWorkingCopy() {
		Iterator<String> keys = map.keySet().iterator();
		while(keys.hasNext()) {
			String k = keys.next();
			Object v = map.get(k);
			if( v == myNull )
				original.setAttribute(k, (String)v);
			if( v instanceof String)
				original.setAttribute(k, (String)v);
			if( v instanceof List<?>)
				original.setAttribute(k, (List<String>)v);
			if( v instanceof Map)
				original.setAttribute(k, (Map)v);
		}
		if( rt != null ) {
			original.setRuntime(rt == myNull ? (IRuntime)null : (IRuntime)rt);
		}
		if( configFolder != null ) {
			original.setServerConfiguration(configFolder == myNull ? null : (IFolder)configFolder);
		}
	}
}
