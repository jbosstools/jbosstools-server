package org.jboss.ide.eclipse.as.ui.wizards;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.bean.JBossServerType;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.Messages;

public class JBoss7ServerWizardFragment extends JBossRuntimeWizardFragment {

	@Override
	public boolean hasComposite() {
		return true;
	}

	@Override
	protected void updateModels() {
		updateJREs();
	}

	@Override
	protected void createWidgets(Composite main) {
		createExplanation(main);
		createNameComposite(main);
		createHomeComposite(main);
		createJREComposite(main);
	}

	protected void fillWidgets() {
		IRuntime rt = (IRuntime) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		if (rt != null) {
			try {
				fillNameWidgets(rt);
				fillHomeDir(rt);
				fillJREWidgets(getRuntime());
			} catch (Exception e) {
				IStatus status = new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, MessageFormat.format(Messages.JBoss7ServerWizardFragment_could_not_create_ui, rt.getName()), e);
				JBossServerUIPlugin.getDefault().getLog().log(status);
			}
		}
	}

	@Override
	protected void updatePage() {
		int sel = jreCombo.getSelectionIndex();
		int offset = -1;
		if( sel + offset >= 0 )
			selectedVM = installedJREs.get(sel + offset);
		else // if sel < 0 or sel == 0 and offset == -1
			selectedVM = null;
		updateErrorMessage();
	}

	protected String getErrorString() {
		if (nameText == null)
			// not yet initialized. no errors
			return null;

		if (getRuntime(name) != null)
			return Messages.rwf_NameInUse;

		if (!isHomeValid())
			return NLS.bind(Messages.rwf_homeMissingFiles, getSystemJarPath());

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
	protected String getSystemJarPath() {
		return JBossServerType.AS7.getSystemJarPath();
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
		IJBossServerRuntime srt = (IJBossServerRuntime) runtimeWC.loadAdapter(
				IJBossServerRuntime.class, new NullProgressMonitor());
		srt.setVM(selectedVM);

		getTaskModel().putObject(TaskModel.TASK_RUNTIME, runtimeWC);
	}
}
