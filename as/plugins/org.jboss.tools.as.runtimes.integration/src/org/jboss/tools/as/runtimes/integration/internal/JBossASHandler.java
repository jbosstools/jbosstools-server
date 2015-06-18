/*******************************************************************************
 * Copyright (c) 2010 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.runtimes.integration.internal;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.bean.JBossServerType;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBean;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.ide.eclipse.as.core.util.ServerNamingUtility;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerProfileInitializer;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel;
import org.jboss.tools.as.runtimes.integration.Messages;
import org.jboss.tools.as.runtimes.integration.ServerRuntimesIntegrationActivator;
import org.jboss.tools.as.runtimes.integration.internal.DriverUtility.DriverUtilityException;
import org.jboss.tools.runtime.core.RuntimeCoreActivator;
import org.jboss.tools.runtime.core.model.AbstractRuntimeDetectorDelegate;
import org.jboss.tools.runtime.core.model.IRuntimeDetector;
import org.jboss.tools.runtime.core.model.RuntimeDefinition;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * This class is INTERNAL and is not expected to be called or implemented directly 
 * by ANY clients!
 */
public class JBossASHandler extends AbstractRuntimeDetectorDelegate implements IRuntimeIntegrationConstants {
	
	private static String[] hasIncludedRuntimes = new String[] {SOA_P, EAP, EPP, EWP, SOA_P_STD};
	static {
		// See also JBIDE-12603 (fileset not added for first runtime)
		Bundle bundle = Platform.getBundle("org.jboss.ide.eclipse.archives.webtools"); //$NON-NLS-1$
		if (bundle != null) {
			try {
				if ((bundle.getState() & Bundle.INSTALLED) == 0) {
					bundle.start(Bundle.START_ACTIVATION_POLICY);
					bundle.start(Bundle.START_TRANSIENT);
				}
			} catch (BundleException e) {
				// failed, try next bundle
			}
		}
	}

	/**
	 * The framework will no longer call this method, but will instead call 
	 * boolean initializeRuntime(RuntimeDefinition runtimeDefinition) throws CoreException 
	 */
	@Override @Deprecated
	public void initializeRuntimes(List<RuntimeDefinition> runtimeDefinitions) {
		initializeRuntimesStatic(runtimeDefinitions);
	}

	@Override
	public boolean initializeRuntime(RuntimeDefinition runtimeDefinition) throws CoreException {
		return createJBossServerFromDefinitionsWithReturn(runtimeDefinition);
	}
	
	/**
	 * Returns true if any definition is created
	 * @param runtimeDefinitions
	 * @return
	 */
	private static boolean initializeRuntimesStatic(List<RuntimeDefinition> runtimeDefinitions) {
		boolean created = false;
		for (RuntimeDefinition runtimeDef : runtimeDefinitions) {
			created |= createJBossServerFromDefinitionsWithReturn(runtimeDef);
		}
		return created;
	}
	
	
	/**
	 * This method should not be public and is Deprecated. 
	 * Please use the interface methods, specifically, 
	 * boolean initializeRuntime(RuntimeDefinition runtimeDefinition) throws CoreException
	 * 
	 * @param runtimeDefinitions
	 */
	@Deprecated
	public static void createJBossServerFromDefinitions(List<RuntimeDefinition> runtimeDefinitions) {
		for (RuntimeDefinition runtimeDefinition:runtimeDefinitions) {
			createJBossServerFromDefinition(runtimeDefinition);
		}
	}
	
	private static void createJBossServerFromDefinition(RuntimeDefinition runtimeDefinition) {
		createJBossServerFromDefinitionsWithReturn(runtimeDefinition);
	}
	
	private static boolean createJBossServerFromDefinitionsWithReturn(RuntimeDefinition runtimeDefinition) {
			boolean created = false;
			if (runtimeDefinition.isEnabled()) {
				ServerBean sb = new ServerBeanLoader(runtimeDefinition.getLocation()).getServerBean();
				File asLocation = getServerAdapterRuntimeLocation(sb, runtimeDefinition.getLocation());
				if (asLocation != null && asLocation.isDirectory()) {
					String type = runtimeDefinition.getType();
					if (serverBeanTypeExists(type)) {
						String typeId = sb.getServerAdapterTypeId();
						String name = runtimeDefinition.getName();
						String runtimeName = name + " " + RUNTIME; //$NON-NLS-1$
						created |= createJBossServer(asLocation, typeId, name, runtimeName);
					}
				}
			}
			created |= initializeRuntimesStatic(runtimeDefinition.getIncludedRuntimeDefinitions());
			return created;
	}
	
	/*
	 * This needs to be cleaned up, but current issues are that
	 * a serverbean type's id and name are not unique. 
	 * 
	 * This method is of questionable utility.
	 */
	private static boolean serverBeanTypeExists(String type) {
		JBossServerType[] all = ServerBeanLoader.typesInOrder;
		for( int i = 0; i < all.length; i++ ) {
			if( all[i].getId().equals(type))
				return true;
		}
		return false;
	}

