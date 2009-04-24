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
package org.jboss.ide.eclipse.as.core.server.xpl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

/**
 * Packages resources to a .zip file
 */
public class ModulePackager {
	private static final String JAR_FILE_SEPERATOR = "/"; //$NON-NLS-1$
	private JarOutputStream outputStream;
//	private StringBuffer manifestContents;

	private boolean useCompression = true;

	/**
	 * Create an instance of this class.
	 * 
	 * @param filename java.lang.String
	 * @param compress boolean
	 * @exception java.io.IOException
	 */
	public ModulePackager(String filename, boolean compress) throws IOException {
		Path directoryPath = new Path(filename);
		directoryPath = (Path) directoryPath.removeLastSegments(1);
		File newZipFile = new File(directoryPath.toString());
		newZipFile.mkdirs();
		outputStream = new JarOutputStream(new FileOutputStream(filename)); 
		useCompression = compress;
	}

	/**
	 * Do all required cleanup now that we're finished with the currently-open .zip
	 * 
	 * @exception java.io.IOException
	 */
	public void finished() throws IOException {
		outputStream.close();
	}

	/**
	 * Create a new ZipEntry with the passed pathname and contents, and write it to the current
	 * archive
	 * 
	 * @param pathname
	 *            java.lang.String
	 * @param contents
	 *            byte[]
	 * @exception java.io.IOException
	 */
	protected void write(String pathname, byte[] contents) throws IOException {
		ZipEntry newEntry = new ZipEntry(pathname);

		// if the contents are being compressed then we get the below for free.
		if (!useCompression) {
			newEntry.setMethod(ZipEntry.STORED);
			newEntry.setSize(contents.length);
			CRC32 checksumCalculator = new CRC32();
			checksumCalculator.update(contents);
			newEntry.setCrc(checksumCalculator.getValue());
		}

		outputStream.putNextEntry(newEntry);
		outputStream.write(contents);
		outputStream.closeEntry();
	}

	/**
	 * @param destinationPath
	 * @throws IOException
	 */
	public void writeFolder(String destinationPath) throws IOException {
		if (!destinationPath.endsWith(JAR_FILE_SEPERATOR )) 
			destinationPath = destinationPath + JAR_FILE_SEPERATOR;
		ZipEntry newEntry = new ZipEntry(destinationPath);
		outputStream.putNextEntry(newEntry);
		outputStream.closeEntry();
	}

	/**
	 * Write the passed resource to the current archive
	 * 
	 * @param resource
	 *            org.eclipse.core.resources.IFile
	 * @param destinationPath
	 *            java.lang.String
	 * @exception java.io.IOException
	 * @exception org.eclipse.core.runtime.CoreException
	 */
	public void write(IFile resource, String destinationPath) throws IOException, CoreException {
		InputStream contentStream = null;
		try {
			contentStream = resource.getContents(false);
			write(contentStream, destinationPath);
		} finally {
			if (contentStream != null)
				contentStream.close();
		}
	}

	/**
	 * Write the passed resource to the current archive
	 * 
	 * @param resource
	 *            java.io.IFile
	 * @param destinationPath
	 *            java.lang.String
	 * @exception java.io.IOException
	 * @exception org.eclipse.core.runtime.CoreException
	 */
	public void write(File resource, String destinationPath) throws IOException, CoreException {
		InputStream contentStream = null;
		try {
			contentStream = new FileInputStream(resource);
			write(contentStream, destinationPath);
		} finally {
			if (contentStream != null)
				contentStream.close();
		}
	}

	/**
	 * @param contentStream
	 * @param destinationPath
	 * @throws IOException
	 * @throws CoreException
	 */
	public void write(InputStream contentStream, String destinationPath) throws IOException, CoreException {
		ByteArrayOutputStream output = null;

		try {
			output = new ByteArrayOutputStream();
			int chunkSize = contentStream.available();
			byte[] readBuffer = new byte[chunkSize];
			int n = contentStream.read(readBuffer);

			while (n > 0) {
				output.write(readBuffer);
				n = contentStream.read(readBuffer);
			}
		} finally {
			if (output != null)
				output.close();
		}

		write(destinationPath, output.toByteArray());
	}
	
	/**
	 * pack directory relative to root
	 * @param directory
	 * @param root
	 * @throws CoreException
	 * @throws IOException
	 */
	public void pack(File directory, String root) throws CoreException, IOException
	{
        File[] files = directory.listFiles();
        for( int i = 0; i < files.length; i++ )
        {
        	String relativeFolder = makeRelative( files[i].getAbsolutePath(), root );
            if( files[i].isDirectory() )
            {
            	if( relativeFolder != null )
                {// should always be true
                    writeFolder( relativeFolder );
                }
                pack( files[i], root );
            } else
            {
            	if( relativeFolder != null )
                {// should always be true
                    write( files[i], relativeFolder );
                }
            }
        }

    }
	/**
	 * Make directoryname relative to root
	 * @param fileName
	 * @param root
	 * @return
	 */
	private String makeRelative(String fileName, String root)
	{
		String folder=null;
		if(fileName.startsWith(root))
		{
			folder=fileName.substring(root.length());
		}
        folder = folder.replaceAll("\\\\", "/");  //$NON-NLS-1$ //$NON-NLS-2$
        if (folder.length() > 0 && folder.charAt(0) == '/')
              folder = folder.substring(1);
		return folder;		
	}
	
}
