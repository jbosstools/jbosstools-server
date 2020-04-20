/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.jmx.jvmmonitor.internal.ui.properties.mbean;

import javax.management.ObjectName;

public class JMXUtils {

	/**
	 * Gets the type name.
	 * 
	 * @param objectName
	 *            The object name
	 * @return The type name
	 */
	public static String getTypeName(ObjectName objectName) {
		String canonicalName = objectName.getCanonicalName();
		if (canonicalName != null) {
			String[] split = canonicalName.split("type=");
			if (split.length < 2) {
				split = canonicalName.split("Type=");
			}
			if (split.length >= 2) {
				String type = split[1];
				return type.split(",")[0]; //$NON-NLS-1$
			}
			//System.out.println("Can't split: " + canonicalName);
		}
		return "";
	}

}
