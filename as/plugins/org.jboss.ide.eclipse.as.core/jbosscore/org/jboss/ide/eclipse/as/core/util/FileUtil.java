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
package org.jboss.ide.eclipse.as.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileUtil {

	public static interface IFileUtilListener {
		public void fileDeleted(File file, boolean result, Exception e);
		public void folderDeleted(File file, boolean result, Exception e);
		public void fileCoppied(File source, File dest, boolean result, Exception e);
	}
	
	
	// Delete the file. If it's a folder, delete all children.
	public static void safeDelete(File file) {
		safeDelete(file, null);
	}
	public static void safeDelete(File file, IFileUtilListener listener) {
		if( file.isDirectory() ) {
			File[] children = file.listFiles();
			if( children != null ) {
				for( int i = 0; i < children.length; i++ ) {
					safeDelete(children[i], listener);
				}
			}
			try {
				boolean tmp = file.delete();
				if( listener != null ) listener.folderDeleted(file, tmp, null);
			} catch( SecurityException sex) {
				if( listener != null ) listener.folderDeleted(file, false, sex);
			}
		}
		
		// files only
		try {
			boolean tmp = file.delete();
			if( listener != null ) listener.fileDeleted(file, tmp, null);
		} catch( SecurityException sex) {
			if( listener != null ) listener.fileDeleted(file, false, sex);
		}
	}
	
	// calls safedelete, but also deletes empty parent folders
	public static void completeDelete(File file) {
		completeDelete(file, null);
	}
	
	public static void completeDelete(File file, IFileUtilListener listener) {
		completeDelete(file, null, listener);
	}
	public static void completeDelete(File file, File archiveRoot, IFileUtilListener listener) {
		safeDelete(file, listener);
		//delete all empty parent folders
		while(!file.getParentFile().equals(archiveRoot) && file.getParentFile().listFiles().length == 0 ) {
			file = file.getParentFile();
			try {
				boolean tmp = file.delete();
				if( listener != null ) listener.folderDeleted(file, tmp, null);
			} catch( SecurityException sex ) {
				listener.folderDeleted(file, false, sex);
			}
		}
	}
	
	public static boolean fileSafeCopy(File src, File dest) {
		return fileSafeCopy(src, dest, null);
	}
	public static boolean fileSafeCopy(File src, File dest, IFileUtilListener listener) {
		File parent = dest.getParentFile();
		parent.mkdirs();
		if( dest.exists()) 
			safeDelete(dest);
		
		if (src.isDirectory()) {
			File[] subFiles = src.listFiles();
			boolean copied = true;
			dest.mkdirs();
			for (int i = 0; i < subFiles.length; i++) {
				File newDest = new File(dest, subFiles[i].getName());
				copied = copied && fileSafeCopy(subFiles[i], newDest, listener);
			}
			return copied;
		} else {
			try {
			    FileInputStream fis  = new FileInputStream(src);
			    FileOutputStream fos = new FileOutputStream(dest);
			    byte[] buf = new byte[1024];
			    int i = 0;
			    while((i=fis.read(buf))!=-1) {
			      fos.write(buf, 0, i);
			      }
			    fis.close();
			    fos.close();
			    if( listener != null ) listener.fileCoppied(src, dest, true, null);
				return true;
			} catch( Exception e ) {
			    if( listener != null ) listener.fileCoppied(src, dest, false, e);
				return false;
			}
		}
	}

	
}
