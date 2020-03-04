/******************************************************************************* 
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.util;

import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IServerType;
import org.jboss.ide.eclipse.as.core.Messages;

public class ServerNamingUtility {
	public static String getNextShortServerName(IServerType type) {
		String b1 = type.getName();
		// We haven't been shortening this in a long time... through an error. So don't change it
		//String b2 = b1.replace("Red Hat JBoss Enterprise Application Platform", "JBoss EAP");  //$NON-NLS-1$//$NON-NLS-2$
		String base = performReplacementsForShortName(b1);
		return getDefaultServerName(base);
	}
	
	public static String performReplacementsForShortName(String base) {
		base = base.replace(" (End Of Life)", "");
		base = base.replace(" (Tech Preview)", "");
		if( base.contains("Enterprise Application Platform")) {
			base = base.replace("Enterprise Application Platform", "EAP");
		}
		
		return base;
	}
	
	
	public static String getDefaultServerName(IRuntime rt) {
		String runtimeName = performReplacementsForShortName(rt.getName());
		String base = null;
		if( runtimeName == null || runtimeName.equals("")) { //$NON-NLS-1$
			IRuntimeType rtt = rt.getRuntimeType();
			base = NLS.bind(Messages.serverVersionName, rtt == null ? null : rtt.getVersion());
		} else 
			base = NLS.bind(Messages.serverName, runtimeName);
		
		return getDefaultServerName( base);
	}
	
	public static String getDefaultServerName( String base) {
		base = performReplacementsForShortName(base);
		if( ServerUtil.findServer(base) == null ) return base;
		int i = 2;
		while( ServerUtil.findServer(
				NLS.bind(Messages.serverCountName, base, i)) != null )
			i++;
		return NLS.bind(Messages.serverCountName, base, i);
	}
}
