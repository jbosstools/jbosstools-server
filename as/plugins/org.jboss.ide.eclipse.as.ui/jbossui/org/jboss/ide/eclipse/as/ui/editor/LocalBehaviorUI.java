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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListenerManager;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel;

public class LocalBehaviorUI implements IDeploymentTypeUI {
	private IServerModeUICallback callback;
	public void fillComposite(Composite parent, final IServerModeUICallback callback) {
		//Do Nothing, just verify
		this.callback = callback;
		callback.setErrorMessage(null);
		if( callback.getCallbackType() == IServerModeUICallback.EDITOR)
			verify();
		parent.setLayout(new FillLayout());
		Composite child = new Composite(parent, SWT.None);
		
		addListeners(parent);
	}
	
	private void addListeners(Composite parent) {
		// Add listeners for the server
		final PropertyChangeListener pcl = new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				if( callback.getCallbackType() == IServerModeUICallback.EDITOR)
					verify();
			}
		};
		callback.getServer().addPropertyChangeListener(pcl);
		
		
		// Add listeners for the runtime
		final UnitedServerListener changeToRuntimeDetailsListener = new UnitedServerListener() {
			public void runtimeChanged(IRuntime runtime) {
				if( callback.getCallbackType() == IServerModeUICallback.EDITOR) {
					Display.getDefault().asyncExec(() -> verify());
				}
			}
			public boolean canHandleRuntime(IRuntime runtime) {
				return runtime != null && runtime.equals(callback.getServer().getRuntime());
			}
		};
		UnitedServerListenerManager.getDefault().addListener(changeToRuntimeDetailsListener);
		parent.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				UnitedServerListenerManager.getDefault().removeListener(changeToRuntimeDetailsListener);
				callback.getServer().removePropertyChangeListener(pcl);
			}
		});
	}
	
	private void verify() {
		String behaviourType = ServerProfileModel.getProfile(callback.getServer());
		if( !ServerProfileModel.DEFAULT_SERVER_PROFILE.equals(behaviourType))
			callback.setErrorMessage(null);
		else {
			ServerExtendedProperties props = ExtendedServerPropertiesAdapterFactory.getServerExtendedProperties(callback.getServer());
			if( props != null ) {
				IStatus status = props.verifyServerStructure();
				callback.setErrorMessage(status.isOK() ? null : status.getMessage());
			}
		}
	}
}
