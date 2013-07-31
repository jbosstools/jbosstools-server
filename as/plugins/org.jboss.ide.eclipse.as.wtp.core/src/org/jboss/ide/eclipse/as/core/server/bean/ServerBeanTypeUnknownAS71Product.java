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
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class ServerBeanTypeUnknownAS71Product extends JBossServerType {
	public ServerBeanTypeUnknownAS71Product() {
		this( asPath("modules","org","jboss","as","server","main"),
				new UnknownAS71ProductServerTypeCondition());
	}
	
	public ServerBeanTypeUnknownAS71Product(String path, Condition condition) {
		this("AS-Product", "Application Server",path, condition);
	}
	
	protected ServerBeanTypeUnknownAS71Product(String id, String desc, String path, Condition condition) {
		super( id, desc, path, new String[]{}, condition);
	}
	
	
	public static class UnknownAS71ProductServerTypeCondition extends org.jboss.ide.eclipse.as.core.server.bean.ServerBeanTypeEAP6.EAP6ServerTypeCondition {
		public String getServerTypeId(String version) {
			return IJBossToolingConstants.SERVER_EAP_60;
		}
		public boolean isServerRoot(File location) {
			return getFullVersion(location, null) != null;
		}
		public String getFullVersion(File location, File systemJarFile) {
			String productSlot = getSlot(location);
			String[] layers = getLayers(location);
			String[] manifestFolders = getManifestFoldersToFindVersion(productSlot, layers == null ? new String[0] : layers);
			for( int i = 0; i < manifestFolders.length; i++ ) {
				String versionInManifest = getEAP6xVersionNoSlotCheck(location, 
						manifestFolders[i], null, null);
				if( versionInManifest != null )
					return versionInManifest;
			}
			return null;
		}
		
		protected String getSlot(File location) {
			IPath rootPath = new Path(location.getAbsolutePath());
			IPath productConf = rootPath.append("bin/product.conf"); //$NON-NLS-1$
			if( productConf.toFile().exists()) {
				Properties p = JBossServerType.loadProperties(productConf.toFile());
				return (String) p.get("slot"); //$NON-NLS-1$
			}
			return null;
		}
		
		protected String[] getLayers(File location) {
			IPath rootPath = new Path(location.getAbsolutePath());
			IPath layersConf = rootPath.append("modules/layers.conf");
			String[] layers = new String[0];
			if( layersConf.toFile().exists()) {
				Properties p = JBossServerType.loadProperties(layersConf.toFile());
				String layers2 = (String) p.get("layers"); //$NON-NLS-1$
				layers = layers2 == null ? new String[0] : layers2.trim().split(",");
			}
			return layers;
		}
		
		// Provided mostly for subclass to override
		protected String[] getManifestFoldersToFindVersion(String productSlot, String[] layers) {
			return new String[]{ getMetaInfFolderForSlot(productSlot)};
		}
		
		protected String getMetaInfFolderForSlot(String slot) {
			return "modules/org/jboss/as/product/" + slot + "/dir/META-INF"; //$NON-NLS-1$
		}
		
		public String getUnderlyingTypeId(File location, File systemFile) {
			String s = getSlot(location);
			return s == null ? null : s.toUpperCase();
		}

	}
}
