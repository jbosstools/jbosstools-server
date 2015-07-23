/******************************************************************************* 
 * Copyright (c) 2015 Red Hat, Inc. 
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

public class ServerBeanTypeWildfly90Web extends JBossServerType {
	private static final String WF_90_RELEASE_MANIFEST_KEY = "JBoss-Product-Release-Version"; //$NON-NLS-1$
	public ServerBeanTypeWildfly90Web() {
		super(
				"WildFly-Web", //$NON-NLS-1$
				"WildFly Application Server", //$NON-NLS-1$
				asPath("modules","system","layers","base",
						"org","jboss","as","server","main"),
				new String[]{"9.0"}, new Wildfly90WebServerTypeCondition());
	}
	
	protected String getServerTypeBaseName() {
		return getId();
	}
	
	public static class Wildfly90WebServerTypeCondition extends AbstractCondition {
		
		@Override
		public String getFullVersion(File location, File systemFile) {
			String vers = ServerBeanType.getManifestPropFromJBossModulesFolder(new File[]{new File(location, "modules")}, 
					"org.jboss.as.product", "wildfly-web/dir/META-INF", WF_90_RELEASE_MANIFEST_KEY);
			if( vers != null && vers.startsWith("9.")) {
				return vers;
			}
			return null;
		}

		
		public boolean isServerRoot(File location) {
			return getFullVersion(location, null) != null;
		}
		
		public String getServerTypeId(String version) {	
			// Just return adapter type wf8 until we discover incompatibility. 
			return IJBossToolingConstants.SERVER_WILDFLY_90;
		}
	}
}
