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
package org.jboss.ide.eclipse.as.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author André Dietisheim
 */
public class StreamUtils {

	public static void safeClose(OutputStream out) {
		try {
			if (out != null) {
				out.close();
			}
		} catch (IOException e) {
			// ignore THIS IS INTENTIONAL
		}
	}
	
	public static void safeClose(InputStream in) {
		try {
			if (in != null) {
				in.close();
			}
		} catch (IOException e) {
			// ignore THIS IS INTENTIONAL
		}
	}

}
