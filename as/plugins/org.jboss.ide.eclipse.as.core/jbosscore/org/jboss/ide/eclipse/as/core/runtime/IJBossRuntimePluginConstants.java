/*******************************************************************************
 * Copyright (c) 2010 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.runtime;

import org.jboss.ide.eclipse.as.core.Messages;


public interface IJBossRuntimePluginConstants {
	public static final String DEFAULT_DS = "DefaultDS";//$NON-NLS-1$
	public static final String RUNTIME = Messages.JBossRuntimeStartup_Runtime;
	public static final String EAP = "EAP"; //$NON-NLS-1$
	public static final String EAP_STD = "EAP_STD"; //$NON-NLS-1$
	public static final String SOA_P = "SOA-P"; //$NON-NLS-1$
	public static final String SOA_P_STD = "SOA-P-STD"; //$NON-NLS-1$
	public static final String EPP = "EPP"; //$NON-NLS-1$
	public static final String EWP = "EWP"; //$NON-NLS-1$
	public static final String AS = "AS"; //$NON-NLS-1$
	
	public static final String HSQLDB_DRIVER_JAR_NAME = "hsqldb.jar"; //$NON-NLS-1$
	
	public static final String HSQLDB_DRIVER_3X_4X_LOCATION = "/server/default/lib/" + HSQLDB_DRIVER_JAR_NAME; //$NON-NLS-1$
	
	public static final String HSQLDB_DRIVER_5X_LOCATION = "/common/lib/" + HSQLDB_DRIVER_JAR_NAME; //$NON-NLS-1$
		
	public static final String HSQL_DRIVER_DEFINITION_ID 
												= "DriverDefn.Hypersonic DB"; //$NON-NLS-1$

	public static final String H2_DRIVER_DEFINITION_ID 
												= "DriverDefn.H2 DB"; //$NON-NLS-1$

	public static final String HSQL_DRIVER_NAME = "Hypersonic DB"; //$NON-NLS-1$
	
	public static final String H2_DRIVER_NAME = "H2 Database"; //$NON-NLS-1$

	public static final String HSQL_DRIVER_TEMPLATE_ID 
						= "org.eclipse.datatools.enablement.hsqldb.1_8.driver"; //$NON-NLS-1$
	
	public static final String H2_DRIVER_TEMPLATE_ID 
					= "org.eclipse.datatools.connectivity.db.generic.genericDriverTemplate"; //$NON-NLS-1$

	public static final String DTP_DB_URL_PROPERTY_ID 
								= "org.eclipse.datatools.connectivity.db.URL"; //$NON-NLS-1$

	public static final String HSQL_PROFILE_ID = "org.eclipse.datatools.enablement.hsqldb.connectionProfile";//$NON-NLS-1$
	
	public static final String H2_PROFILE_ID = "org.eclipse.datatools.connectivity.db.generic.connectionProfile";//$NON-NLS-1$

}
