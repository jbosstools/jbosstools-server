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
