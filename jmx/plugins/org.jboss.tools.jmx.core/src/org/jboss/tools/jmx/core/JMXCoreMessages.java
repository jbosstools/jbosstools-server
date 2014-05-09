/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jboss.tools.jmx.core;

import org.eclipse.osgi.util.NLS;

public class JMXCoreMessages extends NLS {
	public static String ConnectJob;
	public static String ConnectJobFailed;
	public static String DisconnectJob;
	public static String DeleteConnectionJob;
	public static String DisconnectJobFailed;
	public static String ExtensionManagerError1;
	public static String DefaultConnection_ErrorAdding;
	public static String DefaultConnection_ErrorRemoving;
	public static String DefaultConnection_ErrorChanging;
	public static String DefaultConnection_ErrorLoading;
	public static String DefaultConnection_ErrorRunningJMXCode;
	public static String ProblemWritingToFile;
	public static String LoadMBeans;
	public static String InspectMBeans;
	public static String RefreshJob;
	public static String RefreshJobFailed;

	static {
	    NLS.initializeMessages("org.jboss.tools.jmx.core.JMXCoreMessages", //$NON-NLS-1$
	                    JMXCoreMessages.class);
	}

}
