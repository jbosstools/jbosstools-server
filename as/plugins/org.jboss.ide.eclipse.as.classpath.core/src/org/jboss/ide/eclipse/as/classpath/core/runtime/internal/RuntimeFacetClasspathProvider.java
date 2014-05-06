/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.ide.eclipse.as.classpath.core.runtime.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.common.project.facet.core.IClasspathProvider;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponent;
import org.jboss.ide.eclipse.as.classpath.core.runtime.util.internal.JREClasspathUtil;

/**
 * This class acts as a front to add whatever entries are available 
 * in the client all runtime classpath provider, unless it's 
 * a java facet being added in which case it does the right thing.
 *
 */
public class RuntimeFacetClasspathProvider implements IClasspathProvider {
	protected IRuntimeComponent rc;

	public RuntimeFacetClasspathProvider() {
	}
	
	public RuntimeFacetClasspathProvider(final IRuntimeComponent rc) {
		this.rc = rc;
	}

	public List<IClasspathEntry> getClasspathEntries(final IProjectFacetVersion fv) {
		List<IClasspathEntry> ret = new ArrayList<IClasspathEntry>();
		if( fv.getProjectFacet().equals(JavaFacet.FACET)) {
			String runtimeId = rc.getProperty("id"); //$NON-NLS-1$
			ret.addAll(JREClasspathUtil.getJavaClasspathEntries(runtimeId));
		} else 
			// If we would prefer to handle on a per-facet
			// basis, this is the place to do it. 
			// Until we decide to do that, this will
			// simply delegate to the "client-all" container.
		{			
			ret.addAll(getProjectRuntimeEntry());
		}
		return ret;
	}
	
	/*
	 	We simply return one entry, which is a classpath container
	 	The container will have a visible display name equivilent to
	 	{runtimeTypeName}/{runtimeName}, for example:
	 	
	     	WildFly 8.x Runtime [My Wildfly 2nd runtime]
	     	
	    The returned container knows nothing of facets,
	    and returns default lists. 
	*/
	private List<IClasspathEntry> getProjectRuntimeEntry() {
		String id = rc.getProperty("id"); //$NON-NLS-1$
		IPath containerPath = ProjectRuntimeClasspathProvider.CONTAINER_PATH;
		IClasspathEntry cpentry = JavaCore.newContainerEntry(containerPath.append(id));
		return Collections.singletonList(cpentry);
	}
}
