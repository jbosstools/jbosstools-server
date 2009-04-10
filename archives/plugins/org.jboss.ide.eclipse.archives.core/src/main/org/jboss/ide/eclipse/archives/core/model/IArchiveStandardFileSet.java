package org.jboss.ide.eclipse.archives.core.model;

public interface IArchiveStandardFileSet extends IArchiveFileSet {
	public static final String ATTRIBUTE_PREFIX = "org.jboss.ide.eclipse.archives.core.model.IPackageFileSet."; //$NON-NLS-1$
	public static final String INCLUDES_ATTRIBUTE = ATTRIBUTE_PREFIX + "includes"; //$NON-NLS-1$
	public static final String EXCLUDES_ATTRIBUTE = ATTRIBUTE_PREFIX + "excludes"; //$NON-NLS-1$
	public static final String IN_WORKSPACE_ATTRIBUTE = ATTRIBUTE_PREFIX + "inWorkspace"; //$NON-NLS-1$
	public static final String FLATTENED_ATTRIBUTE = ATTRIBUTE_PREFIX + "flattened"; //$NON-NLS-1$
	public static final String SOURCE_PATH_ATTRIBUTE = ATTRIBUTE_PREFIX + "sourcePath"; //$NON-NLS-1$

	/**
	 * @return Whether or not this fileset's basedir is inside the workspace
	 */
	public boolean isInWorkspace();

	/**
	 * @return Whether or not the fileset is flattened
	 */
	public boolean isFlattened();

	/**
	 * @return the source path from the delegate with no translation at all
	 */
	public String getRawSourcePath();

	/**
	 * @return The includes pattern for this fileset
	 */
	public String getIncludesPattern();

	/**
	 * @return The excludes pattern for this fileset
	 */
	public String getExcludesPattern();

	/**
	 * Sets the "root" or "source" of this fileset (file-system or workspace relative)
	 * @param path The absolute path that is the source of this fileset
	 */
	public void setRawSourcePath (String raw);

	/**
	 * Set the includes pattern for this fileset. This pattern uses the same syntax as Ant's include pattern.
	 * @param includes The includes pattern for this fileset
	 */
	public void setIncludesPattern(String includes);

	/**
	 * Set the excludes pattern for this fileset. This pattern uses the same syntax as Ant's exclude pattern.
	 * @param excludes The excludes pattern for this fileset
	 */
	public void setExcludesPattern(String excludes);

	/**
	 * Set whether or not this fileset's source is in the workspace. This will automatically be handled if you
	 * use setSingleFile, setSourceProject, setSourceContainer, or setSourceFolder.
	 * @param isInWorkspace Whether or not this fileset's source is in the workspace
	 */
	public void setInWorkspace(boolean isInWorkspace);

	/**
	 * Sets whether or not this fileset is flattened.
	 */
	public void setFlattened(boolean flattened);
}
