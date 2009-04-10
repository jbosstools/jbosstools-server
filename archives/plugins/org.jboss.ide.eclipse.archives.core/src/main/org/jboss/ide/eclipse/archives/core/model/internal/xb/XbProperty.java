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


public class XbProperty extends XbPackageNode {

	private String name, value;
	
	public XbProperty () {
		super("property"); //$NON-NLS-1$
	}
	
	public XbProperty(XbProperty property) {
		super(property);
		this.name = property.name == null ? null : new String(property.name);
		this.value = property.value ==  null ? null : new String(property.value);
	}
	
	protected Object clone() throws CloneNotSupportedException {
		return new XbProperty(this);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