	private static boolean serverExistsForPath(IPath locPath) {
		IServer[] servers = ServerCore.getServers();
		for (int i = 0; i < servers.length; i++) {
			IRuntime runtime = servers[i].getRuntime();
			if(runtime != null && runtime.getLocation() != null && runtime.getLocation().equals(locPath)) {
				return true;
			}
		}
		return false;
	}
	
	private static IRuntime findRuntimeForPath(IPath locPath) {
		IRuntime[] runtimes = ServerCore.getRuntimes();
		for (int i = 0; i < runtimes.length; i++) {
			if (runtimes[i] == null || runtimes[i].getLocation() == null) {
				continue;
			}
			if (runtimes[i].getLocation().equals(locPath)) {
				return runtimes[i];
			}
		}
		return null;
	}
	
	private static boolean createJBossServer(File asLocation, String serverTypeId, String name, String runtimeName) {
		if (asLocation == null || !asLocation.isDirectory() || serverTypeId == null)
			return false;
		IServerType serverType = ServerCore.findServerType(serverTypeId);
		if( serverType == null )
			return false;
		IRuntimeType rtType = serverType.getRuntimeType();
		if( rtType == null )
			return false;
		
		IPath jbossAsLocationPath = new Path(asLocation.getAbsolutePath());
		if( serverExistsForPath(jbossAsLocationPath))
			return false;
		
		IServer s = null;
		IRuntime runtime = findRuntimeForPath(jbossAsLocationPath);
		IProgressMonitor progressMonitor = new NullProgressMonitor();
		try {
			if (runtime == null) {
				runtime = createRuntime(runtimeName, asLocation.getAbsolutePath(), progressMonitor, rtType);
			}
			if (runtime != null) {
				s = createServer(progressMonitor, runtime, serverType, name);
			}
			
			if( isDtpPresent())
				new DriverUtility().createDriver(asLocation.getAbsolutePath(), serverType);
		} catch (CoreException e) {
			ServerRuntimesIntegrationActivator.pluginLog().logError(Messages.JBossRuntimeStartup_Cannot_create_new_JBoss_Server,e);
		} catch (DriverUtilityException e) {
			ServerRuntimesIntegrationActivator.pluginLog().logError(Messages.JBossRuntimeStartup_Cannott_create_new_DTP_Connection_Profile,e);
		}
		return s != null;
	}

	private static boolean isDtpPresent() {
		String bundle1 = "org.eclipse.datatools.connectivity"; //$NON-NLS-1$
		String bundle2 = "org.eclipse.datatools.connectivity.db.generic"; //$NON-NLS-1$
		Bundle b1 = Platform.getBundle(bundle1);
		Bundle b2 = Platform.getBundle(bundle2);
		return b1 != null && b2 != null;
	}
	
	/**
	 * Creates new JBoss AS Runtime
	 * @param jbossASLocation location of JBoss AS
	 * @param progressMonitor
	 * @return runtime working copy
	 * @throws CoreException
	 */
	private static IRuntime createRuntime(String runtimeName, String jbossASLocation, 
			IProgressMonitor progressMonitor, IRuntimeType rtType) throws CoreException {
		runtimeName = getUniqueRuntimeName(runtimeName, rtType);
		IRuntimeWorkingCopy runtime = null;
		IPath jbossAsLocationPath = new Path(jbossASLocation);
		runtime = rtType.createRuntime(null, progressMonitor);
		runtime.setLocation(jbossAsLocationPath);
		if(runtimeName!=null) {
			runtime.setName(runtimeName);				
		}
		return runtime.save(false, progressMonitor);
	}

	private static String getUniqueRuntimeName(String runtimeName, IRuntimeType type) {
		String name = runtimeName;
		int i = 2;
		while (getRuntime(name, type) != null) {
			name = name + " (" + i++ + ")";  //$NON-NLS-1$//$NON-NLS-2$
		}
		return name;
	}

	private static IRuntime getRuntime(String name, IRuntimeType type) {
		if (name == null || type == null) {
			return null;
		}
		IRuntime[] runtimes = ServerCore.getRuntimes();
		for (int i = 0; i < runtimes.length; i++) {
			if (runtimes[i] != null && runtimes[i].getRuntimeType() != null 
					&& name.equals(runtimes[i].getName()) &&  type.equals(runtimes[i].getRuntimeType())) {
				return runtimes[i];
			}
		}
		return null;
	}

	/**
	 * Creates new JBoss Server
	 * @param progressMonitor
	 * @param runtime parent JBoss AS Runtime
	 * @return server working copy
	 * @throws CoreException
	 */
	private static IServer createServer(IProgressMonitor progressMonitor, IRuntime runtime,
			IServerType serverType, String name) throws CoreException {
		if( serverWithNameExists(name)) {
			name = ServerNamingUtility.getDefaultServerName(runtime);
		}
		
		IServerWorkingCopy serverWC = serverType.createServer(null, null,
				new NullProgressMonitor());
		serverWC.setRuntime(runtime);
		if( name != null )
			serverWC.setName(name);
		IServerProfileInitializer[] initializers = ServerProfileModel.getDefault().getInitializers(serverWC.getServerType().getId(), "local");
		for( int i = 0; i < initializers.length; i++ ) {
			initializers[i].initialize(serverWC);
		}
		
		IServer ret = serverWC.save(true, new NullProgressMonitor());
		ServerRuntimesIntegrationActivator.getDefault().trackNewDetectedServerEvent(serverType.getId());
		return ret;
	}
	
