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

public class JBoss71DefaultLaunchArguments extends JBoss70DefaultLaunchArguments {
	public JBoss71DefaultLaunchArguments(IServer s) {
		super(s);
	}
	public JBoss71DefaultLaunchArguments(IRuntime rt) {
		super(rt);
	}
	
	@Override
	protected String getLoggingProgramArg() {
		// logging params removed
		return new String();
	}
	
	protected String getJBossJavaFlags() {
		return " -Djboss.modules.system.pkgs=org.jboss.byteman " + //$NON-NLS-1$
				super.getJBossJavaFlags();
	}
}
