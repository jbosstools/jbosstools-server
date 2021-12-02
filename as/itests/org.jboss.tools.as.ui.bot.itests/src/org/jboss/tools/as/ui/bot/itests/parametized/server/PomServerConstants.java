/*******************************************************************************
 * Copyright (c) 2017 - 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.ui.bot.itests.parametized.server;

public class PomServerConstants {
	// AUTOGEN_SERVER_ADAPTER_CHUNK
	public static final String JBOSS_240_HOME="jbosstools.test.jboss.home.24.0";
	public static final String JBOSS_250_HOME="jbosstools.test.jboss.home.25.0";
	// AUTOGEN_SERVER_ADAPTER_CHUNK
	public static final String JBOSS_EAP_70_HOME="jbosstools.test.jboss.home.eap.7.0";
	public static final String JBOSS_EAP_71_HOME="jbosstools.test.jboss.home.eap.7.1";
	public static final String JBOSS_EAP_72_HOME="jbosstools.test.jboss.home.eap.7.2";
	public static final String JBOSS_EAP_73_HOME="jbosstools.test.jboss.home.eap.7.3";
	public static final String JBOSS_EAP_74_HOME="jbosstools.test.jboss.home.eap.7.4";
	// AUTOGEN_SERVER_ADAPTER_CHUNK
	// NEW_SERVER_ADAPTER

	public static final String[] PUBLIC = new String[]{
			// AUTOGEN_SERVER_ADAPTER_CHUNK
			JBOSS_240_HOME,
			JBOSS_250_HOME,
			// AUTOGEN_SERVER_ADAPTER_CHUNK
	};
	// NEW_SERVER_ADAPTER

	public static final String[] ALL = new String[]{
			// AUTOGEN_SERVER_ADAPTER_CHUNK
			JBOSS_240_HOME,
			JBOSS_250_HOME,
			// AUTOGEN_SERVER_ADAPTER_CHUNK
			JBOSS_EAP_70_HOME, 
			JBOSS_EAP_71_HOME,
			JBOSS_EAP_72_HOME, 
			JBOSS_EAP_73_HOME,
			JBOSS_EAP_74_HOME,
			// AUTOGEN_SERVER_ADAPTER_CHUNK
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
