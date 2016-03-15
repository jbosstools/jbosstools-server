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
package org.jboss.tools.wtp.server.launchbar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.launchbar.core.ILaunchConfigurationProvider;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.jboss.tools.wtp.server.launchbar.descriptors.ModuleArtifactDetailsLaunchDescriptor;
import org.jboss.tools.wtp.server.launchbar.descriptors.ModuleArtifactLaunchDescriptor;
import org.jboss.tools.wtp.server.launchbar.descriptors.ModuleLaunchDescriptor;

public class ModuleLaunchConfigurationProvider implements ILaunchConfigurationProvider {
	
	public ModuleLaunchConfigurationProvider() {
		// TODO Auto-generated constructor stub
	}

	
	
	protected static final char[] INVALID_CHARS = new char[] {'/','\\', ':', '*', '?', '"', '<', '>', '|', '\0', '@', '&'};
	protected String getValidLaunchConfigurationName(String s) {
		if (s == null || s.length() == 0)
			return "1";
		int size = INVALID_CHARS.length;
		for (int i = 0; i < size; i++) {
			s = s.replace(INVALID_CHARS[i], '_');
		}
		return s;
	}
	
	@Override
	public boolean supports(ILaunchDescriptor descriptor, ILaunchTarget target) throws CoreException {
		if( target.getTypeId().equals(Activator.TARGET_TYPE_ID)) {
			return true;
		}
		return false;
	}

	
	@Override
	public ILaunchConfigurationType getLaunchConfigurationType(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		return DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(Activator.LAUNCH_TYPE_ID);
	}

	@Override
	public ILaunchConfiguration getLaunchConfiguration(ILaunchDescriptor descriptor, ILaunchTarget target)
			throws CoreException {
		ILaunchConfigurationType type = getLaunchConfigurationType(descriptor, target);
		String descriptorName = descriptor.getName();
		String validName = getValidLaunchConfigurationName(descriptorName);
		ILaunchConfigurationWorkingCopy wc = type.newInstance(null, validName);
		
		
		if( descriptor instanceof ModuleLaunchDescriptor ) {
			
			// I would prefer to somehow pass an actual Object here, but cannot persist it in launch config
			// So instead i must workaround by passing the project, and later trying to get 
			// module from project. Unfortunately, its not 1:1 mapping, so I may lose my module selection
			wc.setAttribute(ServerLaunchBarDelegate.ATTR_LAUNCH_TYPE, ServerLaunchBarDelegate.ATTR_LAUNCH_TYPE_MODULE);
			wc.setAttribute(ServerLaunchBarDelegate.ATTR_PROJECT, ((ModuleLaunchDescriptor)descriptor).getModule().getProject().getName());
			wc.setAttribute(ServerLaunchBarDelegate.ATTR_TARGET_NAME, target.getName());
			return wc;
		}
		
		if( descriptor instanceof ModuleArtifactDetailsLaunchDescriptor ) {
			wc.setAttribute(ServerLaunchBarDelegate.ATTR_LAUNCH_TYPE, ServerLaunchBarDelegate.ATTR_LAUNCH_TYPE_ARTIFACT);
			wc.setAttribute(ServerLaunchBarDelegate.ATTR_ARTIFACT_STRING, ((ModuleArtifactDetailsLaunchDescriptor)descriptor).getArtifactWrapper().getArtifactString());
			wc.setAttribute(ServerLaunchBarDelegate.ATTR_ARTIFACT_CLASS, ((ModuleArtifactDetailsLaunchDescriptor)descriptor).getArtifactWrapper().getArtifactClass());
			wc.setAttribute(ServerLaunchBarDelegate.ATTR_TARGET_NAME, target.getName());
		}

		if( descriptor instanceof ModuleArtifactLaunchDescriptor ) {
			wc.setAttribute(ServerLaunchBarDelegate.ATTR_LAUNCH_TYPE, ServerLaunchBarDelegate.ATTR_LAUNCH_TYPE_ARTIFACT);
			wc.setAttribute(ServerLaunchBarDelegate.ATTR_ARTIFACT_STRING, ((ModuleArtifactLaunchDescriptor)descriptor).getArtifactWrapper().getArtifactString());
			wc.setAttribute(ServerLaunchBarDelegate.ATTR_ARTIFACT_CLASS, ((ModuleArtifactLaunchDescriptor)descriptor).getArtifactWrapper().getArtifactClass());
			wc.setAttribute(ServerLaunchBarDelegate.ATTR_TARGET_NAME, target.getName());
		}
		
		// These launches are private and should not appear in the UI
		wc.setAttribute(ILaunchManager.ATTR_PRIVATE, true);
		
		return wc.doSave();
	}

	@Override
	public void launchDescriptorRemoved(ILaunchDescriptor descriptor) throws CoreException {
		
	}


	@Override
	public boolean launchConfigurationRemoved(ILaunchConfiguration configuration) throws CoreException {
		return false;
	}

	@Override
	public boolean launchConfigurationAdded(ILaunchConfiguration configuration) throws CoreException {
		return false;
	}

	@Override
	public boolean launchConfigurationChanged(ILaunchConfiguration configuration) throws CoreException {
		return false;
	}

	@Override
	public void launchTargetRemoved(ILaunchTarget target) throws CoreException {
		
	}

	public boolean launchDescriptorMatches(ILaunchDescriptor descriptor, ILaunchConfiguration configuration,
			ILaunchTarget target) throws CoreException {
		return getLaunchConfiguration(descriptor, target).equals(configuration);
	}

}
