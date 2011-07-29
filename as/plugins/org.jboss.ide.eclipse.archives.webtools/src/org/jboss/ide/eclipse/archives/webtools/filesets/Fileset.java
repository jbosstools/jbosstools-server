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

import org.eclipse.core.internal.variables.StringSubstitutionEngine;
import org.eclipse.core.internal.variables.StringVariableManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.archives.core.asf.DirectoryScanner;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory;


public class Fileset implements Cloneable {
	private static final String HASH_SEPARATOR = "::_::"; //$NON-NLS-1$
	private static final String SEP = "\n"; //$NON-NLS-1$
	private String name, folder, includesPattern, excludesPattern;
	private IRuntime runtime;
	private IServer server;
	
	public Fileset() {
	}
	public Fileset(String string) {
		try {
			name = folder = includesPattern =excludesPattern = ""; //$NON-NLS-1$
			String[] parts = string.split(SEP);
			name = parts[0];
			folder = parts[1];
			includesPattern = parts[2];
			excludesPattern = parts[3];
		} catch( ArrayIndexOutOfBoundsException aioobe) {}
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
	
	public static final String JBOSS_CONFIG_DIR_ARG = "${jboss_config_dir}"; //$NON-NLS-1$
	public static final String JBOSS_SERVER_ARG = "${jboss_config}"; //$NON-NLS-1$
	
	public static final String getConfigDirSubstitute(IRuntime rt) {
		return "${jboss_config_dir:" + rt.getName() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static final String getServerSubstitute(IRuntime rt) {
		return "${jboss_config:" + rt.getName() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
    /**
	 * @return the folder
	 */
	public String getFolder() {
		return getFolder(folder, runtime);
	}
	
	public static String getFolder(String folder, IRuntime runtime) {
		String tmp = folder == null ? "" : folder;  //$NON-NLS-1$
		if( runtime != null ) {
			tmp = tmp.replace(JBOSS_CONFIG_DIR_ARG, getConfigDirSubstitute(runtime));
			tmp = tmp.replace(JBOSS_SERVER_ARG, getServerSubstitute(runtime));
		}
		try {
			StringSubstitutionEngine engine = new StringSubstitutionEngine();
			tmp = engine.performStringSubstitution(tmp, true,
					true, StringVariableManager.getDefault());
		} catch( CoreException ce ) {}

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
	/**
	 * @return the includesPattern
	 */
	public String getIncludesPattern() {
		return includesPattern == null ? "" : includesPattern; //$NON-NLS-1$
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
		try {
			return super.clone();
		} catch( Exception e ) {}
		return null;
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
		String includes = getIncludesPattern();
		String excludes = getExcludesPattern();
		return findPaths(dir, includes, excludes);
	}
	public static IPath[] findPaths(String dir, String includes, String excludes) {
		IPath[] paths = new IPath[0];
		try {
			if (dir != null && new File(dir).exists()) {
				DirectoryScanner scanner = DirectoryScannerFactory
						.createDirectoryScanner(dir, null, includes, excludes,
								null, false, 1, true);
				if (scanner != null) {
					String[] files = scanner.getIncludedFiles();
					paths = new IPath[files.length];
					for (int i = 0; i < files.length; i++) {
						paths[i] = new Path(files[i]);
					}
				}
			}
		} catch (IllegalStateException ise) {
		}
		return paths;
	}

	
}