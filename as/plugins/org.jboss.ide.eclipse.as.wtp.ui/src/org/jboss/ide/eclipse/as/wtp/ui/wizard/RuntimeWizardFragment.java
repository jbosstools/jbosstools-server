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
package org.jboss.ide.eclipse.as.wtp.ui.wizard;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.ide.eclipse.as.wtp.ui.Messages;
import org.jboss.ide.eclipse.as.wtp.ui.composites.AbstractJREComposite;
import org.jboss.ide.eclipse.as.wtp.ui.composites.IJRECompositeListener;
import org.jboss.ide.eclipse.as.wtp.ui.composites.IRuntimeHomeCompositeListener;
import org.jboss.ide.eclipse.as.wtp.ui.composites.RuntimeHomeComposite;

/**
 * @author Stryker
 */
public abstract class RuntimeWizardFragment extends WizardFragment {

	protected IWizardHandle handle;
	protected boolean beenEntered = false;
	
	protected Label nameLabel, explanationLabel;
	protected Text nameText;
	protected AbstractJREComposite jreComposite;
	protected RuntimeHomeComposite homeDirComposite;
	protected Composite nameComposite;
	protected String originalName;
	protected String name;

	public Composite createComposite(Composite parent, IWizardHandle handle) {
		/*
		 * Any state should be CLEARED right now or in enclosed methods and loaded from the model.
		 * WTP creates only one instance of this wizard fragment for the entire
		 * life of the workspace.
		 */
		this.handle = handle;
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new FormLayout());

		updateModels();
		createWidgets(main);
		
