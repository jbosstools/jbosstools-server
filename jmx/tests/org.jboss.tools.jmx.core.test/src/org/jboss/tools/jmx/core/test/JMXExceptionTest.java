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
package org.jboss.tools.jmx.core.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import junit.framework.TestCase;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.tools.jmx.core.JMXActivator;
import org.jboss.tools.jmx.core.JMXException;
import org.junit.Test;

public class JMXExceptionTest extends TestCase{

	public void testJMXExceptionGetStatus() {
		JMXException ex = new JMXException(new Status(IStatus.ERROR,JMXActivator.PLUGIN_ID,"Test message"));
		assertTrue(ex.getStatus().getSeverity() == IStatus.ERROR);
		ex = new JMXException(new Status(IStatus.INFO,JMXActivator.PLUGIN_ID,"Test message"));
		assertTrue(ex.getStatus().getSeverity() == IStatus.INFO);
		ex = new JMXException(new Status(IStatus.OK,JMXActivator.PLUGIN_ID,"Test message"));
		assertTrue(ex.getStatus().getSeverity() == IStatus.OK);
	}

}
