/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.rse.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.Trace;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.publishers.AbstractPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IDelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.IDeployableServerBehaviour;
import org.jboss.ide.eclipse.as.core.server.IJBoss6Server;
import org.jboss.ide.eclipse.as.core.server.IPublishCopyCallbackHandler;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ProgressMonitorUtil;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.rse.core.RSEHostShellModel.ServerShellModel;

@Deprecated
public class RSEPublishMethod extends AbstractPublishMethod {

	public static final String RSE_ID = "rse"; //$NON-NLS-1$
	
	private IDeployableServerBehaviour behaviour;
	private IFileServiceSubSystem fileSubSystem = null;
	private IPath remoteRootFolder;

	@Override
	public String getPublishMethodId() {
		return RSE_ID;
	}
	
	public void setBehaviour(IDeployableServerBehaviour beh) {
		this.behaviour = beh;
	}
	
	public IDeployableServerBehaviour getBehaviour() {
		return this.behaviour;
	}
		
	@Override
	public void publishStart(IDeployableServerBehaviour behaviour,
			IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Beginning Publish for server " + behaviour.getServer().getName(), 300);
		super.publishStart(behaviour, ProgressMonitorUtil.getSubMon(monitor, 100));
		this.behaviour = behaviour;
		loadRemoteDeploymentDetails();
		IStatus connected = RSEFrameworkUtils.ensureActiveConnection(behaviour.getServer(), getFileServiceSubSystem(), ProgressMonitorUtil.getSubMon(monitor, 100));
		if( !connected.isOK() ) {
			throw new CoreException(connected);
		}
		
		DelegatingServerBehavior b = (DelegatingServerBehavior) behaviour.getServer().loadAdapter(DelegatingServerBehavior.class, new NullProgressMonitor());
		if( b != null && getServer().getServerState() == IServer.STATE_STARTED ) {
			stopDeploymentScanner();
		}
		Trace.trace(Trace.STRING_FINER, "Finished publish start for server " + getServer().getName());
	}
	
	@Override
	public int publishFinish(IDeployableServerBehaviour behaviour,
			IProgressMonitor monitor) throws CoreException {
		Trace.trace(Trace.STRING_FINER, "Beginning publishFinish for server " + getServer().getName());
		IDelegatingServerBehavior b = (IDelegatingServerBehavior) behaviour.getServer().loadAdapter(IDelegatingServerBehavior.class, new NullProgressMonitor());
		if( b != null && getServer().getServerState() == IServer.STATE_STARTED ) {
			startDeploymentScanner();
		}
		return super.publishFinish(behaviour, monitor);
	}
	
	protected void startDeploymentScanner() {
		// This block of code should only be entered for AS < 7, and NOT for deploy-only server
		JBossExtendedProperties eprops = (JBossExtendedProperties) behaviour.getServer().loadAdapter(JBossExtendedProperties.class, null);
		if( eprops.getFileStructure() != JBossExtendedProperties.FILE_STRUCTURE_SERVER_CONFIG_DEPLOY)
			return;
		
		Trace.trace(Trace.STRING_FINER, "Starting remote deployment scanner for server " + getServer().getName());
		String cmd = getDeploymentScannerCommand(new NullProgressMonitor(), true);
		if( cmd != null )
			launchCommandNoResult((IDelegatingServerBehavior)behaviour, 3000, cmd);
	}

	protected void stopDeploymentScanner() {
		// This block of code should only be entered for AS < 7, and NOT for deploy-only server
		JBossExtendedProperties eprops = (JBossExtendedProperties) behaviour.getServer().loadAdapter(JBossExtendedProperties.class, null);
		if( eprops.getFileStructure() != JBossExtendedProperties.FILE_STRUCTURE_SERVER_CONFIG_DEPLOY)
			return;
		
		Trace.trace(Trace.STRING_FINER, "Stopping remote deployment scanner for server " + getServer().getName());
		String cmd = getDeploymentScannerCommand(new NullProgressMonitor(), false);
		if( cmd != null )
			launchCommandNoResult((DelegatingServerBehavior)behaviour, 3000, cmd);
	}

