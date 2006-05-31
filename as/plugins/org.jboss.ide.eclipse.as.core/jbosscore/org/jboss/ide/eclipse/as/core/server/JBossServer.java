package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerPort;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.jboss.ide.eclipse.as.core.model.DescriptorModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel;
import org.jboss.ide.eclipse.as.core.model.DescriptorModel.ServerDescriptorModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ServerProcessModelEntity;
import org.jboss.ide.eclipse.as.core.server.runtime.JBossRuntimeConfiguration;
import org.jboss.ide.eclipse.as.core.server.runtime.JBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.ASDebug;

public class JBossServer extends ServerDelegate {

	
	private JBossServerRuntime runtime;
	private JBossRuntimeConfiguration rtConfig;
	
	
	public JBossServer() {
		rtConfig = new JBossRuntimeConfiguration(this);
	}

	
	protected void initialize() {
		
	}
	
	public void debug( String s ) {
		ASDebug.p(s, this);
	}
	
	/*
	 * OVERRIDES
	 */

	public void setDefaults(IProgressMonitor monitor) {
		debug("setDefaults");
	}
	
	public void importRuntimeConfiguration(IRuntime runtime, IProgressMonitor monitor) throws CoreException {
		debug("import Runtime Configuration");
		//getJBossRuntime();
	}

	public void saveConfiguration(IProgressMonitor monitor) throws CoreException {
		debug("saveConfiguration");
		rtConfig.save();
	}

	public void configurationChanged() {
		debug("configurationChanged");
		rtConfig.save();		
	}


	/*
	 * Other
	 */
	public void setRuntime(JBossServerRuntime run) {
		runtime = run;
	}
	
	public JBossServerRuntime getJBossRuntime() {
		if( runtime == null ) {
			runtime = (JBossServerRuntime) getServer().getRuntime().loadAdapter(JBossServerRuntime.class, null);
		}
		return runtime;
		
	}
	
	
	public JBossRuntimeConfiguration getRuntimeConfiguration() {
		return rtConfig;
	}

	

	
	
	
	
	/*
	 * Abstracts to implement
	 */
	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
		debug("canModifyModules");
		return null;
	}

	public IModule[] getChildModules(IModule[] module) {
		//debug("*****  getChildModules");
		return null;
	}

	// As of now none of my modules are implementing the parent / child nonesense
	public IModule[] getRootModules(IModule module) throws CoreException {
		//debug("***** getRootModules");
		return new IModule[] {  };
	}

	
	public void modifyModules(IModule[] add, IModule[] remove,
			IProgressMonitor monitor) throws CoreException {
		if( add == null ) add = new IModule[0];
		if( remove == null ) add = new IModule[0];
		
		debug("****** modifyModules, " + add.length + " added, " + remove.length + " removed.");
		Object o = getServer().loadAdapter(JBossServerBehavior.class, monitor);
		if( o != null ) {
			JBossServerBehavior behavior = (JBossServerBehavior)o;
			behavior.publishStart(monitor);
			behavior.publishModule(IServer.PUBLISH_INCREMENTAL, ServerBehaviourDelegate.REMOVED, remove, monitor);
			behavior.publishModule(IServer.PUBLISH_INCREMENTAL, ServerBehaviourDelegate.ADDED, add, monitor);
			behavior.publishFinish(monitor);
		}
	}
	
	public ServerPort[] getServerPorts() {
		debug("****** getServerPorts");
		return null;
	}

	public ServerProcessModelEntity getProcessModel() {
		return ServerProcessModel.getDefault().getModel(getServer().getId(), true);
	}
	
	public ServerDescriptorModel getDescriptorModel() {
		return DescriptorModel.getDefault().getServerModel(getServer());
	}

	
}
