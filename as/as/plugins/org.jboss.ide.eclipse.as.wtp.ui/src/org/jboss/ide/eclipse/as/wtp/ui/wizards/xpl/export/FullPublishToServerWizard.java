package org.jboss.ide.eclipse.as.wtp.ui.wizards.xpl.export;

import java.util.List;

import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.internal.wizard.TaskWizard;
import org.eclipse.wst.server.ui.internal.wizard.WizardTaskUtil;
import org.eclipse.wst.server.ui.internal.wizard.fragment.ModifyModulesWizardFragment;
import org.eclipse.wst.server.ui.internal.wizard.fragment.NewServerWizardFragment;
import org.eclipse.wst.server.ui.internal.wizard.fragment.RunOnServerWizardFragment;
import org.eclipse.wst.server.ui.internal.wizard.fragment.TasksWizardFragment;
import org.eclipse.wst.server.ui.wizard.WizardFragment;

public class FullPublishToServerWizard extends TaskWizard {

	public FullPublishToServerWizard(IModule module, IModuleArtifact moduleArtifact) {
		super("Full Publish To Server", createRootFragment(module, moduleArtifact));
		getTaskModel().putObject(TaskModel.TASK_LAUNCH_MODE, "run");
		setNeedsProgressMonitor(true);
	}
	
	public static WizardFragment createRootFragment(IModule module,IModuleArtifact artifact) {
		return new RunOnServerWizardFragment(module, null, artifact) {
			protected void createChildFragments(List<WizardFragment> list) {
				if (server == null) {
					list.add(new NewServerWizardFragment(module));
					list.add(WizardTaskUtil.TempSaveRuntimeFragment);
					list.add(WizardTaskUtil.TempSaveServerFragment);
					list.add(new ModifyModulesWizardFragment(module));
				}
				list.add(new TasksWizardFragment());
				list.add(WizardTaskUtil.SaveRuntimeFragment);
				list.add(WizardTaskUtil.SaveServerFragment);
				if (server == null)
					list.add(WizardTaskUtil.SaveHostnameFragment);
			}
		};
	}

	public IServer getServer() {
		return (IServer) getTaskModel().getObject(TaskModel.TASK_SERVER);
	}

	public boolean isPreferredServer() {
		try {
			Boolean b = (Boolean) getTaskModel().getObject(WizardTaskUtil.TASK_DEFAULT_SERVER);
			return b.booleanValue();
		} catch (Exception e) {
			return false;
		}
	}

	public boolean shouldAppear() {
		return getServer() == null;
	}
}