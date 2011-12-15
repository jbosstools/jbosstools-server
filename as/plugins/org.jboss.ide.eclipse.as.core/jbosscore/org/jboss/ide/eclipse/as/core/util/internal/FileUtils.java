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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Various util methods that allow to read and write to files.
 * 
 * @author André Dietisheim
 * 
 */
public class FileUtils {

	private static final int BUFFER = 65536;
	private static byte[] buffer = new byte[BUFFER];

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
		int avail = in.read(buffer);
		while (avail > 0) {
			out.write(buffer, 0, avail);
			avail = in.read(buffer);
		}
	}
}
