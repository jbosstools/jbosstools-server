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

public class XbLibFileSet extends XbPackageNodeWithProperties {

	private String id;
	
	public XbLibFileSet () {
		super("lib-fileset"); //$NON-NLS-1$
	}
	
	public XbLibFileSet (XbLibFileSet fileset) {
		super(fileset);
		copyFrom(fileset);
	}
	
	public void copyFrom (XbLibFileSet fileset) {
		super.copyFrom(fileset);
		this.id = fileset.id;
	}
	
	protected Object clone() throws CloneNotSupportedException {
		return new XbLibFileSet(this);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
