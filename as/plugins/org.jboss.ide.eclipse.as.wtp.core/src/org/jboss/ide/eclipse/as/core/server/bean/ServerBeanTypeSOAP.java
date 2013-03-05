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
import java.util.regex.Pattern;

public class ServerBeanTypeSOAP extends JBossServerType {
	private static final String TWIDDLE_JAR_NAME = "twiddle.jar"; //$NON-NLS-1$
	private static final String SOAP_JBPM_JPDL_PATH = "jbpm-jpdl"; //$NON-NLS-1$
	public ServerBeanTypeSOAP() {
		super(
		"SOA-P",//$NON-NLS-1$
		"SOA Platform",//$NON-NLS-1$
		asPath(JBOSS_AS_PATH,BIN_PATH,TWIDDLE_JAR_NAME),
		new String[]{V4_3, V5_0, V5_1 }, new SOAPServerTypeCondition());
	}

	public static class SOAPServerTypeCondition extends org.jboss.ide.eclipse.as.core.server.bean.ServerBeanTypeEAP.EAPServerTypeCondition{
		public boolean isServerRoot(File location) {
			File jbpmFolder = new File(location, SOAP_JBPM_JPDL_PATH);
			return super.isServerRoot(location) && jbpmFolder.exists() && jbpmFolder.isDirectory();
		}
		
		public String getFullVersion(File location, File systemFile) {
			String fullVersion = ServerBeanLoader.getFullServerVersionFromZip(systemFile);
			if (fullVersion != null && fullVersion.length() >= 5) {
				// SOA-P 5.2, SOA-P 5.3 ...
				String check = fullVersion.substring(0,5); 
				Pattern pattern = Pattern.compile("5\\.1\\.[1-9]");
				Pattern pattern531 = Pattern.compile("5\\.2\\.[0-9]");
				if (pattern.matcher(check).matches() || pattern531.matcher(check).matches()) {
					String runJar = JBossServerType.JBOSS_AS_PATH + File.separatorChar + 
						JBossServerType.BIN_PATH+ File.separatorChar + JBossServerType.RUN_JAR_NAME;
					fullVersion = ServerBeanLoader.getFullServerVersionFromZip(new File(location, runJar));
				}
			}
			return fullVersion;
		}
	}
}
