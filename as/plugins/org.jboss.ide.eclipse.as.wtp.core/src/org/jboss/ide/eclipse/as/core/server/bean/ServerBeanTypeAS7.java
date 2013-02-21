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


public class ServerBeanTypeAS7 extends JBossServerType {
	private static final String JBAS7_RELEASE_VERSION = "JBossAS-Release-Version"; //$NON-NLS-1$
	protected ServerBeanTypeAS7() {
		super(
			"AS", //$NON-NLS-1$
			"Application Server", //$NON-NLS-1$
			asPath( "modules","org","jboss","as","server","main"),
			new String[]{V7_0,V7_1}, new AS7ServerTypeCondition());
	}
	public static class AS7ServerTypeCondition extends AbstractCondition {
		public boolean isServerRoot(File location) {
			String mainFolder = JBossServerType.AS7.jbossSystemJarPath;
			return scanFolderJarsForManifestProp(location, mainFolder, JBAS7_RELEASE_VERSION, "7.");
		}
		public String getServerTypeId(String version) {
			if( version.equals(V7_0)) return IJBossToolingConstants.SERVER_AS_70;
			if( version.equals(V7_1)) return IJBossToolingConstants.SERVER_AS_71;
			if( version.equals(V7_2)) return IJBossToolingConstants.SERVER_AS_71;
			return null;
		}
	}

}
