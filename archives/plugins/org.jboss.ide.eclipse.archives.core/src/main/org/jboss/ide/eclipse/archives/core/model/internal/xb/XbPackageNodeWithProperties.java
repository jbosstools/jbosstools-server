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

import java.util.Iterator;
import java.util.Properties;

public abstract class XbPackageNodeWithProperties extends XbPackageNode {

	protected XbProperties properties;
	
	public XbPackageNodeWithProperties(String nodeType) {
		super(nodeType);
		properties = new XbProperties();
	}
	
	public XbPackageNodeWithProperties (XbPackageNodeWithProperties node) {
		super(node);
		properties = new XbProperties();
	}
	
	public void copyFrom (XbPackageNodeWithProperties node) {
		properties.getProperties().clear();
		
		Properties props = node.getProperties().getProperties();
		for (Iterator iter = props.keySet().iterator(); iter.hasNext(); ) {
			String key = (String) iter.next();
			properties.getProperties().setProperty(key, (String) props.get(key));
		}
	}
	
	public XbProperties getProperties () {
		return properties;
	}
	
	public void setProperties (Object object) {
		setProperties((XbProperties)object);
	}
	
	protected void setProperties (XbProperties properties) {
		this.properties = properties;
	}

}
