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
import java.io.FileFilter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jst.server.core.RuntimeClasspathProviderDelegate;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathConstants;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathCorePlugin;
import org.jboss.ide.eclipse.as.classpath.core.Messages;
import org.jboss.ide.eclipse.as.classpath.core.RuntimeKey;

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

	public static class Entry {
		private IPath path;
		private String name;
		private long length;
		
		public Entry(IPath path, String name, long length) {
			super();
			this.path = path;
			this.name = name;
			this.length = length;
		}
		
		public IPath getPath() {
			return path;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (length ^ (length >>> 32));
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Entry other = (Entry) obj;
			if (length != other.length)
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	}
	
	public IClasspathEntry[] resolveClasspathContainer(IProject project, IRuntime runtime) {
		if( runtime == null ) 
			return new IClasspathEntry[0];

		RuntimeKey key = ClasspathCorePlugin.getRuntimeKey(runtime);
		if( key == null ) {
			// log error
			IStatus status = new Status(IStatus.WARNING, ClasspathCorePlugin.PLUGIN_ID, MessageFormat.format(Messages.ClientAllRuntimeClasspathProvider_wrong_runtime_type,
					runtime.getName()));
			ClasspathCorePlugin.getDefault().getLog().log(status);
			return new IClasspathEntry[0];
		}
		IClasspathEntry[] runtimeClasspath = ClasspathCorePlugin.getRuntimeClasspaths().get(key);
		if (runtimeClasspath != null) {
			return runtimeClasspath;
		}
		IPath loc = key.getLocation();
		IPath configPath = key.getConfigPath();
		String rtID  = key.getId();
		Set<Entry> list = new HashSet<Entry>();
		if(AS_32.equals(rtID)) list = get32(loc, configPath);
		if(AS_40.equals(rtID)) list = get40(loc,configPath);
		if(AS_42.equals(rtID)) list = get42(loc,configPath);
		if(AS_50.equals(rtID)) list = get50(loc,configPath);
		if(EAP_43.equals(rtID)) list = getEAP43(loc,configPath);
		
		// Added cautiously, not sure on changes, may change
		if(AS_51.equals(rtID)) list = get50(loc,configPath);
		if(AS_60.equals(rtID)) list = get60(loc,configPath);
		if(EAP_50.equals(rtID)) list = get50(loc,configPath);
		
		if( AS_70.equals(rtID)) list = get70(loc);
		
		if( list == null ) {
			runtimeClasspath = new IClasspathEntry[0];
		} else {
			List<IClasspathEntry> entries = convert(list);
			runtimeClasspath = entries.toArray(new IClasspathEntry[entries.size()]);
		}
		ClasspathCorePlugin.getRuntimeClasspaths().put(key, runtimeClasspath);
		return runtimeClasspath;
	}

	protected List<IClasspathEntry> convert(Set<Entry> list) {
		List<IClasspathEntry> fin = new ArrayList<IClasspathEntry>();
		Iterator<Entry> i = list.iterator();
		while(i.hasNext()) {
			fin.add(getEntry(i.next()));
		}
		return fin;
	}
	
	protected Set<Entry> get32(IPath location, IPath configPath) {
		Set<Entry> list = new HashSet<Entry>();
		addPaths(location.append(LIB), list);
		addPaths(configPath.append(LIB), list);
		addPaths(location.append(CLIENT), list);
		return list;
	}
	
	protected Set<Entry> get40(IPath location, IPath configPath) {
		Set<Entry> list = new HashSet<Entry>();
		addPaths(location.append(LIB), list);
		addPaths(configPath.append(LIB), list);
		IPath deployPath = configPath.append(DEPLOY);
		addPaths(deployPath.append(JBOSS_WEB_DEPLOYER).append(JSF_LIB), list);
		addPaths(deployPath.append(AOP_JDK5_DEPLOYER), list);
		addPaths(deployPath.append(EJB3_DEPLOYER), list);
		addPaths(location.append(CLIENT), list);
		return list;
	}

	protected Set<Entry> get42(IPath location, IPath configPath) {
		return get40(location, configPath);
	}

	protected Set<Entry> getEAP43(IPath location, IPath configPath) {
		return get40(location, configPath);
	}
	
	protected Set<Entry> get50(IPath location, IPath configPath) {
		Set<Entry> list = new HashSet<Entry>();
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
	
	protected Set<Entry> get60(IPath location, IPath configPath) {
		Set<Entry> list = new HashSet<Entry>();
		list.addAll(get50(location, configPath));
		addPaths(configPath.append(DEPLOYERS).append(REST_EASY_DEPLOYER), list);
		addPaths(configPath.append(DEPLOYERS).append(JSF_DEPLOYER).append(MOJARRA_20).append(JSF_LIB), list);
		return list;
	}
	
	protected Set<Entry> get70(IPath location) {
		Set<Entry> list = new HashSet<Entry>();
		SimpleFileFilter filter = new SimpleFileFilter(new String[]{"jsf-api-1.2_13.jar", "jsf-impl-1.2_13.jar"}); // Problematic jar //$NON-NLS-1$
		addPaths(location.append(AS7_MODULES).append(JAVAX), list, true, filter);
		addPaths(location.append(AS7_MODULES).append("org/hibernate/validator"),list, true);
		addPaths(location.append(AS7_MODULES).append("org/resteasy"),list, true);
		addPaths(location.append(AS7_MODULES).append("org/picketbox"),list, true);
		
		return list;
	}
	
	protected IClasspathEntry getEntry(Entry entry) {
		return JavaRuntime.newArchiveRuntimeClasspathEntry(entry.getPath()).getClasspathEntry();
	}

	protected void addPaths(IPath folder, Set<Entry> list) {
		addPaths(folder, list, false);
	}
	
	protected class SimpleFileFilter implements FileFilter {
		private List<String> ignore;
		public SimpleFileFilter(String[] ignore) {
			this.ignore = Arrays.asList(ignore);
		}
		public boolean accept(File pathname) {
			if( !pathname.getName().endsWith(EXT_JAR)) return false;
			boolean contains = ignore.contains(pathname.getName());
			return !contains;
		}
	}
	
	protected void addPaths(IPath folder, Set<Entry> list, boolean recurse) {
		addPaths(folder, list, recurse, new SimpleFileFilter(new String[]{"jaxb-xjc.jar"})); //$NON-NLS-1$
	}
	
	protected void addPaths(IPath folder, Set<Entry> list, boolean recurse, FileFilter filter) {
		if( folder.toFile().exists()) {
			File f = folder.toFile();
			if(f.isDirectory()) {
				File[] asFiles = f.listFiles();
				for( int i = 0; i < asFiles.length; i++ ) {
					if( filter == null || filter.accept(folder.append(asFiles[i].getName()).toFile())) {
						addSinglePath(folder.append(asFiles[i].getName()), list);
					} else if( recurse && asFiles[i].isDirectory()) {
						addPaths(folder.append(asFiles[i].getName()), list, true, filter);
					}
				}
			} else { // item is a file, not a folder
				if( filter == null || filter.accept(folder.toFile()))
					addSinglePath(folder, list);
			}
		}
	}
	
	protected void addSinglePath(IPath p, Set<Entry> list) {
		if (!p.toFile().exists()) {
			return;
		}
		list.add(new Entry(p, p.lastSegment(), p.toFile().length()));
		
	}

}
