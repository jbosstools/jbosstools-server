package org.jboss.ide.eclipse.as.core.server.internal.extendedproperties;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.resolvers.ConfigNameResolver;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;

/**
 * The superclass containing most functionality, to be overridden as necessary.
 * The contents of this are all sorts of errata that do not really fit anywhere
 * else, but need to be customized on a per-server or per-server-type basis
 *
 */
public class JBossExtendedProperties {
	private IServer server;
	private IRuntime runtime;
	public JBossExtendedProperties(IAdaptable adaptable) {
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
	
	/**
	 * Returns the full path of a local server's server/{config}/deploy folder
	 */
	public String getServerDeployLocation() {
		String original = ConfigNameResolver.getVariablePattern(ConfigNameResolver.JBOSS_CONFIG_DIR) +
				"/" + IJBossRuntimeResourceConstants.DEPLOY;  //$NON-NLS-1$
		return new ConfigNameResolver().performSubstitutions(
				original, server.getName());
	}
	
	
	public static final int JMX_DEFAULT_PROVIDER = 0;
	public static final int JMX_AS_3_TO_6_PROVIDER = 1;
	public static final int JMX_AS_710_PROVIDER = 2;
	public int getJMXProviderType() {
		return JMX_AS_3_TO_6_PROVIDER;
	}
	
	
	public boolean runtimeSupportsBindingToAllInterfaces() {
		return true;
	}
	
	protected ServerBeanLoader getServerBeanLoader() {
		return new ServerBeanLoader(runtime.getLocation().toFile());
	}
}
