/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.wtp.runtimes.tomcat.internal.detection;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.launching.environments.EnvironmentsManager;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jst.server.tomcat.core.internal.ITomcatRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.wtp.core.launching.IExecutionEnvironmentConstants;
import org.jboss.tools.runtime.core.model.AbstractRuntimeDetectorDelegate;
import org.jboss.tools.runtime.core.model.RuntimeDefinition;

/**
 * Tomcat Runtime detector delegate
 * 
 * @author Fred Bricon
 */
@SuppressWarnings("restriction")
public class TomcatRuntimeDetectorDelegate extends AbstractRuntimeDetectorDelegate {
	
	private static final String RUNTIME_SUFFIX = " Runtime"; //$NON-NLS-1$

	@SuppressWarnings("nls")
	protected static final String[] runtimeTypes = new String[] {
		//Put most recent versions first, to avoid traversing the whole list while creating working copies in #getRuntimeWorkingCopyFromDir()
		"org.eclipse.jst.server.tomcat.runtime.80",
		"org.eclipse.jst.server.tomcat.runtime.70",
		"org.eclipse.jst.server.tomcat.runtime.60",
		"org.eclipse.jst.server.tomcat.runtime.55",
		"org.eclipse.jst.server.tomcat.runtime.50",
		"org.eclipse.jst.server.tomcat.runtime.41",
		"org.eclipse.jst.server.tomcat.runtime.40",
		"org.eclipse.jst.server.tomcat.runtime.32",
    };

	protected static final HashMap<String, IExecutionEnvironment> environmentMap = new HashMap<String, IExecutionEnvironment>();
	static {
		environmentMap.put("org.eclipse.jst.server.tomcat.runtime.32", EnvironmentsManager.getDefault().getEnvironment(IExecutionEnvironmentConstants.EXEC_ENV_J2SE13)); //$NON-NLS-1$
		environmentMap.put("org.eclipse.jst.server.tomcat.runtime.40", EnvironmentsManager.getDefault().getEnvironment(IExecutionEnvironmentConstants.EXEC_ENV_J2SE13));//$NON-NLS-1$
		environmentMap.put("org.eclipse.jst.server.tomcat.runtime.41", EnvironmentsManager.getDefault().getEnvironment(IExecutionEnvironmentConstants.EXEC_ENV_J2SE13));//$NON-NLS-1$
		environmentMap.put("org.eclipse.jst.server.tomcat.runtime.50", EnvironmentsManager.getDefault().getEnvironment(IExecutionEnvironmentConstants.EXEC_ENV_J2SE14));//$NON-NLS-1$
		environmentMap.put("org.eclipse.jst.server.tomcat.runtime.55", EnvironmentsManager.getDefault().getEnvironment(IExecutionEnvironmentConstants.EXEC_ENV_J2SE14));//$NON-NLS-1$
		environmentMap.put("org.eclipse.jst.server.tomcat.runtime.60", EnvironmentsManager.getDefault().getEnvironment(IExecutionEnvironmentConstants.EXEC_ENV_J2SE15));//$NON-NLS-1$
		environmentMap.put("org.eclipse.jst.server.tomcat.runtime.70", EnvironmentsManager.getDefault().getEnvironment(IExecutionEnvironmentConstants.EXEC_ENV_JavaSE16));//$NON-NLS-1$
		environmentMap.put("org.eclipse.jst.server.tomcat.runtime.80", EnvironmentsManager.getDefault().getEnvironment(IExecutionEnvironmentConstants.EXEC_ENV_JavaSE17));//$NON-NLS-1$
	}
	
	
	public static final String TOMCAT_TYPE = "TOMCAT";  //$NON-NLS-1$
		
	/**
	 * The framework will no longer call this method, but should instead call 
	 * boolean initializeRuntime(RuntimeDefinition runtimeDef) throws CoreException 
	 * 
	 */
	@Override @Deprecated
	public void initializeRuntimes(List<RuntimeDefinition> runtimeDefinitions) {
		for (RuntimeDefinition runtimeDef : runtimeDefinitions) {
			try {
				initializeRuntime(runtimeDef);
			} catch(CoreException ce) {
				// We just have to swallow this, but this method is deprecated
				// So log it. 
				RuntimeTomcatActivator.logError("An error occured while creating a tomcat server", ce); //$NON-NLS-1$
			}
		}
	}
	
