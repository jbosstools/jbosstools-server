package org.jboss.ide.eclipse.as.core.server.internal;

import java.io.File;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.core.extensions.events.IEventCodes;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JBossServerConnectionProvider;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXException;

public class ServerListener extends UnitedServerListener {
	private static ServerListener instance;
	public static ServerListener getDefault() {
		if( instance == null )
			instance = new ServerListener();
		return instance;
	}
	
	public void serverAdded(IServer server) {
		// create metadata area
		File location = IJBossServerConstants.PLUGIN_LOCATION.append(server.getId().replace(' ', '_')).toFile();
		location.mkdirs();
		
		// create temp deploy folder
		IDeployableServer ds = (IDeployableServer)server.loadAdapter(IDeployableServer.class, null);
		if( ds != null ) {
			File d1 = new File(location, IJBossServerConstants.DEPLOY);
			File d2 = new File(location, IJBossServerConstants.TEMP_DEPLOY);
			d1.mkdirs();
			d2.mkdirs();
			if( !new File(ds.getDeployFolder()).equals(d1)) 
				new File(ds.getDeployFolder()).mkdirs();
			if( !new File(ds.getTempDeployFolder()).equals(d2))
				new File(ds.getTempDeployFolder()).mkdirs();
			IRuntime rt = server.getRuntime();
			IJBossServerRuntime jbsrt = (IJBossServerRuntime)rt.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
			String config = jbsrt.getJBossConfiguration();
			IPath newTemp = new Path(IJBossServerConstants.SERVER).append(config)
				.append(IJBossServerConstants.TMP)
				.append(IJBossServerConstants.JBOSSTOOLS_TMP).makeRelative();
			IPath newTempAsGlobal = DeployableServer.makeGlobal(jbsrt, newTemp);
			newTempAsGlobal.toFile().mkdirs();
		}
	}

	public void serverRemoved(IServer server) {
		// delete metadata area
		File f = IJBossServerConstants.PLUGIN_LOCATION.append(server.getId().replace(' ', '_')).toFile();
		FileUtil.safeDelete(f);
	}
	
	public void serverChanged(ServerEvent event) {
		IServer server = event.getServer();
		JBossServer jbs = (JBossServer)server.loadAdapter(JBossServer.class, new NullProgressMonitor());
		if( jbs != null ) {
			doDeploymentAddition(event);
		}
	}
	
	protected void doDeploymentAddition(final ServerEvent event) {
		int eventKind = event.getKind();
		if ((eventKind & ServerEvent.SERVER_CHANGE) != 0) {
			// server change event
			if ((eventKind & ServerEvent.STATE_CHANGE) != 0) {
				if( event.getServer().getServerState() == IServer.STATE_STARTED ) {
					if( shouldAddDeployLocation(event.getServer())) {
						IJMXRunnable r = new IJMXRunnable() {
							public void run(MBeanServerConnection connection) throws Exception {
								ensureDeployLocationAdded(event.getServer(), connection);
							}
						};
						try {
							JBossServerConnectionProvider.run(event.getServer(), r);
						} catch( JMXException jmxe ) {
							IStatus s = jmxe.getStatus();
							IStatus newStatus = new Status(s.getSeverity(), s.getPlugin(), IEventCodes.ADD_DEPLOYMENT_FOLDER_FAIL, 
									"Error adding deployment folder to Deployment Scanner", s.getException());
							ServerLogger.getDefault().log(event.getServer(), newStatus);
						}
					}
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
