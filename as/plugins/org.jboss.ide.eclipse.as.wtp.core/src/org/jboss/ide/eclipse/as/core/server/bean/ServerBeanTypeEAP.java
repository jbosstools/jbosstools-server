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

import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class ServerBeanTypeEAP extends JBossServerType {
	private static final String TWIDDLE_JAR_NAME = "twiddle.jar"; //$NON-NLS-1$
	public ServerBeanTypeEAP() {
		super(
			"EAP",//$NON-NLS-1$
			"Enterprise Application Platform",//$NON-NLS-1$
			asPath(JBOSS_AS_PATH,BIN_PATH,TWIDDLE_JAR_NAME),
			new String[]{V4_2,V4_3,V5_0,V5_1}, new EAPServerTypeCondition());
	}
	public static abstract class AbstractEAPTypeCondition extends AbstractCondition {
		public String getServerTypeId(String version) {
			// TODO this needs to be split up, does not belong here
			if( V4_2.equals(version)) return IJBossToolingConstants.SERVER_EAP_43;
			if( V4_3.equals(version)) return IJBossToolingConstants.SERVER_EAP_43;
			if( V5_0.equals(version)) return IJBossToolingConstants.SERVER_EAP_50;
			if( V5_1.equals(version)) return IJBossToolingConstants.SERVER_EAP_50;
			if( V5_2.equals(version)) return IJBossToolingConstants.SERVER_EAP_50;
			if( V5_3.equals(version)) return IJBossToolingConstants.SERVER_EAP_50;
			if( V6_0.equals(version)) return IJBossToolingConstants.SERVER_EAP_60;
			
			// TODO eap 6.1 will probably need a different adapter type
			if( V6_1.equals(version)) return IJBossToolingConstants.SERVER_EAP_61;
			return null;
		}
	}
	public static class EAPServerTypeCondition extends AbstractEAPTypeCondition {
		public boolean isServerRoot(File location) {
			File asSystemJar = new File(location, JBossServerType.EAP.getSystemJarPath());
			return asSystemJar.exists() && asSystemJar.isFile();
		}
	}

}
