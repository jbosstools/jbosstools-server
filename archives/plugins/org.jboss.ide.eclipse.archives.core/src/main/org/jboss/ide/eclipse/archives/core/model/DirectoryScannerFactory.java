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
package org.jboss.ide.eclipse.archives.core.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.asf.DirectoryScanner;
import org.jboss.ide.eclipse.archives.core.util.ModelUtil;
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

	public static DirectoryScannerExtension createDirectoryScanner(IArchiveFileSet fs, boolean scan) {
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
			String includes = fs.includes == null ? "" : fs.includes;
			String excludes = fs.excludes == null ? "" : fs.excludes;
			String includesList[] = includes.split(" ?, ?");
			String excludesList[] = excludes.split(" ?, ?");
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
				setBasedir(new FileWrapper(p.toFile(), translatedPath));
			} else {
				setBasedir(new FileWrapper(translatedPath.toFile(), translatedPath));
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
	    
	    protected File[] list2workspace(File file) {
	    	IPath workspaceRelative = ((FileWrapper)file).getWrapperPath(); 
	    	if( workspaceRelative == null )
	    		return new File[0];
	    	
	    	IPath[] childrenWorkspace = ArchivesCore.getInstance()
	    			.getVFS().getWorkspaceChildren(workspaceRelative);
	    	IPath[] childrenAbsolute = globalize(childrenWorkspace);
	    	File[] files = new File[childrenAbsolute.length];
	    	for( int i = 0; i < files.length; i++ ) {
	    		files[i] = new FileWrapper(childrenAbsolute[i].toFile(), childrenWorkspace[i]);
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
	    	FileWrapper[] children2 = new FileWrapper[children.length];
	    	for( int i = 0; i < children.length; i++ )
	    		children2[i] = new FileWrapper(children[i], new Path(children[i].getAbsolutePath()));
	    	return children2;
	    }

	    protected void postInclude(File f, String relative) {
	    	if( f.isFile() ) {
		    	if( f instanceof FileWrapper ) {
		    		FileWrapper f2 = ((FileWrapper)f);
		    		f2.setFilesetRelative(relative);
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

	    // what files are being added
	    public FileWrapper[] getMatchedArray() {
	    	return (FileWrapper[]) matches.toArray(new FileWrapper[matches.size()]);
	    }
	    
	    public HashMap<String, ArrayList<FileWrapper>> getMatchedMap() {
	    	return matchesMap;
	    }
	    
	    public class FileWrapper extends File {
	    	// The actual source file
	    	File f;
	    	
	    	// The path of this file, either workspace relative or global
	    	IPath path;
	    	
	    	// the path of this file relative to the fileset
	    	String fsRelative;
	    	
			public FileWrapper(File delegate, IPath path2) {
				super(delegate.getAbsolutePath());
				f = delegate;
				path = path2;
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
				if( fs.rootArchiveRelativePath != null )
					return fs.rootArchiveRelativePath.append(fsRelative);
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
	    	return super.isIncluded(name) && !super.isExcluded(name);
	    }
	}
}
