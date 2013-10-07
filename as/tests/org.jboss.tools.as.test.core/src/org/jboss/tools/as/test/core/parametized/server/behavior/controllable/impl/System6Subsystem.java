package org.jboss.tools.as.test.core.parametized.server.behavior.controllable.impl;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.SubsystemModel;

public class System6Subsystem extends AbstractSubsystemController {
	public static final String PROP_LEGS = "subsystem6.prop.LEGS";
	private static final String VARIABLE_SYSTEM = "system4a";
	
	@Override
	public ISubsystemController findDependency(String system, String serverType) throws CoreException {
		if( !VARIABLE_SYSTEM.equals(system) || getEnvironment() == null )
			return super.findDependency(system, serverType);
		
		// We're looking for our animal subsystem
		Object legs = getEnvironment().get(PROP_LEGS);
		String subsystemId = null;
		if( legs != null && legs instanceof Integer) {
			int legCount = ((Integer)legs).intValue();
			if( legCount == 4 ) 
				subsystemId = "system4a.tiger";
			else
				subsystemId = "system4a.mantis";
		}
		return SubsystemModel.getInstance().createControllerForSubsystem(
				getServer(), serverType, system, subsystemId, getEnvironment());
	}

	public IStatus validate() {
		Object legs = getEnvironment().get(PROP_LEGS);
		if( legs == null )
			return new Status(IStatus.ERROR, "test.plugin", "No legs have been set for this service");
		if( !(legs instanceof Integer)) {
			return new Status(IStatus.ERROR, "test.plugin", "legs property must be an integer");			
		}
		if( ((Integer)legs).intValue() == 666) {
			return new Status(IStatus.ERROR, "test.plugin", "The Devil Has NO POWER HERE");						
		}
		return Status.OK_STATUS;
	}
	
}
