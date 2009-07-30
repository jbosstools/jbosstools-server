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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;


public class Fileset implements Cloneable {
	private static final String HASH_SEPARATOR = "::_::"; //$NON-NLS-1$
	private static final String SEP = "\n"; //$NON-NLS-1$
	private String name, folder, includesPattern, excludesPattern;
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
	/**
	 * @return the folder
	 */
	public String getFolder() {
		String tmp = folder == null ? "" : folder;  //$NON-NLS-1$
		
		// TODO do the string replacement! perhaps use variables plugin
		IJBossServerRuntime ajbsrt = (IJBossServerRuntime) server.getRuntime()
		.loadAdapter(IJBossServerRuntime.class,
				new NullProgressMonitor());
		String config = null;
		if( ajbsrt != null ) 
			config = ajbsrt.getJBossConfiguration();
		if( config != null )
			tmp = tmp.replace("${config}", config); //$NON-NLS-1$
		
		IPath p = new Path(tmp);
		if( !p.isAbsolute() && server != null ) {
			if( server.getRuntime() != null ) 
				p = server.getRuntime().getLocation().append(p);
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
	
	public IServer getServer() { return this.server; }
	public void setServer(IServer server) { this.server = server; }

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
}