package org.jboss.ide.eclipse.as.core.extensions.descriptors;

import org.apache.tools.ant.DirectoryScanner;

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
