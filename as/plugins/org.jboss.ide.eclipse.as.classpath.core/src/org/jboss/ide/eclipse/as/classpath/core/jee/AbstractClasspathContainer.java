/*
 * JBoss, a division of Red Hat
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
package org.jboss.ide.eclipse.as.classpath.core.jee;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathCorePlugin;
import org.jboss.ide.eclipse.as.classpath.core.xpl.ClasspathDecorations;
import org.jboss.ide.eclipse.as.classpath.core.xpl.ClasspathDecorationsManager;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public abstract class AbstractClasspathContainer implements IClasspathContainer {

	public static final String LIB_FOLDER = "lib";
	public static final String LIB_SOURCE_FOLDER = "libsrc";
	public final static String CLASSPATH_CONTAINER_PREFIX = "org.jboss.ide.eclipse.as.classpath.core";
	
	protected IClasspathEntry[] entries;
	protected IPath path;
	protected String description;
	protected String libFolder;

	protected static ClasspathDecorationsManager decorations;
	static {
		
		decorations = new ClasspathDecorationsManager();
	}
	
	public AbstractClasspathContainer(IPath path, String description,
			String libFolder) {
		this.path = path;
		this.description = description;
		this.libFolder = libFolder;
	}

	public IClasspathEntry[] getClasspathEntries() {
		if (entries == null) {
			entries = computeEntries();
		}
		return entries;
	}

	public String getDescription() {
		return this.description;
	}

	public int getKind() {
		return IClasspathContainer.K_APPLICATION;
	}

	public IPath getPath() {
		return this.path;
	}

	protected IClasspathEntry[] computeEntries() {
		ArrayList<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();

		String baseDir = getBaseDir();
		if (baseDir == null)
			return new IClasspathEntry[0];

		File libDir = new File(baseDir
				+ "/" + LIB_FOLDER + "/" + getLibFolder());//$NON-NLS-1$ //$NON-NLS-2$
		File libSrcDir = new File(baseDir
				+ "/" + LIB_SOURCE_FOLDER + "/" + getLibFolder());//$NON-NLS-1$ //$NON-NLS-2$

		// Lists every modules in the lib dir
		File[] jars = libDir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return (file.toString().endsWith(".jar"));//$NON-NLS-1$
			}
		});

		if (jars != null) {
			for (int i = 0; i < jars.length; i++) {
				File jarFile = jars[i];
				String jarFileName = jarFile.getName();
				File jarSrcFile = new File(libSrcDir, jarFileName);

				IPath entryPath = new Path(jarFile.toString());

				IPath sourceAttachementPath = null;
				IPath sourceAttachementRootPath = null;

				final ClasspathDecorations dec 
	            = decorations.getDecorations( getDecorationManagerKey(getPath().toString()), entryPath.toString() );
	        
	        
				IClasspathAttribute[] attrs = {};
				if( dec != null ) {
		            sourceAttachementPath = dec.getSourceAttachmentPath();
		            sourceAttachementRootPath = dec.getSourceAttachmentRootPath();
		            attrs = dec.getExtraAttributes();
		        } else if (jarSrcFile.exists()) {
					sourceAttachementPath = new Path(jarSrcFile.toString());
					sourceAttachementRootPath = new Path("/");//$NON-NLS-1$
				}
		        
				IAccessRule[] access = {};
				IClasspathEntry entry = JavaCore.newLibraryEntry( entryPath, sourceAttachementPath, 
						sourceAttachementRootPath, access, attrs, false );
				entries.add(entry);
			}
		}

		return entries.toArray(new IClasspathEntry[entries.size()]);
	}

	protected String getLibFolder() {
		return this.libFolder;
	}

	protected String getBaseDir() {
		try {
			URL installURL = FileLocator.toFileURL(ClasspathCorePlugin
					.getDefault().getBundle().getEntry("/"));
			return installURL.getFile().toString();
		} catch (IOException ioe) {
			// LOG THE ERROR (one day)
			IStatus status = new Status(IStatus.ERROR, ClasspathCorePlugin.PLUGIN_ID, "Error loading classpath container", ioe);
			ClasspathCorePlugin.getDefault().getLog().log(status);
		}
		return null;
	}
	
	public static String getDecorationManagerKey( String container){
    	return container;
    }
	
	protected static ClasspathDecorationsManager getDecorationsManager() {
        return decorations;
    }

	public void refresh() {
		entries = computeEntries();
	}

}
