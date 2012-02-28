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

import org.eclipse.core.internal.variables.StringSubstitutionEngine;
import org.eclipse.core.internal.variables.StringVariableManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7ServerRuntime;
/**
 * These classes are primarily geared for as6-and-below
 * and are meant to serve as a dereferencing point to discover 
 * the configuration name and it's parent directory in the case
 * other portions of the tool, such as xpaths or classpaths, 
 * want to use these variables in their saved strings. 
 */
public class ConfigNameResolver implements IDynamicVariableResolver {

	public static final String JBOSS_CONFIG = "jboss_config"; //$NON-NLS-1$
	public static final String JBOSS_CONFIG_DIR = "jboss_config_dir"; //$NON-NLS-1$
	public static final String JBOSS_AS7_CONFIG_FILE = "jboss_as7_config_file"; //$NON-NLS-1$
	
	/*
	 * entry points to fill out the server name in these variable substitutions
	 */
	
	/**
	 * Kick off the substitution engine for this string, doing 
	 * server-related replacements (adding variables) beforehand. 
	 * 
	 * @param dir1
	 * @param serverName
	 * @return
	 */
	public String performSubstitutions(String dir1, String serverName) {
		String dir2 = null;
		if( dir1 != null ) {
			dir2 = replace(dir1, ConfigNameResolver.JBOSS_CONFIG, serverName);
			dir2 = replace(dir2, ConfigNameResolver.JBOSS_CONFIG_DIR, serverName);
			dir2 = replace(dir2, ConfigNameResolver.JBOSS_AS7_CONFIG_FILE, serverName);
			
			try {
				StringSubstitutionEngine engine = new StringSubstitutionEngine();
				dir2 = engine.performStringSubstitution(dir2, true,
						true, StringVariableManager.getDefault());
			} catch( CoreException ce ) {
				JBossServerCorePlugin.log(ce.getStatus());
			}
		}
		return dir2;
	}
	
	private String replace(String original, String variable, String serverName) {
		if( original != null ) {
			return original.replace(getVariablePattern(variable), getVariablePattern(variable, serverName));
		}
		return null;
	}
	
	private String getVariablePattern(String var) {
		return "${" + var + "}"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private String getVariablePattern(String var, String serverName) {
		return "${" + var + ":" + serverName + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	
	/*
	 * Actual resolution of these dynamic variables are performed below
	 */
	
	public String resolveValue(IDynamicVariable variable, String argument)
			throws CoreException {
		if( variable.getName().equals(JBOSS_CONFIG))
			return handleConfig(variable, argument);
		if( variable.getName().equals(JBOSS_CONFIG_DIR)) 
			return handleConfigDir(variable, argument);
		if( variable.getName().equals(JBOSS_AS7_CONFIG_FILE)) 
			return handleAS7ConfigFile(variable, argument);
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
	
	protected String handleAS7ConfigFile(IDynamicVariable variable, String argument) {
		IJBossServerRuntime ajbsrt = findJBossServerRuntime(argument);
		if( ajbsrt != null && ajbsrt instanceof LocalJBoss7ServerRuntime) {
			return ((LocalJBoss7ServerRuntime)ajbsrt).getConfigurationFile();
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
