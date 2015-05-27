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
package org.jboss.tools.wtp.server.launchbar.descriptors;

import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptorType;
import org.eclipse.wst.server.core.IModule;
import org.jboss.tools.wtp.server.launchbar.ModuleObjectProvider;
import org.jboss.tools.wtp.server.launchbar.objects.ModuleArtifactWrapper;

/**
 * A launch descriptor representing a module artifact
 */
public class ModuleArtifactLaunchDescriptor implements ILaunchDescriptor {
	private ModuleArtifactWrapper module;
	private ILaunchDescriptorType type;
	public ModuleArtifactLaunchDescriptor(ModuleArtifactWrapper wrap,ILaunchDescriptorType type) {
		this.module = wrap;
		this.type = type;
	}
	public IModule getModule() {
		return module.getModule();
	}
	
	public ModuleArtifactWrapper getArtifactWrapper() {
		return module;
	}
	
	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return module.getName();
	}

	@Override
	public ILaunchDescriptorType getType() {
		return type;
	}
}