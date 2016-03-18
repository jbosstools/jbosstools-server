/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.core.console;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.jboss.ide.eclipse.as.core.server.IServerConsoleWriter;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;

public class ServerConsoleModel {
	private static ServerConsoleModel model = new ServerConsoleModel();
	public static ServerConsoleModel getDefault() {
		return model;
	}
	private IServerConsoleWriter consoleWriter;
	
	public IServerConsoleWriter getConsoleWriter() {
		if( consoleWriter == null ) {
			loadConsoleWriter();
		}
		return consoleWriter;
	}
	
	private void loadConsoleWriter() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(ASWTPToolsPlugin.PLUGIN_ID, "consoleWriter"); //$NON-NLS-1$
		for( int i = 0; i < cf.length; i++ ) {
			try {
				IServerConsoleWriter l = (IServerConsoleWriter)cf[i].createExecutableExtension("class");
				consoleWriter = l;
			} catch( CoreException e ) {
				ASWTPToolsPlugin.pluginLog().logError(e);
			}
		}
		
	}
}
