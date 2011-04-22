/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.jboss.ide.eclipse.as.core.server.internal.v7;

/**
 * @author André Dietisheim
 */
public class JBoss7ManangementException extends Exception {

	private static final long serialVersionUID = 1L;

	public JBoss7ManangementException(String message, Throwable cause) {
		super(message, cause);
	}

	public JBoss7ManangementException(Throwable cause) {
		super(cause);
	}

	public JBoss7ManangementException(String message) {
		super(message);
	}

}
