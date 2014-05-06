/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.core.runtime;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.classpath.core.runtime.internal.PathProviderResolutionUtil;
import org.jboss.ide.eclipse.as.classpath.core.runtime.internal.SourceJarsLocator;
import org.jboss.ide.eclipse.as.core.resolvers.ConfigNameResolver;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.v7.LocalJBoss7ServerRuntime;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;

/**
 * This class assists in fetching a set of jars based on a given
 * criteria for a relevant runtime
 * 
 * @since 3.0
 */
public class RuntimeJarUtility {
	/**
	 * A key representing only jars which should be on a project classpath
	 */
	public static final int CLASSPATH_JARS = 1;
	
	/**
	 * A key representing ALL jars for the appropriate 
	 * current configuration of the runtime only.
	 * (ie default, all, minimal, standalone, etc)
	 */
	public static final int ALL_JARS = 2;
	
	/**
	 * This constant for string substitution which represents a configuration directory 
	 * This constant is the variable form of the constant.  ${varname}
	 */
	public static final String CONFIG_DIR_VAR_PATTERN = ConfigNameResolver.getVariablePattern(ConfigNameResolver.JBOSS_CONFIG_DIR); 

	/**
	 * This constant for string substitution which represents a server's home directory 
	 * This constant is the variable form of the constant.  ${varname}
	 */
	public static final String SERVER_HOME_VAR_PATTERN = ConfigNameResolver.getVariablePattern(ConfigNameResolver.JBOSS_SERVER_HOME);


	
	/**
	 * Return a list of jars for the given request type and runtime,
	 * or, return null if the request type is unknown for the given runtime. 
	 *  
	 * @param rt  A runtime 
	 * @param type  The type of request for jars being made
	 * @return
	 */
	public IPath[] getJarsForRuntime(IRuntime rt, int type) {
		if( type == CLASSPATH_JARS) {
			IRuntimePathProvider[] sets = CustomRuntimeClasspathModel.getInstance().getEntries(rt.getRuntimeType());
			IPath[] allPaths = PathProviderResolutionUtil.getAllPaths(rt, sets);
			return allPaths;
		} else if( type == ALL_JARS) {
			IRuntimePathProvider[] sets = new SourceJarsLocator().getDefaultPathProviders(rt.getRuntimeType());
			IPath[] allPaths = PathProviderResolutionUtil.getAllPaths(rt, sets);
			return allPaths;
		}
		return null;
	}
	
	/**
	 * Return a list of jars for the given request type and runtime,
	 * or, return null if the request type is unknown for the given runtime. 
	 *  
	 * @param rt  A runtime 
	 * @param type  The type of request for jars being made
	 * @return
	 */
	public IPath[] getJarsForRuntimeHome(String home, int type) {
		return getJarsForRuntimeHome(home, type, true);
	}
	
	
	/**
 	 * Return a list of jars for the given request type and runtime,
	 * or, return null if the request type is unknown for the given runtime,
	 * or if the given home directory represents an unknown server type.  
	 * 
	 * @param home  The server home
	 * @param type  The type of request being made
	 * @param setDefaults Whether to add unset default string replacements, like jboss_config_name
	 * @return
	 */
	public IPath[] getJarsForRuntimeHome(String home, int type, boolean setDefaults) {
		HashMap<String, String> map = new HashMap<String, String>();
		return getJarsForRuntimeHome(home, type, map, setDefaults);
	}
	
	/**
 	 * Return a list of jars for the given request type and runtime,
	 * or, return null if the request type is unknown for the given runtime,
	 * or if the given home directory represents an unknown server type.  
	 * 
	 * @param home  The server home
	 * @param type  The type of request being made
	 * @param replacements  A map of string replacements
	 * @param setDefaults Whether to add unset default string replacements, like jboss_config_name
	 * @return
	 */
	public IPath[] getJarsForRuntimeHome(String home, int type, Map<String, String> replacements, boolean setDefaultReplacements) {
		// Force the server home to be accurate
		replacements.put(ConfigNameResolver.JBOSS_SERVER_HOME, home);

		ServerBeanLoader loader = new ServerBeanLoader(new File(home));
		String serverType = loader.getServerAdapterId();
		IServerType t = serverType == null ? null : ServerCore.findServerType(serverType);
		IRuntimeType rtType = t == null ? null : t.getRuntimeType();
		if( rtType == null ) {
			return null;
		}
		
		if( setDefaultReplacements ) {
			setDefaultReplacements(t, home, replacements);
		}
		
		if( type == CLASSPATH_JARS) {
			IRuntimePathProvider[] sets = CustomRuntimeClasspathModel.getInstance().getEntries(rtType);
			IPath[] allPaths = PathProviderResolutionUtil.getAllPaths(replacements, sets);
			return allPaths;
		} else if( type == ALL_JARS) {
			IRuntimePathProvider[] sets = new SourceJarsLocator().getDefaultPathProviders(rtType);
			IPath[] allPaths = PathProviderResolutionUtil.getAllPaths(replacements, sets);
			return allPaths;
		}
		return null;
	}

	/**
	 * Add default replacements in the same way a runtime object with the property value empty would behave. 
	 * 
	 * @param type
	 * @param home
	 * @param replacements
	 */
	private void setDefaultReplacements(IServerType type, String home, Map<String, String> replacements) {
		ServerExtendedProperties props = (ServerExtendedProperties)Platform.getAdapterManager().getAdapter(type,ServerExtendedProperties.class);
		if( props != null ) {
			int fileStructure = props.getFileStructure();
			IPath serverHome = new Path(home);
			if( fileStructure == ServerExtendedProperties.FILE_STRUCTURE_SERVER_CONFIG_DEPLOY) {
				if( replacements.get(ConfigNameResolver.JBOSS_CONFIG) == null ) {
					replacements.put(ConfigNameResolver.JBOSS_CONFIG, IJBossRuntimeResourceConstants.DEFAULT_CONFIGURATION);
				}
				String cfg = replacements.get(ConfigNameResolver.JBOSS_CONFIG);
				if( replacements.get(ConfigNameResolver.JBOSS_CONFIG_DIR) == null ) {
					replacements.put(ConfigNameResolver.JBOSS_CONFIG_DIR, serverHome.append(IJBossRuntimeResourceConstants.SERVER).append(cfg).toOSString());
				}
			}
			
			if( fileStructure == ServerExtendedProperties.FILE_STRUCTURE_SERVER_CONFIG_DEPLOY) {
				if( replacements.get(ConfigNameResolver.JBOSS_AS7_CONFIG_FILE) == null ) {
					replacements.put(ConfigNameResolver.JBOSS_AS7_CONFIG_FILE, LocalJBoss7ServerRuntime.CONFIG_FILE_DEFAULT);
				}
				if( replacements.get(ConfigNameResolver.JBOSS_CONFIG_DIR) == null ) {
					replacements.put(ConfigNameResolver.JBOSS_CONFIG_DIR, serverHome.append(IJBossRuntimeResourceConstants.AS7_STANDALONE)
							.append(IJBossRuntimeResourceConstants.CONFIGURATION).toOSString());
				}
			}
			
		}
	}
}
