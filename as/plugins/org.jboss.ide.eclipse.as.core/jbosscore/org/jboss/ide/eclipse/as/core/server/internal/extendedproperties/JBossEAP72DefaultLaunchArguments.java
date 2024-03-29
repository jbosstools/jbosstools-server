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

public class JBossEAP72DefaultLaunchArguments extends
JBossEAP70DefaultLaunchArguments {
	public JBossEAP72DefaultLaunchArguments(IServer s) {
		super(s);
	}
	public JBossEAP72DefaultLaunchArguments(IRuntime rt) {
		super(rt);
	}
	
	protected String getJavaVersionVMArgs() {
		return EapWildflyJavaVersionFlagUtil.getJavaVersionVMArgs(getRuntime());
	}
	
	@Override
	public String getStartDefaultVMArgs() {
		return super.getStartDefaultVMArgs() + getJavaVersionVMArgs();
	}
}
