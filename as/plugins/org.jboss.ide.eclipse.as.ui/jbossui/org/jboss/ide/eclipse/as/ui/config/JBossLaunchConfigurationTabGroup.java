package org.jboss.ide.eclipse.as.ui.config;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.JBossServerBehavior;
import org.jboss.ide.eclipse.as.core.server.runtime.JBossRuntimeConfiguration;
import org.jboss.ide.eclipse.as.core.server.runtime.JBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.RuntimeConfigUtil;



public class JBossLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

	private IServer server;
	private JBossServer jbServer;
	private JBossServerBehavior jbServerBehavior;
	private JBossServerRuntime jbRuntime;
	private JBossRuntimeConfiguration jbRtConfig;
	
	private ILaunchConfiguration launchConfiguration;
	private ILaunchConfigurationWorkingCopy launchWC;

	
	
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[10];
		int i = 0;
		tabs[i] = new JavaArgumentsTab2();
		tabs[i++].setLaunchConfigurationDialog(dialog);
//		tabs[i] = new JavaClasspathTab();
//		tabs[i++].setLaunchConfigurationDialog(dialog);
		tabs[i] = new SourceLookupTab();
		tabs[i++].setLaunchConfigurationDialog(dialog);
//		tabs[i] = new EnvironmentTab();
//		tabs[i++].setLaunchConfigurationDialog(dialog);
//		tabs[i] = new JavaJRETab();
//		tabs[i++].setLaunchConfigurationDialog(dialog);	 
//		tabs[i] = new CommonTab();
//		tabs[i++].setLaunchConfigurationDialog(dialog);

		
		ILaunchConfigurationTab[] tabs2 = new ILaunchConfigurationTab[i];
		System.arraycopy(tabs, 0, tabs2, 0, i);
		setTabs(tabs2);

	}
	
	public void dispose() {
		super.dispose();
	}

	private class JavaArgumentsTab2 extends JavaArgumentsTab {
		private String originalProgramArgs;
		public void initializeFrom(ILaunchConfiguration configuration) {
			try {
				originalProgramArgs = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "");
			} catch( CoreException ce ) {
			}
			super.initializeFrom(configuration);
		}
		
		public boolean canSave() {
			return isHostAndConfigValid();
		}

		private boolean isHostAndConfigValid() {
			String progArgs = fPrgmArgumentsText.getText();
			String workingConfig = RuntimeConfigUtil.getCommandArgument(progArgs, "-c", "--configuration");
			String workingHost = RuntimeConfigUtil.getCommandArgument(progArgs, "-b", "--host");

			String oldConfig = RuntimeConfigUtil.getCommandArgument(originalProgramArgs, "-c", "--configuration");
			String oldHost = RuntimeConfigUtil.getCommandArgument(originalProgramArgs, "-b", "--host");
			
			boolean valid = true;
			if( !workingHost.equals(oldHost)) {
				valid = false;
			}
			if( !workingConfig.equals(oldConfig)) {
				valid = false;
			}

			return valid;
		}
		public boolean isValid(ILaunchConfiguration config) {
			boolean sup = super.isValid(config);
			if( !sup ) return false;
			
			try {
				
				boolean valid = isHostAndConfigValid();
				if( !valid ) {
					setErrorMessage("You may not change the configuration directory or the host in this fashion.");
					//getLaunchConfigurationDialog().updateButtons();
					return false;
				}
			} catch( Exception ce) {
				ce.printStackTrace();
			}
			
			setErrorMessage(null);
			return true;
		}
	}
	
	

	
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);
		try {
			server = ServerUtil.getServer(configuration);
			jbServer = (JBossServer)server.getAdapter(JBossServer.class);
			if (jbServer == null) {
				jbServer = (JBossServer) server.loadAdapter(JBossServer.class, new NullProgressMonitor());
			}
			jbServerBehavior = (JBossServerBehavior) server.getAdapter(JBossServerBehavior.class);
			jbRuntime = jbServer.getJBossRuntime();
			jbRtConfig = jbServer.getRuntimeConfiguration();
	
			jbRtConfig.updateConfiguration(configuration);		
		} catch( CoreException ce ) {
			
		}
	}


}
