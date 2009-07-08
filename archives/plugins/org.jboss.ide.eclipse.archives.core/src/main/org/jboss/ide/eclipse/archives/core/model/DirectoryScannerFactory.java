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
package org.jboss.ide.eclipse.archives.core.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.asf.DirectoryScanner;
import org.jboss.ide.eclipse.archives.core.util.PathUtils;

/**
 * Utility methods to create scanners for matching
 * @author rob.stryker@jboss.com
 */
public class DirectoryScannerFactory {
	private static class ScannableFileSet {
		public String rawPath;
		public IPath rootArchiveRelativePath;
		public String includes;
		public String excludes;
		public boolean inWorkspace;
		public String projectName;
		public double version;
	};

	public static DirectoryScannerExtension createDirectoryScanner(IArchiveStandardFileSet fs, boolean scan) {
		return createDirectoryScanner(fs.getRawSourcePath(), fs.getRootArchiveRelativePath(),
				fs.getIncludesPattern(), fs.getExcludesPattern(), fs.getProjectName(),
				fs.isInWorkspace(), fs.getDescriptorVersion(), scan);
	}

	public static DirectoryScannerExtension createDirectoryScanner (
			String rawPath, IPath rootArchiveRelativePath,
			String includes, String excludes, String projectName,
			boolean inWorkspace, double version, boolean scan) {

		ScannableFileSet fs = new ScannableFileSet();
		fs.rawPath = rawPath;
		fs.rootArchiveRelativePath = rootArchiveRelativePath;
		fs.includes = includes;
		fs.excludes = excludes;
		fs.inWorkspace = inWorkspace;
		fs.projectName = projectName;
		fs.version = version;
		DirectoryScannerExtension scanner = new DirectoryScannerExtension(fs);
		if (scan) {
			scanner.scan();
		}
		return scanner;
	}


	/**
	 * Exposes the isIncluded method so that entire scans do not need to occur
	 * to find matches.
	 *
	 * Overwrites
	 */
	public static class DirectoryScannerExtension extends DirectoryScanner {
		protected boolean workspaceRelative;
		protected ScannableFileSet fs;
		protected ArrayList<FileWrapper> matches;
		protected HashMap<String, ArrayList<FileWrapper>> matchesMap;
		public DirectoryScannerExtension(ScannableFileSet fs) {
			this.fs = fs;
			String includes = fs.includes == null ? "" : fs.includes; //$NON-NLS-1$
			String excludes = fs.excludes == null ? "" : fs.excludes; //$NON-NLS-1$
			String includesList[] = includes.split(" ?, ?"); //$NON-NLS-1$
			String excludesList[] = excludes.split(" ?, ?"); //$NON-NLS-1$
			setExcludes(excludesList);
			setIncludes(includesList);
			workspaceRelative = fs.inWorkspace;
			matches = new ArrayList<FileWrapper>();
			matchesMap = new HashMap<String, ArrayList<FileWrapper>>();
			setBasedir2(fs.rawPath);
		}

		public void setBasedir2(String path) {

			IPath translatedPath = new Path(PathUtils.getAbsoluteLocation(path, fs.projectName, fs.inWorkspace, fs.version));
			if( workspaceRelative ) {
				IPath p = PathUtils.getGlobalLocation(path, fs.projectName, true, fs.version);
				setBasedir(new FileWrapper(p.toFile(), translatedPath, fs.rootArchiveRelativePath));
			} else {
				setBasedir(new FileWrapper(translatedPath.toFile(), translatedPath,  fs.rootArchiveRelativePath));
			}
		}

		protected String getName(File file) {
	    	return workspaceRelative ? ((FileWrapper)file).getOutputName() : super.getName(file);
	    }

	    /* Only used when workspace relative! */
	    protected File[] list2(File file) {
	    	if( fs.inWorkspace )
	    		return list2workspace(file);
	    	else
	    		return list2absolute(file);
	    }

	    protected File getChild(File file, String element) {
	    	if( !fs.inWorkspace)
	    		return new FileWrapper(file, new Path(file.getAbsolutePath()), fs.rootArchiveRelativePath);
	    	FileWrapper pWrapper = (FileWrapper)file;
	    	File child = super.getChild(file, element);
	    	FileWrapper childWrapper = new FileWrapper(child, pWrapper.getWrapperPath().append(element), fs.rootArchiveRelativePath);
	    	return childWrapper;
	    }
	    
	    protected File[] list2workspace(File file) {
	    	IPath workspaceRelative = ((FileWrapper)file).getWrapperPath();
	    	if( workspaceRelative == null )
	    		return new File[0];

	    	IPath[] childrenWorkspace = ArchivesCore.getInstance()
	    			.getVFS().getWorkspaceChildren(workspaceRelative);
	    	IPath[] childrenAbsolute = globalize(childrenWorkspace);
	    	File[] files = new File[childrenAbsolute.length];
	    	for( int i = 0; i < files.length; i++ ) {
	    		files[i] = new FileWrapper(childrenAbsolute[i].toFile(), childrenWorkspace[i], fs.rootArchiveRelativePath);
	    	}
	    	return files;
	    }

