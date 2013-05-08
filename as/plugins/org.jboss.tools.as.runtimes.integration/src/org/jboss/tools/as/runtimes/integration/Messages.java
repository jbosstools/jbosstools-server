/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.runtimes.integration;

import org.eclipse.osgi.util.NLS;

public class Messages {
	private static final String RESOURCE_NAME = "org.jboss.tools.as.runtimes.download.messages"; //$NON-NLS-1$
	public static String JBossRuntimeStartup_Cannot_create_new_JBoss_Server;
	public static String JBossRuntimeStartup_Cannott_create_new_DTP_Connection_Profile;
	public static String JBossRuntimeStartup_Cannott_create_new_HSQL_DB_Driver;
	public static String JBossRuntimeStartup_Cannot_create_new_DB_Driver;
	public static String JBossRuntimeStartup_Runtime;
	public static String JBossRuntimeStartup_The_JBoss_AS_Hypersonic_embedded_database;
	public static String JBossRuntimeStartup_The_JBoss_AS_H2_embedded_database;
	public static String LoadRemoteRuntimes;
	public static String CreateDownloadRuntimes;
	
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(RESOURCE_NAME, Messages.class);
	}

	public Messages() {
	}

}
