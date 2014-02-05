/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.as.core.server.controllable.systems;

import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants.DEPLOY;

import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;

/**
 * An abstract class that is exposed though not encouraged to be extended. 
 * This class aids in getting and setting deployment preferences for filesystem-based
 * servers that intend to use the filesystem for deployment, whether local or remote. 
 *
 */
public abstract class AbstractJBossDeploymentOptionsController extends
		AbstractSubsystemController implements IDeploymentOptionsController {
	
	/*
	 * Constants taken from IDeployableServer
	 * They should be removed from there if possible
	 */
	protected static final String DEPLOY_METADATA = "metadata"; //$NON-NLS-1$
	protected static final String DEPLOY_SERVER = "server"; //$NON-NLS-1$
	protected static final String DEPLOY_CUSTOM = "custom"; //$NON-NLS-1$
	protected static final String DEPLOY_DIRECTORY = "org.jboss.ide.eclipse.as.core.server.deployDirectory"; //$NON-NLS-1$
	protected static final String TEMP_DEPLOY_DIRECTORY = "org.jboss.ide.eclipse.as.core.server.tempDeployDirectory"; //$NON-NLS-1$
	protected static final String DEPLOY_DIRECTORY_TYPE = "org.jboss.ide.eclipse.as.core.server.deployDirectoryType"; //$NON-NLS-1$
	protected static final String ZIP_DEPLOYMENTS_PREF = "org.jboss.ide.eclipse.as.core.server.zipDeploymentsPreference"; //$NON-NLS-1$

	public AbstractJBossDeploymentOptionsController() {
		super();
	}

	
	protected IServerWorkingCopy getWorkingCopy() throws IllegalStateException {
		IServerWorkingCopy o = super.getWorkingCopy();
		if( o == null)
			throw new IllegalStateException();
		return o;
	}

	/**
	 * Convert the given server-relative path to an absolute filesystem path 
	 * 
	 * @param server
	 * @param original
	 * @return
	 */
	protected abstract String makeGlobal(IServerAttributes server, String original);
	
	@Override
	public String getDeploymentsRootFolder(boolean absolute) {
		String result = getDeployFolder(getCurrentDeploymentLocationType());
		if( absolute ) {
			result = makeGlobal(getServer(), result);
		}
		return result;
	}

	@Override
	public String getDeploymentsTemporaryFolder(boolean absolute) {
		String result = getTempDeployFolder(getCurrentDeploymentLocationType());
		if( absolute ) {
			return makeGlobal(getServer(), result);
		}
		return result;
	}

	@Override
	public String getCurrentDeploymentLocationType() {
		return getServerOrWC().getAttribute(DEPLOY_DIRECTORY_TYPE, DEPLOY_CUSTOM);
	}

	@Override
	public void setCurrentDeploymentLocationType(String type)
			throws IllegalStateException {
		getWorkingCopy().setAttribute(DEPLOY_DIRECTORY_TYPE, type);
	}

	@Override
	public void setDeploymentsRootFolder(String folder)
			throws IllegalStateException {
		getWorkingCopy().setAttribute(DEPLOY_DIRECTORY, folder);
	}

	@Override
	public void setDeploymentsTemporaryFolder(String folder)
			throws IllegalStateException {
		getWorkingCopy().setAttribute(TEMP_DEPLOY_DIRECTORY, folder);
	}

	@Override
	public void setPrefersZippedDeployments(boolean val) throws IllegalStateException {
		getWorkingCopy().setAttribute(ZIP_DEPLOYMENTS_PREF, val);
	}

	@Override
	public boolean prefersZippedDeployments() {
		return getServerOrWC().getAttribute(ZIP_DEPLOYMENTS_PREF, false);
	}
	
	
	/**
	 * 
	 * Get the deploy folder for the given setting.
	 * Examples of settings are any element of the result array
	 * from getDeploymentLocationTypes().
	 * 
	 * These may be 'SERVER_RELATIVE', 'CUSTOM', 'ABSOLUTE', "METADATA", 
	 * or any other constant that fits for your server adapter type
	 * 
	 * @param type
	 * @return
	 */
	protected abstract String getDeployFolder(String type);

	/**
	 * 
	 * Get the temporary deploy folder for the given setting.
	 * Examples of settings are any element of the result array
	 * from getDeploymentLocationTypes().
	 * 
	 * These may be 'SERVER_RELATIVE', 'CUSTOM', 'ABSOLUTE', "METADATA", 
	 * or any other constant that fits for your server adapter type
	 * 
	 * @param type
	 * @return
	 */
	protected abstract String getTempDeployFolder(String type);
	
	
	/**
	 * A utility method useful for checking if the server-type is
	 * of which layout style
	 * @return
	 */
	protected boolean isAS7Structure() {
		ServerExtendedProperties sep = ExtendedServerPropertiesAdapterFactory.getServerExtendedProperties(getServerOrWC());
		if (sep != null && sep.getFileStructure() == ServerExtendedProperties.FILE_STRUCTURE_CONFIG_DEPLOYMENTS) {
			return true;
		}
		return false;
	}
	
	protected boolean isDeployOnlyServer() {
		return getServer().getServerType().getId().equals(IJBossToolingConstants.DEPLOY_ONLY_SERVER);
	}

	/**
	 * Get the metadata location in the workspace's metadata folder
	 * for the given server
	 *  
	 * @param server
	 * @return
	 */
	protected String getMetadataDeployLocation(IServer server) {
		return JBossServerCorePlugin.getServerStateLocation(server).append(DEPLOY).makeAbsolute().toString();
	}
	
	protected JBossExtendedProperties getExtendedProperties() {
		return (JBossExtendedProperties)getServer().loadAdapter(ServerExtendedProperties.class, null);
	}
}
