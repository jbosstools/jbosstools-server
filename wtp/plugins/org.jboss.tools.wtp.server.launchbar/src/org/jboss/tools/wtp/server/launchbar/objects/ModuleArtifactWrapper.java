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
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.model.ModuleArtifactDelegate;

/**
 * An object, to be returned by an ObjectProvider,
 * which represents a module artifact.
 */
public class ModuleArtifactWrapper {
	protected IModuleArtifact moduleArtifact;
	public ModuleArtifactWrapper(IModuleArtifact artifact ) {
		this.moduleArtifact = artifact;
	}
	
	public String getArtifactClass() {
		return moduleArtifact.getClass().getName();
	}
	
	public String getArtifactString() {
		return ((ModuleArtifactDelegate) moduleArtifact).serialize();
	}
	
	public String getName() {
		return ((ModuleArtifactDelegate) moduleArtifact).getName();
	}
	
	public IModule getModule() {
		return moduleArtifact.getModule();
	}
	
	public boolean equals(Object other) {
		return moduleArtifact.equals(other);
	}
	public int hashcode() {
		return moduleArtifact.hashCode();
	}
}