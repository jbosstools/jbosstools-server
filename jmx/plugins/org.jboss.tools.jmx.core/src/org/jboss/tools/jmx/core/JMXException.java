package org.jboss.tools.jmx.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

public class JMXException extends CoreException {
	private static final long serialVersionUID = 1L;
	public JMXException(IStatus status) {
		super(status);
	}
}
