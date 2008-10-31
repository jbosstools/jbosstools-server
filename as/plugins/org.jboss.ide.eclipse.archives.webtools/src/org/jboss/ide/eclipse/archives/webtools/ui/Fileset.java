package org.jboss.ide.eclipse.archives.webtools.ui;


public class Fileset implements Cloneable {
	private static final String HASH_SEPARATOR = "::_::"; //$NON-NLS-1$
	private String name, folder, includesPattern, excludesPattern;
	public Fileset() {
	}
	public Fileset(String string) {
		try {
			name = folder = includesPattern =excludesPattern = ""; //$NON-NLS-1$
			String[] parts = string.split("\n"); //$NON-NLS-1$
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
		return name + "\n" + folder + "\n" + includesPattern + "\n" + excludesPattern;   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	}
	/**
	 * @return the folder
	 */
	public String getFolder() {
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