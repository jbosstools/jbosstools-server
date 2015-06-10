/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.core.runtime.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathCorePlugin;
import org.jboss.ide.eclipse.as.classpath.core.runtime.modules.manifest.DeploymentStructureUtil;

public class DeploymentStructureChangeListener extends ManifestChangeListener {
	public static void register() {
		try {
			final DeploymentStructureChangeListener listener = new DeploymentStructureChangeListener();
			final IWorkspace ws = ResourcesPlugin.getWorkspace();
			ws.addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_BUILD);
		} catch(Exception e) {
			ClasspathCorePlugin.log("Unable to add jboss-deployment-structure.xml change listener", e);
		}
	}


	protected String getFilePattern() {
		return "jboss-deployment-structure.xml";
	}
	
	protected void ensureInCache(IFile f) {
		new DeploymentStructureUtil().esureInCache(f);
	}
}
