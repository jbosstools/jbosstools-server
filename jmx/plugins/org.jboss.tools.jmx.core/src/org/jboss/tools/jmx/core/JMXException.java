/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    "Rob Stryker" <rob.stryker@redhat.com> - Initial implementation
 *******************************************************************************/
package org.jboss.tools.jmx.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

public class JMXException extends CoreException {
	private static final long serialVersionUID = 1L;
	public JMXException(IStatus status) {
		super(status);
	}
}
