/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.core.runtime.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.common.project.facet.core.IClasspathProvider;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponent;
import org.jboss.ide.eclipse.as.core.runtime.internal.WebtoolsProjectJBossClasspathContainerInitializer.WebtoolsProjectJBossClasspathContainer;

public class ProjectRuntimeClasspathProvider implements IClasspathProvider {
	private IRuntimeComponent rc;

	public ProjectRuntimeClasspathProvider() {
		
	}
	
	public ProjectRuntimeClasspathProvider(final IRuntimeComponent rc) {
		this.rc = rc;
	}

	public List getClasspathEntries(final IProjectFacetVersion fv) {
		IProjectFacet pf = fv.getProjectFacet();
		if (pf == null)
			return null;
		
		
		// initializer/runtimeId/facetId/facetVersion
		IPath path = new Path("org.jboss.ide.eclipse.as.core.runtime.ProjectInitializer");
		path = path.append(rc.getProperty("id"));
		path = path.append(fv.getProjectFacet().getId());
		path = path.append(fv.getVersionString());
		
		WebtoolsProjectJBossClasspathContainer temp = new WebtoolsProjectJBossClasspathContainer(path);
		// If we're a java runtime, just return that.
		if( temp.getClasspathEntries().length == 1 && fv.getProjectFacet().getId().equals("jst.java")) {
			return Arrays.asList(temp.getClasspathEntries());
		}

		// Otherwise just return the path to it, unless its empty in which case dont even include it
		if( temp.getClasspathEntries().length != 0 ) {
			IClasspathEntry cpentry = JavaCore.newContainerEntry(path);
			return Collections.singletonList(cpentry);
		}
		return new ArrayList();	
	}
	
	public static final class Factory implements IAdapterFactory {
		private static final Class[] ADAPTER_TYPES = { IClasspathProvider.class };

		public Object getAdapter(final Object adaptable, final Class adapterType) {
			IRuntimeComponent rc = (IRuntimeComponent) adaptable;
			return new ProjectRuntimeClasspathProvider(rc);
		}

		public Class[] getAdapterList() {
			return ADAPTER_TYPES;
		}
	}
}
