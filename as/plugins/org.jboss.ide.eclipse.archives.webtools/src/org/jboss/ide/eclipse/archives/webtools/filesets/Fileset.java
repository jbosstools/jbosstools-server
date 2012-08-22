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
package org.jboss.ide.eclipse.archives.webtools.filesets;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.archives.core.asf.DirectoryScanner;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory;
import org.jboss.ide.eclipse.as.core.resolvers.ConfigNameResolver;


public class Fileset implements Cloneable {
	private static final String HASH_SEPARATOR = "::_::"; //$NON-NLS-1$
	private static final String SEP = "\n"; //$NON-NLS-1$
	private String name, folder, includesPattern, excludesPattern;
	private IRuntime runtime;
	private IServer server;
	
	public Fileset() {
	}
	public Fileset(String string) {
		name = folder = includesPattern =excludesPattern = ""; //$NON-NLS-1$
		String[] parts = string.split(SEP);
		name = parts.length <= 0 ? null : parts[0];
		folder = parts.length <= 1 ? null : parts[1];
		includesPattern = parts.length <= 2 ? null : parts[2];
		excludesPattern = parts.length <= 3 ? null : parts[3];
	}

	public Fileset(String name, String folder, String inc, String exc) {
		this.name = name;
		this.folder = folder;
		includesPattern = inc;
		excludesPattern = exc;
	}
	public String toString() {
		return name + SEP + folder + SEP + includesPattern + SEP + excludesPattern;
	}
	
    /**
	 * @return the folder
	 */
	public String getFolder() {
		return getFolder(folder, runtime);
	}
	
	public static String getFolder(String folder, IRuntime runtime) {
		return getFolder(folder, runtime, true);
	}
	public static String getFolder(String folder, IRuntime runtime, boolean ignoreError) {
		String tmp = new ConfigNameResolver().performSubstitutions(folder, runtime == null ? null : runtime.getName(), ignoreError);
		IPath p = new Path(tmp);
		if( !p.isAbsolute() && runtime != null ) {
			p = runtime.getLocation().append(p);
		}
		return p.toString();
	}
	
	public String getRawFolder() {
		return folder == null ? "" : folder; //$NON-NLS-1$
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name == null ? "" : name; //$NON-NLS-1$
	}
	/**
	 * @return the excludesPattern
	 */
	public String getExcludesPattern() {
		return excludesPattern == null ? "" : excludesPattern; //$NON-NLS-1$
	}
	
	public String getResolvedExclude() {
		String pattern = excludesPattern == null ? "" : excludesPattern; //$NON-NLS-1$
		String resolved = new ConfigNameResolver().performSubstitutions(pattern, runtime == null ? null : runtime.getName(), true);
		return resolved;
	}
	/**
	 * @return the includesPattern
	 */
	public String getIncludesPattern() {
		return includesPattern == null ? "" : includesPattern; //$NON-NLS-1$
	}
	public String getResolvedIncludesPattern() {
		String pattern = includesPattern == null ? "" : includesPattern; //$NON-NLS-1$
		String resolved = new ConfigNameResolver().performSubstitutions(pattern, runtime == null ? null : runtime.getName(), true);
		return resolved;
	}

	/**
	 * @param excludesPattern the excludesPattern to set
	 */
	public void setExcludesPattern(String excludesPattern) {
		this.excludesPattern = excludesPattern;
	}

	/**
	 * @param folder the folder to set
	 */
	public void setFolder(String folder) {
		this.folder = folder;
	}

	/**
	 * @param includesPattern the includesPattern to set
	 */
	public void setIncludesPattern(String includesPattern) {
		this.includesPattern = includesPattern;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public IServer getServer() { 
		return this.server; 
	}
	public void setServer(IServer server) { 
		this.server = server;
		this.runtime = server == null ? null : server.getRuntime();
	}
	public IRuntime getRuntime() {
		return runtime;
	}
	public void setRuntime(IRuntime rt) {
		runtime = rt;
		if( server == null || server.getRuntime().equals(rt))
			server = null;
	}
	
	public Object clone() {
		Fileset fs = new Fileset(name, folder, includesPattern, excludesPattern);
		fs.setServer(server);
		fs.setRuntime(runtime);
		return fs;
	}

	public boolean equals(Object other) {
		if( !(other instanceof Fileset)) return false;
		if( other == this ) return true;
		Fileset o = (Fileset)other;
		return o.getName().equals(getName()) && o.getFolder().equals(getFolder())
			&& o.getIncludesPattern().equals(getIncludesPattern()) && o.getExcludesPattern().equals(getExcludesPattern());
	}
	public int hashCode() {
		return (name + HASH_SEPARATOR +  folder + HASH_SEPARATOR +  includesPattern + HASH_SEPARATOR +  excludesPattern + HASH_SEPARATOR).hashCode();
	}
	
	public IPath[] findPaths() {
		String dir = getFolder();
		String includes = getResolvedIncludesPattern();
		String excludes = getResolvedExclude();
		return findPaths(dir, includes, excludes);
	}
	
	/**
	 * This method intentionally will not log any exceptions. It will only
	 * return an empty array if the scanner is incapable of scanning
	 * with the given parameters. 
	 * 
	 * @param dir
	 * @param includes
	 * @param excludes
	 * @return
	 */
	public static IPath[] findPaths(String dir, String includes, String excludes) {
		try {
			if (dir != null && new File(dir).exists()) {
				DirectoryScanner scanner = DirectoryScannerFactory
						.createDirectoryScanner(dir, null, includes, excludes,
								null, false, 1, true);
				if (scanner != null) {
					String[] files = scanner.getIncludedFiles();
					IPath[] paths = new IPath[files.length];
					for (int i = 0; i < files.length; i++) {
						paths[i] = new Path(files[i]);
					}
					return paths;
				}
			}
		} catch (IllegalStateException ise) {
			return new IPath[0];
		}
		return new IPath[0];
	}

	
}