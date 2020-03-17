/******************************************************************************* 
 * Copyright (c) 2019 Red Hat, Inc. 
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

public class ServerBeanTypeWildfly190Web extends JBossServerType {
	private static final String WF_100_RELEASE_MANIFEST_KEY = "JBoss-Product-Release-Version"; //$NON-NLS-1$
	public ServerBeanTypeWildfly190Web() {
		super(
				"WildFly-Web", //$NON-NLS-1$
				"WildFly Application Server", //$NON-NLS-1$
				asPath("modules","system","layers","base",
						"org","jboss","as","server","main"),
				new String[]{"19.0"}, new Wildfly190WebServerTypeCondition());
	}
	
	@Override
	protected String getServerTypeBaseName() {
		return getId();
	}
	
	public static class Wildfly190WebServerTypeCondition extends AbstractCondition {
		
		@Override
		public String getFullVersion(File location, File systemFile) {
			String vers = ServerBeanType.getManifestPropFromJBossModulesFolder(new File[]{new File(location, "modules")}, 
					"org.jboss.as.product", "wildfly-web/dir/META-INF", WF_100_RELEASE_MANIFEST_KEY);
			if( vers != null && vers.startsWith("19.")) {
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
			return IJBossToolingConstants.SERVER_WILDFLY_190;
		}
	}
}
