package org.jboss.tools.as.test.core;

import java.util.HashMap;

import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class TestConstants {
	public static final String TOMCAT_RUNTIME_55 = "org.eclipse.jst.server.tomcat.runtime.55";
	public static final String JBOSS_AS_32_HOME = System.getProperty("jbosstools.test.jboss.home.3.2", "C:\\apps\\jboss\\jboss-3.2.8.SP1\\");
	public static final String JBOSS_AS_40_HOME = System.getProperty("jbosstools.test.jboss.home.4.0", "C:\\apps\\jboss\\jboss-4.0.5.GA\\");
	public static final String JBOSS_AS_42_HOME = System.getProperty("jbosstools.test.jboss.home.4.2", "C:\\apps\\jboss\\jboss-4.2.1.GA\\");
	public static final String JBOSS_AS_50_HOME = System.getProperty("jbosstools.test.jboss.home.5.0", "C:\\apps\\jboss\\jboss-5.0.0.GA\\");
	public static final String JBOSS_AS_51_HOME = System.getProperty("jbosstools.test.jboss.home.5.1", "C:\\apps\\jboss\\jboss-5.1.0.GA\\");
	public static final String JBOSS_AS_52_HOME = System.getProperty("jbosstools.test.jboss.home.5.2", "C:\\apps\\jboss\\jboss-5.2.0.GA\\");
	public static final String JBOSS_AS_60_HOME = System.getProperty("jbosstools.test.jboss.home.6.0", "C:\\apps\\jboss\\jboss-6.0.0.GA\\");
	public static final String JBOSS_AS_70_HOME = System.getProperty("jbosstools.test.jboss.home.7.0", "C:\\apps\\jboss\\jboss-7.0.0.GA\\");
	public static final String JBOSS_AS_71_HOME = System.getProperty("jbosstools.test.jboss.home.7.1", "C:\\apps\\jboss\\jboss-7.1.0.GA\\");
	public static final String JBOSS_EAP_43_HOME = System.getProperty("jbosstools.test.jboss.home.eap.4.3", "C:\\apps\\jboss\\jboss-eap-4.3.0.GA\\");
	public static final String JBOSS_EAP_50_HOME = System.getProperty("jbosstools.test.jboss.home.eap.5.0", "C:\\apps\\jboss\\jboss-eap-5.0.0.GA\\");
	public static final String JBOSS_EAP_60_HOME = System.getProperty("jbosstools.test.jboss.home.eap.6.0", "C:\\apps\\jboss\\jboss-eap-6.0.0.GA\\");
	
	public static final String JRE_14_HOME = System.getProperty("jbosstools.test.jre.1.4", "C:\\apps\\jboss\\jboss-5.2.0.GA\\");
	public static final String JRE_5_HOME = System.getProperty("jbosstools.test.jre.5", "C:\\apps\\jboss\\jboss-5.2.0.GA\\");
	public static final String JRE_6_HOME = System.getProperty("jbosstools.test.jre.6", "C:\\apps\\jboss\\jboss-5.2.0.GA\\");

	public static final String JBOSS_AS_HOME = System.getProperty("jbosstools.test.jboss.home", JBOSS_AS_42_HOME);

	public static final HashMap<String, String> serverTypeToHome = new HashMap<String, String>();
	static {
		serverTypeToHome.put(IJBossToolingConstants.SERVER_AS_32, JBOSS_AS_32_HOME);
		serverTypeToHome.put(IJBossToolingConstants.SERVER_AS_40, JBOSS_AS_40_HOME);
		serverTypeToHome.put(IJBossToolingConstants.SERVER_AS_42, JBOSS_AS_42_HOME);
		serverTypeToHome.put(IJBossToolingConstants.SERVER_AS_50, JBOSS_AS_50_HOME);
		serverTypeToHome.put(IJBossToolingConstants.SERVER_AS_51, JBOSS_AS_51_HOME);
		serverTypeToHome.put(IJBossToolingConstants.SERVER_AS_60, JBOSS_AS_60_HOME);
		serverTypeToHome.put(IJBossToolingConstants.SERVER_AS_70, JBOSS_AS_70_HOME);
		serverTypeToHome.put(IJBossToolingConstants.SERVER_AS_71, JBOSS_AS_71_HOME);
		serverTypeToHome.put(IJBossToolingConstants.SERVER_EAP_43, JBOSS_EAP_43_HOME);
		serverTypeToHome.put(IJBossToolingConstants.SERVER_EAP_50, JBOSS_EAP_50_HOME);
		serverTypeToHome.put(IJBossToolingConstants.SERVER_EAP_60, JBOSS_EAP_60_HOME);
	}
	
	public static String getServerHome(String serverTypeId) {
		return serverTypeToHome.get(serverTypeId);
	}
}
