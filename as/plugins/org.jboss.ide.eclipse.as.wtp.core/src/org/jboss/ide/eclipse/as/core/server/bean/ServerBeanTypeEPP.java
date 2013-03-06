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
package org.jboss.ide.eclipse.as.core.server.bean;

import java.io.File;

import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;

public class ServerBeanTypeEPP extends JBossServerType {
	private static final String JBOSS_PORTLETBRIDGE_PATH = "portletbridge"; //$NON-NLS-1$
	private static final String JBOSS_PORTAL_SAR = "jboss-portal.sar";  //$NON-NLS-1$
	public ServerBeanTypeEPP() {
		super(
				"EPP",//$NON-NLS-1$
				"Enterprise Portal Platform",//$NON-NLS-1$
				asPath(JBOSS_AS_PATH,BIN_PATH,RUN_JAR_NAME),
				new String[]{V4_3, V5_0}, new EPPTypeCondition());
	}
	@Override
	public String getRootToAdapterRelativePath(String version) {
		return "jboss-as";
	}
	public static class EPPTypeCondition extends org.jboss.ide.eclipse.as.core.server.bean.ServerBeanTypeEAP.EAPServerTypeCondition {
		public boolean isServerRoot(File location) {
			if( !super.isServerRoot(location))
				return false;
			
			File portletBridgeFolder = new File(location, JBOSS_PORTLETBRIDGE_PATH);
			File portlalSarFolder = new File(location, 
					asPath(
					JBOSS_AS_PATH, 
					IJBossRuntimeResourceConstants.SERVER,
					IJBossRuntimeResourceConstants.DEFAULT_CONFIGURATION,
					IJBossRuntimeResourceConstants.DEPLOY, JBOSS_PORTAL_SAR));			
			File asStdSystemJar = new File(location,
					asPath(JBOSS_AS_PATH,BIN_PATH, RUN_JAR_NAME));
			boolean pbfIsDir = portletBridgeFolder.exists() && portletBridgeFolder.isDirectory(); 
			boolean psfIsDir = portlalSarFolder.exists() && portlalSarFolder.isDirectory(); 
			boolean sysJarIsFile = asStdSystemJar.exists() && asStdSystemJar.isFile(); 
			return ( pbfIsDir || psfIsDir ) && sysJarIsFile; 
		}
	}

}
