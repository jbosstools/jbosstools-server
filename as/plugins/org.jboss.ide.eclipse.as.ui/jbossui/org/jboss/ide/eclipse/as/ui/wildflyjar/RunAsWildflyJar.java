package org.jboss.ide.eclipse.as.ui.wildflyjar;

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.m2e.actions.MavenLaunchConstants;
import org.eclipse.m2e.internal.launch.LaunchingUtils;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.jboss.tools.as.core.server.wildflyjar.WildflyJarLaunchConfigurationDelegate;

public class RunAsWildflyJar implements ILaunchShortcut {
	public static final String ID_EXTERNAL_TOOLS_LAUNCH_GROUP = "org.eclipse.ui.externaltools.launchGroup"; //$NON-NLS-1$

	@Override
	public void launch(IEditorPart editor, String mode) {
		// Do nothing
	}

	@Override
	public void launch(ISelection selection, String mode) {
		IContainer basedir = getBaseDir(selection);
		if (basedir == null)
			return;

		ILaunchConfiguration launchConfiguration = getLaunchConfiguration(basedir, mode);
		if (launchConfiguration == null) {
			return;
		}

		ILaunchGroup group = DebugUITools.getLaunchGroup(launchConfiguration, mode);
		String groupId = group != null ? group.getIdentifier() : ID_EXTERNAL_TOOLS_LAUNCH_GROUP;
		DebugUITools.openLaunchConfigurationDialog(getShell(), launchConfiguration, groupId, null);
	}

	private ILaunchConfiguration getLaunchConfiguration(IContainer basedir, String mode) {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType launchConfigurationType = launchManager
				.getLaunchConfigurationType(WildflyJarLaunchConfigurationDelegate.ID);

		try {
			ArrayList<ILaunchConfiguration>  match = findMatchingConfigs(launchManager, launchConfigurationType, basedir.getLocation());
			if( match != null && match.size() > 0 )
				return match.get(0);
			
			String newName = launchManager.generateLaunchConfigurationName(basedir.getLocation().lastSegment());
			ILaunchConfigurationWorkingCopy workingCopy = launchConfigurationType.newInstance(null, newName);
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_POM_DIR,
					LaunchingUtils.generateProjectLocationVariableExpression(basedir.getProject()));
			workingCopy.setAttribute(MavenLaunchConstants.ATTR_GOALS, WildflyJarLaunchConfigurationDelegate.DEFAULT_GOAL);
			return workingCopy.doSave();
		} catch (Exception ex) {
		}
		return null;
	}

	private ArrayList<ILaunchConfiguration> findMatchingConfigs(ILaunchManager manager, ILaunchConfigurationType type,
			IPath basedirLocation) throws CoreException {
		ILaunchConfiguration[] launchConfigurations = manager.getLaunchConfigurations(type);
		ArrayList<ILaunchConfiguration> matchingConfigs = new ArrayList<ILaunchConfiguration>();
		for (ILaunchConfiguration configuration : launchConfigurations) {
			try {
				// substitute variables (may throw exceptions)
				String workDir = LaunchingUtils
						.substituteVar(configuration.getAttribute(MavenLaunchConstants.ATTR_POM_DIR, (String) null));
				if (workDir == null) {
					continue;
				}
				IPath workPath = new Path(workDir);
				if (basedirLocation.equals(workPath)) {
					matchingConfigs.add(configuration);
				}
			} catch (CoreException e) {
			}
		}
		return matchingConfigs;
	}

	private Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	protected IContainer getBaseDir(ISelection selection) {
		IContainer basedir = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object object = structuredSelection.getFirstElement();

			if (object instanceof IProject || object instanceof IFolder) {
				basedir = (IContainer) object;
			} else if (object instanceof IFile) {
				basedir = ((IFile) object).getParent();
			} else if (object instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable) object;
				Object adapter = adaptable.getAdapter(IProject.class);
				if (adapter != null) {
					basedir = (IContainer) adapter;
				} else {
					adapter = adaptable.getAdapter(IFolder.class);
					if (adapter != null) {
						basedir = (IContainer) adapter;
					} else {
						adapter = adaptable.getAdapter(IFile.class);
						if (adapter != null) {
							basedir = ((IFile) object).getParent();
						}
					}
				}
			}
		}
		return basedir;
	}
}
