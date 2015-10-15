/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.jmx.local.internal.ui;

import org.eclipse.swt.graphics.Image;
import org.jboss.tools.common.jdt.debug.tools.ToolsCore;
import org.jboss.tools.common.jdt.debug.tools.ToolsCoreException;
import org.jboss.tools.jmx.jvmmonitor.core.IActiveJvm;
import org.jboss.tools.jmx.local.internal.Activator;
import org.jboss.tools.jmx.local.internal.LocalVMSharedImages;
import org.jboss.tools.jmx.local.ui.JVMLabelProviderDelegate;

public class MavenLabelProvider implements JVMLabelProviderDelegate {
	static final String ECLIPSE_MAVEN_PROCESS_PREFIX  = "-DECLIPSE_PROCESS_NAME='";
	static final String ECLIPSE_MAVEN_PROCESS_POSTFIX = "'";
	static final String MAVEN_PREFIX = "org.codehaus.plexus.classworlds.launcher.Launcher";
	public boolean accepts(IActiveJvm jvm) {
		return jvm.getMainClass().startsWith(MAVEN_PREFIX);
	}
	public Image getImage(IActiveJvm connection) {
		return Activator.getDefault().getSharedImages().image(LocalVMSharedImages.CONTAINER_GIF);
	}
	public String getDisplayString(IActiveJvm jvm) {
		String displayName = "maven" + jvm.getMainClass().substring(MAVEN_PREFIX.length());
		if (!jvm.isRemote()) {
			String pInfo = null;
			try {
				pInfo= ToolsCore.getMainArgs(jvm.getHost().getName(), jvm.getPid());
			} catch(ToolsCoreException tce) {
				// Ignore
			}
			if (pInfo != null) {
				int start = pInfo.indexOf(ECLIPSE_MAVEN_PROCESS_PREFIX);
				if (start != -1) {
					int end   = pInfo.indexOf(ECLIPSE_MAVEN_PROCESS_POSTFIX, start+ECLIPSE_MAVEN_PROCESS_PREFIX.length()+1);
					if (end != -1) {
						displayName = pInfo.substring(start + ECLIPSE_MAVEN_PROCESS_PREFIX.length(), end);
					} else {
						displayName = pInfo.substring(start + ECLIPSE_MAVEN_PROCESS_PREFIX.length());
					}
				}
			}
		}
		return displayName;
	}
}