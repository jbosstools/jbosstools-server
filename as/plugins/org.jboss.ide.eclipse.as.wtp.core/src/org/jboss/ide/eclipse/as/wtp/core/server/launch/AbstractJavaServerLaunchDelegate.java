package org.jboss.ide.eclipse.as.wtp.core.server.launch;

import java.io.File;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaHotCodeReplaceListener;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jst.server.core.ServerProfilerDelegate;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;
import org.jboss.ide.eclipse.as.wtp.core.Messages;

public abstract class AbstractJavaServerLaunchDelegate extends AbstractJavaLaunchConfigurationDelegate {

	protected void fireVMRunner(ILaunchConfiguration configuration,
			String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		// And off we go!
		IVMInstall vm = verifyVMInstall(configuration);
		IVMRunner runner = vm.getVMRunner(mode);

		if (runner == null && ILaunchManager.PROFILE_MODE.equals(mode)) {
			runner = vm.getVMRunner(ILaunchManager.RUN_MODE);
		}
		if (runner == null) {
			throw new CoreException(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, 0,
					Messages.runModeNotSupported, null));
		}

		File workingDir = verifyWorkingDirectory(configuration);
		String workingDirName = null;
		if (workingDir != null)
			workingDirName = workingDir.getAbsolutePath();

		// Program & VM args
		String pgmArgs = getProgramArguments(configuration);
		String vmArgs = getVMArguments(configuration);
		ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);

		// VM-specific attributes
		Map<String, Object> vmAttributesMap = getVMSpecificAttributesMap(configuration);

		// Classpath
		String[] classpath = getClasspath(configuration);

		// Environment
		String[] environment = getEnvironment(configuration);

		// Create VM config
		String mainType = getMainTypeName(configuration);
		VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainType, classpath);
		runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
		runConfig.setVMArguments(execArgs.getVMArgumentsArray());
		runConfig.setWorkingDirectory(workingDirName);
		runConfig.setVMSpecificAttributesMap(vmAttributesMap);
		runConfig.setEnvironment(environment);

		// Bootpath
		String[] bootpath = getBootpath(configuration);
		if (bootpath != null && bootpath.length > 0)
			runConfig.setBootClassPath(bootpath);

		setDefaultSourceLocator(launch, configuration);

		if (ILaunchManager.PROFILE_MODE.equals(mode)) {
			try {
				ServerProfilerDelegate.configureProfiling(launch, vm, runConfig, monitor);
			} catch (CoreException ce) {
				IServer server = org.eclipse.wst.server.core.ServerUtil.getServer(configuration);
				ServerBehaviourDelegate jbsb = (ServerBehaviourDelegate) server.getAdapter(ServerBehaviourDelegate.class);
				jbsb.stop(true);
				throw ce;
			}
		}
		// Launch the configuration
		runner.run(runConfig, launch, monitor);
		
		overrideHotcodeReplace(configuration, launch);
	}
	
	/**
	 * Override the hotcode replacement mechanism. 
	 * Return true if override, false if no changes were made.
	 * 
	 * @param configuration
	 * @param launch
	 * @return
	 * @throws CoreException
	 */
	protected boolean overrideHotcodeReplace(ILaunchConfiguration configuration, ILaunch launch) throws CoreException {
		IJavaHotCodeReplaceListener l = getHotCodeReplaceListener(
				ServerUtil.getServer(configuration), launch);
		if( l != null) {
			IDebugTarget[] targets = launch.getDebugTargets();
			if( targets != null && l != null) {
				for( int i = 0; i < targets.length; i++ ) {
					if( targets[i] instanceof IJavaDebugTarget) {
						((IJavaDebugTarget)targets[i]).addHotCodeReplaceListener(l);
					}
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Clients are encouraged to override this if they will always use the enhanced hotcode replacement, 
	 * or if they have a different default value
	 * 
	 * @param server
	 * @return
	 */
	protected boolean addCustomHotcodeReplaceLogic(IServer server) {
		return server.getAttribute(ServerHotCodeReplaceListener.PROPERTY_HOTCODE_REPLACE_OVERRIDE, false);
	}
	
	/**
	 * Clients should override this method if they intend to use a custom 
	 * prompter with behavior different than the default. 
	 * 
	 * @param server
	 * @param launch
	 * @param target
	 * @return
	 */
	protected IJavaHotCodeReplaceListener getHotCodeReplaceListener(IServer server, ILaunch launch) {
		if( addCustomHotcodeReplaceLogic(server)) {
			return new ServerHotCodeReplaceListener(server, launch);
		}
		return null;
	}
	
	
}
