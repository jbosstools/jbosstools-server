package org.jboss.tools.as.ui.bot.itests.parametized.server;

public class PomServerConstants {
	public static final String JBOSS_32_HOME="jbosstools.test.jboss.home.3.2";
	public static final String JBOSS_40_HOME="jbosstools.test.jboss.home.4.0";
	public static final String JBOSS_42_HOME="jbosstools.test.jboss.home.4.2";
	public static final String JBOSS_50_HOME="jbosstools.test.jboss.home.5.0";
	public static final String JBOSS_51_HOME="jbosstools.test.jboss.home.5.1";
	public static final String JBOSS_60_HOME="jbosstools.test.jboss.home.6.0";
	public static final String JBOSS_70_HOME="jbosstools.test.jboss.home.7.0";
	public static final String JBOSS_71_HOME="jbosstools.test.jboss.home.7.1";
	public static final String JBOSS_80_HOME="jbosstools.test.jboss.home.8.0";
	public static final String JBOSS_81_HOME="jbosstools.test.jboss.home.8.1";
	public static final String JBOSS_90_HOME="jbosstools.test.jboss.home.9.0";
	public static final String JBOSS_10_HOME="jbosstools.test.jboss.home.10.0";
	public static final String JBOSS_101_HOME="jbosstools.test.jboss.home.10.1";
	public static final String JBOSS_110_HOME="jbosstools.test.jboss.home.11.0";
	public static final String JBOSS_EAP_43_HOME="jbosstools.test.jboss.home.eap.4.3";
	public static final String JBOSS_EAP_50_HOME="jbosstools.test.jboss.home.eap.5.0";
	public static final String JBOSS_EAP_60_HOME="jbosstools.test.jboss.home.eap.6.0";
	public static final String JBOSS_EAP_61_HOME="jbosstools.test.jboss.home.eap.6.1";
	public static final String JBOSS_EAP_62_HOME="jbosstools.test.jboss.home.eap.6.2";
	public static final String JBOSS_EAP_63_HOME="jbosstools.test.jboss.home.eap.6.3";
	public static final String JBOSS_EAP_70_HOME="jbosstools.test.jboss.home.eap.7.0";
	public static final String JBOSS_EAP_71_HOME="jbosstools.test.jboss.home.eap.7.1";
	// NEW_SERVER_ADAPTER

	public static final String[] PUBLIC = new String[]{
			JBOSS_32_HOME, JBOSS_40_HOME, JBOSS_42_HOME, JBOSS_50_HOME, JBOSS_51_HOME,
			JBOSS_60_HOME, JBOSS_70_HOME, JBOSS_71_HOME, JBOSS_80_HOME,
			JBOSS_81_HOME, JBOSS_90_HOME, JBOSS_10_HOME, JBOSS_101_HOME, 
			JBOSS_110_HOME
	};
	// NEW_SERVER_ADAPTER

	public static final String[] ALL = new String[]{
			JBOSS_32_HOME, JBOSS_40_HOME, JBOSS_42_HOME, JBOSS_50_HOME, JBOSS_51_HOME,
			JBOSS_60_HOME, JBOSS_70_HOME, JBOSS_71_HOME, JBOSS_80_HOME,
			JBOSS_81_HOME, JBOSS_90_HOME, JBOSS_10_HOME, JBOSS_101_HOME,
			JBOSS_110_HOME,
			JBOSS_EAP_43_HOME, JBOSS_EAP_50_HOME, JBOSS_EAP_60_HOME, JBOSS_EAP_61_HOME, 
			JBOSS_EAP_62_HOME, JBOSS_EAP_63_HOME, JBOSS_EAP_70_HOME//, JBOSS_EAP_71_HOME
	};
	// NEW_SERVER_ADAPTER


	public static final String SKIP_PRIVATE_REQUIREMENTS =  "org.jboss.tools.tests.skipPrivateRequirements";
	public static boolean skipPrivateRequirements() {
        if( Boolean.getBoolean(SKIP_PRIVATE_REQUIREMENTS))
            return true;
        return false;
	}
	
	public static String[] getJBossHomeFlags() {
		if( skipPrivateRequirements()) {
			return PUBLIC;
		}
		return ALL;
	}
}
