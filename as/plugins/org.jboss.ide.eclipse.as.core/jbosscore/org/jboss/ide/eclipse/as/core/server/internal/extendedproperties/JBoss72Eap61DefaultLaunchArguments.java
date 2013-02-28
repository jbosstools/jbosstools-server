package org.jboss.ide.eclipse.as.core.server.internal.extendedproperties;

import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;

public class JBoss72Eap61DefaultLaunchArguments extends
		JBoss71DefaultLaunchArguments {
	public JBoss72Eap61DefaultLaunchArguments(IServer s) {
		super(s);
	}
	public JBoss72Eap61DefaultLaunchArguments(IRuntime rt) {
		super(rt);
	}
	public String getStartDefaultVMArgs() {
		return super.getStartDefaultVMArgs() 
				+ "-Dorg.jboss.logmanager.nocolor=true "; //$NON-NLS-1$
	}
}
