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

public class ServerBeanTypeWildfly80 extends JBossServerType {
	private static final String AS_RELEASE_MANIFEST_KEY = "JBossAS-Release-Version"; //$NON-NLS-1$
	public ServerBeanTypeWildfly80() {
		super(
				"Wildfly", //$NON-NLS-1$
				"Wildfly Application Server", //$NON-NLS-1$
				asPath("modules","system","layers","base",
						"org","jboss","as","server","main"),
				new String[]{V8_0}, new Wildfly80ServerTypeCondition());
	}
	public static class Wildfly80ServerTypeCondition extends AbstractCondition {
		public boolean isServerRoot(File location) {
			String mainFolder = JBossServerType.WILDFLY80.jbossSystemJarPath;
			return scanFolderJarsForManifestProp(location, mainFolder, AS_RELEASE_MANIFEST_KEY, "8.");
		}
		public String getServerTypeId(String version) {
			if( version.equals(V8_0)) return IJBossToolingConstants.SERVER_WILDFLY_80;
			return null;
		}
	}

}
