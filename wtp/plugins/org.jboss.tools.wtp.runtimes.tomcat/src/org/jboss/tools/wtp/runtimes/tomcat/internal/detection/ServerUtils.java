/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.wtp.runtimes.tomcat.internal.detection;

import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;

/**
 * Utility class for handling {@link IServer} related tasks
 * 
 * @author Fred Bricon
 *
 */
public class ServerUtils {

	private ServerUtils() {}
	
	/**
	 * Return a unique server name.<br/> 
	 * If duplicates are found in exisiting servers, a (number) suffix will be added to the candidate name.
	 */
	public static String getUniqueServerName(String candidateName) {
		return getUniqueServerName(candidateName, 1);
	}
	
	private static String getUniqueServerName(String name, int iteration) {
		String candidatelName = (iteration > 1)? name + " (" + iteration + ")" : name;  //$NON-NLS-1$//$NON-NLS-2$
		IServer[] servers = ServerCore.getServers();
		for (IServer server : servers) {
			if (candidatelName.equals(server.getName())) {
				return getUniqueServerName(name, ++iteration);
			}
		}
		return candidatelName;
	}
	
}
