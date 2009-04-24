/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.extensions.descriptors;

import org.apache.tools.ant.DirectoryScanner;

/**
 * A class to scann files and folders for files that match a pattern
 * Delegates to a DirectoryScanner provided by the ant plugins
 * @author rob.stryker@redhat.com
 */
public class AntFileFilter {
	private String includes;
	private String baseDir;
	private boolean hasScanned;
	private transient DirectoryScanner scanner;
	public AntFileFilter(String baseDir, String includes) {
		this.includes = includes == null ? "**/*.xml" : includes; //$NON-NLS-1$
		this.baseDir = baseDir;
		this.scanner = new DirectoryScanner();
		String includesList[] = this.includes.split(" ?, ?"); //$NON-NLS-1$
		scanner.setBasedir(baseDir);
		scanner.setIncludes(includesList);
	}
	public String getBaseDir() { return baseDir; }
	public String getIncludes() { return includes; }
	public void setIncludes(String includes) { this.includes = includes; }
	public void setBaseDir(String baseDir) { this.baseDir = baseDir; }
	public String[] getIncludedFiles() {
		if( !hasScanned ) {
			hasScanned = true;
			scanner.scan();
		}
		return scanner.getIncludedFiles();
	}
}
