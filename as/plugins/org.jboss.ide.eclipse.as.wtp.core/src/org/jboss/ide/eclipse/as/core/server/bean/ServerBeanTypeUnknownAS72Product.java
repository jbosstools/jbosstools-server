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

public class ServerBeanTypeUnknownAS72Product extends ServerBeanTypeUnknownAS71Product {
	public ServerBeanTypeUnknownAS72Product() {
		super("EAP-Product", "EAP-Based Product", 
				asPath("modules","system","layers","base","org","jboss","as","server","main"), 
				new UnknownAS72ProductServerTypeCondition());
	}
	public static class UnknownAS72ProductServerTypeCondition extends UnknownAS71ProductServerTypeCondition {

		@Override
		public boolean isServerRoot(File location) {
			return server72OrHigher(location) && getFullVersion(location, null) != null;
		}
		
		protected boolean server72OrHigher(File loc) {
			File[] mods = new File[]{new File(loc, "modules")};
			String serverVersion = getManifestPropFromJBossModules(mods, "org.jboss.as.server", "main", "JBoss-Product-Release-Version");
			if( serverVersion == null ) {
				serverVersion = getManifestPropFromJBossModules(mods, "org.jboss.as.server", "main", "Implementation-Version");
			}
			if( serverVersion != null && serverVersion.length() > 3) {
				if( serverVersion.startsWith("7.") && "2".compareTo(""+serverVersion.charAt(2)) <= 0) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		public String getServerTypeId(String version) {
			return IJBossToolingConstants.SERVER_EAP_61;
		}
	}
}
