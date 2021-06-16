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
import java.util.ArrayList;
import java.util.Collections;

import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class ServerBeanTypeWildfly10Plus extends JBossServerType {
	private static final String WF_100_RELEASE_MANIFEST_KEY = "JBoss-Product-Release-Version"; //$NON-NLS-1$
	public ServerBeanTypeWildfly10Plus(String version4Char, String serverTypeId) {
		this(version4Char, serverTypeId, "wildfly-full");
	}
	public ServerBeanTypeWildfly10Plus(String version4Char, String serverTypeId, String productFolder) {
		super(
				"WildFly", //$NON-NLS-1$
				"WildFly Application Server", //$NON-NLS-1$
				asPath("modules","system","layers","base",
						"org","jboss","as","server","main"),
				new String[]{version4Char}, 
				new ServerBeanTypeWildfly10PlusCondition(version4Char, serverTypeId, productFolder));
	}
	protected String getServerTypeBaseName() {
		return getId();
	}
	
	public static class ServerBeanTypeWildfly10PlusCondition extends AbstractCondition {
		
		private String version4Char;
		private String serverTypeId;
		private String productFolder;
		public ServerBeanTypeWildfly10PlusCondition(String version4Char, 
				String serverTypeId, String productFolder) {
			this.version4Char = version4Char;
			this.serverTypeId = serverTypeId;
			this.productFolder = productFolder;
		}


		@Override
		public String getFullVersion(File location, File systemFile) {
			String vers = ServerBeanType.getManifestPropFromJBossModulesFolder(new File[]{new File(location, "modules")}, 
					"org.jboss.as.product", productFolder + "/dir/META-INF", WF_100_RELEASE_MANIFEST_KEY);
			return getFullVersionWildFly10Impl(vers, this.version4Char, this.serverTypeId);
		}

		public static String getFullVersionWildFly10Impl(String vers, 
				String version4Char, String serverTypeId) {
			if( vers == null )
				return null;
			
			String targetMajorMinor = vers.substring(0,2) + vers.substring(3,4);
			String conditionMajorMinor = version4Char.substring(0,2) + version4Char.substring(3,4);
			
			int conditionMajorMinorInt = Integer.parseInt(conditionMajorMinor);
			int targetMajorMinorInt = Integer.parseInt(targetMajorMinor);
			if( conditionMajorMinorInt < 240 || targetMajorMinorInt < 240 ) {
				// Old logic
				if( vers.startsWith(version4Char.substring(0,3))) {
					return vers;
				}
				return null;
			}
			
			if(isMyResponsibility(conditionMajorMinorInt, targetMajorMinorInt, serverTypeId)) {
				return vers;
			}
			
			return null;
		}
		
		private static boolean isMyResponsibility(int conditionMajorMinorInt, 
				int targetMajorMinorInt, String serverTypeId) {
			// Find all wildfly server types above me
			ArrayList<Integer> possibleAdapterVersions = new ArrayList<>();
			String[] serverTypes = IJBossToolingConstants.ALL_JBOSS_SERVERS;
			String v = null;
			for( int i = 0; i < serverTypes.length; i++ ) {
				if( serverTypes[i].startsWith(IJBossToolingConstants.WF_SERVER_PREFIX)) {
					v = serverTypes[i].substring(IJBossToolingConstants.WF_SERVER_PREFIX.length());
					int vInt = Integer.parseInt(v);
					if( vInt >= conditionMajorMinorInt && vInt <= targetMajorMinorInt)
						possibleAdapterVersions.add(vInt);
				}
			}
			
			Collections.sort(possibleAdapterVersions);
			Collections.reverse(possibleAdapterVersions);
			if( possibleAdapterVersions.size() == 0 ) 
				return false;
			int last = possibleAdapterVersions.get(0);
			if( (IJBossToolingConstants.WF_SERVER_PREFIX + last).equals(serverTypeId))
				return true;
			return false;
		}

		public boolean isServerRoot(File location) {
			return getFullVersion(location, null) != null;
		}
		
		public String getServerTypeId(String version) {	
			return serverTypeId;
		}
	}
}