		IRuntime rt = getRuntimeFromTaskModel();
		if (rt != null) {
			fillWidgets(rt);
		}
		updateWizardHandle(parent);
		updatePage();
		return main;
	}
	
	protected void updateModels() {
		// clean state from last time this fragment was used
		// (fragments apparently do not have new instances constructed. ugh)
	}
	
	protected IRuntime getRuntimeFromTaskModel() {
		IRuntime r = (IRuntime) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		return r;
	}
	
	protected abstract void updateWizardHandle(Composite parent);
	
	protected void createWidgets(Composite main) {
		createExplanation(main);
		createNameComposite(main);
		createHomeComposite(main);
		createJREComposite(main);
	}
	
	protected void fillNameWidgets(IRuntime rt) {
		originalName = rt.getName();
		nameText.setText(originalName);
		name = originalName;
	}
	
	protected void fillHomeDir(IRuntime rt) {
		homeDirComposite.fillHomeDir(rt);
	}
	
	protected boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}
	
	protected void fillWidgets(IRuntime rt) {
		fillNameWidgets(rt);
		fillHomeDir(rt);
	}

	protected void createExplanation(Composite main) {
		explanationLabel = new Label(main, SWT.WRAP);
		FormData data = new FormData();
		data.top = new FormAttachment(0, 5);
		data.left = new FormAttachment(0, 5);
		data.right = new FormAttachment(100, -5);
		explanationLabel.setLayoutData(data);
		explanationLabel.setText(getExplanationText());
	}
	
	protected abstract String getExplanationText();

	protected void createNameComposite(Composite main) {
		// Create our name composite
		nameComposite = new Composite(main, SWT.NONE);

		FormData cData = new FormData();
		cData.left = new FormAttachment(0, 5);
		cData.right = new FormAttachment(100, -5);
		cData.top = new FormAttachment(explanationLabel, 10);
		nameComposite.setLayoutData(cData);

		nameComposite.setLayout(new FormLayout());

		// create internal widgets
		nameLabel = new Label(nameComposite, SWT.NONE);
		nameLabel.setText(Messages.wf_NameLabel);

		nameText = new Text(nameComposite, SWT.BORDER);
		nameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				name = nameText.getText();
				updatePage();
			}
		});

		// organize widgets inside composite
		FormData nameLabelData = new FormData();
		nameLabelData.left = new FormAttachment(0, 0);
		nameLabel.setLayoutData(nameLabelData);

		FormData nameTextData = new FormData();
		nameTextData.left = new FormAttachment(0, 5);
		nameTextData.right = new FormAttachment(100, -5);
		nameTextData.top = new FormAttachment(nameLabel, 5);
		nameText.setLayoutData(nameTextData);
	}

	protected void createHomeComposite(Composite main) {
		// Create our composite
		homeDirComposite = createHomeCompositeWidget(main);
		FormData cData = new FormData();
		cData.left = new FormAttachment(0, 5);
		cData.right = new FormAttachment(100, -5);
		cData.top = new FormAttachment(nameComposite, 10);
		homeDirComposite.setLayoutData(cData);
		
		homeDirComposite.setListener(new IRuntimeHomeCompositeListener() {
			public void homeChanged() {
				updatePage();
			}
		});
	}	
	

	protected abstract RuntimeHomeComposite createHomeCompositeWidget(Composite main);
	
	
	protected void createJREComposite(Composite main) {
		// Create our composite
		jreComposite = createJRECompositeWidget(main);
		FormData cData = new FormData();
		cData.left = new FormAttachment(0, 5);
		cData.right = new FormAttachment(100, -5);
		cData.top = new FormAttachment(homeDirComposite, 10);
		jreComposite.setLayoutData(cData);
		jreComposite.setListener(new IJRECompositeListener(){
			public void vmChanged(AbstractJREComposite comp) {
				updatePage();
			}
		});
	}

	protected abstract AbstractJREComposite createJRECompositeWidget(Composite main);
	
	// Launchable only from UI thread
	protected void updatePage() {
		updateDependentWidgets();
		saveDetailsInRuntime();
		updateErrorMessage();
	}

	protected void updateDependentWidgets() {
		// todo override
	}
	
	protected void updateErrorMessage() {
		if( !beenEntered)
			return;
		String error = getErrorString();
		if (error == null) {
			String warn = getWarningString();
			if( warn != null )
				handle.setMessage(warn, IMessageProvider.WARNING);
			else
				handle.setMessage(null, IMessageProvider.NONE);
		} else
			handle.setMessage(error, IMessageProvider.ERROR);
	}

	protected String getErrorString() {
		if (nameText == null) {
			// not yet initialized. no errors
			return null;
		}

		if (getRuntime(name) != null) {
			return Messages.rwf_NameInUse;
		}

		if( jreComposite != null ) {
			IExecutionEnvironment selectedEnv = jreComposite.getSelectedExecutionEnvironment();
			IVMInstall install = jreComposite.getSelectedVM();
			if( install == null ) {
				// user has selected an exec-env, not a vm
				if( selectedEnv != null ) {
					if( selectedEnv.getCompatibleVMs().length == 0 ) {
						return NLS.bind(Messages.rwf_noValidJRE, selectedEnv.getId());
					}
				}
			}
		}
			
		if (name == null || name.equals("")) //$NON-NLS-1$
			return Messages.rwf_nameTextBlank;

		if( !homeDirComposite.isHomeValid() ) {
			return Messages.rwf_jboss7homeNotValid;
		}
		
		return null;
	}
	
	protected String getWarningString() {
		String homeVersionWarning = getHomeVersionWarning();
		if( homeVersionWarning != null )
			return homeVersionWarning;
		if( !jreComposite.selectedVMisCompatible()) {
			return Messages.rwf_incompatibleJRE;
		}
		return null;
	}
	
	protected String getVersionString(File loc) {
		String version = new ServerBeanLoader(loc).getFullServerVersion();
		return version == null ? "UNKNOWN" : version;
	}
	
	protected String getHomeVersionWarning() {
		String homeDir = homeDirComposite.getHomeDirectory();
		File loc = new File(homeDir);
		String serverId = new ServerBeanLoader(loc).getServerAdapterId();
		IServerType st = serverId == null ? null : ServerCore.findServerType(serverId);
		String rtId = RuntimeUtils.getRuntimeTypeId(st);
		IRuntime adapterRt = getRuntimeFromTaskModel();
		String adapterRuntimeId = adapterRt.getRuntimeType().getId();
		if( !adapterRuntimeId.equals(rtId)) {
			return NLS.bind(Messages.rwf_homeIncorrectVersionError, 
					adapterRt.getRuntimeType().getVersion(), 
					getVersionString(loc));
		}
		return null;
	}


	protected static File getDirectory(File startingDirectory, Shell shell) {
		DirectoryDialog fileDialog = new DirectoryDialog(shell, SWT.OPEN);
		if (startingDirectory != null) {
			fileDialog.setFilterPath(startingDirectory.getPath());
		}

		String dir = fileDialog.open();
		if (dir != null) {
			dir = dir.trim();
			if (dir.length() > 0) {
				return new File(dir);
			}
		}
		return null;
	}

	// WST API methods
	public void enter() {
		beenEntered = true;
	}

	public void exit() {
		saveDetailsInRuntime();
	}
	
	protected void saveDetailsInRuntime() {
		IRuntime r = getRuntimeFromTaskModel();
		IRuntimeWorkingCopy runtimeWC = r.isWorkingCopy() ? ((IRuntimeWorkingCopy) r)
				: r.createWorkingCopy();
		saveDetailsInRuntime(runtimeWC);
	}
	
	protected void saveDetailsInRuntime(IRuntimeWorkingCopy wc) {
		saveBasicDetailsInRuntime(wc);
	}
	
	protected void saveBasicDetailsInRuntime(IRuntimeWorkingCopy runtimeWC) {
		String homeDir = homeDirComposite.getHomeDirectory();
		if( name != null )
			runtimeWC.setName(name);
		if( homeDir != null)
			runtimeWC.setLocation(new Path(homeDir));
		saveJreInRuntime(runtimeWC);
	}
	
	protected abstract void saveJreInRuntime(IRuntimeWorkingCopy wc);

	public void performFinish(IProgressMonitor monitor) throws CoreException {
		exit();
		IRuntime rt = getRuntimeFromTaskModel();
		if( rt instanceof IRuntimeWorkingCopy ) {
			IRuntimeWorkingCopy r = (IRuntimeWorkingCopy) rt;
			IRuntime saved = r.save(false, new NullProgressMonitor());
			getTaskModel().putObject(TaskModel.TASK_RUNTIME, saved);
			saveRuntimeLocationInPreferences(saved);
		}
	}
	
	protected abstract void saveRuntimeLocationInPreferences(IRuntime runtime);

	public boolean isComplete() {
		return beenEntered && (getErrorString() == null ? true : false);
	}

	public boolean hasComposite() {
		return true;
	}

	protected IRuntime getRuntime(String runtimeName) {
		if (runtimeName.equals(originalName))
			return null; // name is same as original. No clash.

		IRuntime[] runtimes = ServerCore.getRuntimes();
		for (int i = 0; i < runtimes.length; i++) {
			if (runtimes[i].getName().equals(runtimeName))
				return runtimes[i];
		}
		return null;
	}
	
	
	public IRuntimeType getRuntimeType() {
		IRuntime r = getRuntimeFromTaskModel();
		return r.getRuntimeType();
	}
}
