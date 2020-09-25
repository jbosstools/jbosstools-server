/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.server.internal.extendedproperties;

import org.eclipse.core.runtime.IAdaptable;
import org.jboss.ide.eclipse.as.core.server.IDefaultLaunchArguments;

public class Wildfly15PlusExtendedProperties extends Wildfly11PlusExtendedProperties {
	public Wildfly15PlusExtendedProperties(IAdaptable obj, String rtVersionString) {
		super(obj, rtVersionString);
	}

	@Override
	public IDefaultLaunchArguments getDefaultLaunchArguments() {
		if( server != null)
			return new Wildfly150DefaultLaunchArguments(server);
		return new Wildfly150DefaultLaunchArguments(runtime);
	}
}
