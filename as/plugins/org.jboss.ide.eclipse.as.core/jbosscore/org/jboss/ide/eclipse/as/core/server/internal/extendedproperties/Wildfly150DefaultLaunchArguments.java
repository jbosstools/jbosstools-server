/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.server.internal.extendedproperties;

import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;

public class Wildfly150DefaultLaunchArguments extends
		Wildfly100DefaultLaunchArguments {
	public Wildfly150DefaultLaunchArguments(IServer s) {
		super(s);
	}
	public Wildfly150DefaultLaunchArguments(IRuntime rt) {
		super(rt);
	}
	
	@Override
	public String getStartDefaultVMArgs() {
		return super.getStartDefaultVMArgs() + EapWildflyJavaVersionFlagUtil.getJavaVersionVMArgs(getRuntime());
	}
}