	    protected IPath[] globalize(IPath[] paths) {
			IPath[] results = new IPath[paths.length];
			for( int i = 0; i < paths.length; i++ )
				results[i] = ArchivesCore.getInstance()
    			.getVFS().workspacePathToAbsolutePath(paths[i]);
			return results;
	    }

	    protected File[] list2absolute(File file) {
	    	File[] children = file.listFiles();
	    	if( children != null ) {
		    	FileWrapper[] children2 = new FileWrapper[children.length];
		    	for( int i = 0; i < children.length; i++ )
		    		children2[i] = new FileWrapper(children[i], new Path(children[i].getAbsolutePath()), fs.rootArchiveRelativePath);
		    	return children2;
	    	} 
	    	return new FileWrapper[]{};
	    }

	    protected void postInclude(File f, String relative) {
	    	super.postInclude(f, relative);
	    	if( f instanceof FileWrapper ) {
	    		FileWrapper f2 = ((FileWrapper)f);
	    		f2.setFilesetRelative(relative);
		    	if( f.isFile() ) {
		    		matches.add(f2);
		    		ArrayList<FileWrapper> l = matchesMap.get(f2);
		    		if( l == null ) {
		    			l = new ArrayList<FileWrapper>();
		    			matchesMap.put(((FileWrapper)f).getAbsolutePath(), l);
		    		}
		    		l.add(f2);
		    	}
	    	}
	    }
	    
	    protected boolean isSelected(String name, File file) {
	    	return super.isSelected(name, file) && file.isFile();
	    }


	    // what files are being added
	    public FileWrapper[] getMatchedArray() {
	    	return (FileWrapper[]) matches.toArray(new FileWrapper[matches.size()]);
	    }

	    public HashMap<String, ArrayList<FileWrapper>> getMatchedMap() {
	    	return matchesMap;
	    }

	    public static class FileWrapper extends File {
	    	// The actual source file
	    	File f;

	    	// The path of this file, either workspace relative or global
	    	IPath path;

	    	// the path of this file relative to the fileset
	    	String fsRelative;
	    	IPath rootArchiveRelativePath;
	    	public FileWrapper(File delegate, IPath path2, IPath rootArchiveRelative) {
				super(delegate.getAbsolutePath());
				f = delegate;
				path = path2;
				rootArchiveRelativePath = rootArchiveRelative;
			}
	    	public FileWrapper(File delegate, IPath path2, IPath rootArchiveRelative, String fsRelative) {
	    		this(delegate, path2, rootArchiveRelative);
	    		this.fsRelative = fsRelative;
			}
			
			public IPath getWrapperPath() {
				return path;
			}
			// workspace name is the one we care about, or absolute if not in workspace
			public String getOutputName() {
				return path.lastSegment();
			}
			public String getFilesetRelative() {
				return fsRelative;
			}

			void setFilesetRelative(String s) {
				fsRelative = s;
			}

			public IPath getRootArchiveRelative() {
				if( rootArchiveRelativePath != null )
					return rootArchiveRelativePath.append(fsRelative);
				return null;
			}

			public boolean equals(Object o) {
				if( o instanceof FileWrapper ) {
					FileWrapper fo = (FileWrapper)o;
					return f.equals(fo.f) && path.equals(fo.path);
				}
				return false;
			}
	    }

	    public boolean couldBeIncluded(String name, boolean inWorkspace) {
	    	IPath targetBase = ((FileWrapper)getBasedir()).getWrapperPath();
	    	IPath[] questionFiles = new IPath[] { new Path(name) };
	    	if( workspaceRelative && !inWorkspace) {
	    		questionFiles = ArchivesCore.getInstance().getVFS().absolutePathToWorkspacePath(questionFiles[0]);
	    	} else if( !workspaceRelative && inWorkspace) {
	    		questionFiles[0] = ArchivesCore.getInstance().
	    				getVFS().workspacePathToAbsolutePath(questionFiles[0]);
	    	}
	    	ArrayList<IPath> acceptablePaths = new ArrayList<IPath>();
	    	for( int i = 0; i < questionFiles.length; i++ ) {
	    		if( targetBase.isPrefixOf(questionFiles[i]))
	    			acceptablePaths.add(questionFiles[i].removeFirstSegments(targetBase.segmentCount()));
	    	}
	    	
	    	if( acceptablePaths.size() == 0 )
	    		return false;
	    	
	    	IPath p;
	    	for( int i = 0; i < acceptablePaths.size(); i++ ) {
	    		p = acceptablePaths.get(i);
	    		if( super.isIncluded(p.toString()) && !super.isExcluded(p.toString()))
	    			return true;
	    	}
	    	return false;
	    }
	}
}
