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
