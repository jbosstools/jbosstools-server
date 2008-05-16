/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.tools.wst.server.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.ServerViewProvider;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class ExtensionManager {
	private static ExtensionManager instance;
	public static ExtensionManager getDefault() {
		if( instance == null ) 
			instance = new ExtensionManager();
		return instance;
	}
	
	private ServerViewProvider[] serverViewExtensions;
	public ServerViewProvider[] getAllServerViewProviders() {
		if( serverViewExtensions == null ) 
			loadAllServerViewProviders();
		Arrays.sort(serverViewExtensions, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				if( arg0 instanceof ServerViewProvider && arg1 instanceof ServerViewProvider) {
					return ((ServerViewProvider)arg0).getWeight() - ((ServerViewProvider)arg1).getWeight();
				}
				return 0;
			}
		});
		return serverViewExtensions;
	}
	private void loadAllServerViewProviders() {
		// Create the extensions from the registry
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = registry.getConfigurationElementsFor(JBossServerUIPlugin.PLUGIN_ID, "ServerViewExtension");
		ArrayList list = new ArrayList();
		for( int i = 0; i < elements.length; i++ ) {
			try {
				list.add(new ServerViewProvider(elements[i]));
			} catch(Exception e) {
				String msg = "Server View Provider (" + elements[i].getAttribute(ServerViewProvider.ID_LABEL) + ") failed to load";
				IStatus status = new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, msg, e);
				JBossServerUIPlugin.getDefault().getLog().log(status);
			}
		}
		serverViewExtensions = (ServerViewProvider[]) list.toArray(new ServerViewProvider[list.size()]);
	}

}
