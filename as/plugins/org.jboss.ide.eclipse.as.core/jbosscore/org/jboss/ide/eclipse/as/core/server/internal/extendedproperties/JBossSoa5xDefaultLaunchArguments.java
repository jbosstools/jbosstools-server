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

public class JBossSoa5xDefaultLaunchArguments extends JBoss5xDefaultLaunchArguments {
	public static final String DEFAULT_MEM_ARGS_SOA_53 = "-Xms1303m -Xmx1303m -XX:MaxPermSize=256m "; //$NON-NLS-1$
	
	public JBossSoa5xDefaultLaunchArguments(IRuntime rt) {
		super(rt);
	}
	public JBossSoa5xDefaultLaunchArguments(IServer server) {
		super(server);
	}

	@Override
	protected String getMemoryArgs() {
		return DEFAULT_MEM_ARGS_SOA_53;
	}
	
	@Override
	public String getStartDefaultVMArgs() {
		return getProgramNameArgs() + getServerFlagArgs() + 
				"-Djava.awt.headless=true " +  //$NON-NLS-1$
				"-Dorg.apache.xml.dtm.DTMManager=org.apache.xml.dtm.ref.DTMManagerDefault " +  //$NON-NLS-1$
				"-Dorg.jboss.net.protocol.file.useURI=false -Dorg.jboss.resolver.warning=true" + //$NON-NLS-1$
				"-Dsun.lang.ClassLoader.allowArraySyntax=true " + //$NON-NLS-1$
				getMemoryArgs() + getJavaFlags() + getJBossJavaFlags();
	}
}
