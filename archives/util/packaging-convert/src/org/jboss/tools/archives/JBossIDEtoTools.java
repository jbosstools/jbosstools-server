/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
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
