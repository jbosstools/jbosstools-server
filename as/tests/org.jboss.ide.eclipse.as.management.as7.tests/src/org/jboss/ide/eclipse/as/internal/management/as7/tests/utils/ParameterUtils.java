package org.jboss.ide.eclipse.as.internal.management.as7.tests.utils;

import java.util.ArrayList;
import java.util.HashMap;

import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class ParameterUtils {
	
	public static final String PROP_JBOSS_AS_70_HOME = "jbosstools.test.jboss.home.7.0";
	public static final String PROP_JBOSS_AS_71_HOME = "jbosstools.test.jboss.home.7.1";
	public static final String PROP_JBOSS_EAP_60_HOME = "jbosstools.test.jboss.home.eap.6.0";
	public static final String PROP_JBOSS_EAP_61_HOME = "jbosstools.test.jboss.home.eap.6.1";

	public static final String JBOSS_AS_70_HOME = System.getProperty(PROP_JBOSS_AS_70_HOME, "C:\\apps\\jboss\\jboss-7.0.0.GA\\");
	public static final String JBOSS_AS_71_HOME = System.getProperty(PROP_JBOSS_AS_71_HOME, "C:\\apps\\jboss\\jboss-7.1.0.GA\\");
	public static final String JBOSS_EAP_60_HOME = System.getProperty(PROP_JBOSS_EAP_60_HOME,"C:\\apps\\jboss\\jboss-eap-6.0.0.GA\\");
	public static final String JBOSS_EAP_61_HOME = System.getProperty(PROP_JBOSS_EAP_61_HOME, "C:\\apps\\jboss\\jboss-eap-6.1.0.GA\\");
	// NEW_SERVER_ADAPTER
	
	public static HashMap<String,String> serverHomeToRuntimeType = new HashMap<String, String>();
	static {
		serverHomeToRuntimeType.put(JBOSS_AS_70_HOME, IJBossToolingConstants.AS_70);
		serverHomeToRuntimeType.put(JBOSS_AS_71_HOME, IJBossToolingConstants.AS_71);
		serverHomeToRuntimeType.put(JBOSS_EAP_60_HOME, IJBossToolingConstants.EAP_60);
		serverHomeToRuntimeType.put(JBOSS_EAP_61_HOME, IJBossToolingConstants.EAP_61);
		// NEW_SERVER_ADAPTER
	}
	
	public static final String SKIP_PRIVATE_REQUIREMENTS = 
			"org.jboss.tools.tests.skipPrivateRequirements";
	public static boolean skipPrivateRequirements() {
        if( Boolean.getBoolean(SKIP_PRIVATE_REQUIREMENTS))
            return true;
        return false;
	}
	
	public static Object[] getAS7ServerHomes() {
		boolean skipReqs = skipPrivateRequirements();
		ArrayList<String> paths = new ArrayList<String>();
		paths.add(JBOSS_AS_70_HOME);
		paths.add(JBOSS_AS_71_HOME);
		if( !skipReqs ) {
			paths.add(JBOSS_EAP_60_HOME);
			// TODO add eap61 when proper client jars are found
//			paths.add(JBOSS_EAP_61_HOME);
		}
		return paths.toArray(new String[paths.size()]);
	}
}
