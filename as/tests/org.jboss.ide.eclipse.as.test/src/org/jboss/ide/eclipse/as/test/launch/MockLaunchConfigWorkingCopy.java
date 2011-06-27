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
package org.jboss.ide.eclipse.as.test.launch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;

/**
 * @author André Dietisheim
 */
public class MockLaunchConfigWorkingCopy implements ILaunchConfigurationWorkingCopy {
	
	protected final Map<String, Object> attributeMap;

	public MockLaunchConfigWorkingCopy() throws CoreException {
		this.attributeMap = new HashMap<String, Object>();
	}
	
	public String getAttribute(String attributeName, String defaultValue) throws CoreException {
		String value = (String) attributeMap.get(attributeName);
		if (value == null) {
			value = defaultValue;
		}
		return value;
	}

	public boolean attributeEquals(String attributeName, Object expectedValue) throws CoreException {
		String attributeValue = getAttribute(attributeName, (String) null);
		return (expectedValue == null && attributeValue == null)
				|| expectedValue != null && expectedValue.equals(attributeValue);
	}

	public boolean getAttribute(String key, boolean defaultValue) throws CoreException {
		Boolean value = (Boolean) attributeMap.get(key);
		if (value == null) {
			value = defaultValue;
		}
		return value;
	}
	
	@SuppressWarnings("rawtypes")
	public List getAttribute(String key, List defaultValue) throws CoreException {
		List value = (List) attributeMap.get(key);
		if (value == null) {
			value = defaultValue;
		}
		return value;
	}
	
	public boolean hasAttribute(String key) throws CoreException {
		return attributeMap.containsKey(key);
	}
	
	public void setAttribute(String key, String value) {
		attributeMap.put(key, value);
	}

	@SuppressWarnings("rawtypes")
	public void setAttribute(String key, List value) {
		attributeMap.put(key, value);
	}

	@SuppressWarnings("rawtypes")
	public void setAttribute(String key, Map map) {
		attributeMap.put(key, map);
	}
	
	public void setAttribute(String key, boolean value) {
		attributeMap.put(key, value);
	}
	
	public boolean contentsEqual(ILaunchConfiguration arg0) {
		throw new UnsupportedOperationException();
	}

	public ILaunchConfigurationWorkingCopy copy(String arg0) throws CoreException {
		throw new UnsupportedOperationException();
	}

	public void delete() throws CoreException {
		throw new UnsupportedOperationException();
	}

	public boolean exists() {
		throw new UnsupportedOperationException();
	}


	public int getAttribute(String arg0, int arg1) throws CoreException {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("rawtypes")
	public Set getAttribute(String arg0, Set arg1) throws CoreException {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("rawtypes")
	public Map getAttribute(String arg0, Map arg1) throws CoreException {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("rawtypes")
	public Map getAttributes() throws CoreException {
		throw new UnsupportedOperationException();
	}

	public String getCategory() throws CoreException {
		throw new UnsupportedOperationException();
	}

	public IFile getFile() {
		throw new UnsupportedOperationException();
	}

	public IPath getLocation() {
		throw new UnsupportedOperationException();
	}

	public IResource[] getMappedResources() throws CoreException {
		throw new UnsupportedOperationException();
	}

	public String getMemento() throws CoreException {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("rawtypes")
	public Set getModes() throws CoreException {
		throw new UnsupportedOperationException();
	}

	public String getName() {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("rawtypes")
	public ILaunchDelegate getPreferredDelegate(Set arg0) throws CoreException {
		throw new UnsupportedOperationException();
	}

	public ILaunchConfigurationType getType() throws CoreException {
		throw new UnsupportedOperationException();
	}

	public ILaunchConfigurationWorkingCopy getWorkingCopy() throws CoreException {
		throw new UnsupportedOperationException();
	}

	public boolean isLocal() {
		throw new UnsupportedOperationException();
	}

	public boolean isMigrationCandidate() throws CoreException {
		throw new UnsupportedOperationException();
	}

	public boolean isReadOnly() {
		throw new UnsupportedOperationException();
	}

	public boolean isWorkingCopy() {
		throw new UnsupportedOperationException();

	}

	public ILaunch launch(String arg0, IProgressMonitor arg1) throws CoreException {
		throw new UnsupportedOperationException();

	}

	public ILaunch launch(String arg0, IProgressMonitor arg1, boolean arg2) throws CoreException {
		throw new UnsupportedOperationException();

	}

	public ILaunch launch(String arg0, IProgressMonitor arg1, boolean arg2, boolean arg3) throws CoreException {
		throw new UnsupportedOperationException();

	}

	public void migrate() throws CoreException {
		throw new UnsupportedOperationException();

	}

	public boolean supportsMode(String arg0) throws CoreException {
		throw new UnsupportedOperationException();

	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("rawtypes")
	public void addModes(Set arg0) {
		throw new UnsupportedOperationException();
	}

	public ILaunchConfiguration doSave() throws CoreException {
		throw new UnsupportedOperationException();
	}

	public ILaunchConfiguration getOriginal() {
		throw new UnsupportedOperationException();
	}

	public ILaunchConfigurationWorkingCopy getParent() {
		throw new UnsupportedOperationException();
	}

	public boolean isDirty() {
		throw new UnsupportedOperationException();
	}

	public Object removeAttribute(String arg0) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("rawtypes")
	public void removeModes(Set arg0) {
		throw new UnsupportedOperationException();
	}

	public void rename(String arg0) {
		throw new UnsupportedOperationException();
	}

	public void setAttribute(String arg0, int arg1) {
		throw new UnsupportedOperationException();
	}


	@SuppressWarnings("rawtypes")
	public void setAttribute(String arg0, Set arg1) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("rawtypes")
	public void setAttributes(Map arg0) {
		throw new UnsupportedOperationException();
	}

	public void setContainer(IContainer arg0) {
		throw new UnsupportedOperationException();
	}

	public void setMappedResources(IResource[] arg0) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("rawtypes")
	public void setModes(Set arg0) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("rawtypes")
	public void setPreferredLaunchDelegate(Set arg0, String arg1) {
		throw new UnsupportedOperationException();
	}
}
