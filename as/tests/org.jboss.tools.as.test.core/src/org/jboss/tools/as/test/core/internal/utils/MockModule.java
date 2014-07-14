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
package org.jboss.tools.as.test.core.internal.utils;

import java.util.HashMap;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.jst.server.core.IJ2EEModule;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleType;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ModuleDelegate;

public class MockModule extends ModuleDelegate implements IModule, IEnterpriseApplication, IJ2EEModule {
	private String id, name;
	private MockModuleType type;
	private HashMap<IModule, String> children;
	private IModuleResource[] members;
	private IProject project;
	private boolean exists = false;
	private boolean binary = false;
	private boolean external = false;
	
	public MockModule(final String id, final String name, 
			final String typeId, final String typeName, final String typeVersion) {
		this.id = id;
		this.name = name;
		this.type = new MockModuleType(typeId, typeName, typeVersion);
		children = new HashMap<IModule, String>();
		project = null;
	}
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public IModuleType getModuleType() {
		return type;
	}
	public IProject getProject() {
		return project;
	}
	public boolean isExternal() {
		return external;
	}
	public boolean exists() {
		return exists;
	}
	public void setExists(boolean b) {
		exists = b;
	}
	public void setBinary(boolean b) {
		binary = b;
	}
	public void setExternal(boolean b) {
		external = b;
	}
	@Override
	public boolean isBinary() {
		return binary;
	}
	public Object getAdapter(Class adapter) {
		if( adapter.equals(IEnterpriseApplication.class))
			return this;
		if( adapter.equals(IJ2EEModule.class))
			return this;
		if( adapter.equals(ModuleDelegate.class))
			return this;
		return null;
	}
	public Object loadAdapter(Class adapter, IProgressMonitor monitor) {
		return getAdapter(adapter);
	}
	public void addChildModule(IModule child, String relativeURI) {
		children.put(child, relativeURI);
	}
	public void clearChildren() {
		children.clear();
	}
	public IModule[] getModules() {
		Set<IModule> s = children.keySet();
		return (IModule[]) s.toArray(new IModule[s.size()]);
	}
	public String getURI(IModule module) {
		return children.get(module);
	}
	public IContainer[] getResourceFolders() {
		return null;
	}
	@Override
	public IStatus validate() {
		return Status.OK_STATUS;
	}
	@Override
	public IModule[] getChildModules() {
		return getModules();
	}
	@Override
	public IModuleResource[] members() throws CoreException {
		return members;
	}
	
	public void setMembers(IModuleResource[] members) {
		this.members = members;
	}
	
	public void setProject(IProject p) {
		this.project = p;
	}
	@Override
	public IContainer[] getJavaOutputFolders() {
		return new IContainer[0];
	}
	public int hashCode() {
		return (getModuleType().getId() + ":" + getId() + ":" + getName()).hashCode();
	}
	public boolean equals(Object other) {
		if( !(other instanceof MockModule))
			return false;
		MockModule o2 = (MockModule)other;
		return o2.getModuleType().getId().equals(getModuleType().getId()) && 
				o2.getId().equals(getId()) && o2.getName().equals(getName());
	}
}
