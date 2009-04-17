/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;

/**
 *
 * @author rob.stryker@jboss.com
 */
public interface IDeployableServer extends IJBossServerConstants {
	public static final String DEPLOY_DIRECTORY = "org.jboss.ide.eclipse.as.core.server.deployDirectory";
	public static final String TEMP_DEPLOY_DIRECTORY = "org.jboss.ide.eclipse.as.core.server.tempDeployDirectory";
	public static final String DEPLOY_DIRECTORY_TYPE = "org.jboss.ide.eclipse.as.core.server.deployDirectoryType";
	public static final String ZIP_DEPLOYMENTS_PREF = "org.jboss.ide.eclipse.as.core.server.zipDeploymentsPreference";

	public static final String DEPLOY_METADATA = "metadata";
	public static final String DEPLOY_CUSTOM = "custom";
	public static final String DEPLOY_SERVER = "server";
	
	public String getDeployFolder();
	public void setDeployFolder(String folder);
	public String getTempDeployFolder();
	public void setTempDeployFolder(String folder);
	public String getDeployLocationType();
	public void setDeployLocationType(String type);
	public boolean zipsWTPDeployments();
	public void setZipWTPDeployments(boolean val);
	
	
	public String getConfigDirectory();
	public ServerAttributeHelper getAttributeHelper();
	public IServer getServer();
}
