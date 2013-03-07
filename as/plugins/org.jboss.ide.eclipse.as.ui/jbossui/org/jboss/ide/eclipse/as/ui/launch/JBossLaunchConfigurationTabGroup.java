/******************************************************************************* 
* Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.launch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.server.internal.RecentlyUpdatedServerLaunches;
import org.jboss.ide.eclipse.as.core.util.ArgsUtil;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.ui.IJBossLaunchTabProvider;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.editor.EditorExtensionManager;
import org.jboss.ide.eclipse.as.ui.xpl.JavaMainTabClone;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class JBossLaunchConfigurationTabGroup extends
		AbstractLaunchConfigurationTabGroup {
	
	public static class JBossStandardTabProvider implements IJBossLaunchTabProvider {
		public ILaunchConfigurationTab[] createTabs() {
			ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
					new JavaArgumentsTabExtension(),
					new JavaMainTabExtension(),
					new JavaClasspathTab(),
					new SourceLookupTab(),
					new EnvironmentTab(),
					new CommonTab()
			};
			return tabs;
		}
	}
	
	protected List<IJBossLaunchTabProvider> getProviders(String mode, String typeId) {
		return EditorExtensionManager.getDefault().getLaunchTabProviders(mode, typeId);
	}
	
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		IServer s = RecentlyUpdatedServerLaunches.getDefault().getRecentServer();
		String behaviorType = s == null ? LocalPublishMethod.LOCAL_PUBLISH_METHOD :
			DeploymentPreferenceLoader.getCurrentDeploymentMethodType(s, LocalPublishMethod.LOCAL_PUBLISH_METHOD).getId();
		String type = s == null ? IJBossToolingConstants.SERVER_AS_60 : s.getServerType().getId();
		List<IJBossLaunchTabProvider> p = getProviders(behaviorType, type);

		Iterator<IJBossLaunchTabProvider> i = p.iterator();
		ArrayList<ILaunchConfigurationTab> tabs = new ArrayList<ILaunchConfigurationTab>();
		while(i.hasNext()) {
			ILaunchConfigurationTab[] tabs2 = i.next().createTabs();
			for( int j = 0; j < tabs2.length; j++ ) {
				tabs2[j].setLaunchConfigurationDialog(dialog);
				tabs.add(tabs2[j]);
			}
		}
		setTabs(tabs.toArray(new ILaunchConfigurationTab[tabs.size()]));
	}
	
	public static Composite createComposite(Composite parent, Font font, int columns, int hspan, int fill) {
    	Composite g = new Composite(parent, SWT.NONE);
    	g.setLayout(new GridLayout(columns, false));
    	g.setFont(font);
    	GridData gd = new GridData(fill);
		gd.horizontalSpan = hspan;
    	g.setLayoutData(gd);
    	return g;
    }

	
	public static class JavaMainTabExtension extends JavaMainTabClone {
		public void createControl(Composite parent) {
			Composite comp = createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH);
			((GridLayout)comp.getLayout()).verticalSpacing = 0;
			//createProjectEditor(comp);
			//createVerticalSpacer(comp, 1);
			createMainTypeEditor(comp, LauncherMessages.JavaMainTab_Main_cla_ss__4);
			setControl(comp);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_MAIN_TAB);
		}
		public void initializeFrom(ILaunchConfiguration config) {
			//super.initializeFrom(config);
			//updateProjectFromConfig(config);
			setCurrentLaunchConfiguration(config);
			updateMainTypeFromConfig(config);
			updateStopInMainFromConfig(config);
			updateInheritedMainsFromConfig(config);
			updateExternalJars(config);		
		}
		public boolean isValid(ILaunchConfiguration config) {
			setErrorMessage(null);
			setMessage(null);
			String name = fMainText.getText().trim();
			if (name.length() == 0) {
				setErrorMessage(LauncherMessages.JavaMainTab_Main_type_not_specified_16); 
				return false;
			}
			return true;
		}
		protected IJavaProject getJavaProject() {
			return null;
		}

	}
	
	public static class JavaArgumentsTabExtension extends JavaArgumentsTab {
		private String originalHost=null;
		private String originalConf=null;
		public void initializeFrom(ILaunchConfiguration configuration) {
			super.initializeFrom(configuration);
			try {
				String startArgs = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);
				originalHost = ArgsUtil.getValue(startArgs, "-b", "--host"); //$NON-NLS-1$ //$NON-NLS-2$
				originalConf = ArgsUtil.getValue(startArgs, "-c", "--configuration"); //$NON-NLS-1$ //$NON-NLS-2$
			} catch( CoreException ce ) {
				// This can only happen if loading properties from a launch config is f'd, 
				// in which case it's a big eclipse issue
				JBossServerUIPlugin.getDefault().getLog().log(
						new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, 
						"Error loading details from launch configuration", ce));
			}
		}
		public boolean isValid(ILaunchConfiguration config) {
			if( !super.isValid(config)) 
				return false;
			try {
				String startArgs = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);
				String newHost = ArgsUtil.getValue(startArgs, "-b", "--host"); //$NON-NLS-1$ //$NON-NLS-2$
				String newConf = ArgsUtil.getValue(startArgs, "-c", "--configuration"); //$NON-NLS-1$ //$NON-NLS-2$
				if( newConf == null || !newConf.equals(originalConf)) 
					return false;
				if( newHost == null || !newHost.equals(originalHost)) 
					return false;
			} catch( CoreException ce ) {
				// This can only happen if loading properties from a launch config is f'd, 
				// in which case it's a big eclipse issue
				JBossServerUIPlugin.getDefault().getLog().log(
						new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, 
						"Error loading details from launch configuration", ce));
			}
			return true;
		}
		public String getErrorMessage() {
			String m = super.getErrorMessage();
			if (m == null) {
				String startArgs = getAttributeValueFrom(fPrgmArgumentsText);
				String newHost = ArgsUtil.getValue(startArgs, "-b", "--host"); //$NON-NLS-1$ //$NON-NLS-2$
				String newConf = ArgsUtil.getValue(startArgs, "-c", "--configuration"); //$NON-NLS-1$ //$NON-NLS-2$
				if( newConf == null || !newConf.equals(originalConf)) 
					return Messages.LaunchInvalidConfigChanged;
				if( newHost == null || !newHost.equals(originalHost)) 
					return Messages.LaunchInvalidHostChanged;
			}
			return m;
		}
	}
}
