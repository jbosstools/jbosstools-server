package org.jboss.ide.eclipse.as.core.client.verifiers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Server;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.client.TwiddleLauncher;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.ProcessLogEvent;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ProcessData;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ServerProcessModelEntity;
import org.jboss.ide.eclipse.as.core.module.factory.JBossModuleDelegate;
import org.jboss.ide.eclipse.as.core.server.IServerProcessListener;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.JBossServerBehavior;
import org.jboss.ide.eclipse.as.core.server.ServerProcessEvent;
import org.jboss.ide.eclipse.as.core.server.runtime.AbstractServerRuntimeDelegate;
import org.jboss.ide.eclipse.as.core.server.runtime.IJBossServerRuntimeDelegate;
import org.jboss.ide.eclipse.as.core.server.runtime.JBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.ASDebug;

public abstract class AbstractDeploymentVerifier implements IJbossDeploymentVerifier,
					IServerProcessListener, IDebugEventSetListener {

	protected JBossModuleDelegate delegate;
	protected ServerProcessModelEntity model;
	protected JBossServer jbServer;
	protected ProcessLogEvent logEvent;
	
	public AbstractDeploymentVerifier(JBossModuleDelegate delegate) {
		this.delegate = delegate;
	}
		
	public boolean supportsVerify(IServer server, String launchMode) {
		if( JBossServerCore.getServer(server) == null ) {
			return false;
		}
		return true;
	}
	
	/**
	 * For now we're just returning a non-error status that the server will
	 * accept as good news. Then I go off doing the verification on my own. 
	 */
	public IStatus verifyDeployed(JBossServer jbServer, String launchMode, ILaunch launch) {
		ASDebug.p("Verifying that module deployed correctly! mod=" + 
				delegate.getResourcePath(), this);

		this.jbServer = jbServer;
		Thread t = new Thread(getVerifierRunnable());
		t.start();

		return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, 0, "Success", null);
	}


	
	
	protected abstract void initialize();
	protected abstract void cleanup();
	
	private void abstractInitialize() {
		model = jbServer.getProcessModel();
		model.clear(ServerProcessModel.TWIDDLE_PROCESSES);
		model.addSPListener(this);
		DebugPlugin.getDefault().addDebugEventListener(this);
		
		logEvent = new ProcessLogEvent("Verifying deployment of " 
				+ delegate.getResourceName(), ProcessLogEvent.SERVER_VERIFY);

		String id = jbServer.getServer().getId();
		ServerProcessModelEntity ent = ServerProcessModel.getDefault().getModel(id);
		ent.getEventLog().addChild(logEvent, ProcessLogEvent.ADD_BEGINNING);

	}
	
	private void abstractCleanup() {
		model.removeSPListener(this);
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}
	
	
	/**
	 * This is the real meat and potatoes of the verifier. 
	 * Initialize the abstract level, let the subclass initialize.
	 * Then run the verification process, and clean up as appropriate.
	 * 
	 * (The runVerifier method should block and not return until 
	 * complete, however the subclass is allowed to ignore 
	 * this suggestion and spawn an additional thread if they wish.) 
	 * @return
	 */
	private Runnable getVerifierRunnable() {
		return new Runnable() {
			public void run() {
				abstractInitialize();
				initialize();

				runVerifier();

				cleanup();
				abstractCleanup();
			} 
		};
	}
	
	protected abstract void runVerifier();

	
	
	

	/**
	 * Create some twiddle threads (with some args) to test whether
	 * a deployment was successful or some service is up and running. 
	 * 
	 * @param twiddleArgs
	 */
	protected void launchTwiddleThreads(String[] twiddleArgs) {
		String seed = getClass().getName() + delegate.getFactory().getFactoryId();
		TwiddleLauncher.launchTwiddles(twiddleArgs, jbServer, seed);
	}

	
	/**
	 * Tell the server process model to start listening right away to 
	 * these procecsses as they start. Their standard out and error
	 * will then be available for us to analyze or document.
	 */
	public void ServerProcessEventFired(ServerProcessEvent event) {
		if( event.getProcessType().equals(ServerProcessModel.TWIDDLE_PROCESSES)) {
			if( event.getEventType().equals(IServerProcessListener.PROCESS_ADDED)) {
				ProcessData[] processDatas = event.getProcessDatas();
				for( int i = 0; i < processDatas.length; i++ ) {
					processDatas[i].startListening();
				}
				processesAdded(processDatas);
			}
		}
	}

	/**
	 * Subclass can respond to a new process as they see fit.
	 * @param pd
	 */
	protected void processesAdded(ProcessData[] pd) {
		
	}
	
	/**
	 * Some debug event has occurred. 
	 * If it's a launch termination, pass this on to the
	 * implementing subclass.
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for( int i = 0; i < events.length; i++ ) {
			if( events[i].getKind() == DebugEvent.TERMINATE ) {
				threadTerminated(events);
			}
		}
	}
	
	/**
	 * Subclass can accept these debug events and know
	 * that they are thread terminations.
	 * @param events
	 */
	protected abstract void threadTerminated(DebugEvent[] events);	

	

	
}
