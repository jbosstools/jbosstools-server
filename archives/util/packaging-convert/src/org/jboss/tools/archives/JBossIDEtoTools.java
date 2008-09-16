package org.jboss.tools.archives;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import groovy.lang.GroovyShell;

public class JBossIDEtoTools {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		GroovyShell groovyShell = new GroovyShell();

		JBossIDEtoTools t = new JBossIDEtoTools();

		InputStream openStream = null;
		try {
			URL resource = t.getClass().getClassLoader().getResource("org/jboss/tools/archives/PackagingConverter.groovy");
			openStream = resource.openStream();
			groovyShell.run(openStream, "PackaginConverter.groovy", args);
		} finally {
			if(openStream!=null) openStream.close();
		}

	}

}
