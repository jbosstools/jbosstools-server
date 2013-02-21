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

import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;

public class GateIn33AS71DefaultLaunchArguments extends JBoss71DefaultLaunchArguments {
	public GateIn33AS71DefaultLaunchArguments(IServer s) {
		super(s);
	}
	public GateIn33AS71DefaultLaunchArguments(IRuntime rt) {
		super(rt);
	}
	
	@Override
	public String getStartDefaultProgramArgs() {
		return DASH + JB7_MP_ARG + SPACE + QUOTE 
				+ getServerHome().append(MODULES).toString() 
				+ ":" //$NON-NLS-1$
				+ getServerHome().append("gatein").append(MODULES).toString()  //$NON-NLS-1$
				+ QUOTE 
				+ getLoggingProgramArg()
				+ SPACE + DASH + JB7_JAXPMODULE + SPACE + JB7_JAXP_PROVIDER
				+ SPACE + JB7_STANDALONE_ARG;
	}
	
	@Override
	protected String getLoggingProgramArg() {
		// logging params removed
		return new String();
	}
	
	protected String getJBossJavaFlags() {
		IPath gateInConfig = 
				getServerHome().append("standalone")//$NON-NLS-1$
				.append("configuration").append("gatein"); //$NON-NLS-1$ //$NON-NLS-2$
		String s1 = "-Dexo.conf.dir=" + QUOTE //$NON-NLS-1$
				+ gateInConfig.toString()
				+ QUOTE + SPACE
				+ "-Dgatein.conf.dir=" + QUOTE  //$NON-NLS-1$
				+ gateInConfig + QUOTE 
				+ SPACE 
				+ "-Dexo.conf.dir.name=gatein" //$NON-NLS-1$
				+ SPACE
				+ "-Dexo.product.developing=true";//$NON-NLS-1$
		return super.getJBossJavaFlags() + s1;
	}
}
