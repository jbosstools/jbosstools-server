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
import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jst.server.core.RuntimeClasspathProviderDelegate;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathConstants;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathCorePlugin;
import org.jboss.ide.eclipse.as.classpath.core.Messages;
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

	public static class ClientAllFilter {
		public static boolean accepts(IPath path) {
			if( !path.lastSegment().endsWith(EXT_JAR)) return false;
			if( path.lastSegment().toLowerCase().endsWith("jaxb-xjc.jar")) return false;
			return true;
		}
	}
	
	public IClasspathEntry[] resolveClasspathContainer(IProject project, IRuntime runtime) {
		if( runtime == null ) 
			return new IClasspathEntry[0];

		IJBossServerRuntime jbsrt = (IJBossServerRuntime)runtime.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
		if( jbsrt == null ) {
			// log error
			IStatus status = new Status(IStatus.WARNING, ClasspathCorePlugin.PLUGIN_ID, MessageFormat.format(Messages.ClientAllRuntimeClasspathProvider_wrong_runtime_type,
					runtime.getName()));
			ClasspathCorePlugin.getDefault().getLog().log(status);
			return new IClasspathEntry[0];
		}
		
		IPath loc = runtime.getLocation();
		IPath configPath = jbsrt.getConfigurationFullPath();
		String rtID  = runtime.getRuntimeType().getId();
		if(AS_32.equals(rtID)) return get32(loc, configPath);
		if(AS_40.equals(rtID)) return get40(loc,configPath);
		if(AS_42.equals(rtID)) return get42(loc,configPath);
		if(AS_50.equals(rtID)) return get50(loc,configPath);
		if(EAP_43.equals(rtID)) return getEAP43(loc,configPath);
		
		// Added cautiously, not sure on changes, may change
		if(AS_51.equals(rtID)) return get50(loc,configPath);
		if(AS_60.equals(rtID)) return get50(loc,configPath);
		if(EAP_50.equals(rtID)) return get50(loc,configPath);
		return null;
	}
	
	protected IClasspathEntry[] get32(IPath location, IPath configPath) {
		ArrayList<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
		addEntries(location.append(CLIENT), list);
		addEntries(location.append(LIB), list);
		addEntries(configPath.append(LIB), list);
		return list.toArray(new IClasspathEntry[list.size()]);
	}
	
	protected IClasspathEntry[] get40(IPath location, IPath configPath) {
		ArrayList<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
		IPath deployPath = configPath.append(DEPLOY);
		addEntries(location.append(CLIENT), list);
		addEntries(location.append(LIB), list);
		addEntries(configPath.append(LIB), list);
		addEntries(deployPath.append(JBOSS_WEB_DEPLOYER).append(JSF_LIB), list);
		addEntries(deployPath.append(AOP_JDK5_DEPLOYER), list);
		addEntries(deployPath.append(EJB3_DEPLOYER), list);
		return list.toArray(new IClasspathEntry[list.size()]);
	}

	protected IClasspathEntry[] get42(IPath location, IPath configPath) {
		return get40(location, configPath);
	}

	protected IClasspathEntry[] getEAP43(IPath location, IPath configPath) {
		return get40(location, configPath);
	}
	
	protected IClasspathEntry[] get50(IPath location, IPath configPath) {
		ArrayList<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
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
		addEntries(deployerPath.append(WEBBEANS_DEPLOYER).append(JSR299_API_JAR), list);
		return list.toArray(new IClasspathEntry[list.size()]);
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
					if( files[i].endsWith(EXT_JAR) && ClientAllFilter.accepts(folder.append(files[i]))) {
						list.add(getEntry(folder.append(files[i])));
					}
				}
			} else {
				list.add(getEntry(folder));
			}
		}
	}

}
