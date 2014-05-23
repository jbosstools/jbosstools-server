/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.core.server.jbossmodules.LayeredModulePathFactory;

/**
 * This class represents a module + slot combination
 * in an AS7/wf-derived runtime.
 */
public class ModuleSlot {
	private String module, slot;
	public ModuleSlot(String module) {
		this(module, null);
	}
	public ModuleSlot(String module, String slot) {
		this.module = module;
		this.slot = slot;
	}
	public String getModule() {
		return module;
	}
	public String getSlot() {
		return slot == null ? "main" : slot;
	}
	@Override
	public boolean equals(Object o) {
		if( o instanceof ModuleSlot ) {
			return isEquals(((ModuleSlot)o).getModule(),module) && 
					isEquals(((ModuleSlot)o).getSlot(), slot);
		}
		return false;
	}
	private boolean isEquals(Object one, Object two) {
		if( one == null ) 
			return two == null;
		return two != null && one.equals(two);
	}
	// Get a relative path
	public IPath getPath() {
		String slashed = getModule().replaceAll("\\.", "/");
		return new Path(slashed).append(getSlot());
	}
	
	public int hashCode() {
		return module.hashCode() + getSlot().hashCode();
	}
	
	public IPath[] getJars(IRuntime runtime) {
		IPath modulesFolder = runtime.getLocation().append("modules");
		return getJars(modulesFolder);
	}
	
	public IPath[] getJars(IPath modulesFolder) {
		File[] layeredPaths = LayeredModulePathFactory.resolveLayeredModulePath(modulesFolder.toFile());
		for( int i = 0; i < layeredPaths.length; i++ ) {
			IPath lay = new Path(layeredPaths[i].getAbsolutePath());
			File layeredPath = new File(lay.append(getPath()).toOSString());
			if( layeredPath.exists()) {
				return getJarsFrom(layeredPath);
			}
		}
		return new IPath[0];
	}

	
	private IPath[] getJarsFrom(File layeredPath) {
		ArrayList<IPath> list = new ArrayList<IPath>();
		File[] children = layeredPath.listFiles();
		for( int i = 0; i < children.length; i++ ) {
			if( children[i].isFile() && children[i].getName().endsWith(".jar")) {
				list.add(new Path(children[i].getAbsolutePath()));
			}
		}
		return (IPath[]) list.toArray(new IPath[list.size()]);
	}
}