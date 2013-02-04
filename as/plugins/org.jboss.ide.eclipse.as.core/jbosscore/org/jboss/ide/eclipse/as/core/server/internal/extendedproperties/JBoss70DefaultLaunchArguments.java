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
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;

public class JBoss70DefaultLaunchArguments extends JBossDefaultLaunchArguments {
	public JBoss70DefaultLaunchArguments(IServer s) {
		super(s);
	}
	public JBoss70DefaultLaunchArguments(IRuntime rt) {
		super(rt);
	}
	
	@Override
	public String getStartDefaultProgramArgs() {
		return DASH + JB7_MP_ARG + SPACE + QUOTE 
				+ getServerHome().append(MODULES).toString() + QUOTE 
				+ getLoggingProgramArg()
				+ SPACE + DASH + JB7_JAXPMODULE + SPACE + JB7_JAXP_PROVIDER
				+ SPACE + JB7_STANDALONE_ARG;
	}
	
	protected String getLoggingProgramArg() {
		return SPACE + DASH + JB7_LOGMODULE_ARG + SPACE + JB7_LOGMODULE_DEFAULT;
	}
	
	public String getStartDefaultVMArgs() {
		return getProgramNameArgs() + getServerFlagArgs() +
				getMemoryArgs() + getResolverWarning() +
				getJavaFlags() + getJBossJavaFlags();
	}
	protected String getMemoryArgs() {
		return "-Xms64m -Xmx512m -XX:MaxPermSize=256m "; //$NON-NLS-1$
	}
	protected String getResolverWarning() {
		return "-Dorg.jboss.resolver.warning=true ";  //$NON-NLS-1$
	}
	protected String getJBossJavaFlags() {
		IPath serverHome = getServerHome();

		// don't like typing that big constants interface over and over; its ugly
		IJBossRuntimeResourceConstants c = new IJBossRuntimeResourceConstants() {};

		// TODO this can be changed to the config folder, if such a feature is added
		IPath bootLog = serverHome.append(c.AS7_STANDALONE).append(c.FOLDER_LOG).append(c.AS7_BOOT_LOG);
		IPath logConfig = serverHome.append(c.AS7_STANDALONE).append(c.CONFIGURATION).append(c.LOGGING_PROPERTIES);

		String ret = 
			"-Djava.awt.headless=true" + //$NON-NLS-1$
			SPACE + QUOTE + SYSPROP + JB7_BOOT_LOG_ARG + EQ + bootLog.toString() + QUOTE + 
			SPACE + QUOTE + SYSPROP + JB7_LOGGING_CONFIG_FILE + EQ + 
			"file:" + logConfig.toString() + QUOTE + //$NON-NLS-1$  
			SPACE + QUOTE + SYSPROP + JBOSS_HOME_DIR + EQ + serverHome.toString() + QUOTE + SPACE;
		return ret;
	}
}
