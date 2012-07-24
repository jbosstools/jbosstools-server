/******************************************************************************* 
 * Copyright (c) 2010 - 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.as.test.core.internal.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.jboss.tools.test.util.JobUtils;

public class IOUtil {
	public static byte[] getBytesFromInputStream(InputStream is) {
		if( is == null )
			return null;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			int nRead;
			byte[] data = new byte[16384];
	
			while ((nRead = is.read(data, 0, data.length)) != -1) {
			  buffer.write(data, 0, nRead);
			}
	
			buffer.flush();
			return buffer.toByteArray();
		} catch(IOException ioe) {
			return null;
		}
    }

	public static void setContents(File file, String contents) throws IOException, CoreException {
		byte[] buffer = new byte[65536];
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

	public static void unzipFile(IPath zipped, IPath toLoc) {
		toLoc.toFile().mkdirs();
		final int BUFFER = 2048;
		try {
			  BufferedOutputStream dest = null;
		      FileInputStream fis = new 
		 	  FileInputStream(zipped.toFile());
			  ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
	          ZipEntry entry;
	          while((entry = zis.getNextEntry()) != null) {
	             int count;
	             byte data[] = new byte[BUFFER];
	             // write the files to the disk
	             if( entry.isDirectory() ) {
	            	 toLoc.append(entry.getName()).toFile().mkdirs();
	             } else {
		             toLoc.append(entry.getName()).toFile().getParentFile().mkdirs();
		             if( !toLoc.append(entry.getName()).toFile().exists()) {
		            	 String out = toLoc.append(entry.getName()).toOSString();
			             FileOutputStream fos = new FileOutputStream(out);
			             dest = new BufferedOutputStream(fos, BUFFER);
			             while ((count = zis.read(data, 0, BUFFER)) != -1) {
			                dest.write(data, 0, count);
			             }
			             dest.flush();
			             dest.close();
		             }
	             }
	          }
	          zis.close();
	       } catch(Exception e) {
	          e.printStackTrace();
	       }
	}

	
	public static boolean isZip(File file) {
	      if(file.isDirectory()) {
	          return false;
	      }
	      if(!file.canRead()) {
	    	  return false;
	      }
	      if(file.length() < 4) {
	          return false;
	      }
	      try {
		      DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
		      int test = in.readInt();
		      in.close();
		      return test == 0x504b0304;
	      }catch( IOException ioe) {
	    	  return false;
	      }
	}
	
	public static IModuleFile findFile(IModuleFile[] files, String needle) {
		if( files != null ) {
			for( int i = 0; i < files.length; i++ ) {
				if( files[i].getName().equals(needle))
					return files[i];
			}
		}
		return null;
	}

}
