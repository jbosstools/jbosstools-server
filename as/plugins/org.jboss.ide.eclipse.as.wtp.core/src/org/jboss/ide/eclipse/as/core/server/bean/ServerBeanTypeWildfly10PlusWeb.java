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

import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanTypeWildfly10Plus.ServerBeanTypeWildfly10PlusCondition;

public class ServerBeanTypeWildfly10PlusWeb extends JBossServerType {
	private static final String WF_100_RELEASE_MANIFEST_KEY = "JBoss-Product-Release-Version"; //$NON-NLS-1$
	public ServerBeanTypeWildfly10PlusWeb(String vers4Char, String serverTypeId) {
		super(
				"WildFly-Web", //$NON-NLS-1$
				"WildFly Application Server", //$NON-NLS-1$
				asPath("modules","system","layers","base",
						"org","jboss","as","server","main"),
				new String[]{vers4Char}, new ServerBeanTypeWildfly10PlusWebCondition(vers4Char, serverTypeId));
	}
	
	protected String getServerTypeBaseName() {
		return getId();
	}
	
	public static class ServerBeanTypeWildfly10PlusWebCondition extends AbstractCondition {
		
		private String vers4Char;
		private String serverTypeId;

		public ServerBeanTypeWildfly10PlusWebCondition(String vers4Char, String serverTypeId) {
			this.vers4Char = vers4Char;
			this.serverTypeId = serverTypeId;
		}


		@Override
		public String getFullVersion(File location, File systemFile) {
			String vers = ServerBeanType.getManifestPropFromJBossModulesFolder(new File[]{new File(location, "modules")}, 
					"org.jboss.as.product", "wildfly-web/dir/META-INF", WF_100_RELEASE_MANIFEST_KEY);
			return ServerBeanTypeWildfly10PlusCondition.getFullVersionWildFly10Impl(vers, this.vers4Char, this.serverTypeId);
		}
		
		public boolean isServerRoot(File location) {
			return getFullVersion(location, null) != null;
		}
		
		public String getServerTypeId(String version) {	
			return serverTypeId;
		}
	}
}
