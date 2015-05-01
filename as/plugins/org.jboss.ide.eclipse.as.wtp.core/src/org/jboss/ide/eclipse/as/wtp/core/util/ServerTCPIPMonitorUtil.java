/******************************************************************************* 
 * Copyright (c) 2015 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.core.util;

import org.eclipse.wst.internet.monitor.core.internal.provisional.IMonitor;
import org.eclipse.wst.internet.monitor.core.internal.provisional.MonitorCore;

/**
 * This class is to ensure that when monitoring is enabled, 
 * all requests go through the proper host/port rather than 
 * to the direct destination. 
 * 
 * For example, if your web port is localhost:8080, 
 * and monitoring is enabled and mapped to localhost:18080, 
 * all requests go through localhost:18080,  allowing the TCP/IP monitor
 * to forward the request to the actual destination.
 */
public class ServerTCPIPMonitorUtil {
	
	/**
	 * Get the host to communicate to, given the actual destination
	 * of host:port
	 * 
	 * @param host
	 * @param port
	 * @return
	 */
	public static String getHostFor(String host, int port) {
		IMonitor mon = getMonitorFor(host, port);
		return mon == null ? host : "localhost";
	}

	/**
	 * Get the port to communicate to, given the actual destination
	 * of host:port
	 * 
	 * @param host
	 * @param port
	 * @return
	 */
	public static int getPortFor(String host, int port) {
		IMonitor mon = getMonitorFor(host, port);
		return mon == null ? port : mon.getLocalPort();
	}
	
	/**
	 * Find the monitor for this host:port combination
	 * 
	 * @param host
	 * @param port
	 * @return
	 */
	private static IMonitor getMonitorFor(String host, int port) {
		IMonitor[] all = MonitorCore.getMonitors();
		for( int i = 0; i < all.length; i++ ) {
			if( all[i].isRunning()) {
				if( all[i].getRemoteHost().equals(host) && all[i].getRemotePort() == port ) {
					return all[i];
				}
			}
		}
		return null;
	}

}
