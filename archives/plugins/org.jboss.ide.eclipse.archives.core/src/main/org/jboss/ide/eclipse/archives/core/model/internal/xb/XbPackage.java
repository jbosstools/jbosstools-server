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

import java.util.List;

public class XbPackage extends XbPackageNodeWithProperties {

	private String name, packageType, toDir, id;
	private boolean exploded, inWorkspace;

	public XbPackage () {
		super();
		exploded = false;
		inWorkspace = true;
	}
	
	public XbPackage (XbPackage pkg)
	{
		super(pkg);
		copyFrom (pkg);
	}
	
	public void copyFrom (XbPackage pkg)
	{
		super.copyFrom(pkg);
		this.name = pkg.name == null ? null: new String(pkg.name);
		this.packageType = pkg.packageType == null ? null : new String(pkg.packageType);
		this.toDir = pkg.toDir == null ? null : new String(pkg.toDir);
		this.exploded = pkg.exploded;
		this.inWorkspace = pkg.inWorkspace;
	}
	
	protected Object clone() throws CloneNotSupportedException {
		return new XbPackage(this);
	}
	
	public List getActions() {
		return getChildren(XbAction.class);
	}

	public List getPackages ()
	{
		return getChildren(XbPackage.class);
	}
	
	public List getFolders ()
	{
		return getChildren(XbFolder.class);
	}
	
	public List getFileSets()
	{
		return getChildren(XbFileSet.class);
	}
	
	public boolean isExploded() {
		return exploded;
	}

	public void setExploded(boolean exploded) {
		this.exploded = exploded;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getToDir() {
		return toDir;
	}

	public void setToDir(String toDir) {
		this.toDir = toDir;
	}

	public String getPackageType() {
		return packageType;
	}

	public void setPackageType(String packageType) {
		this.packageType = packageType;
	}

	/**
	 * Get the inWorkspace.
	 * 
	 * @return the inWorkspace.
	 */
	public boolean isInWorkspace() {
		return inWorkspace;
	}

	/**
	 * Set the inWorkspace.
	 * 
	 * @param inWorkspace The inWorkspace to set.
	 */
	public void setInWorkspace(boolean inWorkspace) {
		this.inWorkspace = inWorkspace;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