	protected String getDeploymentScannerCommand(IProgressMonitor monitor, boolean start) {
		//   ./twiddle.sh -s localhost -u admin -p admin invoke 
		//   jboss.deployment:flavor=URL,type=DeploymentScanner start
		IPath home = new Path(RSEUtils.getRSEHomeDir(behaviour.getServer()));
		IPath twiddle = home.append(IJBossRuntimeResourceConstants.BIN).append(IJBossRuntimeResourceConstants.TWIDDLE_SH);
		
		JBossServer jbs = (JBossServer)behaviour.getServer().loadAdapter(JBossServer.class, new NullProgressMonitor());
		if( jbs != null ) {
			String runtimeTypeId = jbs.getRuntime().getRuntime().getRuntimeType().getId();
			String serverUrl;
			if (runtimeTypeId.equals(IJBossToolingConstants.AS_60)){
				IJBoss6Server server6 = (IJBoss6Server)jbs.getServer().loadAdapter(IJBoss6Server.class, new NullProgressMonitor());
				serverUrl = "service:jmx:rmi:///jndi/rmi://" + jbs.getHost() + ":" + server6.getJMXRMIPort() + "/jmxrmi"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else {
				serverUrl = jbs.getHost() + ":" + jbs.getJNDIPort(); //$NON-NLS-1$
			}
 
			String cmd = twiddle.toString() + " -s " + serverUrl + 
				  " -u " + getJBossServer().getUsername() +
				  " -p " + getJBossServer().getPassword() + 
				  " invoke jboss.deployment:flavor=URL,type=DeploymentScanner " 
				+ (start ? "start" : "stop"); 
			return cmd;
		}
		return null;
	}

	protected JBossServer getJBossServer() {
		return (JBossServer)getServer().loadAdapter(JBossServer.class, new NullProgressMonitor());
	}
	
	protected IServer getServer() {
		return behaviour.getServer();
	}
	
	
	public IPath getRemoteRootFolder() {
		if( remoteRootFolder == null )
			try {
				loadRemoteDeploymentDetails();
			} catch(CoreException ce) {
				IStatus status = new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, "Could not load remote deployment details", ce);
				org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.getLog().log(status);
			}
		return remoteRootFolder;
	}
	
	public IFileServiceSubSystem getFileServiceSubSystem() {
		return fileSubSystem;
	}
	
	public IFileService getFileService() throws CoreException {
		if( fileSubSystem == null ) {
			try {
				loadRemoteDeploymentDetails();
			} catch(CoreException ce) {
				IStatus status = new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, "Could not load remote deployment details", ce);
				org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.getLog().log(status);
			}
		}
		return fileSubSystem.getFileService();
	}
	
	protected void loadRemoteDeploymentDetails() throws CoreException{
		Trace.trace(Trace.STRING_FINER, "Loading remote deployment details for server " + getServer().getName());
		String deployRoot = RSEUtils.getDeployRootFolder(behaviour.getServer());
		if( deployRoot == null )
			throw new CoreException(new Status(IStatus.ERROR, org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.PLUGIN_ID, "Server has null deploy root folder. This may be caused by a missing runtime, or improperly configured server adapter"));
		this.remoteRootFolder = new Path(deployRoot);
		
		// Host stuff
		Trace.trace(Trace.STRING_FINER, "Ensuring RSE is initialized");
		RSEFrameworkUtils.waitForFullInit();
		String connectionName = RSEUtils.getRSEConnectionName(behaviour.getServer());
		IHost host = RSEFrameworkUtils.findHost(connectionName);
		if( host == null )
			throw new CoreException(new Status(IStatus.ERROR, org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.PLUGIN_ID, "RSE Host Not Found."));
		fileSubSystem = RSEFrameworkUtils.findFileTransferSubSystem(host);
	}
	
	
	public IPublishCopyCallbackHandler getCallbackHandler(IPath path, IServer server) {
		return new RSERemotePublishHandler(path, this);
	}

	public String getPublishDefaultRootFolder(IServer server) {
		this.behaviour = ServerConverter.getDeployableServerBehavior(server);
		return getRemoteRootFolder().toString();
	}
	public String getPublishDefaultRootTempFolder(IServer server) {
		// unsupported
		return getPublishDefaultRootFolder(server);
	}
	
	private void launchCommandNoResult(IDelegatingServerBehavior behaviour, int delay, String command) {
		Trace.trace(Trace.STRING_FINER, "Launching remote command: " + command);
		try {
			ServerShellModel model = RSEHostShellModel.getInstance().getModel(behaviour.getServer());
			model.executeRemoteCommand("/", command, new String[]{}, new NullProgressMonitor(), delay, true);
		} catch( CoreException ce ) {
			Trace.trace(Trace.STRING_FINER, "Exception launching remote command (command="+command+"): " + ce.getMessage());
			ServerLogger.getDefault().log(behaviour.getServer(), ce.getStatus());
		}
	}

	public IPublishCopyCallbackHandler getCallbackHandler(IPath deployPath,
			IPath tmpFolder, IServer server) {
		// Currently RSE support does not copy files to a temporary folder and then renameTo. 
		// In fact, RSE support does not use any temporary folder at all. 
		return getCallbackHandler(deployPath, server);
	}
}