	@Override
	public boolean initializeRuntime(RuntimeDefinition runtimeDef) throws CoreException {
			boolean ret = false; 
			if (runtimeDef instanceof TomcatRuntimeDefinition) {
				TomcatRuntimeDefinition trd = (TomcatRuntimeDefinition) runtimeDef;
				IProgressMonitor monitor = new NullProgressMonitor();
				IRuntimeWorkingCopy trwc = getTomcatRuntimeWorkingCopy(trd.getLocation(), trd.getRuntimeTypeId(), monitor);
				if (trwc != null) {
					try {
						String serverName = trwc.getName();
						IRuntime runtime = trwc.getOriginal();
						if (runtime == null) {
							runtime = createRuntime(trwc, monitor);
						}
						if (runtime != null) {
							ret = true;
							IServerType serverType = findServerType(runtime.getRuntimeType());
							if (serverType != null) {
								createServer(runtime, serverName, serverType, monitor);
							}
						}
					} catch (CoreException e) {
						RuntimeTomcatActivator.logError("An error occured while creating a tomcat server", e); //$NON-NLS-1$
					}
				}
			}
			return ret;
	}

	private IRuntime createRuntime(IRuntimeWorkingCopy trwc, IProgressMonitor monitor) throws CoreException {
		if (trwc != null) {
			if (!trwc.getName().endsWith(RUNTIME_SUFFIX)) {
			  trwc.setName(trwc.getName()+RUNTIME_SUFFIX);
			}
			trwc.save(true, monitor);
			return trwc.getOriginal();
		}
		return null;
	}
	
	private IServerType findServerType(IRuntimeType runtimeType) {
		if( runtimeType != null ) {
			for (IServerType serverType : ServerCore.getServerTypes()) {
				if (runtimeType.equals(serverType.getRuntimeType())) {
					return serverType;
				}
			}
		}
		return null;
	}

	private static void createServer(IRuntime runtime, String name, IServerType serverType, IProgressMonitor monitor) throws CoreException {
		if (name.endsWith(RUNTIME_SUFFIX)) {
			name = name.substring(0,  name.lastIndexOf(RUNTIME_SUFFIX));
		}
		String uniqueName = ServerUtils.getUniqueServerName(name);
		IServerWorkingCopy serverWC = serverType.createServer(null, null,monitor);
		serverWC.setRuntime(runtime);
		serverWC.setName(uniqueName);
		serverWC.save(true, monitor);
	}
	
	@Override
	public RuntimeDefinition getRuntimeDefinition(File root,
			IProgressMonitor monitor) {
		if (monitor.isCanceled() || root == null) {
			return null;
		}
		RuntimeDefinition rd = null;
		IRuntimeWorkingCopy rwc = searchDir(root, monitor);
		if (rwc != null) {
			rd = new TomcatRuntimeDefinition(rwc.getName(), 
					                         rwc.getRuntimeType().getVersion(), 
					                         TOMCAT_TYPE, 
					                         root,
					                         rwc.getRuntimeType().getId());
		}
		return rd;
	}


