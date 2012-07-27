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

package org.jboss.ide.eclipse.as.classpath.core.runtime;

import java.io.File;
import java.io.FileFilter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.jboss.ide.eclipse.as.classpath.core.ClasspathCorePlugin;
import org.jboss.ide.eclipse.as.classpath.core.Messages;
import org.jboss.ide.eclipse.as.classpath.core.runtime.CustomRuntimeClasspathModel.IDefaultPathProvider;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;

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
		implements IJBossServerConstants {

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
		runtimeClasspath = getClasspathEntriesForRuntime(runtime);
		ClasspathCorePlugin.getRuntimeClasspaths().put(key, runtimeClasspath);
		return runtimeClasspath;
	}

	protected List<IClasspathEntry> convert(Collection<Entry> list) {
		List<IClasspathEntry> fin = new ArrayList<IClasspathEntry>();
		Iterator<Entry> i = list.iterator();
		while(i.hasNext()) {
			fin.add(getEntry(i.next()));
		}
		return fin;
	}
	
	protected IClasspathEntry[] getClasspathEntriesForRuntime(IRuntime rt) {
		IDefaultPathProvider[] sets = CustomRuntimeClasspathModel.getInstance().getEntries(rt.getRuntimeType());
		IPath[] allPaths = CustomRuntimeClasspathModel.getInstance().getAllEntries(rt, sets);
		ArrayList<Entry> entries = new ArrayList<Entry>();
		for( int i = 0; i < allPaths.length; i++ ) {
			addSinglePath(allPaths[i], entries);
		}
		List<IClasspathEntry> ret = convert(entries);
		return (IClasspathEntry[]) ret.toArray(new IClasspathEntry[ret.size()]);
	}
	
	protected IClasspathEntry getEntry(Entry entry) {
		return JavaRuntime.newArchiveRuntimeClasspathEntry(entry.getPath()).getClasspathEntry();
	}
	
	protected void addSinglePath(IPath p, ArrayList<Entry> list) {
		if (!p.toFile().exists()) {
			return;
		}
		list.add(new Entry(p, p.lastSegment(), p.toFile().length()));
	}
}
