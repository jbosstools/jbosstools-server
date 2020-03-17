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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.tools.as.test.core.TestConstants;

public class ParameterUtils {
	
	public static final String JBOSS_WILDFLY_170_HOME = TestConstants.JBOSS_WF_170_HOME;
	public static final String JBOSS_WILDFLY_180_HOME = TestConstants.JBOSS_WF_180_HOME;
	public static final String JBOSS_WILDFLY_190_HOME = TestConstants.JBOSS_WF_190_HOME;
	public static final String JBOSS_EAP_70_HOME = TestConstants.JBOSS_EAP_70_HOME;
	public static final String JBOSS_EAP_71_HOME = TestConstants.JBOSS_EAP_71_HOME;
	public static final String JBOSS_EAP_72_HOME = TestConstants.JBOSS_EAP_72_HOME;
	public static final String JBOSS_EAP_73_HOME = TestConstants.JBOSS_EAP_73_HOME;
	// NEW_SERVER_ADAPTER
	
	public static HashMap<String,String> serverHomeToRuntimeType = new HashMap<String, String>();
	static {
		serverHomeToRuntimeType.put(JBOSS_WILDFLY_170_HOME, IJBossToolingConstants.WILDFLY_170);
		serverHomeToRuntimeType.put(JBOSS_WILDFLY_180_HOME, IJBossToolingConstants.WILDFLY_180);
		serverHomeToRuntimeType.put(JBOSS_WILDFLY_190_HOME, IJBossToolingConstants.WILDFLY_190);
		serverHomeToRuntimeType.put(JBOSS_EAP_70_HOME, IJBossToolingConstants.EAP_70);
		serverHomeToRuntimeType.put(JBOSS_EAP_71_HOME, IJBossToolingConstants.EAP_71);
		serverHomeToRuntimeType.put(JBOSS_EAP_72_HOME, IJBossToolingConstants.EAP_72);
		serverHomeToRuntimeType.put(JBOSS_EAP_73_HOME, IJBossToolingConstants.EAP_73);
		// NEW_SERVER_ADAPTER
	}
	
	public static final String SKIP_PRIVATE_REQUIREMENTS =  "org.jboss.tools.tests.skipPrivateRequirements";
	public static boolean skipPrivateRequirements() {
        if( Boolean.getBoolean(SKIP_PRIVATE_REQUIREMENTS))
            return true;
        return false;
	}
	
	public static final String getServerType(String serverHome) {
		String rtType = serverHomeToRuntimeType.get(serverHome);
		IServerType[] all = ServerCore.getServerTypes();
		for( int i = 0; i < all.length; i++ ) {
			if( all[i].getRuntimeType().getId().equals(rtType)) {
				return all[i].getId();
			}
		}
		return null;
	}
	
	public static Object[] getAS7ServerHomes() {
		boolean skipReqs = skipPrivateRequirements();
		ArrayList<String> paths = new ArrayList<String>();
		paths.add(JBOSS_WILDFLY_170_HOME);
		paths.add(JBOSS_WILDFLY_180_HOME);
		paths.add(JBOSS_WILDFLY_190_HOME);
		if( !skipReqs ) {
			paths.add(JBOSS_EAP_70_HOME);
			paths.add(JBOSS_EAP_71_HOME);
			paths.add(JBOSS_EAP_72_HOME);
			paths.add(JBOSS_EAP_73_HOME);
		}
		// NEW_SERVER_ADAPTER
		return paths.toArray(new String[paths.size()]);
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
		boolean skipReqs = skipPrivateRequirements();
		ArrayList<String> paths = new ArrayList<String>();
		paths.add(JBOSS_WILDFLY_170_HOME);
		paths.add(JBOSS_WILDFLY_180_HOME);
		paths.add(JBOSS_WILDFLY_190_HOME);
		if( !skipReqs ) {
			paths.add(JBOSS_EAP_71_HOME);
			paths.add(JBOSS_EAP_72_HOME);
			paths.add(JBOSS_EAP_73_HOME);
		}
		// NEW_SERVER_ADAPTER
		return paths.toArray(new String[paths.size()]);
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
