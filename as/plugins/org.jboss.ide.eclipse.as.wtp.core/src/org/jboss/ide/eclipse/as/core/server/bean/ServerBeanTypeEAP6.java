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
import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanTypeEAP.AbstractEAPTypeCondition;

public class ServerBeanTypeEAP6 extends JBossServerType {
	private static final String EAP60_DIR_META_INF = "modules/org/jboss/as/product/eap/dir/META-INF"; //$NON-NLS-1$
	public ServerBeanTypeEAP6() {
		super(
				"EAP", //$NON-NLS-1$
				"Enterprise Application Platform", //$NON-NLS-1$
				asPath("modules", "org", "jboss", "as", "server", "main"),
				new String[]{V6_0}, new EAP6ServerTypeCondition());
	}
	public static class EAP6ServerTypeCondition extends AbstractEAPTypeCondition {
		
		public boolean isServerRoot(File location) {
			return getEAP6xVersion(location, EAP60_DIR_META_INF, "6.", "eap", "EAP") != null; //$NON-NLS-1$
		}
		public String getFullVersion(File location, File systemJarFile) {
			return getEAP6xVersion(location, EAP60_DIR_META_INF, "6.", "eap", "EAP"); //$NON-NLS-1$
		}
		/**
		 * Get the eap6-style version string, or null if not found.
		 * This method will check for a product.conf, a corresponding 'slot', 
		 * and a proper manifest.mf file to read in that product slot. 
		 * 
		 * @param location
		 * @param versionPrefix
		 * @return
		 */
		public static String getEAP6xVersion(File location,  String metaInfPath,
				String versionPrefix, String slot, String releaseName) {
			IPath rootPath = new Path(location.getAbsolutePath());
			IPath productConf = rootPath.append("bin/product.conf"); //$NON-NLS-1$
			if( productConf.toFile().exists()) {
				Properties p = JBossServerType.loadProperties(productConf.toFile());
				String product = (String) p.get("slot"); //$NON-NLS-1$
				if(slot.equals(product)) { //$NON-NLS-1$
					return getEAP6xVersionNoSlotCheck(location, metaInfPath, versionPrefix, releaseName);
				}
			}
			return null;
		}
		public static String getEAP6xVersionNoSlotCheck(File location,  String metaInfPath,
				String versionPrefix, String releaseName) {
			IPath rootPath = new Path(location.getAbsolutePath());
			IPath eapDir = rootPath.append(metaInfPath); //$NON-NLS-1$
			if( eapDir.toFile().exists()) {
				IPath manifest = eapDir.append("MANIFEST.MF"); //$NON-NLS-1$
				Properties p2 = JBossServerType.loadProperties(manifest.toFile());
				String type = p2.getProperty("JBoss-Product-Release-Name"); //$NON-NLS-1$
				String version = p2.getProperty("JBoss-Product-Release-Version"); //$NON-NLS-1$
				boolean matchesName = releaseName == null || releaseName.equals(type);
				boolean matchesVersion = versionPrefix == null || version.startsWith(versionPrefix);
				if( matchesName && matchesVersion )
					return version;
			}
			return null;
		}
	}

}
