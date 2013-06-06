/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
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

public class Wildfly80DefaultLaunchArguments extends
		JBoss71DefaultLaunchArguments {
	public Wildfly80DefaultLaunchArguments(IServer s) {
		super(s);
	}
	public Wildfly80DefaultLaunchArguments(IRuntime rt) {
		super(rt);
	}
	public String getStartDefaultVMArgs() {
		return super.getStartDefaultVMArgs() 
				+ "-Dorg.jboss.logmanager.nocolor=true "; //$NON-NLS-1$
	}
	protected String getMemoryArgs() {
		return "-Xms64m -Xmx512m -XX:MaxPermSize=256m "; //$NON-NLS-1$
	}
}
