/*
 * JBoss, a division of Red Hat
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