	@Override
	public boolean exists(RuntimeDefinition runtimeDefinition) {
		// Does a wtp-style runtime with this location already exist?
		String path = getLocationForRuntimeDefinition(runtimeDefinition);
		if (path == null) {
			return false;
		}
		IServer[] servers = ServerCore.getServers();
		for (int i = 0; i < servers.length; i++) {
			IRuntime runtime = servers[i].getRuntime();
			if (runtime != null && runtime.getLocation() != null) {
				if (isRuntimeMatchLocation(path, runtime)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isRuntimeMatchLocation(String absolutePath, IRuntime runtime) {
		if (runtime == null || runtime.getLocation() == null || absolutePath == null) {
			return false;
		}
		String loc = runtime.getLocation().toOSString();
		try {
			loc = new File(loc).getCanonicalPath();
		} catch (IOException e) {
			RuntimeTomcatActivator.logError(e);
		}
		return absolutePath.equals(loc);
	}

	private String getLocationForRuntimeDefinition(RuntimeDefinition runtimeDefinition) {
		String path = null;
		if (runtimeDefinition != null && runtimeDefinition.getLocation() != null) {
			File location = runtimeDefinition.getLocation();
			if (location != null && location.isDirectory()) {
				try {
					path = location.getCanonicalPath();
				} catch (IOException e) {
					RuntimeTomcatActivator.logError(e);
					path = location.getAbsolutePath();
				}
			}
		}
		return path;
	}
	
	private IRuntimeWorkingCopy searchDir(File dir, IProgressMonitor monitor) {
		File conf = new File(dir,"conf"); //$NON-NLS-1$
		if (conf.isDirectory() && conf.exists()) {
			return getRuntimeWorkingCopyFromDir(dir, monitor);
		}
		return null;
	}

	private IRuntimeWorkingCopy getRuntimeWorkingCopyFromDir(File dir, IProgressMonitor monitor) {
		for (String runtimeTypeId : runtimeTypes) {
			IRuntimeWorkingCopy rwc = getTomcatRuntimeWorkingCopy(dir, runtimeTypeId, monitor);
			if (rwc != null) {
				ITomcatRuntimeWorkingCopy wc = (ITomcatRuntimeWorkingCopy) rwc.loadAdapter(ITomcatRuntimeWorkingCopy.class, null);
				return rwc;
			}
		}
		return null;
	}

	private IRuntimeWorkingCopy getTomcatRuntimeWorkingCopy(File dir, String runtimeTypeId, IProgressMonitor monitor) {
		try {
			IRuntimeType runtimeType = ServerCore.findRuntimeType(runtimeTypeId);
			String absolutePath = dir.getAbsolutePath();
			IRuntime runtime = getRuntimeAt(runtimeTypeId, absolutePath);
			IRuntimeWorkingCopy runtimeWc = null; 
			if (runtime == null) {
				String id = absolutePath.replace(File.separatorChar,'_').replace(':','-');
				runtimeWc = runtimeType.createRuntime(id, monitor);
				runtimeWc.setName(dir.getName());
				runtimeWc.setLocation(new Path(absolutePath));
				ITomcatRuntimeWorkingCopy wc = (ITomcatRuntimeWorkingCopy) runtimeWc.loadAdapter(ITomcatRuntimeWorkingCopy.class, null);
				wc.setVMInstall(findVMForRuntimeType(runtimeTypeId));
			} else {
				runtimeWc = runtime.createWorkingCopy();
			}
			
			IStatus status = runtimeWc.validate(monitor);
			if (status == null || status.getSeverity() != IStatus.ERROR) {
				return runtimeWc;
			}
		} catch (Exception e) {
			RuntimeTomcatActivator.logError("Could not find runtime", e); //$NON-NLS-1$
		}
		return null;
	}
	
	private IVMInstall findVMForRuntimeType(String runtimeTypeId) {
		IExecutionEnvironment env = environmentMap.get(runtimeTypeId);
		if( env != null ) {
			IVMInstall install = env.getDefaultVM();
			if( install != null ) {
				return install;
			}
			IVMInstall[] arr = env.getCompatibleVMs();
			if( arr != null && arr.length > 0 ) {
				return arr[0];
			}
		}
		// Could maybe return null here?  Until we know for sure, lets just return workspace default
		return JavaRuntime.getDefaultVMInstall();
	}
	
	
	private IRuntime getRuntimeAt(String runtimeTypeId, String absolutePath) {
		for (IRuntime runtime : ServerCore.getRuntimes()) {
			IRuntimeType runtimeType = runtime.getRuntimeType();
			if(runtimeType != null 
				&& runtimeTypeId.equals(runtimeType.getId())
				&& isRuntimeMatchLocation(absolutePath, runtime)) {
				return runtime;
			}
		}
		return null;
	}

	public static class TomcatRuntimeDefinition extends RuntimeDefinition {

		private String runtimeTypeId;

		public TomcatRuntimeDefinition(String name, String version,
				String type, File location, String runtimeTypeId) {
			super(name, version, type, location);
			this.runtimeTypeId = runtimeTypeId;
		}

		public String getRuntimeTypeId() {
			return runtimeTypeId;
		}
	}

}
