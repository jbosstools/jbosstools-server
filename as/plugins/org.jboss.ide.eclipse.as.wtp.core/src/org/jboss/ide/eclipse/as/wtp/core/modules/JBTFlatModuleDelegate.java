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
package org.jboss.ide.eclipse.as.wtp.core.modules;

import org.eclipse.core.resources.IProject;
import org.eclipse.wst.common.componentcore.internal.flat.IChildModuleReference;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.web.internal.deployables.FlatComponentDeployable;

public class JBTFlatModuleDelegate extends FlatComponentDeployable {

	private JBTFlatProjectModuleFactory factory;
	public JBTFlatModuleDelegate(IProject project, JBTFlatProjectModuleFactory myFactory) {
		super(project);
		this.factory = myFactory;
	}
	public JBTFlatModuleDelegate(IProject project, IVirtualComponent aComponent, JBTFlatProjectModuleFactory myFactory) {
		super(project, aComponent);
		this.factory = myFactory;
	}
    @Override
	protected IModule gatherModuleReference(IVirtualComponent component, IChildModuleReference child ) {
    	if (!child.isBinary()) 
    		return super.gatherModuleReference(component, child);
    	return factory.createChildModule(this, child);
    }
}
