package org.jboss.ide.eclipse.as.core.server.internal.extendedproperties;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;

public class ServerExtendedProperties {
	protected IServer server;
	protected IRuntime runtime;
	public ServerExtendedProperties(IAdaptable adaptable) {
		if( adaptable instanceof IServer) {
			this.server = (IServer)adaptable;
			this.runtime = server.getRuntime();
		} else if( adaptable instanceof IRuntime){
			this.runtime = (IRuntime)adaptable;
		}
	}

	public String getNewFilesetDefaultRootFolder() {
		return "servers/${jboss_config}"; //$NON-NLS-1$
	}
	
	public static final int JMX_NULL_PROVIDER = -1;
	public static final int JMX_DEFAULT_PROVIDER = 0;
	public static final int JMX_AS_3_TO_6_PROVIDER = 1;
	public static final int JMX_AS_710_PROVIDER = 2;
	public int getJMXProviderType() {
		return JMX_NULL_PROVIDER;
	}
	
	public boolean hasWelcomePage() {
		return false;
	}
	
	public String getWelcomePageUrl() {
		return null;
	}
	
	public static final int DEPLOYMENT_SCANNER_NO_SUPPORT = 1;
	public static final int DEPLOYMENT_SCANNER_JMX_SUPPORT = 2;
	public static final int DEPLOYMENT_SCANNER_AS7_MANAGEMENT_SUPPORT = 3;
	
	public int getMultipleDeployFolderSupport() {
		return DEPLOYMENT_SCANNER_NO_SUPPORT;
	}
}
