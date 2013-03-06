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

public class ServerBeanTypeSOAPStandalone extends JBossServerType {
	private static final String JBOSS_ESB_PATH = "jboss-esb"; //$NON-NLS-1$
	private static final String SOAP_JBPM_JPDL_PATH = "jbpm-jpdl"; //$NON-NLS-1$
	public ServerBeanTypeSOAPStandalone() {
		super(
				"SOA-P-STD",//$NON-NLS-1$
				"SOA Platform Standalone",//$NON-NLS-1$
				asPath(JBOSS_ESB_PATH,BIN_PATH,RUN_JAR_NAME),
				new String[]{V4_3, V5_0, V5_1 }, new SOAPStandaloneServerTypeCondition());
	}
	@Override
	public String getRootToAdapterRelativePath(String version) {
		return "jboss-esb";
	}

	public static class SOAPStandaloneServerTypeCondition extends org.jboss.ide.eclipse.as.core.server.bean.ServerBeanTypeEAP.EAPServerTypeCondition {
		
		public boolean isServerRoot(File location) {
			File jbpmFolder = new File(location, SOAP_JBPM_JPDL_PATH);
			File soaStdSystemJar = new File(location,JBOSS_ESB_PATH + File.separatorChar + BIN_PATH + File.separatorChar + RUN_JAR_NAME);			
			boolean sysJarIsFile = soaStdSystemJar.exists() && soaStdSystemJar.isFile();
			boolean jbpmFolderIsDir = jbpmFolder.exists() && jbpmFolder.isDirectory(); 
			return jbpmFolderIsDir && sysJarIsFile;
		}
	}
}
