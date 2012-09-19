/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.core.runtime.IPath;
import org.jboss.ide.eclipse.as.wtp.core.Messages;

public interface IJBoss6Server {
	public static final String JMX_RMI_PORT = "org.jboss.ide.eclipse.as.core.server.jmxrmiport"; //$NON-NLS-1$
	public static final String JMX_RMI_PORT_DETECT = "org.jboss.ide.eclipse.as.core.server.jmxrmiport_AutoDetect"; //$NON-NLS-1$
	public static final String JMX_RMI_PORT_DETECT_XPATH = "org.jboss.ide.eclipse.as.core.server.jmxrmiport_AutoDetect.XPath"; //$NON-NLS-1$
	public static final String JMX_RMI_PORT_DEFAULT_XPATH = Messages.Ports + IPath.SEPARATOR + "JMX RMI Port"; //$NON-NLS-1$
	public static final int JMX_RMI_DEFAULT_PORT = 1090;
	
	public int getJMXRMIPort();
}
