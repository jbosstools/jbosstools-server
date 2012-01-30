/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.util.internal;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Various util methods that allow to read and write to files.
 * 
 * @author André Dietisheim
 * 
 */
public class FileUtils {

	private static final int BUFFER = 65536;

	public static void writeTo(InputStream in, String fileName) throws IOException {
		writeTo(in, new File(fileName));
	}

	public static void writeTo(InputStream in, File file) throws IOException {
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file));
			writeTo(in, out);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	public static void writeTo(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[BUFFER];
		int avail = in.read(buffer);
		while (avail > 0) {
			out.write(buffer, 0, avail);
			avail = in.read(buffer);
		}
	}
	
	public static void setContents(File file, String contents) throws IOException, CoreException {
		byte[] buffer = new byte[BUFFER];
		InputStream in = new ByteArrayInputStream(contents.getBytes());
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file));
			int avail = in.read(buffer);
			while (avail > 0) {
				out.write(buffer, 0, avail);
				avail = in.read(buffer);
			}
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
	
	public static String getContents(IFile file) throws IOException, CoreException  {
		return getContents(file.getLocation().toFile());
	}

	public static String getContents(File aFile) throws IOException {
		return new String(getBytesFromFile(aFile));
	}

	public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        byte[] bytes = new byte[(int)file.length()];
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
        is.close();
        return bytes;
    }
	
	public static void setContents(IFile file, int val) throws IOException , CoreException{
		setContents(file, "" + val); //$NON-NLS-1$
	}
	
	public static void setContents(IFile file, String val) throws IOException , CoreException{
		if( !file.exists()) 
			file.create(new ByteArrayInputStream((val).getBytes()), false, null);
		else
			file.setContents(new ByteArrayInputStream((val).getBytes()), false, false, new NullProgressMonitor());
		try {
			Thread.sleep(2000);
		} catch( InterruptedException ie) {}
		//JobUtils.waitForIdle(); 
	}
	
	public static int countFiles(File root) {
		int count = 0;
		if( !root.isDirectory() )
			return 1;
		File[] children = root.listFiles();
		for( int i = 0; i < children.length; i++ ) 
			count += countFiles(children[i]);
		return count;
	}
	
	public static int countAllResources(File root) {
		int count = 0;
		if( !root.isDirectory() )
			return 1;
		File[] children = root.listFiles();
		for( int i = 0; i < children.length; i++ ) 
			count += countAllResources(children[i]);
		return 1 + count;
	}

}
