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

import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanTypeEAP.AbstractEAPTypeCondition;


public class ServerBeanTypeEAPStandalone extends JBossServerType {
	private static final String TWIDDLE_JAR_NAME = "twiddle.jar"; //$NON-NLS-1$
	public ServerBeanTypeEAPStandalone() {
		super(
			"EAP_STD",//$NON-NLS-1$
			"Enterprise Application Platform",//$NON-NLS-1$
			asPath(BIN_PATH, TWIDDLE_JAR_NAME),
			new String[]{V4_2,V4_3,V5_0,V5_1}, new EAPStandaloneServerTypeCondition());
	}

	public static class EAPStandaloneServerTypeCondition extends AbstractEAPTypeCondition {
		public boolean isServerRoot(File location) {
			File asSystemJar = new File(location, JBossServerType.EAP_STD.getSystemJarPath());
			if (asSystemJar.exists() && asSystemJar.isFile()) {
				String title = getJarProperty(asSystemJar, IMPLEMENTATION_TITLE);
				boolean isEAP = title != null && title.contains("EAP"); //$NON-NLS-1$
				return isEAP;
			}
			return false;
		}
	}
}
