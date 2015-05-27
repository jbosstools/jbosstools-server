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
package org.jboss.tools.wtp.server.launchbar.ui;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.ui.ServerUICore;
import org.jboss.tools.wtp.server.launchbar.descriptors.ModuleArtifactDetailsLaunchDescriptor;
import org.jboss.tools.wtp.server.launchbar.descriptors.ModuleArtifactLaunchDescriptor;
import org.jboss.tools.wtp.server.launchbar.descriptors.ModuleLaunchDescriptor;

/**
 * A label provider for the launchbar descriptors that
 * are provided by this plugin. 
 *
 */
public class ModuleDescriptorLabelProvider extends LabelProvider {

	/**
	 * The <code>LabelProvider</code> implementation of this
	 * <code>ILabelProvider</code> method returns <code>null</code>.
	 * Subclasses may override.
	 */
	@Override
	public Image getImage(Object element) {
		IModule m = null;
		if( element instanceof ModuleArtifactDetailsLaunchDescriptor) {
			m = ((ModuleArtifactDetailsLaunchDescriptor)element).getModule();
		} else if( element instanceof ModuleArtifactLaunchDescriptor) {
			m = ((ModuleArtifactLaunchDescriptor)element).getModule();
		} else if( element instanceof ModuleLaunchDescriptor) {
			m = ((ModuleLaunchDescriptor)element).getModule();
		}
		if( m != null ) {
			Image i =  ServerUICore.getLabelProvider().getImage(m);
			return i;
		}
		return null;
	}

	/**
	 * The <code>LabelProvider</code> implementation of this
	 * <code>ILabelProvider</code> method returns the element's
	 * <code>toString</code> string. Subclasses may override.
	 */
	@Override
	public String getText(Object element) {
		if( element instanceof ModuleArtifactDetailsLaunchDescriptor) {
			return ((ModuleArtifactDetailsLaunchDescriptor)element).getName();
		} else if( element instanceof ModuleArtifactLaunchDescriptor) {
			return ((ModuleArtifactLaunchDescriptor)element).getName();
		} else if( element instanceof ModuleLaunchDescriptor) {
			return ((ModuleLaunchDescriptor)element).getName();
		}
		return element == null ? "null" : element.toString();
	}
}
