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
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
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
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;

public class JBossRuntimeLocator extends RuntimeLocatorDelegate {

	public JBossRuntimeLocator() {
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
		String serverId = loader.getServerAdapterId();
		
		IServerType st = serverId == null ? null : ServerCore.findServerType(serverId);
		String rtId = RuntimeUtils.getRuntimeTypeId(st);
		String append = getServerHomePathAppend(rtId);
		
		if( rtId != null ) {
			IPath path2 = append == null ? path : path.append(append); 
			try {
				IRuntimeWorkingCopy wc = createRuntimeWorkingCopy(rtId, path2.toOSString());
				return launchRuntimeWizard(wc);
			} catch( CoreException ce) {
			}
		}
		return null;
	}
	
	private static String getServerHomePathAppend(String runtimeType) {
		boolean b = IJBossToolingConstants.EAP_43.equals(runtimeType) ||
				IJBossToolingConstants.EAP_50.equals(runtimeType);
		return b ? IJBossRuntimeResourceConstants.JBOSS_AS_EAP_DIRECTORY : null;
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
	
	// Copied from RuntimePreferencePage,opens a new runtime wizard
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
	
	private static IRuntimeWorkingCopy createRuntimeWorkingCopy(String runtimeId, String homeDir) throws CoreException {
		return createRuntimeWorkingCopy(runtimeId, homeDir, null);
	}
	
	/*
	 * This should not have been public. There is no replacement. Make your own working copy!
	 */
	@Deprecated
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
		
		// THIS should NOT be needed. Defaults should be set properly. But since this is an api method now, it remains.
		((RuntimeWorkingCopy) runtimeWC).setAttribute(
				IJBossServerRuntime.PROPERTY_CONFIGURATION_NAME, config);
		return runtimeWC;
	}

}
