/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.jmx.commons.properties;

import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jboss.tools.jmx.commons.util.ReturnType;


/**
 * @author jstrachan
 */
public class ComplexPropertyDescriptor extends PropertyDescriptor implements ReturnType {

	private final Class<?> propertyType;

	/**
	 * creates a property descriptor for complex properties
	 *
	 * @param id	the id
	 * @param displayName	the display name
	 */
	public ComplexPropertyDescriptor(Object id, String displayName, Class<?> propertyType) {
		super(id, displayName);
		this.propertyType = propertyType;
	}

	public Class<?> getPropertyType() {
		return propertyType;
	}


	@Override
	public Class<?> getReturnType() {
		return propertyType;
	}
}
