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
package org.jboss.ide.eclipse.as.core.util;

import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethodType;
import org.jboss.ide.eclipse.as.core.server.internal.BehaviourModel;

/**
 * A stripped down version of this class has been copied and made internal in the 
 * org.jboss.tools.as.core.internal.modules package. 
 * 
 * This class will be removed once all servers and sections of code 
 * no longer make use of it. 
 * @deprecated
 */
public class DeploymentPreferenceLoader {
	public static final String DEPLOYMENT_PREFERENCES_KEY = "org.jboss.ide.eclipse.as.core.util.deploymentPreferenceKey"; //$NON-NLS-1$
	
	/**
	 * This method can return null and is only meant to show what deployment method
	 * is currently stored in a server's deployment preferences.
	 * 
	 * To get a more accurate version (with a default) please use
	 * {@link DeployableServerBehavior#createPublishMethod()#getPublishMethodType()}
	 * 
	 * @deprecated use BehaviourModel.getPublishMethodType
	 * @param server
	 * @return
	 */
	public static IJBossServerPublishMethodType getCurrentDeploymentMethodType(IServer server) {
		return getCurrentDeploymentMethodType(server, null);
	}

	/**
	 * @deprecated use BehaviourModel.getPublishMethodType
	 * @param server
	 * @param defaultType
	 * @return
	 */
	public static IJBossServerPublishMethodType getCurrentDeploymentMethodType(IServer server, String defaultType) {
		return BehaviourModel.getPublishMethodType(server, defaultType);
	}

	@Deprecated
	public static String getCurrentDeploymentMethodTypeId(IServerAttributes server) {
		return getCurrentServerBehaviorModeTypeId(server, null);
	}
	
	@Deprecated
	public static String getCurrentDeploymentMethodTypeId(IServerAttributes server, String defaultType) {
		return getCurrentServerBehaviorModeTypeId(server, defaultType);
	}

	@Deprecated
	public static String getCurrentServerBehaviorModeTypeId(IServerAttributes server, String defaultType) {
		return server.getAttribute(IDeployableServer.SERVER_MODE, defaultType);
	}
}
