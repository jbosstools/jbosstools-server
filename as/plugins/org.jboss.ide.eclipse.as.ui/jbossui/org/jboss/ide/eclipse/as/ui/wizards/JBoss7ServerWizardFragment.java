package org.jboss.ide.eclipse.as.ui.wizards;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.jboss.ide.eclipse.as.ui.Messages;

public class JBoss7ServerWizardFragment extends JBossRuntimeWizardFragment {

	@Override
	public boolean hasComposite() {
		return true;
	}

	@Override
	protected void updateModels() {
		// Do nothing
	}

	@Override
	protected void createWidgets(Composite main) {
		createExplanation(main);
		createNameComposite(main);
		createHomeComposite(main);
	}

	protected void fillWidgets() {
		IRuntime rt = (IRuntime) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		if (rt != null) {
			try {
				fillNameWidgets(rt);
				fillHomeDir(rt);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void updatePage() {
		// Do Nothing
		updateErrorMessage();
	}

	protected String getErrorString() {
		if (nameText == null)
			// not yet initialized. no errors
			return null;

		if (getRuntime(name) != null)
			return Messages.rwf_NameInUse;

		if (!isHomeValid())
			return Messages.rwf_homeMissingFiles;

		if (name == null || name.equals("")) //$NON-NLS-1$
			return Messages.rwf_nameTextBlank;

		return null;
	}

	@Override
	protected boolean isHomeValid() {
		if (homeDir == null || homeDir.length() == 0 || !(new File(homeDir).exists()))
			return false;
		return standaloneScriptExists();
	}

	private boolean standaloneScriptExists() {
		String standaloneScriptPath = new StringBuilder(homeDir)
				.append(File.separator)
				.append("bin") //$NON-NLS-1$
				.append(File.separator)
				.append("standalone.sh") //$NON-NLS-1$
				.toString();
		return new File(standaloneScriptPath).exists();
	}

	@Override
	protected String getVersionString(File loc) {
		// TODO clean this up for later
		return "7.0"; //$NON-NLS-1$
	}

	@Override
	public void performFinish(IProgressMonitor monitor) throws CoreException {
		IRuntime rt = (IRuntime) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		((IRuntimeWorkingCopy) rt).setLocation(new Path(homeDir));
	}

	@Override
	public void exit() {
		IRuntime r = (IRuntime) getTaskModel()
				.getObject(TaskModel.TASK_RUNTIME);
		IRuntimeWorkingCopy runtimeWC = r.isWorkingCopy() ? ((IRuntimeWorkingCopy) r)
				: r.createWorkingCopy();

		runtimeWC.setName(name);
		runtimeWC.setLocation(new Path(homeDir));
		getTaskModel().putObject(TaskModel.TASK_RUNTIME, runtimeWC);
	}
}
