/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
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
import java.util.Arrays;
import java.util.List;

import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanTypeUnknownAS72Product.UnknownAS72ProductServerTypeCondition;

/**
 * Verifies the installation has:
 *   1) slot=soa
 *   2) layers contains soa and sramp
 *   3) modules/system/layers/sramp/org/jboss/as/product/sramp/dir/META-INF/MANIFEST.MF
 *         has a key JBoss-Product-Release-Version that begins with "6."
 * @since 3.0
 */
public class ServerBeanTypeFSW6 extends ServerBeanTypeUnknownAS71Product {
	public ServerBeanTypeFSW6() {
		super("FSW", "JBoss Fuse Source Works", 
				asPath("modules","system","layers","base","org","jboss","as","server","main"),
				new String[] { V6_1 },
				new FSW6Condition());
	}
	
	@Override
	public String getServerBeanName(File root) {
		return "jboss-fsw-" + condition.getFullVersion(root, null);
	}
	public static class FSW6Condition extends UnknownAS72ProductServerTypeCondition {
		public String getFullVersion(File location, File systemJarFile) {
			String productSlot = getSlot(location);
			if( "soa".equalsIgnoreCase(productSlot)) {
				List<String> layers = Arrays.asList(getLayers(location));
				if( layers.contains("soa") && layers.contains("sramp")) {
					String srampProductDir = "org.jboss.as.product.sramp.dir";
					File[] modules = new File[]{new File(location, "modules")};
					String vers = ServerBeanType.getManifestPropFromJBossModulesFolder(modules, srampProductDir, 
							"META-INF", "JBoss-Product-Release-Version");
					if( vers.startsWith("6.0"))
						return vers;
				}
			}
			return null;
		}
		public String getUnderlyingTypeId(File location, File systemFile) {
			if( getFullVersion(location, systemFile) != null ) 
				return "FSW";
			return null;
		}
	}
}
