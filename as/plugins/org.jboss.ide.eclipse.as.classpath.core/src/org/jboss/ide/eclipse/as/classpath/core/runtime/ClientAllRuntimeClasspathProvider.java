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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

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
		List<IPath> list = new ArrayList<IPath>();
		if(AS_32.equals(rtID)) list = get32(loc, configPath);
		if(AS_40.equals(rtID)) list = get40(loc,configPath);
		if(AS_42.equals(rtID)) list = get42(loc,configPath);
		if(AS_50.equals(rtID)) list = get50(loc,configPath);
		if(EAP_43.equals(rtID)) list = getEAP43(loc,configPath);
		
		// Added cautiously, not sure on changes, may change
		if(AS_51.equals(rtID)) list = get50(loc,configPath);
		if(AS_60.equals(rtID)) list = get60(loc,configPath);
		if(EAP_50.equals(rtID)) list = get50(loc,configPath);
		
		if( list == null )
			return null;
		List<IClasspathEntry> entries = convert(list);
		return entries.toArray(new IClasspathEntry[entries.size()]);
	}

	protected List<IClasspathEntry> convert(List<IPath> list) {
		ArrayList<IClasspathEntry> fin = new ArrayList<IClasspathEntry>();
		Iterator<IPath> i = list.iterator();
		while(i.hasNext()) {
			fin.add(getEntry(i.next()));
		}
		return fin;
	}
	
	protected List<IPath> get32(IPath location, IPath configPath) {
		ArrayList<IPath> list = new ArrayList<IPath>();
		addPaths(location.append(LIB), list);
		addPaths(configPath.append(LIB), list);
		addPaths(location.append(CLIENT), list);
		return list;
	}
	
	protected List<IPath> get40(IPath location, IPath configPath) {
		ArrayList<IPath> list = new ArrayList<IPath>();
		addPaths(location.append(LIB), list);
		addPaths(configPath.append(LIB), list);
		IPath deployPath = configPath.append(DEPLOY);
		addPaths(deployPath.append(JBOSS_WEB_DEPLOYER).append(JSF_LIB), list);
		addPaths(deployPath.append(AOP_JDK5_DEPLOYER), list);
		addPaths(deployPath.append(EJB3_DEPLOYER), list);
		addPaths(location.append(CLIENT), list);
		return list;
	}

	protected List<IPath> get42(IPath location, IPath configPath) {
		return get40(location, configPath);
	}

	protected List<IPath> getEAP43(IPath location, IPath configPath) {
		return get40(location, configPath);
	}
	
	protected List<IPath> get50(IPath location, IPath configPath) {
		ArrayList<IPath> list = new ArrayList<IPath>();
		addPaths(location.append(COMMON).append(LIB), list);
		addPaths(location.append(LIB), list);
		addPaths(configPath.append(LIB), list);
		IPath deployerPath = configPath.append(DEPLOYERS);
		IPath deployPath = configPath.append(DEPLOY);
		addPaths(deployPath.append(JBOSSWEB_SAR).append(JSF_LIB),list);
		addPaths(deployPath.append(JBOSSWEB_SAR).append(JBOSS_WEB_SERVICE_JAR),list);
		addPaths(deployPath.append(JBOSSWEB_SAR).append(JSTL_JAR),list);
		addPaths(deployerPath.append(AS5_AOP_DEPLOYER), list);
		addPaths(deployerPath.append(EJB3_DEPLOYER), list);
		addPaths(deployerPath.append(WEBBEANS_DEPLOYER).append(JSR299_API_JAR), list);
		addPaths(location.append(CLIENT), list);
		return list;
	}
	
	protected List<IPath> get60(IPath location, IPath configPath) {
		ArrayList<IPath> list = new ArrayList<IPath>();
		list.addAll(get50(location, configPath));
		addPaths(configPath.append(DEPLOYERS).append(REST_EASY_DEPLOYER), list);
		addPaths(configPath.append(DEPLOYERS).append(JSF_DEPLOYER).append(MOJARRA_20).append(JSF_LIB), list);
		return list;
	}
	
	protected IClasspathEntry getEntry(IPath path) {
		return JavaRuntime.newArchiveRuntimeClasspathEntry(path).getClasspathEntry();
	}

	protected void addPaths(IPath folder, ArrayList<IPath> list) {
		if( folder.toFile().exists()) {
			File f = folder.toFile();
			if(f.isDirectory()) {
				String[] files = f.list();
				for( int i = 0; i < files.length; i++ ) {
					if( files[i].endsWith(EXT_JAR) && ClientAllFilter.accepts(folder.append(files[i]))) {
						addSinglePath(folder.append(files[i]), list);
					}
				}
			} else { // folder is a file, not a folder
				addSinglePath(folder, list);
			}
		}
	}
	
	protected void addSinglePath(IPath p, ArrayList<IPath> list) {
		Iterator<IPath> i = list.iterator();
		IPath l;
		while(i.hasNext()) {
			l = i.next();
			if( !p.toFile().exists() || 
					(p.lastSegment().equals(l.lastSegment()) 
						&& p.toFile().length() == l.toFile().length() )) {
				return;
			}
		}
		list.add(p);
	}

}
