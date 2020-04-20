/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.as.test.core.internal.utils.classpath;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class WorkspaceTestUtil {
	
	public static void setAutoBuildEnabled( boolean b) {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        if (workspace.isAutoBuilding()) {
                IWorkspaceDescription description = workspace.getDescription();
                description.setAutoBuilding(false);
                try {
                	workspace.setDescription(description);
                }catch(CoreException ce) {}
        }
	}
	
	public static boolean isAutoBuildEnabled() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceDescription description = workspace.getDescription();
        return description.isAutoBuilding();
	}

}
