package org.jboss.ide.eclipse.as.internal.management.as7.tests.utils;

import java.util.ArrayList;
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
	public static final String JBOSS_EAP_60_HOME = TestConstants.JBOSS_EAP_60_HOME;
	public static final String JBOSS_EAP_61_HOME = TestConstants.JBOSS_EAP_61_HOME;
	public static final String JBOSS_EAP_62_HOME = TestConstants.JBOSS_EAP_62_HOME;
	public static final String JBOSS_EAP_63_HOME = TestConstants.JBOSS_EAP_63_HOME;
	// NEW_SERVER_ADAPTER
	
	public static HashMap<String,String> serverHomeToRuntimeType = new HashMap<String, String>();
	static {
		serverHomeToRuntimeType.put(JBOSS_AS_70_HOME, IJBossToolingConstants.AS_70);
		serverHomeToRuntimeType.put(JBOSS_AS_71_HOME, IJBossToolingConstants.AS_71);
		serverHomeToRuntimeType.put(JBOSS_WILDFLY_80_HOME, IJBossToolingConstants.WILDFLY_80);
		serverHomeToRuntimeType.put(JBOSS_WILDFLY_81_HOME, IJBossToolingConstants.WILDFLY_80);
		serverHomeToRuntimeType.put(JBOSS_EAP_60_HOME, IJBossToolingConstants.EAP_60);
		serverHomeToRuntimeType.put(JBOSS_EAP_61_HOME, IJBossToolingConstants.EAP_61);
		serverHomeToRuntimeType.put(JBOSS_EAP_62_HOME, IJBossToolingConstants.EAP_61);
		serverHomeToRuntimeType.put(JBOSS_EAP_63_HOME, IJBossToolingConstants.EAP_61);
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
		if( !skipReqs ) {
			paths.add(JBOSS_EAP_60_HOME);
			paths.add(JBOSS_EAP_61_HOME);
			paths.add(JBOSS_EAP_62_HOME);
			paths.add(JBOSS_EAP_63_HOME);
		}
		return paths.toArray(new String[paths.size()]);
	}
}
