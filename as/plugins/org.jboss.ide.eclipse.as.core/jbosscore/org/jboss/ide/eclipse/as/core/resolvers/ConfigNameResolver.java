package org.jboss.ide.eclipse.as.core.resolvers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;

public class ConfigNameResolver implements IDynamicVariableResolver {

	public String resolveValue(IDynamicVariable variable, String argument)
			throws CoreException {
		if( variable.getName().equals("jboss_config"))  //$NON-NLS-1$
			return handleConfig(variable, argument);
		if( variable.getName().equals("jboss_config_dir")) //$NON-NLS-1$
			return handleConfigDir(variable, argument);
		return null;
	}
	
	protected String handleConfig(IDynamicVariable variable, String argument) {
		IServer[] servers = ServerCore.getServers();
		for( int i = 0; i < servers.length; i++ ) {
			if( servers[i].getName().equals(argument)) {
				IJBossServerRuntime ajbsrt = (IJBossServerRuntime) servers[i].getRuntime()
				.loadAdapter(IJBossServerRuntime.class,
						new NullProgressMonitor());
				String config = null;
				if( ajbsrt != null ) 
					config = ajbsrt.getJBossConfiguration();
				if( config != null )
					return config;
			}
		}
		return null;
	}
	protected String handleConfigDir(IDynamicVariable variable, String argument) {
		IServer[] servers = ServerCore.getServers();
		for( int i = 0; i < servers.length; i++ ) {
			if( servers[i].getName().equals(argument)) {
				IJBossServerRuntime ajbsrt = (IJBossServerRuntime) servers[i].getRuntime()
				.loadAdapter(IJBossServerRuntime.class,
						new NullProgressMonitor());
				String config = null;
				if( ajbsrt != null ) 
					config = ajbsrt.getConfigLocationFullPath().append(ajbsrt.getJBossConfiguration()).toString();
				if( config != null )
					return config;
			}
		}
		return null;
	}
}
