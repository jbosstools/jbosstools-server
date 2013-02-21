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
import org.jboss.ide.eclipse.as.core.util.IWTPConstants;

public class ServerBeanTypeAS7GateIn extends JBossServerType {
	private static final String AS7_GATE_IN_SYSTEM_JAR_FOLDER = "/gatein/modules/org/gatein/main/";
	private static final String JBAS7_RELEASE_VERSION = "JBossAS-Release-Version"; //$NON-NLS-1$

	protected ServerBeanTypeAS7GateIn() {
		super(
			"GateIn", //$NON-NLS-1$
			"GateIn Application Server", //$NON-NLS-1$
			asPath( "modules","org","jboss","as","server","main"),
			new String[]{V3_3,V3_4 /*, 3_5 is indistinguishable from as7.1 */}, new AS7GateInServerTypeCondition());
	}

	public static class AS7GateInServerTypeCondition extends AbstractCondition {
		public boolean isServerRoot(File location) {
			String mainFolder = JBossServerType.AS7.jbossSystemJarPath;
			boolean isAS7 = scanFolderJarsForManifestProp(location, mainFolder, JBAS7_RELEASE_VERSION, "7.");
			if( isAS7 && getFullVersion(location, null) != null ) {
				return true;
			}
			return false;
		}

		public String getFullVersion(File location, File systemJarFile) {
			File f = new File(location, AS7_GATE_IN_SYSTEM_JAR_FOLDER);
			if( f.exists() ) {
				File[] children = f.listFiles();
				for( int i = 0; i < children.length; i++ ) {
					if( children[i].getName().endsWith(IWTPConstants.EXT_JAR)) {
						String value = getJarProperty(children[i], "Specification-Version");
						return value;
					}
				}
			}
			return null;
		}
		
		public String getServerTypeId(String version) {
			return IJBossToolingConstants.SERVER_AS_71;
		}
	}

}
