package org.jboss.tools.as.test.core.parametized.server.behavior.controllable.impl;

import org.eclipse.core.runtime.CoreException;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;

public class System1aSubsystem extends AbstractSubsystemController {
	@Override
	public ISubsystemController findDependency(String system, String serverType) throws CoreException {
		return super.findDependency(system, serverType);
	}

}
