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
public class EnumPropertyDescriptor extends PropertyDescriptor implements ReturnType {

	private final Class<? extends Enum> enumType;

	/**
	 * creates a property descriptor for enum properties
	 *
	 * @param id	the id
	 * @param displayName	the display name
	 */
	public EnumPropertyDescriptor(Object id, String displayName, Class<? extends Enum> enumType) {
		super(id, displayName);
		this.enumType = enumType;
	}

	public Class<? extends Enum> getEnumType() {
		return enumType;
	}

	@Override
	public Class<?> getReturnType() {
		return enumType;
	}}
