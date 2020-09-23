/******************************************************************************* 
 * Copyright (c) 2020 Red Hat, Inc. 
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

public class ServerBeanTypeWildfly210 extends JBossServerType {
	private static final String WF_100_RELEASE_MANIFEST_KEY = "JBoss-Product-Release-Version"; //$NON-NLS-1$
	public ServerBeanTypeWildfly210() {
		super(
				"WildFly", //$NON-NLS-1$
				"WildFly Application Server", //$NON-NLS-1$
				asPath("modules","system","layers","base",
						"org","jboss","as","server","main"),
				new String[]{"21.0"}, new Wildfly210ServerTypeCondition());
	}
	
	@Override
	protected String getServerTypeBaseName() {
		return getId();
	}
	
	public static class Wildfly210ServerTypeCondition extends AbstractCondition {
		
		@Override
		public String getFullVersion(File location, File systemFile) {
			String vers = ServerBeanType.getManifestPropFromJBossModulesFolder(new File[]{new File(location, "modules")}, 
					"org.jboss.as.product", "main/dir/META-INF", WF_100_RELEASE_MANIFEST_KEY);
			if( vers != null && vers.startsWith("21.")) {
				return vers;
			}
			return null;
		}

		@Override
		public boolean isServerRoot(File location) {
			return getFullVersion(location, null) != null;
		}
		@Override
		public String getServerTypeId(String version) {	
			return IJBossToolingConstants.SERVER_WILDFLY_210;
		}
	}
}
