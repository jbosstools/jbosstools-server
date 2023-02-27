/*******************************************************************************
 * Copyright (c) 2013-2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.management.itests.utils;

import java.util.Arrays;

import org.jboss.tools.as.test.core.TestConstants;
import org.jboss.tools.as.test.core.internal.utils.ServerParameterUtils;

public class ParameterUtils {

	// AUTOGEN_SERVER_ADAPTER_CHUNK
	public static final String[] JAVAX_PACKAGE_RUNTIME_HOMES = new String[] {
			TestConstants.JBOSS_WF_260_HOME,
			TestConstants.JBOSS_EAP_73_HOME, TestConstants.JBOSS_EAP_74_HOME
	};
	// AUTOGEN_SERVER_ADAPTER_CHUNK
	
	
	public static final String SKIP_PRIVATE_REQUIREMENTS =  "org.jboss.tools.tests.skipPrivateRequirements";
	public static boolean skipPrivateRequirements() {
        if( Boolean.getBoolean(SKIP_PRIVATE_REQUIREMENTS))
            return true;
        return false;
	}
	
	public static final String getServerType(String serverHome) {
		String serverType = TestConstants.serverHomeDirToServerType().get(serverHome);
		return serverType;
	}
	
	public static Object[] getAS7ServerHomes() {
		return 	ServerParameterUtils.getJBossServerHomeParameters();
	}
	
	public static Object[] getIncrementalMgmtDeploymentHomes() {
		Object[] all = getAllIncrementalMgmtDeploymentHomes();
		if( !isSingleRuntime()) 
			return all;
		if( Arrays.asList(all).contains(SINGLE_RUNTIME))
			return new String[] {SINGLE_RUNTIME};
		return null;
	}
	
	public static Object[] getAllIncrementalMgmtDeploymentHomes() {
		return getAS7ServerHomes();
	}
	
	
	public static final String SINGLE_RUNTIME_KEY = "jbosstools.test.singleruntime.location";
	public static final String SINGLE_RUNTIME = System.getProperty(SINGLE_RUNTIME_KEY);

	public static Object[] getServerHomes() {
		Object[] ret = getAS7ServerHomes();
		if( !isSingleRuntime()) {
			return getAS7ServerHomes();
		}
		
		if( Arrays.asList(ret).contains(SINGLE_RUNTIME))
			return new String[] {SINGLE_RUNTIME};
		return null;
	}
	
	public static String getSingleRuntimeHome() {
		if( isSingleRuntime()) {
			return SINGLE_RUNTIME;
		}
		return null;
	}

	public static boolean isSingleRuntime() {
		if( SINGLE_RUNTIME == null || SINGLE_RUNTIME.isEmpty())
			return false;
		if("${jbosstools.test.singleruntime.location}".equals(SINGLE_RUNTIME))
			return false;
		
		System.out.println("Single runtime location parameter: " + SINGLE_RUNTIME);
		return true;
	}
	
}
