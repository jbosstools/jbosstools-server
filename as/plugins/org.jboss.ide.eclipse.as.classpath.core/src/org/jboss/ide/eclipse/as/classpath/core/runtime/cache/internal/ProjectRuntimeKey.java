/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * A key for use in the cache for 
 * runtime+project combinations
 */
public class ProjectRuntimeKey {
	private IProject project;
	private IPath location;
	private IPath configPath;
	private String id;

	public ProjectRuntimeKey(IProject project, IPath location, 
			IPath configPath, String id) {
		this.project = project;
		this.location = location;
		this.configPath = configPath;
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((configPath == null) ? 0 : configPath.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
		result = prime * result + (project.getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProjectRuntimeKey other = (ProjectRuntimeKey) obj;
		if (configPath == null) {
			if (other.configPath != null)
				return false;
		} else if (!configPath.equals(other.configPath))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if( project == null ) {
			if( other.project != null ) {
				return false;
			}
		} else if( !project.getName().equals(other.project.getName())) {
			return false;
		}
		return true;
	}
}
