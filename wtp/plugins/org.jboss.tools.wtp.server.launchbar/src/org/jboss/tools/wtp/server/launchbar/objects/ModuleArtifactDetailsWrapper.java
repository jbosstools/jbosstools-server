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
package org.jboss.tools.wtp.server.launchbar.objects;

import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.ModuleArtifactDelegate;
import org.jboss.tools.wtp.server.launchbar.ServerLaunchBarDelegate;

/**
 * An object, to be returned by an ObjectProvider,
 * which represents a module artifact via only its 
 * name, serialization, and class, as pulled from
 * existing launch configurations used internally by wst.servertools
 */
public class ModuleArtifactDetailsWrapper {
	protected String artifact, clazz, name;
	protected ModuleArtifactDelegate moduleArtifact;
	boolean attemptedArtifactLoad = false;
	public ModuleArtifactDetailsWrapper(String name, String artifact, String clazz ) {
		this.artifact = artifact;
		this.clazz = clazz;
		this.name = name;
		if( getArtifactDelegate() != null ) {
			try {
				this.name = getArtifactDelegate().getName();
			} catch(Exception e) {
				// Ignore, impl error in re-serialized artifact, use provided name instead
			}
		}
	}
	
	public ModuleArtifactDelegate getArtifactDelegate() {
		if( moduleArtifact == null && !attemptedArtifactLoad) {
			moduleArtifact = ServerLaunchBarDelegate.getArtifact(clazz, artifact);
			attemptedArtifactLoad = true;
		}
		return moduleArtifact;
	}
	
	public String getArtifactClass() {
		return clazz;
	}
	
	public String getArtifactString() {
		return artifact;
	}
	
	public String getName() {
		return name;
	}
	
	public IModule getModule() {
		return getArtifactDelegate() == null ? null : getArtifactDelegate().getModule();
	}
	
	public boolean equals(Object other) {
		if(other instanceof ModuleArtifactDetailsWrapper) {
			String otherArt = ((ModuleArtifactDetailsWrapper)other).artifact;
			String otherClazz = ((ModuleArtifactDetailsWrapper)other).clazz;
			return otherArt.equals(this.artifact) && otherClazz.equals(clazz); 
		}
		return false;
	}
	public int hashcode() {
		return (artifact + "::" + clazz).hashCode();
	}
}