/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.core.runtime.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.classpath.core.runtime.IRuntimePathProvider;
import org.jboss.ide.eclipse.as.classpath.core.runtime.path.internal.LayeredProductPathProvider;
import org.jboss.ide.eclipse.as.core.resolvers.RuntimeVariableResolver;
import org.jboss.tools.foundation.core.expressions.ExpressionResolver;
import org.jboss.tools.foundation.core.expressions.IVariableResolver;

public class PathProviderResolutionUtil {
	
	/**
	 * This is an internal class used during creation 
	 * of the set of classpath entries. It should not
	 * be used or referenced by clients. 
	 */
	private static class Entry {
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
	
	public static IClasspathEntry getEntry(Entry entry) {
		return JavaRuntime.newArchiveRuntimeClasspathEntry(entry.getPath()).getClasspathEntry();
	}

	public static List<IClasspathEntry> convert(Collection<Entry> list) {
		List<IClasspathEntry> fin = new ArrayList<IClasspathEntry>();
		Iterator<Entry> i = list.iterator();
		while(i.hasNext()) {
			fin.add(getEntry(i.next()));
		}
		return fin;
	}
	public static void addSinglePath(IPath p, ArrayList<Entry> list) {
		if (!p.toFile().exists()) {
			return;
		}
		list.add(new Entry(p, p.lastSegment(), p.toFile().length()));
	}
	
	public static IPath[] getAllPaths(IRuntime runtime, IRuntimePathProvider[] sets) {
		return getAllPaths(new RuntimeVariableResolver(runtime), sets);
	}

	public static IPath[] getAllPaths(Map<String, String> map, IRuntimePathProvider[] sets) {
		return getAllPaths(new ExpressionResolver.MapVariableResolver(map), sets);
	}
	
	public static IPath[] getAllPaths(IVariableResolver resolver, IRuntimePathProvider[] sets) {
		ArrayList<IPath> retval = new ArrayList<IPath>();
		for( int i = 0; i < sets.length; i++ ) {
			sets[i].setVariableResolver(resolver);
			IPath[] absolute = sets[i].getAbsolutePaths();
			for( int j = 0; j < absolute.length; j++ ) {
				if( !retval.contains(absolute[j]))
					retval.add(absolute[j]);
			}
		}
		return (IPath[]) retval.toArray(new IPath[retval.size()]);
	}
	
	public static  IClasspathEntry[] getClasspathEntriesForResolvedPaths(IPath[] allPaths) {
		ArrayList<Entry> entries = new ArrayList<Entry>();
		for( int i = 0; i < allPaths.length; i++ ) {
			addSinglePath(allPaths[i], entries);
		}
		List<IClasspathEntry> ret = convert(entries);
		return (IClasspathEntry[]) ret.toArray(new IClasspathEntry[ret.size()]);
	}
}
