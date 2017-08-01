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
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7ServerRuntime;

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
				+ getServerHome().append(MODULES).toOSString() 
				+ ":" //$NON-NLS-1$
				+ getServerHome().append("gatein").append(MODULES).toOSString()  //$NON-NLS-1$
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
		LocalJBoss7ServerRuntime jb7rt = (LocalJBoss7ServerRuntime)runtime.getAdapter(LocalJBoss7ServerRuntime.class);
		IPath basedir = new Path(jb7rt.getBaseDirectory());
		IPath gateInConfig = 
				basedir.append("configuration").append("gatein"); //$NON-NLS-1$ //$NON-NLS-2$
		String s1 = "-Dexo.conf.dir=" + QUOTE //$NON-NLS-1$
				+ gateInConfig.toOSString()
				+ QUOTE + SPACE
				+ "-Dgatein.conf.dir=" + QUOTE  //$NON-NLS-1$
				+ gateInConfig.toOSString() + QUOTE 
				+ SPACE 
				+ "-Dexo.conf.dir.name=gatein" //$NON-NLS-1$
				+ SPACE
				+ "-Dexo.product.developing=true";//$NON-NLS-1$
		return super.getJBossJavaFlags() + s1;
	}
}
