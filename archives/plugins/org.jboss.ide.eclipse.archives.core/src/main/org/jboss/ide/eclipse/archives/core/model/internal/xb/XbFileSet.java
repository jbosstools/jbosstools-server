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
package org.jboss.ide.eclipse.archives.core.model.internal.xb;

public class XbFileSet extends XbPackageNodeWithProperties {

	private String dir, includes, excludes;
	private boolean inWorkspace;
	private boolean flattened = false;
	
	public XbFileSet ()
	{
		super();
	}
	
	public XbFileSet (XbFileSet fileset)
	{
		super(fileset);
		copyFrom(fileset);
	}
	
	public void copyFrom (XbFileSet fileset)
	{
		super.copyFrom(fileset);
		this.dir = fileset.dir == null ? null : new String(fileset.dir);
		this.includes = fileset.includes == null ? null : new String(fileset.includes);
		this.excludes = fileset.excludes == null ? null : new String(fileset.excludes);
		this.inWorkspace = fileset.inWorkspace;
		this.flattened = fileset.flattened;
	}
	
	protected Object clone() throws CloneNotSupportedException {
		return new XbFileSet(this);
	}
	
	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public String getExcludes() {
		return excludes;
	}

	public void setExcludes(String excludes) {
		this.excludes = excludes;
	}

	public String getIncludes() {
		return includes;
	}

	public void setIncludes(String includes) {
		this.includes = includes;
	}
	
	public boolean isInWorkspace() {
		return inWorkspace;
	}

	public void setInWorkspace(boolean inWorkspace) {
		this.inWorkspace = inWorkspace;
	}

	public boolean isFlattened() {
		return flattened;
	}

	public void setFlattened(boolean flatten) {
		this.flattened = flatten;
	}
}
