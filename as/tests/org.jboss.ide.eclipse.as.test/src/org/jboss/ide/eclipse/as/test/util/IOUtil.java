package org.jboss.ide.eclipse.as.test.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.test.util.JobUtils;

public class IOUtil {
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
		setContents(file, "" + val);
	}
	
	public static void setContents(IFile file, String val) throws IOException , CoreException{
		if( !file.exists()) 
			file.create(new ByteArrayInputStream((val).getBytes()), false, null);
		else
			file.setContents(new ByteArrayInputStream((val).getBytes()), false, false, new NullProgressMonitor());
		try {
			Thread.sleep(2000);
		} catch( InterruptedException ie) {}
		JobUtils.waitForIdle(); 
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
			count += countFiles(children[i]);
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
	             toLoc.append(entry.getName()).toFile().getParentFile().mkdirs();
	             if( !toLoc.append(entry.getName()).toFile().exists()) {
		             FileOutputStream fos = new FileOutputStream(toLoc.append(entry.getName()).toOSString());
		             dest = new BufferedOutputStream(fos, BUFFER);
		             while ((count = zis.read(data, 0, BUFFER)) != -1) {
		                dest.write(data, 0, count);
		             }
		             dest.flush();
		             dest.close();
	             }
	          }
	          zis.close();
	       } catch(Exception e) {
	          e.printStackTrace();
	       }
	}

}
