/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.ide.eclipse.as.classpath.core.ejb3;

import java.io.FileNotFoundException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class EJB30SupportVerifier {
	protected static boolean definitelySupports(IRuntime rt) {
		IJBossServerRuntime  jbRuntime = (IJBossServerRuntime)rt.loadAdapter(IJBossServerRuntime.class, null);
		String jbossVersion = jbRuntime.getRuntime().getRuntimeType().getVersion();
		if( jbossVersion.compareTo(IJBossToolingConstants.V5_0) < 0)
			return false;
		return true;
	}
	
	public static boolean verify(IRuntime rt) {
		if( rt == null ) // deploy only server protection
			return false;
		IJBossServerRuntime  jbRuntime = (IJBossServerRuntime)rt.loadAdapter(IJBossServerRuntime.class, null);
		if( jbRuntime == null )
			return false;
		if( definitelySupports(rt))
			return true;
		if( rt.getRuntimeType().getVersion().equals(IJBossToolingConstants.V3_2))
			return false;

		IPath serverHome = rt.getLocation();
		IPath configPath = jbRuntime.getConfigurationFullPath();
		
		try {
			if( rt.getRuntimeType().getVersion().equals(IJBossToolingConstants.V4_0))
				EJB3ClasspathContainer.get40Jars(serverHome, configPath);
			else if( rt.getRuntimeType().getVersion().equals(IJBossToolingConstants.V4_2))
				EJB3ClasspathContainer.get42Jars(serverHome, configPath);
		} catch(FileNotFoundException fnfe) {
			return false;
		}
		return true;
	}
}
