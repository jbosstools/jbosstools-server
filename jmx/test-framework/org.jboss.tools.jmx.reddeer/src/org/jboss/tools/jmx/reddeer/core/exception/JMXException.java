/******************************************************************************* 
 * Copyright (c) 2018 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.jmx.reddeer.core.exception;

import org.eclipse.reddeer.common.exception.RedDeerException;

/**
 * JMX exception
 * @author odockal
 *
 */
public class JMXException extends RedDeerException {

	/**
	 * Generated UID
	 */
	private static final long serialVersionUID = -898636838443345070L;

	public JMXException(String message) {
		super(message);
	}	
	
	public JMXException(String message, Throwable cause) {
		super(message, cause);
	}
	

}
