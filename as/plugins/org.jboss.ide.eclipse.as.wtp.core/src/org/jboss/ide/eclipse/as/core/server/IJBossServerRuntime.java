/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server;

import java.util.HashMap;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.wst.server.core.IRuntime;

/**
 * 
 * @author Rob Stryker
 */
public interface IJBossServerRuntime {
	public static String PROPERTY_VM_ID = "PROPERTY_VM_ID"; //$NON-NLS-1$
	public static String PROPERTY_VM_TYPE_ID = "PROPERTY_VM_TYPE_ID"; //$NON-NLS-1$
	public static String PROPERTY_EXECUTION_ENVIRONMENT = "PROPERTY_EXEC_ENVIRONMENT"; //$NON-NLS-1$
	
	public static String PROPERTY_CONFIGURATION_NAME = "org.jboss.ide.eclipse.as.core.runtime.configurationName"; //$NON-NLS-1$
	public static String PROPERTY_CONFIG_LOCATION="org.jboss.ide.eclipse.as.core.runtime.configurationLocation"; //$NON-NLS-1$
	
	public IRuntime getRuntime();
	public IVMInstall getHardVM();
	public IVMInstall getVM();
	public void setVM(IVMInstall install);
	public IExecutionEnvironment getExecutionEnvironment();
	public void setExecutionEnvironment(IExecutionEnvironment environment);
	
	public String getJBossConfiguration();
	public void setJBossConfiguration(String config);
	
	/**
	 * The folder this config is located in, for example:
	 *   /home/rob/tmp/default_copy1  would return /home/rob/tmp/
	 *   whereas /home/rob/apps/jboss/server/default_copy3 would return server
	 * @return
	 */
	public String getConfigLocation();
	
	/**
	 * The full path of the folder this config is located in, for example:
	 *   /home/rob/tmp/default_copy1  would return 
	 *   /home/rob/tmp/
	 *       whereas 
	 *   /home/rob/apps/jboss/server/default_copy3 would return 
	 *   /home/rob/apps/jboss/server
	 * @return
	 */
	public IPath getConfigLocationFullPath();
	
	public void setConfigLocation(String configLocation);
	
	/**
	 * The full path of the configuration, ex:
	 *  /home/rob/tmp/default_copy3 would return /home/rob/tmp/default_copy3 
	 * @return
	 */
	public IPath getConfigurationFullPath();
	
	// for startup
	public String getDefaultRunArgs();
	public String getDefaultRunArgs(IPath serverHome);
	public String getDefaultRunVMArgs();
	public String getDefaultRunVMArgs(IPath serverHome);
	public HashMap<String, String> getDefaultRunEnvVars();
	public boolean isUsingDefaultJRE();
	public boolean isEAP();
}
