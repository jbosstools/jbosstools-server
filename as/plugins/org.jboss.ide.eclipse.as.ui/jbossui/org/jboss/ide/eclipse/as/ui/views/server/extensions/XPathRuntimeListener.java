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
package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeLifecycleListener;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathCategory;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathModel;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;

public class XPathRuntimeListener extends UnitedServerListener {
	public static XPathRuntimeListener instance;
	public static XPathRuntimeListener getDefault() {
		if( instance == null )
			instance = new XPathRuntimeListener();
		return instance;
	}
	
	public void runtimeChanged(IRuntime runtime) {
		IViewPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("org.eclipse.wst.server.ui.ServersView");
		IServer[] allServers = ServerCore.getServers();
		for( int i = 0; i < allServers.length; i++ ) {
			if( runtime.equals(allServers[i].getRuntime())) {
				XPathCategory[] cats = XPathModel.getDefault().getCategories(allServers[i]);
				for( int j = 0; j < cats.length; j++ ) {
					cats[j].clearCache();
				}
			}
			if( part != null && part instanceof CommonNavigator) {
				((CommonNavigator)part).getCommonViewer().refresh(allServers[i]);
			}
		}
	}
}
