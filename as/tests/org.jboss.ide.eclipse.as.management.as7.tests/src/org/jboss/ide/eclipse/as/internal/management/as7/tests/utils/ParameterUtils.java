package org.jboss.ide.eclipse.as.internal.management.as7.tests.utils;

import java.util.ArrayList;

public class ParameterUtils {
	
	public static final String JBOSS_AS_70_HOME = System.getProperty("jbosstools.test.jboss.home.7.0", "C:\\apps\\jboss\\jboss-7.0.0.GA\\");
	public static final String JBOSS_AS_71_HOME = System.getProperty("jbosstools.test.jboss.home.7.1", "C:\\apps\\jboss\\jboss-7.1.0.GA\\");
	public static final String JBOSS_EAP_60_HOME = System.getProperty("jbosstools.test.jboss.home.eap.6.0", "C:\\apps\\jboss\\jboss-eap-6.0.0.GA\\");
	public static final String JBOSS_EAP_61_HOME = System.getProperty("jbosstools.test.jboss.home.eap.6.1", "C:\\apps\\jboss\\jboss-eap-6.1.0.GA\\");
	// NEW_SERVER_ADAPTER
	
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
			paths.add(JBOSS_EAP_61_HOME);
		}
		return paths.toArray(new String[paths.size()]);
	}
}
