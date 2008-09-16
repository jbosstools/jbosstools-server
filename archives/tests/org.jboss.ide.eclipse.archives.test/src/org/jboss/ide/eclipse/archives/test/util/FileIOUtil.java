package org.jboss.ide.eclipse.archives.test.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileIOUtil {

	public static void clearFolder(File[] children) {
		for (int i = 0; i < children.length; i++) {
			File[] second = children[i].listFiles();
			if (second != null && second.length > 0)
				clearFolder(second);
			children[i].delete();
		}
	}

	public static String getFileContents(File f) {
		try {
			BufferedReader r = new BufferedReader(new FileReader(f));
			StringBuffer contents = new StringBuffer();
			int c = r.read();
			while (c != -1) {
				contents.append((char)c);
				c=r.read();
			}
			r.close();
			return contents.toString();
		} catch (IOException ioe) {
			return null;
		}
	}
}
