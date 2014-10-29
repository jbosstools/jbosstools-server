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
package org.jboss.ide.eclipse.as.rse.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.ui.editor.internal.JBossDeploymentOptionsComposite;
import org.jboss.ide.eclipse.as.ui.editor.internal.NoTempDeploymentPageController;


public class RSE7xDeploymentPageController extends NoTempDeploymentPageController {
	protected JBossDeploymentOptionsComposite createServerDeploymentOptions(Composite parent) {
		return new JBossDeploymentOptionsComposite(parent, this) {
			protected boolean showTempDeployText() {
				return false;
			}
			public IStatus[] validate() {
				if( supportsExposeManagementCheckbox()) {
					boolean exposePort = getPage().getServer().getAttribute(IJBossToolingConstants.EXPOSE_MANAGEMENT_SERVICE, false);
					if( !exposePort ) {
						return new IStatus[]{
								new Status(IStatus.WARNING, RSEUIPlugin.PLUGIN_ID, "Your server is not currently configured to expose the management port. Deploying to custom remote folders may not work as expected if the remote server is not configured to scan those folders for deployments")
						};
					}
				}
				return new IStatus[0];
			}
			protected boolean supportsExposeManagementCheckbox() {
				JBossExtendedProperties props = getExtendedProperties();
				return props == null ? false : props.runtimeSupportsExposingManagement();
			}
			protected JBossExtendedProperties getExtendedProperties() {
				IServerWorkingCopy wc = getPage().getServer();
				JBossExtendedProperties props = (JBossExtendedProperties)wc
						.loadAdapter(JBossExtendedProperties.class, 
									 new NullProgressMonitor());
				return props;
			}
		};
	}
}