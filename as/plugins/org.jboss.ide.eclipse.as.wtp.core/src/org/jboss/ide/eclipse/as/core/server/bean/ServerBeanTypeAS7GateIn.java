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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.IWTPConstants;

public class ServerBeanTypeAS7GateIn extends JBossServerType {
	private static final String AS7_GATE_IN_SYSTEM_JAR_FOLDER = "/gatein/modules/org/gatein/main/";
	private static final String GATEIN_35_PROPERTY_FILE = "gatein/extensions/gatein-wsrp-integration.ear/extension-war.war/META-INF/maven/org.gatein.integration/extension-war/pom.properties";
	private static final String VERSION_PROP = "version";
	
	private static final String JBAS7_RELEASE_VERSION = "JBossAS-Release-Version"; //$NON-NLS-1$

	protected ServerBeanTypeAS7GateIn() {
		super(
			"GateIn", //$NON-NLS-1$
			"GateIn Application Server", //$NON-NLS-1$
			asPath( "modules","org","jboss","as","server","main"),
			new String[]{V3_3,V3_4, V3_5, "3.6"}, new AS7GateInServerTypeCondition());
	}

	public static class AS7GateInServerTypeCondition extends AbstractCondition {
		public boolean isServerRoot(File location) {
			String mainFolder = JBossServerType.AS7.systemJarPath;
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
			
			File f2 = new File(location, GATEIN_35_PROPERTY_FILE);
			if( f2.exists()) {
				try {
					Properties p = new Properties();
					p.load(new FileInputStream(f2));
					return p.getProperty(VERSION_PROP);
				} catch(IOException ioe) {
					// ignore
				}
			}
			return null;
		}
		
		public String getServerTypeId(String version) {
			return IJBossToolingConstants.SERVER_AS_71;
		}
	}

}
