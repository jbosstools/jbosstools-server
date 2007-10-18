/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
		this.includes = includes == null ? "**/*.xml" : includes;
		this.baseDir = baseDir;
		this.scanner = new DirectoryScanner();
		String includesList[] = this.includes.split(" ?, ?");
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
