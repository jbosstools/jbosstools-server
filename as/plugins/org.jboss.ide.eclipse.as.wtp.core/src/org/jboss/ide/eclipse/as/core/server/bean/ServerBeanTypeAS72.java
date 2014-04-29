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


public class ServerBeanTypeAS72 extends JBossServerType {
	private static final String JBAS7_RELEASE_VERSION = "JBossAS-Release-Version"; //$NON-NLS-1$
	protected ServerBeanTypeAS72() {
		super(
				"AS", //$NON-NLS-1$
				"Application Server", //$NON-NLS-1$
				asPath("modules","system","layers","base",
						"org","jboss","as","server","main"),
				new String[]{V7_2}, new AS72ServerTypeCondition());
	}

	public static class AS72ServerTypeCondition extends AbstractCondition {
		public boolean isServerRoot(File location) {
			return checkAS72Version(location, JBAS7_RELEASE_VERSION, V7_2); //$NON-NLS-1$
		}
		protected static boolean checkAS72Version(File location, String property, String propPrefix) {
			String mainFolder = JBossServerType.AS72.systemJarPath;
			return scanFolderJarsForManifestProp(location, mainFolder, property, propPrefix);
		}

		public String getServerTypeId(String version) {
			if( version.equals(V7_2)) return IJBossToolingConstants.SERVER_EAP_61;
			return null;
		}
	}
}
