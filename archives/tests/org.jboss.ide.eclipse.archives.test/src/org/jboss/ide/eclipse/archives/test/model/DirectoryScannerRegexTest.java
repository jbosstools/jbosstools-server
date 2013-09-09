/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.test.model;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.jboss.ide.eclipse.archives.test.ArchivesTest;
import org.jboss.tools.archives.scanner.FilesystemDirectoryScanner;

public class DirectoryScannerRegexTest extends TestCase {
	protected void setUp() throws Exception {
		IPath p = ArchivesTest.getDefault().getStateLocation().append("ds_regex");
		p.toFile().mkdirs();
		p.append("a").toFile().mkdirs();
		p.append("b").toFile().mkdirs();
		
		// create 3 files in each
		File a1 = p.append("a").append("f1").toFile();
		File a2 = p.append("a").append("f2").toFile();
		File a3 = p.append("a").append("f3").toFile();
		File b1 = p.append("b").append("f1").toFile();
		File b2 = p.append("b").append("f2").toFile();
		File b3 = p.append("b").append("f3").toFile();
		setContents(a1, "a1");
		setContents(a2, "a2");
		setContents(a3, "a3");
		setContents(b1, "b1");
		setContents(b2, "b2");
		setContents(b3, "b3");
	}
	protected void tearDown() throws Exception {
		IPath p = ArchivesTest.getDefault().getStateLocation().append("ds_regex");
		File a1 = p.append("a").append("f1").toFile();
		File a2 = p.append("a").append("f2").toFile();
		File a3 = p.append("a").append("f3").toFile();
		File b1 = p.append("b").append("f1").toFile();
		File b2 = p.append("b").append("f2").toFile();
		File b3 = p.append("b").append("f3").toFile();
		a1.delete();
		a2.delete();
		a3.delete();
		b1.delete();
		b2.delete();
		b3.delete();
		p.append("a").toFile().delete();
		p.append("b").toFile().delete();
		p.toFile().delete();
	}
	
	public void testRegexScanner() {
		IPath p = ArchivesTest.getDefault().getStateLocation().append("ds_regex");
		FilesystemDirectoryScanner scanner = new FilesystemDirectoryScanner();
		scanner.setBasedir(p.toFile());
		// matches a/f1, a/f2, a/f3
		scanner.setIncludes("%regex[a/.*]");
		scanner.scan();
		String[] results = scanner.getIncludedFiles();
		assertNotNull(results);
		assertEquals(results.length, 3);

		FilesystemDirectoryScanner scanner2 = new FilesystemDirectoryScanner();
		scanner2.setBasedir(p.toFile());
		// matches a/f2, b/f2
		scanner2.setIncludes("%regex[.*/f2]");
		scanner2.scan();
		String[] results2 = scanner2.getIncludedFiles();
		assertNotNull(results2);
		assertEquals(results2.length, 2);
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
}
