package org.jboss.ide.eclipse.as.core.server.stripped;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.model.DescriptorModel;
import org.jboss.ide.eclipse.as.core.model.DescriptorModel.ServerDescriptorModel;
import org.jboss.ide.eclipse.as.core.server.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.server.attributes.IDeployableServer;

public class DeployableServer extends ServerDelegate implements IDeployableServer {

	public static final String DEPLOY_DIRECTORY = "org.jboss.ide.eclipse.as.core.server.stripped.deploy_directory";
	
	public DeployableServer() {
		// TODO Auto-generated constructor stub
	}

	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
		return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID,0, "OK", null);
	}

	public IModule[] getChildModules(IModule[] module) {
		// TODO Auto-generated method stub
		return null;
	}

	public IModule[] getRootModules(IModule module) throws CoreException {
		return new IModule[] { module };
	}

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

	public ServerDescriptorModel getDescriptorModel() {
		return DescriptorModel.getDefault().getServerModel(new Path(getDeployDirectory()));
	}


}
