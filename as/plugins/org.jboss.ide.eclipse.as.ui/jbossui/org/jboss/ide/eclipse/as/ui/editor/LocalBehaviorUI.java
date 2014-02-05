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
package org.jboss.ide.eclipse.as.ui.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader;

public class LocalBehaviorUI implements IDeploymentTypeUI {
	private IServerModeUICallback callback;
	public void fillComposite(Composite parent, final IServerModeUICallback callback) {
		//Do Nothing, just verify
		this.callback = callback;
		callback.setErrorMessage(null);
		if( callback.getCallbackType() == callback.EDITOR)
			verify();
		callback.getServer().addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				if( callback.getCallbackType() == callback.EDITOR)
					verify();
			}});
		parent.setLayout(new FillLayout());
		Composite child = new Composite(parent, SWT.None);
	}
	
	private void verify() {
		String behaviourType = DeploymentPreferenceLoader.getCurrentDeploymentMethodTypeId(callback.getServer());
		if( !LocalPublishMethod.LOCAL_PUBLISH_METHOD.equals(behaviourType))
			callback.setErrorMessage(null);
		else {
			ServerExtendedProperties props = ExtendedServerPropertiesAdapterFactory.getServerExtendedProperties(callback.getServer());
			if( props != null ) {
				IStatus status = props.verifyServerStructure();
				callback.setErrorMessage(status.isOK() ? null : status.getMessage());
			}
		}
	}
	
	public void performFinish(IServerModeUICallback callback, IProgressMonitor monitor) throws CoreException {
		// Do Nothing
	}

}