	private static boolean serverWithNameExists(String name) {
		IServer[] servers = ServerCore.getServers();
		for (IServer server:servers) {
			if (name.equals(server.getName()) ) {
				return true;
			}
		}
		return false;
	}
	
	public RuntimeDefinition getRuntimeDefinition(File root,
			IProgressMonitor monitor) {
		if (monitor.isCanceled() || root == null) {
			return null;
		}
		ServerBeanLoader loader = new ServerBeanLoader(root);
		ServerBean serverBean = loader.getServerBean();
		
		if (serverBean.getBeanType() != null && !JBossServerType.UNKNOWN.equals(serverBean.getBeanType())) {
			RuntimeDefinition runtimeDefinition = new RuntimeDefinition(serverBean.getName(), 
					serverBean.getVersion(), serverBean.getUnderlyingTypeId(), new File(serverBean.getLocation()));
			calculateIncludedRuntimeDefinition(runtimeDefinition, monitor);
			return runtimeDefinition;
		}
		return null;
	}
	
	private void calculateIncludedRuntimeDefinition(
			RuntimeDefinition runtimeDefinition, IProgressMonitor monitor) {
		// Sanity check
		if (runtimeDefinition == null || runtimeDefinition.getType() == null ||
				!hasIncludedRuntimes(runtimeDefinition.getType())) {
			return;
		}
		
		runtimeDefinition.getIncludedRuntimeDefinitions().clear();
		Set<IRuntimeDetector> s = getNestedSearchRuntimeDetectors();
		for(Iterator<IRuntimeDetector> i = s.iterator(); i.hasNext(); ) {
			IRuntimeDetector iNext = i.next();
			if( iNext.isEnabled() )
				iNext.computeIncludedRuntimeDefinition(runtimeDefinition);
		}
	}
	
	/*
	 * This method must clone the core model's set, because otherwise we will be modifying 
	 * the actual model's set. Also, want to remove ourself from the list of acceptable handlers.
	 */
	private Set<IRuntimeDetector> getNestedSearchRuntimeDetectors() {
		Set<IRuntimeDetector> runtimeDetectors = RuntimeCoreActivator.getDefault().getRuntimeDetectors();
		TreeSet<IRuntimeDetector> cloned = new TreeSet<IRuntimeDetector>(runtimeDetectors);
		cloned.remove(findMyDetector());
		return cloned;
	}

	private boolean hasIncludedRuntimes(String type) {
		return Arrays.asList(hasIncludedRuntimes).contains(type);
	}

	private String getLocationForRuntimeDefinition(RuntimeDefinition runtimeDefinition) {
		String path = null;
		if (runtimeDefinition != null && runtimeDefinition.getLocation() != null) {
			File location = getServerAdapterRuntimeLocation(runtimeDefinition);
			if (location != null && location.isDirectory()) {
				try {
					path = location.getCanonicalPath();
				} catch (IOException e) {
					ServerRuntimesIntegrationActivator.pluginLog().logError(e);
					path = location.getAbsolutePath();
				}
			}
		}
		return path;
	}
	
	private static File getServerAdapterRuntimeLocation(RuntimeDefinition runtimeDefinitions) {
		ServerBeanLoader loader = new ServerBeanLoader( runtimeDefinitions.getLocation() );
		return getServerAdapterRuntimeLocation(loader.getServerBean(), runtimeDefinitions.getLocation());
	}
	private static File getServerAdapterRuntimeLocation(ServerBean sb, File root) {
		String version = sb.getVersion();
		String relative = sb.getBeanType().getRootToAdapterRelativePath(version);
		if( relative == null )
			return root;
		return new File(root, relative);
	}
	
	@Override
	public boolean exists(RuntimeDefinition runtimeDefinition) {
		// Does a wtp-style runtime with this location already exist?
		String path = getLocationForRuntimeDefinition(runtimeDefinition);
		if (path != null) {
			IServer[] servers = ServerCore.getServers();
			for (int i = 0; i < servers.length; i++) {
				IRuntime runtime = servers[i].getRuntime();
				if (runtime != null && runtime.getLocation() != null) {
					String loc = runtime.getLocation().toOSString();
					try {
						loc = new File(loc).getCanonicalPath();
					} catch (IOException e) {
						ServerRuntimesIntegrationActivator.pluginLog().logError(e);
					}
					if(path.equals(loc)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void computeIncludedRuntimeDefinition(RuntimeDefinition runtimeDefinition) {
		// We are not currently included in any other larger entities, so we do nothing here
	}

	@Override
	public String getVersion(RuntimeDefinition runtimeDefinition) {
		return runtimeDefinition.getVersion();
	}
	
}
