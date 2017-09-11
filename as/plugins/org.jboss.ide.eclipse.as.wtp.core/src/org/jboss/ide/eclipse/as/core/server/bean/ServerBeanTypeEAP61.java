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

public class ServerBeanTypeEAP61 extends ServerBeanTypeEnterprise {
	private static final String EAP61_DIR_META_INF = "modules/system/layers/base/org/jboss/as/product/eap/dir/META-INF"; //$NON-NLS-1$
	public ServerBeanTypeEAP61() {
		super(
				"EAP", //$NON-NLS-1$
				"Enterprise Application Platform", //$NON-NLS-1$
				asPath("modules","system","layers","base",
						"org","jboss","as","server","main"),
				new String[]{V6_1, V6_2, "6.3", "6.4"}, new EAP61ServerTypeCondition());
	}
	public static class EAP61ServerTypeCondition extends org.jboss.ide.eclipse.as.core.server.bean.ServerBeanTypeEAP6.EAP6ServerTypeCondition {
		@Override
		public String getServerTypeId(String version) {
			return IJBossToolingConstants.SERVER_EAP_61;
		}
		public boolean isServerRoot(File location) {
			return getEAP6xVersion(location, EAP61_DIR_META_INF, "6.", "eap", "EAP") != null; //$NON-NLS-1$
		}
		public String getFullVersion(File location, File systemJarFile) {
			return getEAP6xVersion(location, EAP61_DIR_META_INF, "6.", "eap", "EAP"); //$NON-NLS-1$
		}
	}

}
