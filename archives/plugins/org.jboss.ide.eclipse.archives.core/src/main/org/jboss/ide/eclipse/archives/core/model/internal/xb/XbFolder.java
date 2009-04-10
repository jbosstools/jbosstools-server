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

public class XbFolder extends XbPackageNodeWithProperties {

	private String name;
	
	public XbFolder () {
		super("folder"); //$NON-NLS-1$
	}
	
	public XbFolder (XbFolder folder) {
		super(folder);
		copyFrom(folder);
	}
	
	public void copyFrom (XbFolder folder) {
		super.copyFrom(folder);
		this.name = folder.name == null ? null : new String(folder.name);
	}
	
	protected Object clone() throws CloneNotSupportedException {
		return new XbFolder(this);
	}
	
	public List getPackages () {
		return getChildren(XbPackage.class);
	}
	
	public List getFolders () {
		return getChildren(XbFolder.class);
	}
	
	public List getFileSets() {
		return getChildren(XbFileSet.class);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
