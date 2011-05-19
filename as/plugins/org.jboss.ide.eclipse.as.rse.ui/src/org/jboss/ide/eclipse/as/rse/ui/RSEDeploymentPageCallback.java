/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.rse.ui;

import java.beans.PropertyChangeEvent;

import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.util.IConstants;
import org.jboss.ide.eclipse.as.rse.core.RSEUtils;
import org.jboss.ide.eclipse.as.ui.editor.DeploymentModuleOptionCompositeAssistant;
import org.jboss.ide.eclipse.as.ui.editor.DeploymentModuleOptionCompositeAssistant.IDeploymentPageCallback;

public class RSEDeploymentPageCallback implements IDeploymentPageCallback {
	public boolean metadataEnabled() {
		return false;
	}
	public String getServerLocation(IServerWorkingCopy wc) {
		return IConstants.SERVER;
	}

	public String getServerConfigName(IServerWorkingCopy wc) {
		return RSEUtils.getRSEConfigName(wc);
	}
	public void propertyChange(PropertyChangeEvent evt,
			DeploymentModuleOptionCompositeAssistant composite) {
		
		if( composite.getServerRadio().getSelection() && ( 
			evt.getPropertyName().equals( RSEUtils.RSE_SERVER_CONFIG) || 
			evt.getPropertyName().equals( RSEUtils.RSE_SERVER_HOME_DIR))) {
			composite.radioSelected(composite.getServerRadio());
		}
		
	}

}
