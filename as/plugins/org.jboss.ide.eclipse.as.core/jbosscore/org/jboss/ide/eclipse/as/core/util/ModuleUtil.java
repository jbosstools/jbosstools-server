/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.util;

import java.util.ArrayList;

import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities;

@Deprecated
/**
 * Please use org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities
 */
public class ModuleUtil {
	@Deprecated
	public static ArrayList<IModule[]> getShallowChildren(IServer server, IModule[] root) {
		return ServerModelUtilities.getShallowChildren(server, root);
	}
	@Deprecated
	public static ArrayList<IModule[]> getDeepChildren(IServer server, IModule[] mod) {
		return ServerModelUtilities.getDeepChildren(server, mod);
	}
	@Deprecated
	public static IModule[] getChildModules(IModule[] module) {
		return ServerModelUtilities.getChildModules(module);
	}
}
