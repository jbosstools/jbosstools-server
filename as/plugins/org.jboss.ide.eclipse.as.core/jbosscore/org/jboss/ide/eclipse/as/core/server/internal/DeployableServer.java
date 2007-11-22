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
package org.jboss.ide.eclipse.as.core.server.internal;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;

// TODO: share the logic for modules with normal deployable server!
public class DeployableServer extends ServerDelegate implements IDeployableServer {

	public DeployableServer() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.wst.server.core.model.ServerDelegate#canModifyModules(org.eclipse.wst.server.core.IModule[], org.eclipse.wst.server.core.IModule[])
	 */
	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
		return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID,0, "OK", null);
	}

	public IModule[] getChildModules(IModule[] module) {
		int last = module.length-1;
		if (module[last] != null && module[last].getModuleType() != null) {
			IModuleType moduleType = module[last].getModuleType();
			if("jst.ear".equals(moduleType.getId())) { //$NON-NLS-1$
				IEnterpriseApplication enterpriseApplication = (IEnterpriseApplication) module[0]
						.loadAdapter(IEnterpriseApplication.class, null);
				if (enterpriseApplication != null) {
					IModule[] earModules = enterpriseApplication.getModules(); 
					if ( earModules != null) {
						return earModules;
					}
				}
			}
			else if ("jst.web".equals(moduleType.getId())) { //$NON-NLS-1$
				IWebModule webModule = (IWebModule) module[last].loadAdapter(IWebModule.class, null);
				if (webModule != null) {
					IModule[] modules = webModule.getModules();
					return modules;
				}
			}
		}
		return new IModule[0];
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
		IModule[] ears = ServerUtil.getModules("jst.ear"); //$NON-NLS-1$
		ArrayList list = new ArrayList();
		for (int i = 0; i < ears.length; i++) {
			IEnterpriseApplication ear = (IEnterpriseApplication)ears[i].loadAdapter(IEnterpriseApplication.class,null);
			IModule[] childs = ear.getModules();
			for (int j = 0; j < childs.length; j++) {
				if(childs[j].equals(module))
					list.add(ears[i]);
			}
		}
		return (IModule[])list.toArray(new IModule[list.size()]);
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.wst.server.core.model.ServerDelegate#modifyModules(org.eclipse.wst.server.core.IModule[], org.eclipse.wst.server.core.IModule[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void modifyModules(IModule[] add, IModule[] remove,
			IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.jboss.ide.eclipse.as.core.server.attributes.IDeployableServer#getDeployDirectory()
	 */
	public String getDeployDirectory() {
		return getAttribute(DEPLOY_DIRECTORY, "");
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
		return getDeployDirectory();
	}
}
