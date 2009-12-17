/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerPort;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.ModuleUtil;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.wtp.core.modules.IJBTModule;

public class DeployableServer extends ServerDelegate implements IDeployableServer {

	public DeployableServer() {
	}

	protected void initialize() {
	}
	
	public void setDefaults(IProgressMonitor monitor) {
		IRuntime rt = getServer().getRuntime();
		if( rt != null ) {
			getServerWorkingCopy().setName(ServerUtil.getDefaultServerName(rt));
		}
	}
	
	public void importRuntimeConfiguration(IRuntime runtime, IProgressMonitor monitor) throws CoreException {
	}

	public void saveConfiguration(IProgressMonitor monitor) throws CoreException {
	}

	public void configurationChanged() {
	}
	
	/*
	 * Abstracts to implement
	 */
	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
		return Status.OK_STATUS;
	}

	public IModule[] getChildModules(IModule[] module) {
		IModule[] children = ModuleUtil.getChildModules(module);
		return children;
	}

    public IModule[] getRootModules(IModule module) throws CoreException {
        IStatus status = canModifyModules(new IModule[] { module }, null);
        if (status != null && !status.isOK())
            throw  new CoreException(status);;
        IModule[] parents = doGetParentModules(module);
        if(parents.length>0)
        	return parents;
        return new IModule[] { module };
    }


	private IModule[] doGetParentModules(IModule module) {
		// get all supported modules
		IModule[] supported = 
			org.eclipse.wst.server.core.ServerUtil.getModules(
					getServer().getServerType().getRuntimeType().getModuleTypes());
		ArrayList<IModule> list = new ArrayList<IModule>();
		
		for( int i = 0; i < supported.length; i++ ) {
			IEnterpriseApplication jeeMod = (IEnterpriseApplication)supported[i].loadAdapter(IEnterpriseApplication.class,null);
			IJBTModule jbtMod = (IJBTModule)supported[i].loadAdapter(IJBTModule.class, null);
			if( jeeMod != null ) {
				IModule[] childs = jeeMod.getModules();
				for (int j = 0; j < childs.length; j++) {
					if(childs[j].equals(module))
						list.add(supported[i]);
				}
			} else if( jbtMod != null ) {
				IModule[] childs = jbtMod.getModules();
				for (int j = 0; j < childs.length; j++) {
					if(childs[j].equals(module))
						list.add(supported[i]);
				}
			}
		}
		return list.toArray(new IModule[list.size()]);
	}

	public ServerPort[] getServerPorts() {
		return new ServerPort[0];
	}
	
	public void modifyModules(IModule[] add, IModule[] remove,
			IProgressMonitor monitor) throws CoreException {
	}
	
	
	public String getDeployFolder() {
		IJBossServerRuntime jbsrt = getRuntime();
		return ServerUtil.makeGlobal(jbsrt, new Path(getAttribute(DEPLOY_DIRECTORY, ""))).toString(); //$NON-NLS-1$
	}
	
	public void setDeployFolder(String folder) {
		setAttribute(DEPLOY_DIRECTORY, ServerUtil.makeRelative(getRuntime(), new Path(folder)).toString());
	}
	
	public String getTempDeployFolder() {
		IJBossServerRuntime jbsrt = getRuntime();
		return ServerUtil.makeGlobal(jbsrt, new Path(getAttribute(TEMP_DEPLOY_DIRECTORY, ""))).toString(); //$NON-NLS-1$
	} 
	
	public void setTempDeployFolder(String folder) {
		setAttribute(TEMP_DEPLOY_DIRECTORY, ServerUtil.makeRelative(getRuntime(), new Path(folder)).toString());
	}
	
	public void setDeployLocationType(String type) {
		setAttribute(DEPLOY_DIRECTORY_TYPE, type);
	}
	
	public String getDeployLocationType() {
		return getAttribute(DEPLOY_DIRECTORY_TYPE, DEPLOY_CUSTOM);
	}
	
	public void setZipWTPDeployments(boolean val) {
		setAttribute(ZIP_DEPLOYMENTS_PREF, val);
	}
	public boolean zipsWTPDeployments() {
		return getAttribute(ZIP_DEPLOYMENTS_PREF, false);
	}

	
	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.as.core.server.attributes.IDeployableServer#getAttributeHelper()
	 */
	public ServerAttributeHelper getAttributeHelper() {
		IServerWorkingCopy copy = getServerWorkingCopy();
		if( copy == null ) {
			copy = getServer().createWorkingCopy();
		}
		return new ServerAttributeHelper(getServer(), copy);
	}

	// only used for xpaths and is a complete crap hack ;) misleading, too
	public String getConfigDirectory() {
		return getDeployFolder();
	}
	
	public IJBossServerRuntime getRuntime() {
		IJBossServerRuntime  ajbsrt = null;
		if( getServer().getRuntime() != null ) {
			ajbsrt = (IJBossServerRuntime) getServer().getRuntime()
					.loadAdapter(IJBossServerRuntime.class,
							new NullProgressMonitor());
		}
		return ajbsrt;
	}

}
