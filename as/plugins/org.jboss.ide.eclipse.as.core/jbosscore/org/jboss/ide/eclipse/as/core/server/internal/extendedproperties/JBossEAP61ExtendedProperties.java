package org.jboss.ide.eclipse.as.core.server.internal.extendedproperties;

import org.eclipse.core.runtime.IAdaptable;
import org.jboss.ide.eclipse.as.core.server.IDefaultLaunchArguments;

public class JBossEAP61ExtendedProperties extends JBossEAP60ExtendedProperties {
	public JBossEAP61ExtendedProperties(IAdaptable obj) {
		super(obj);
	}
	
	public String getRuntimeTypeVersionString() {
		return "6.1"; //$NON-NLS-1$
	}
	
	public IDefaultLaunchArguments getDefaultLaunchArguments() {
		if( server != null)
			return new JBoss72Eap61DefaultLaunchArguments(server);
		return new JBoss72Eap61DefaultLaunchArguments(runtime);
	}

}
