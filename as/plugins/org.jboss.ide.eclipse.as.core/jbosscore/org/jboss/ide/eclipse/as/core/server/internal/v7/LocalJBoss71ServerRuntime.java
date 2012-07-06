/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.server.internal.v7;

import org.eclipse.core.runtime.IPath;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.JavaUtils;

public class LocalJBoss71ServerRuntime extends LocalJBoss7ServerRuntime implements IJBossRuntimeConstants {
	public static final String CONFIG_FILE = "org.jboss.ide.eclipse.as.core.server.internal.v7.CONFIG_FILE"; //$NON-NLS-1$
	public static final String CONFIG_FILE_DEFAULT = "standalone.xml"; //$NON-NLS-1$
	
	@Override
	public String getDefaultRunArgs(IPath serverHome) {
		return DASH + JB7_MP_ARG + SPACE + QUOTE 
				+ serverHome.append(MODULES).toString() + QUOTE 
				+ SPACE + DASH + JB7_JAXPMODULE + SPACE + JB7_JAXP_PROVIDER
				+ SPACE + JB7_STANDALONE_ARG;
	}
		
	@Override
	public String getDefaultRunVMArgs(IPath serverHome) {
		IJBossRuntimeResourceConstants c = new IJBossRuntimeResourceConstants() {};
		IPath bootLog = serverHome.append(c.AS7_STANDALONE).append(c.FOLDER_LOG).append(c.AS7_BOOT_LOG);
		IPath logConfig = serverHome.append(c.AS7_STANDALONE).append(c.CONFIGURATION).append(c.LOGGING_PROPERTIES);
		String serverArg =JavaUtils.supportsServerMode(getVM()) ? SERVER_ARG : "";  //$NON-NLS-1$
		return  serverArg
				+ " -Xms64m" //$NON-NLS-1$
				+ " -Xmx512m" //$NON-NLS-1$
				+ " -XX:MaxPermSize=256m" //$NON-NLS-1$
				+ " -Djava.net.preferIPv4Stack=true" //$NON-NLS-1$
				+ " -Dorg.jboss.resolver.warning=true"  //$NON-NLS-1$
				+ " -Dsun.rmi.dgc.client.gcInterval=3600000" //$NON-NLS-1$
				+ " -Dsun.rmi.dgc.server.gcInterval=3600000" //$NON-NLS-1$
				+ " -Djboss.modules.system.pkgs=org.jboss.byteman"  //$NON-NLS-1$
				+ " -Djava.awt.headless=true" //$NON-NLS-1$
				+ SPACE + QUOTE + SYSPROP + JB7_BOOT_LOG_ARG + EQ + bootLog.toString() + QUOTE 
				+ SPACE + QUOTE + SYSPROP + JB7_LOGGING_CONFIG_FILE + EQ + "file:" + logConfig.toString() + QUOTE //$NON-NLS-1$  
				+ SPACE + QUOTE + SYSPROP + JBOSS_HOME_DIR + EQ + serverHome.toString() + QUOTE;
	}
}
