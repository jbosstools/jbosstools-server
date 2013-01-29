/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal.extendedproperties;

import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;

public class JBoss5xDefaultLaunchArguments extends JBossDefaultLaunchArguments {
	public JBoss5xDefaultLaunchArguments(IRuntime rt) {
		super(rt);
	}
	public JBoss5xDefaultLaunchArguments(IServer server) {
		super(server);
	}

	protected String getMemoryArgs() {
		return DEFAULT_MEM_ARGS_AS50;
	}
}
