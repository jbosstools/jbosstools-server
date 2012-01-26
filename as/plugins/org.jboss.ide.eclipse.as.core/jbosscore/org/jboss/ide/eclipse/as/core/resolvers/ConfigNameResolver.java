/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 

package org.jboss.ide.eclipse.as.core.resolvers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
/**
 * These classes are primarily geared for as6-and-below
 * and are meant to serve as a dereferencing point to discover 
 * the configuration name and it's parent directory in the case
 * other portions of the tool, such as xpaths or classpaths, 
 * want to use these variables in their saved strings. 
 * 
 * They are not really geared for use with as7 and above, and their
 * behaviour with regards to as7 and above are officially undefined, 
 * though unofficially both should return the empty string in these cases 
 * @author rob
 *
 */
public class ConfigNameResolver implements IDynamicVariableResolver {

	public String resolveValue(IDynamicVariable variable, String argument)
			throws CoreException {
		if( variable.getName().equals("jboss_config"))  //$NON-NLS-1$
			return handleConfig(variable, argument);
		if( variable.getName().equals("jboss_config_dir")) //$NON-NLS-1$
			return handleConfigDir(variable, argument);
		return null;
	}
	
	protected String handleConfig(IDynamicVariable variable, String argument) {
		IJBossServerRuntime ajbsrt = findJBossServerRuntime(argument);
		if( ajbsrt != null ) {
			String config = null;
			if( ajbsrt != null ) 
				config = ajbsrt.getJBossConfiguration();
			if( config != null )
				return config;
		}
		return null;
	}
	protected String handleConfigDir(IDynamicVariable variable, String argument) {
		IJBossServerRuntime ajbsrt = findJBossServerRuntime(argument);
		if( ajbsrt != null ) {
			String config = null;
			if( ajbsrt != null ) 
				config = ajbsrt.getConfigLocationFullPath().append(ajbsrt.getJBossConfiguration()).toString();
			if( config != null )
				return config;
		}
		return null;
	}
	
	private IJBossServerRuntime findJBossServerRuntime(String serverName) {
		IServer[] servers = ServerCore.getServers();
		for( int i = 0; i < servers.length; i++ ) {
			if( servers[i].getName().equals(serverName)) {
				return  (IJBossServerRuntime) servers[i].getRuntime()
						.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
			}
		}
		IRuntime[] runtimes = ServerCore.getRuntimes();
		for( int i = 0; i < runtimes.length; i++ ) {
			if( runtimes[i].getName().equals(serverName)) {
				return (IJBossServerRuntime) runtimes[i]
						.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
			}
		}
		return null;
		
	}
	
}
