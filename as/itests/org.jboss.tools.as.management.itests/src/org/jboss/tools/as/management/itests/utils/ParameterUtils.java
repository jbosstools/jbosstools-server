package org.jboss.tools.as.management.itests.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.tools.as.test.core.TestConstants;

public class ParameterUtils {
	
	public static final String JBOSS_AS_70_HOME = TestConstants.JBOSS_AS_70_HOME;
	public static final String JBOSS_AS_71_HOME = TestConstants.JBOSS_AS_71_HOME;
	public static final String JBOSS_WILDFLY_80_HOME = TestConstants.JBOSS_AS_80_HOME;
	public static final String JBOSS_WILDFLY_81_HOME = TestConstants.JBOSS_AS_81_HOME;
	public static final String JBOSS_WILDFLY_90_HOME = TestConstants.JBOSS_AS_90_HOME;
	public static final String JBOSS_WILDFLY_100_HOME = TestConstants.JBOSS_WF_100_HOME;
	public static final String JBOSS_WILDFLY_101_HOME = TestConstants.JBOSS_WF_101_HOME;
	public static final String JBOSS_WILDFLY_110_HOME = TestConstants.JBOSS_WF_110_HOME;
	public static final String JBOSS_WILDFLY_120_HOME = TestConstants.JBOSS_WF_120_HOME;
	public static final String JBOSS_WILDFLY_130_HOME = TestConstants.JBOSS_WF_130_HOME;
	public static final String JBOSS_WILDFLY_140_HOME = TestConstants.JBOSS_WF_140_HOME;
	public static final String JBOSS_WILDFLY_150_HOME = TestConstants.JBOSS_WF_150_HOME;
	public static final String JBOSS_EAP_60_HOME = TestConstants.JBOSS_EAP_60_HOME;
	public static final String JBOSS_EAP_61_HOME = TestConstants.JBOSS_EAP_61_HOME;
	public static final String JBOSS_EAP_62_HOME = TestConstants.JBOSS_EAP_62_HOME;
	public static final String JBOSS_EAP_63_HOME = TestConstants.JBOSS_EAP_63_HOME;
	public static final String JBOSS_EAP_70_HOME = TestConstants.JBOSS_EAP_70_HOME;
	public static final String JBOSS_EAP_71_HOME = TestConstants.JBOSS_EAP_71_HOME;
	// NEW_SERVER_ADAPTER
	
	public static HashMap<String,String> serverHomeToRuntimeType = new HashMap<String, String>();
	static {
		serverHomeToRuntimeType.put(JBOSS_AS_70_HOME, IJBossToolingConstants.AS_70);
		serverHomeToRuntimeType.put(JBOSS_AS_71_HOME, IJBossToolingConstants.AS_71);
		serverHomeToRuntimeType.put(JBOSS_WILDFLY_80_HOME, IJBossToolingConstants.WILDFLY_80);
		serverHomeToRuntimeType.put(JBOSS_WILDFLY_81_HOME, IJBossToolingConstants.WILDFLY_80);
		serverHomeToRuntimeType.put(JBOSS_WILDFLY_90_HOME, IJBossToolingConstants.WILDFLY_90);
		serverHomeToRuntimeType.put(JBOSS_WILDFLY_100_HOME, IJBossToolingConstants.WILDFLY_100);
		serverHomeToRuntimeType.put(JBOSS_WILDFLY_101_HOME, IJBossToolingConstants.WILDFLY_100);
		serverHomeToRuntimeType.put(JBOSS_WILDFLY_110_HOME, IJBossToolingConstants.WILDFLY_110);
		serverHomeToRuntimeType.put(JBOSS_WILDFLY_120_HOME, IJBossToolingConstants.WILDFLY_120);
		serverHomeToRuntimeType.put(JBOSS_WILDFLY_130_HOME, IJBossToolingConstants.WILDFLY_130);
		serverHomeToRuntimeType.put(JBOSS_WILDFLY_140_HOME, IJBossToolingConstants.WILDFLY_140);
		serverHomeToRuntimeType.put(JBOSS_WILDFLY_150_HOME, IJBossToolingConstants.WILDFLY_150);
		serverHomeToRuntimeType.put(JBOSS_EAP_60_HOME, IJBossToolingConstants.EAP_60);
		serverHomeToRuntimeType.put(JBOSS_EAP_61_HOME, IJBossToolingConstants.EAP_61);
		serverHomeToRuntimeType.put(JBOSS_EAP_62_HOME, IJBossToolingConstants.EAP_61);
		serverHomeToRuntimeType.put(JBOSS_EAP_63_HOME, IJBossToolingConstants.EAP_61);
		serverHomeToRuntimeType.put(JBOSS_EAP_70_HOME, IJBossToolingConstants.EAP_70);
		serverHomeToRuntimeType.put(JBOSS_EAP_71_HOME, IJBossToolingConstants.EAP_71);
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
		paths.add(JBOSS_AS_70_HOME);
		paths.add(JBOSS_AS_71_HOME);
		paths.add(JBOSS_WILDFLY_80_HOME);
		paths.add(JBOSS_WILDFLY_81_HOME);
		paths.add(JBOSS_WILDFLY_90_HOME);
		paths.add(JBOSS_WILDFLY_100_HOME);
		paths.add(JBOSS_WILDFLY_101_HOME);
		paths.add(JBOSS_WILDFLY_110_HOME);
		paths.add(JBOSS_WILDFLY_120_HOME);
		paths.add(JBOSS_WILDFLY_130_HOME);
		paths.add(JBOSS_WILDFLY_140_HOME);
		if( !skipReqs ) {
			paths.add(JBOSS_EAP_60_HOME);
			paths.add(JBOSS_EAP_61_HOME);
			paths.add(JBOSS_EAP_62_HOME);
			paths.add(JBOSS_EAP_63_HOME);
			paths.add(JBOSS_EAP_70_HOME);
			paths.add(JBOSS_EAP_71_HOME);
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
		paths.add(JBOSS_WILDFLY_110_HOME);
		if( !skipReqs ) {
			paths.add(JBOSS_EAP_71_HOME);
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
			Object[] ret = getAS7ServerHomes();
			if( Arrays.asList(ret).contains(SINGLE_RUNTIME))
				return SINGLE_RUNTIME;
		}
		return null;
	}

	public static boolean isSingleRuntime() {
		if( SINGLE_RUNTIME == null || SINGLE_RUNTIME.isEmpty())
			return false;
		if("${jbosstools.test.singleruntime.location}".equals(SINGLE_RUNTIME))
			return false;
		
		System.out.println(SINGLE_RUNTIME);
		return true;
	}
	
}
