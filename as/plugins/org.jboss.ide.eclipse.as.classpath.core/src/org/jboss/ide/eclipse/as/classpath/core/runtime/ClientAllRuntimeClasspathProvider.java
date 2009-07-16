/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.ide.eclipse.as.classpath.core.runtime;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jst.server.core.RuntimeClasspathProviderDelegate;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathConstants;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;

/**
 * This class uses the "throw everything you can find" strategy
 * in providing additions to the classpath.  Given a server runtime, 
 * it will try to add whatever could possibly ever be used.
 * 
 * @author Rob Stryker
 *
 */
public class ClientAllRuntimeClasspathProvider 
		extends RuntimeClasspathProviderDelegate
		implements ClasspathConstants {

	public ClientAllRuntimeClasspathProvider() {
		// TODO Auto-generated constructor stub
	}

	public IClasspathEntry[] resolveClasspathContainer(IProject project, IRuntime runtime) {
		if( runtime == null ) 
			return new IClasspathEntry[0];

		IJBossServerRuntime jbsrt = (IJBossServerRuntime)runtime.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
		if( jbsrt == null ) {
			// log error
			IStatus status = new Status(IStatus.WARNING, ClasspathCorePlugin.PLUGIN_ID, "Runtime " + runtime.getName() + "is not of the proper type");
			ClasspathCorePlugin.getDefault().getLog().log(status);
			return new IClasspathEntry[0];
		}
		
		IPath loc = runtime.getLocation();
		String config = jbsrt.getJBossConfiguration();
		String rtID  = runtime.getRuntimeType().getId();
		if(AS_32.equals(rtID)) return get32(loc, config);
		if(AS_40.equals(rtID)) return get40(loc,config);
		if(AS_42.equals(rtID)) return get42(loc,config);
		if(AS_50.equals(rtID)) return get50(loc,config);
		if(AS_51.equals(rtID)) return get51(loc,config);
		if(EAP_43.equals(rtID)) return getEAP43(loc,config);
		if(EAP_50.equals(rtID)) return getEAP50(loc,config);
		return null;
	}
	
	protected IClasspathEntry[] get32(IPath location, String config) {
		ArrayList<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
		IPath configPath = location.append(SERVER).append(config);
		addEntries(location.append(CLIENT), list);
		addEntries(location.append(LIB), list);
		addEntries(configPath.append(LIB), list);
		return list.toArray(new IClasspathEntry[list.size()]);
	}
	
	protected IClasspathEntry[] get40(IPath location, String config) {
		ArrayList<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
		IPath configPath = location.append(SERVER).append(config);
		IPath deployPath = configPath.append(DEPLOY);
		addEntries(location.append(CLIENT), list);
		addEntries(location.append(LIB), list);
		addEntries(configPath.append(LIB), list);
		addEntries(deployPath.append(JBOSS_WEB_DEPLOYER).append(JSF_LIB), list);
		addEntries(deployPath.append(AOP_JDK5_DEPLOYER), list);
		addEntries(deployPath.append(EJB3_DEPLOYER), list);
		return list.toArray(new IClasspathEntry[list.size()]);
	}

	protected IClasspathEntry[] get42(IPath location, String config) {
		return get40(location, config);
	}

	protected IClasspathEntry[] getEAP43(IPath location, String config) {
		return get40(location, config);
	}
	
	protected IClasspathEntry[] getEAP50(IPath location, String config) {
		return get51(location, config);
	}
	
	protected IClasspathEntry[] get50(IPath location, String config) {
		ArrayList<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
		IPath configPath = location.append(SERVER).append(config);
		IPath deployerPath = configPath.append(DEPLOYERS);
		IPath deployPath = configPath.append(DEPLOY);
		addEntries(location.append(CLIENT), list);
		addEntries(location.append(LIB), list);
		addEntries(location.append(COMMON).append(LIB), list);
		addEntries(configPath.append(LIB), list);
		addEntries(deployPath.append(JBOSSWEB_SAR).append(JSF_LIB),list);
		addEntries(deployPath.append(JBOSSWEB_SAR).append(JBOSS_WEB_SERVICE_JAR),list);
		addEntries(deployerPath.append(AS5_AOP_DEPLOYER), list);
		addEntries(deployerPath.append(EJB3_DEPLOYER), list);
		return list.toArray(new IClasspathEntry[list.size()]);
	}
	
	protected IClasspathEntry[] get51(IPath location, String config) {
		return get50(location, config);
	}
	
	protected IClasspathEntry getEntry(IPath path) {
		return JavaRuntime.newArchiveRuntimeClasspathEntry(path).getClasspathEntry();
	}
	protected void addEntries(IPath folder, ArrayList<IClasspathEntry> list) {
		if( folder.toFile().exists()) {
			File f = folder.toFile();
			if(f.isDirectory()) {
				String[] files = f.list();
				for( int i = 0; i < files.length; i++ ) {
					if( files[i].endsWith(JAR_EXT)) {
						list.add(getEntry(folder.append(files[i])));
					}
				}
			} else {
				list.add(getEntry(folder));
			}
		}
	}

}
