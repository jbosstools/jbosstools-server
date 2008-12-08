package org.jboss.ide.eclipse.as.core.server.internal;

import java.io.File;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JBossServerConnectionProvider;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXException;

public class ServerStartupListener extends UnitedServerListener {
	private static ServerStartupListener instance;
	public static ServerStartupListener getDefault() {
		if( instance == null )
			instance = new ServerStartupListener();
		return instance;
	}
	
	public void serverChanged(ServerEvent event) {
		int eventKind = event.getKind();
		if ((eventKind & ServerEvent.SERVER_CHANGE) != 0) {
			// server change event
			if ((eventKind & ServerEvent.STATE_CHANGE) != 0) {
				checkState(event.getServer());
			}
		}
	}
	
	protected void checkState(final IServer server) {
		if( server.getServerState() == IServer.STATE_STARTED ) {
			if( shouldAddDeployLocation(server)) {
				IJMXRunnable r = new IJMXRunnable() {
					public void run(MBeanServerConnection connection) throws Exception {
						ensureDeployLocationAdded(server, connection);
					}
				};
				try {
					JBossServerConnectionProvider.run(server, r);
				} catch( JMXException jmxe ) {
					// TODO log
				}
			}
		}
	}
	
	protected boolean shouldAddDeployLocation(IServer server) {
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		boolean shouldAdd = server.getServerState() == IServer.STATE_STARTED;
		String type = ds.getDeployLocationType();
		String deployFolder = ds.getDeployFolder();
		if( type.equals(IDeployableServer.DEPLOY_SERVER))
			shouldAdd = false;
		else if( type.equals(IDeployableServer.DEPLOY_METADATA))
			shouldAdd = true;
		else if( type.equals( IDeployableServer.DEPLOY_CUSTOM )) {
			if( !new File(deployFolder).exists())
				shouldAdd = false;
			else {
				IRuntime rt = server.getRuntime();
				IJBossServerRuntime jbsrt = (IJBossServerRuntime)rt.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
				String config = jbsrt.getJBossConfiguration();
				IPath deploy = new Path(IJBossServerConstants.SERVER)
						.append(config)
						.append(IJBossServerConstants.DEPLOY).makeRelative();
				IPath deployGlobal = DeployableServer.makeGlobal(jbsrt, deploy);
				if( new Path(deployFolder).equals(deployGlobal))
					shouldAdd = false;
			}
		}
		return shouldAdd;
	}
	
	protected void ensureDeployLocationAdded(IServer server, MBeanServerConnection connection) throws Exception {
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		String deployFolder = ds.getDeployFolder();
		String asURL = new File(deployFolder).toURL().toString(); 
		ObjectName name = new ObjectName("jboss.deployment:flavor=URL,type=DeploymentScanner");
		connection.invoke(name, "addURL", new Object[] { asURL }, new String[] {String.class.getName()});
	}
}
