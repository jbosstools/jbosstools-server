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
