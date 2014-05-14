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

import java.beans.PropertyDescriptor;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
//import org.fusesource.camel.tooling.util.Strings;
import org.jboss.tools.jmx.commons.Activator;
import org.jboss.tools.jmx.commons.util.Objects;
import org.jboss.tools.jmx.commons.util.ReturnType;


public class PropertyDescriptors {

	/**
	 * Returns a readable property descriptor, converting camelCase to more readable words if there is no description configured
	 */
	public static String getReadablePropertyName(final IPropertyDescriptor descriptor) {
		String name = descriptor.getDisplayName();
		Object id = descriptor.getId();
		if (id instanceof String && Objects.equal(name, id)) {
			// lets split any camel case to make it more readable
			name = capitalizeAndSplitCamelCase(name);
		}
		return name;
	}

	public static String getReadablePropertyName(PropertyDescriptor descriptor) {
		String name = descriptor.getDisplayName();
		String id = descriptor.getName();
		// TODO use shortName???
		if (Objects.equal(name, id)) {
			name = capitalizeAndSplitCamelCase(name);
		}
		return name;
	}

	protected static String capitalizeAndSplitCamelCase(String name) {
	    // pleacu
	    // https://github.com/fabric8io/fabric8/blob/c0a127e93abb755543703f277ace535b45c7b0b9/tooling/camel-tooling-util/src/main/scala/io/fabric8/camel/tooling/util/Strings.scala
	    //String name2 = Strings.splitCamelCase(name);
	    // name = Strings.capitalize(name2);
		return name;
	}

	public static Class<?> getPropertyType(IPropertyDescriptor descriptor) {
		if (descriptor instanceof ReturnType) {
			ReturnType rt = (ReturnType) descriptor;
			return rt.getReturnType();
		} else if (descriptor instanceof TextPropertyDescriptor) {
			return String.class;
		} else {
			Activator.getLogger().debug("Unknown property type for " + descriptor + " of class: "
					+ descriptor.getClass().getName() + " " + descriptor.getId());
			return String.class;
		}
	}

}
