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
