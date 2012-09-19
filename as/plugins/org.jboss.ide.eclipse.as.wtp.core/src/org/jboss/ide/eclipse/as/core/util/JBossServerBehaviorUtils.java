/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.IDelegatingServerBehavior;

/**
 * TODO These methods should be put into ServerConverter
 */
public class JBossServerBehaviorUtils {

	/**
	 * Return a DelegatingServerBehavior or null
	 * @param configuration
	 * @return
	 */
	public static IDelegatingServerBehavior getServerBehavior(ILaunchConfiguration configuration) {
		try {
			IServer server = ServerUtil.getServer(configuration);
			return (IDelegatingServerBehavior) server.getAdapter(IDelegatingServerBehavior.class);
		} catch(CoreException ce ) {
			return null;
		}
	}
}
