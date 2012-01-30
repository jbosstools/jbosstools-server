/*******************************************************************************
 * Copyright (c) 2010 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.wizards;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
import org.eclipse.wst.server.core.model.RuntimeLocatorDelegate;
import org.eclipse.wst.server.ui.internal.Messages;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
import org.eclipse.wst.server.ui.internal.wizard.TaskWizard;
import org.eclipse.wst.server.ui.internal.wizard.WizardTaskUtil;
import org.eclipse.wst.server.ui.internal.wizard.fragment.NewRuntimeWizardFragment;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.bean.JBossServerType;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class JBossRuntimeLocator extends RuntimeLocatorDelegate {

	public JBossRuntimeLocator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void searchForRuntimes(IPath path, IRuntimeSearchListener listener,
			IProgressMonitor monitor) {
		IRuntimeWorkingCopy wc = searchForRuntimes(path, monitor);
		if( wc != null )
			listener.runtimeFound(wc);
	}
	
	public IRuntimeWorkingCopy searchForRuntimes(IPath path, IProgressMonitor monitor) {
		if( monitor.isCanceled())
			return null;
		ServerBeanLoader loader = new ServerBeanLoader(path.toFile());
		if( loader.getServerType() != JBossServerType.UNKNOWN) {
			// return found
			IRuntimeWorkingCopy wc = createRuntime(path, loader);
			if( wc == null )
				monitor.setCanceled(true);
			return wc;
		}
		File[] children = path.toFile().listFiles();
		children = (children == null ? new File[]{} : children);
		monitor.beginTask("Searching for JBoss runtime...", children.length); //$NON-NLS-1$
		if( children != null ) {
			for( int i = 0; i < children.length; i++ ) {
				if( children[i].isDirectory()) {
					SubProgressMonitor newMon = new SubProgressMonitor(monitor, 1);
					IRuntimeWorkingCopy wc = searchForRuntimes(path.append(children[i].getName()), newMon);
					if( wc != null ) {
						monitor.done();
						return wc;
					}
				} else {
					monitor.worked(1);
				}
			}
		}
		monitor.done();
		return null;
	}	

	public static IRuntimeWorkingCopy createRuntime(IPath path) {
		return createRuntime(path, new ServerBeanLoader(path.toFile()));
	}

	public static IRuntimeWorkingCopy createRuntime(IPath path, ServerBeanLoader loader) {
		JBossServerType type = loader.getServerType();
		if( type == JBossServerType.AS)
			return createASRuntime(path, loader);
		if( type == JBossServerType.AS7)
			return createAS7Runtime(path, loader);
		if( type == JBossServerType.EAP)
			return createEAPRuntime(path, loader);
		if( type == JBossServerType.EAP6)
			return createEAP6Runtime(path, loader);
		if( type == JBossServerType.SOAP)
			return createSOAPRuntime(path, loader);
		return null;
	}
	
	private static IRuntimeWorkingCopy createAS7Runtime(IPath path, ServerBeanLoader loader) {
		String version = loader.getFullServerVersion();
		String runtimeTypeId;
		if( version.startsWith(IJBossToolingConstants.V7_0))
			runtimeTypeId = IJBossToolingConstants.AS_70;
		else 
			runtimeTypeId = IJBossToolingConstants.AS_71;
		
		try {
			IRuntimeWorkingCopy wc = createRuntimeWorkingCopy(runtimeTypeId, path.toOSString(), IJBossRuntimeResourceConstants.DEFAULT_CONFIGURATION);
			return launchRuntimeWizard(wc);
		} catch( CoreException ce) {
		}
		return null;
	}
	
	private static IRuntimeWorkingCopy createEAP6Runtime(IPath path, ServerBeanLoader loader) {
		String runtimeTypeId = IJBossToolingConstants.EAP_60;
		try {
			IRuntimeWorkingCopy wc = createRuntimeWorkingCopy(runtimeTypeId, path.toOSString(), IJBossRuntimeResourceConstants.DEFAULT_CONFIGURATION);
			return launchRuntimeWizard(wc);
		} catch( CoreException ce) {
		}
		return null;
	}

	private static IRuntimeWorkingCopy createASRuntime(IPath path, ServerBeanLoader loader) {
		String version = loader.getFullServerVersion();
		String runtimeTypeId = null;
		if( version.compareTo("4.0") < 0 ) //$NON-NLS-1$
			runtimeTypeId=IJBossToolingConstants.AS_32;
		else if( version.compareTo("4.2") < 0 ) //$NON-NLS-1$
			runtimeTypeId=IJBossToolingConstants.AS_40;
		else if( version.compareTo("5.0") < 0 ) //$NON-NLS-1$
			runtimeTypeId=IJBossToolingConstants.AS_42;
		else if( version.compareTo("5.1") < 0 ) //$NON-NLS-1$
			runtimeTypeId=IJBossToolingConstants.AS_50;
		else if( version.compareTo("6.0") < 0 ) //$NON-NLS-1$
			runtimeTypeId=IJBossToolingConstants.AS_51;
		else
			runtimeTypeId=IJBossToolingConstants.AS_60;
		if( runtimeTypeId != null ) {
			try {
				IRuntimeWorkingCopy wc = createRuntimeWorkingCopy(runtimeTypeId, path.toOSString(), IJBossRuntimeResourceConstants.DEFAULT_CONFIGURATION);
				return launchRuntimeWizard(wc);
			} catch( CoreException ce) {
			}
		}
		return null;
	}
	private static IRuntimeWorkingCopy createEAPRuntime(IPath path, ServerBeanLoader loader) {
		String version = loader.getFullServerVersion();
		String runtimeTypeId = null;
		if( version.compareTo("5.0") < 0 ) //$NON-NLS-1$
			runtimeTypeId=IJBossToolingConstants.EAP_43;
		else if( version.compareTo("6.0") < 0 ) //$NON-NLS-1$
			runtimeTypeId=IJBossToolingConstants.EAP_50;
		else
			runtimeTypeId=IJBossToolingConstants.EAP_60
			;
		IPath path2 = path.append(IJBossRuntimeResourceConstants.JBOSS_AS_EAP_DIRECTORY);
		if( runtimeTypeId != null ) {
			try {
				IRuntimeWorkingCopy wc = createRuntimeWorkingCopy(runtimeTypeId, path2.toOSString(), IJBossRuntimeResourceConstants.DEFAULT_CONFIGURATION);
				return launchRuntimeWizard(wc);
			} catch( CoreException ce) {
			}
		}
		return null;
	}
	
	private static IRuntimeWorkingCopy createSOAPRuntime(IPath path, ServerBeanLoader loader) {
		return createEAPRuntime(path, loader);
	}
	
	private static IRuntimeWorkingCopy launchRuntimeWizard(final IRuntimeWorkingCopy wc) {
		final IRuntimeWorkingCopy[] wcRet = new IRuntimeWorkingCopy[1];
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				IRuntimeWorkingCopy wc2 = showWizard(wc);
				wcRet[0] = wc2;
			}
		});
		return wcRet[0];
	}
	
	// Copied from RuntimePreferencePage
	protected static IRuntimeWorkingCopy showWizard(final IRuntimeWorkingCopy runtimeWorkingCopy) {
		String title = null;
		WizardFragment fragment = null;
		TaskModel taskModel = new TaskModel();
		if (runtimeWorkingCopy == null) {
			title = Messages.wizNewRuntimeWizardTitle;
			fragment = new WizardFragment() {
				protected void createChildFragments(List<WizardFragment> list) {
					list.add(new NewRuntimeWizardFragment());
					list.add(WizardTaskUtil.SaveRuntimeFragment);
				}
			};
		} else {
			title = Messages.wizEditRuntimeWizardTitle;
			final WizardFragment fragment2 = ServerUIPlugin.getWizardFragment(runtimeWorkingCopy.getRuntimeType().getId());
			if (fragment2 == null) {
				return null;
			}
			taskModel.putObject(TaskModel.TASK_RUNTIME, runtimeWorkingCopy);
			fragment = new WizardFragment() {
				protected void createChildFragments(List<WizardFragment> list) {
					list.add(fragment2);
					list.add(WizardTaskUtil.SaveRuntimeFragment);
				}
			};
		}
		TaskWizard wizard = new TaskWizard(title, fragment, taskModel);
		wizard.setForcePreviousAndNextButtons(true);
		WizardDialog dialog = new WizardDialog(new Shell(), wizard);
		int result = dialog.open();
		if( result == Window.OK ) {
			IRuntime rt = (IRuntime)taskModel.getObject(TaskModel.TASK_RUNTIME);
			if( rt != null ) {
				IRuntimeWorkingCopy rtwc = rt.createWorkingCopy();
				return rtwc;
			}
		}
		return null;
	}

	public static IRuntimeWorkingCopy createRuntimeWorkingCopy(String runtimeId, String homeDir,
			String config) throws CoreException {
		IRuntimeType[] runtimeTypes = ServerUtil.getRuntimeTypes(null, null,
				runtimeId);
		IRuntimeType runtimeType = runtimeTypes[0];
		IRuntimeWorkingCopy runtimeWC = runtimeType.createRuntime(null,
				new NullProgressMonitor());
		runtimeWC.setLocation(new Path(homeDir));
		((RuntimeWorkingCopy) runtimeWC).setAttribute(
				IJBossServerRuntime.PROPERTY_VM_ID, JavaRuntime.getDefaultVMInstall().getId());
		((RuntimeWorkingCopy) runtimeWC).setAttribute(
				IJBossServerRuntime.PROPERTY_VM_TYPE_ID, JavaRuntime.getDefaultVMInstall()
						.getVMInstallType().getId());
		((RuntimeWorkingCopy) runtimeWC).setAttribute(
				IJBossServerRuntime.PROPERTY_CONFIGURATION_NAME, config);
		return runtimeWC;
	}

}
