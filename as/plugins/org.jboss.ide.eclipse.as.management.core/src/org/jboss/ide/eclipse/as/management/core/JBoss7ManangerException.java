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
package org.jboss.ide.eclipse.as.management.core;

/**
 * @author André Dietisheim
 */
public class JBoss7ManangerException extends Exception {

	private static final long serialVersionUID = 1L;

	public JBoss7ManangerException(String message, Throwable cause) {
		super(message, cause);
	}

	public JBoss7ManangerException(Throwable cause) {
		super(cause);
	}

	public JBoss7ManangerException(String message) {
		super(message);
	}

}
