/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal;

import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants.DEPLOY;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.JBOSSTOOLS_TMP;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.SERVER_AS_50;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.SERVER_PASSWORD;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.SERVER_USERNAME;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.TEMP_DEPLOY;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.TMP;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IURLProvider;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.ILaunchConfigConfigurator;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel;
import org.jboss.tools.as.core.server.controllable.systems.IPortsController;
import org.jboss.tools.jmx.core.IConnectionFacade;
import org.jboss.tools.jmx.core.IConnectionWrapper;

/**
 * 
 * @author Rob Stryker rob.stryker@jboss.com
 *
 */
public class JBossServer extends DeployableServer 
		implements IJBossServer, IURLProvider, IConnectionFacade {
	
	public static final String AUTOMATICALLY_UPDATE_LAUNCH = ILaunchConfigConfigurator.AUTOMATICALLY_UPDATE_LAUNCH;
	
	public void setDefaults(IProgressMonitor monitor) {
		super.setDefaults(monitor);
		setAttribute(IJBossToolingConstants.IGNORE_LAUNCH_COMMANDS, false);
		setAttribute("auto-publish-time", 1); //$NON-NLS-1$
		setAttribute("id", getAttribute("id", (String)"") + new Date().getTime()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		setUsername("admin"); //$NON-NLS-1$
		
		// TODO move this to an extended properties setting
		boolean defaultServerDeployment = isAS50() || isEAP(getServer());
		setDeployLocationType(defaultServerDeployment ? IDeployableServer.DEPLOY_SERVER : IDeployableServer.DEPLOY_METADATA);
		ServerProfileModel.setProfile(getServerWorkingCopy(), ServerProfileModel.DEFAULT_SERVER_PROFILE);
	}
	
	@Deprecated
	public static boolean isEAP(IServer server) {
		return RuntimeUtils.isEAP(server.getServerType().getRuntimeType());
	}

	private boolean isAS50() {
		IRuntimeType rtt = getServer().getServerType().getRuntimeType();
		return rtt == null ? false : rtt.getId().equals(SERVER_AS_50);
	}
	
	public String getHost() {
		return getServer().getHost();
	}
		
	/**
	 * The full path of the configuration, ex:
	 *  /home/rob/tmp/default_copy3 would return /home/rob/tmp/default_copy3 
	 *  /home/rob/jboss-5.x.x/server/default would return /home/rob/jboss-5.x.x/server/default
	 * @return
	 */
	public String getConfigDirectory() {
		IJBossServerRuntime runtime = (IJBossServerRuntime)getServer().getRuntime().loadAdapter(IJBossServerRuntime.class, null);
		return runtime.getConfigurationFullPath().toOSString();
	}
	
	public String getDeployFolder() {
		return getDeployFolder(getDeployLocationType());
	}
	
	public String getDeployFolder(String type) {
		if( type.equals(DEPLOY_SERVER)) {
			return getExtendedProperties().getServerDeployLocation();
		}
		return getDeployFolder(this, type);
	}
	
	/**
	 * Legacy method used by some (such as UI) who want to 
	 * deeply introspect the value for specific deployment location
	 * constants. 
	 * 
	 * Only Custom and Metadata are supported for this method
	 * Server paths will not be returned here (server/default/deploy etc)
	 * 
	 * @param jbs
	 * @param type
	 * @return
	 */
	public static String getDeployFolder(JBossServer jbs, String type) {
		String ret = null;
		if( type.equals(DEPLOY_CUSTOM)) {
			ret = getCustomDeployLocationFromSettings(jbs.getServer(), DEPLOY_DIRECTORY);
		}
		if( ret == null || type.equals(DEPLOY_METADATA)) {
			ret = getMetadataDeployLocation(jbs.getServer());
		} 
		return ret;
	}
	
	public static String getCustomDeployLocationFromSettings(IServer server, String attribute) {
		String val = server.getAttribute(attribute, (String)null);
		if( val != null ) {
			IPath val2 = new Path(val);
			return ServerUtil.makeGlobal(server.getRuntime(), val2).toString();
		}
		return null;
	}
	
	public static String getMetadataDeployLocation(IServer server) {
		return JBossServerCorePlugin.getServerStateLocation(server).append(DEPLOY).makeAbsolute().toString();
	}
	
	public JBossExtendedProperties getExtendedProperties() {
		return (JBossExtendedProperties)getServer().loadAdapter(ServerExtendedProperties.class, null);
	}
	
	public String getTempDeployFolder() {
		return getTempDeployFolder(this, getDeployLocationType());
	}
	
	/**
	 * Legacy method used by some (such as UI) who want to 
	 * deeply introspect the value for specific deployment location
	 * constants. 
	 * 
	 * @param jbs
	 * @param type
	 * @return
	 */
	public static String getTempDeployFolder(JBossServer jbs, String type) {
		IServer server = jbs.getServer();
		IJBossServerRuntime jbsrt =RuntimeUtils.getJBossServerRuntime(server);
		if( type.equals(DEPLOY_CUSTOM))
			return ServerUtil.makeGlobal(jbsrt.getRuntime(), 
					new Path(server.getAttribute(TEMP_DEPLOY_DIRECTORY, ""))).toString(); //$NON-NLS-1$
		if( type.equals(DEPLOY_METADATA)) {
			return JBossServerCorePlugin.getServerStateLocation(server).
				append(TEMP_DEPLOY).makeAbsolute().toString();
		} else if( type.equals(DEPLOY_SERVER)) {
			if( jbsrt != null ) {
				String loc = jbsrt.getConfigLocation();
				String config = jbsrt.getJBossConfiguration();
				IPath p = new Path(loc)
					.append(config).append(TMP)
					.append(JBOSSTOOLS_TMP);
				return ServerUtil.makeGlobal(jbsrt.getRuntime(), p).toString();
			}
		}
		return null;
	}
	
	public int getJNDIPort() {
		return findPort(IPortsController.KEY_JNDI, 1099);
	}
	
	protected int getWebPort() {
		return getJBossWebPort();
	}

	public int getJBossWebPort() {
		return findPort(IPortsController.KEY_WEB, 8080);
	}
	
	/**
	 * @since 2.4
	 */
	protected int getPortOffset() {
		return findPort(IPortsController.KEY_PORT_OFFSET, 0);
	}

	protected int findPort(int key, int defaultValue) {
		try {
			IControllableServerBehavior behavior = JBossServerBehaviorUtils.getControllableBehavior(getServer());
			if (behavior != null) {
				IPortsController pc = (IPortsController) behavior.getController(IPortsController.SYSTEM_ID);
				if (pc != null) {
					return pc.findPort(key, defaultValue);
				}
			}
		} catch(CoreException ce) {
			JBossServerCorePlugin.log(ce.getStatus());
		} catch(Throwable t) {
			JBossServerCorePlugin.log(t);
		}
		return defaultValue;
	}
	
	
	// first class parameters
	@Override
	public String getUsername() {
		return getAttribute(SERVER_USERNAME, "admin"); //$NON-NLS-1$
	}
	
	/**
	 * Get this value raw, as it is currently stored, with no defaults 
	 * @return
	 */
	public String getRawUsername() {
		return getAttribute(SERVER_USERNAME, (String)null);
	}

	
	public void setUsername(String name) {
		setAttribute(SERVER_USERNAME, name);
	}
	
	@Override
	public String getPassword() {
		String s = ServerUtil.getFromSecureStorage(getServer(), SERVER_PASSWORD);
		if( s == null )
			return getAttribute(SERVER_PASSWORD, "admin"); //$NON-NLS-1$
		return s;
	}
	
	/**
	 * Get this password as it is currently stored, without any default values appended
	 */
	public String getRawPassword() {
		String s = ServerUtil.getFromSecureStorage(getServer(), SERVER_PASSWORD);
		if( s == null )
			return getAttribute(SERVER_PASSWORD, (String)null);
		return s;
	}

	
	public void setPassword(String pass) {
		try {
			ServerUtil.storeInSecureStorage(getServer(), SERVER_PASSWORD, pass);
        } catch (StorageException e) {
        	JBossServerCorePlugin.log(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Could not save password for server in secure storage.", e)); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
        	JBossServerCorePlugin.log(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Could not save password for server in secure storage.", e)); //$NON-NLS-1$	
        }
	}
	
	
	/*
	 * WTF method??
	 */
	public boolean hasJMXProvider() {
		org.jboss.tools.as.core.server.controllable.internal.DeployableServerBehavior beh = 
		(org.jboss.tools.as.core.server.controllable.internal.DeployableServerBehavior)getServer().loadAdapter(
				org.jboss.tools.as.core.server.controllable.internal.DeployableServerBehavior.class, new NullProgressMonitor());
		if( beh == null )
			return false;
		return true;
	}

	@Override
	public IConnectionWrapper getJMXConnection() {
		return (IConnectionWrapper) Platform.getAdapterManager().loadAdapter((JBossServer)this, IConnectionWrapper.class.getName());
	}

}