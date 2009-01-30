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

public class XbPackages extends XbPackageNodeWithProperties {
	
	private double version;
	public XbPackages () {
		super();
	}
	
	public XbPackages (XbPackages packages) {
		super(packages);
		copyFrom(packages);
	}
	public void copyFrom (XbPackages node) {
		super.copyFrom(node);
		this.version = node.version;
	}

	protected Object clone() throws CloneNotSupportedException {
		return new XbPackages(this);
	}
	
	public List getPackages () {
		return getChildren(XbPackage.class);
	}

	public double getVersion() {
		return version;
	}

	public void setVersion(double version) {
		this.version = version;
	}
}
