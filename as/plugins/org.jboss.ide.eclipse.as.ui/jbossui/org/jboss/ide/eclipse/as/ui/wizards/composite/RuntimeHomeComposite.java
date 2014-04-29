package org.jboss.ide.eclipse.as.ui.wizards.composite;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.ide.eclipse.as.ui.IPreferenceKeys;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.UIUtil;
import org.jboss.ide.eclipse.as.ui.wizards.JBossASDownloadRuntimeFilter;
import org.jboss.ide.eclipse.as.ui.wizards.ServerProfileWizardFragment;
import org.jboss.tools.as.runtimes.integration.util.DownloadRuntimeServerUtil;
import org.jboss.tools.foundation.core.jobs.DelegatingProgressMonitor;
import org.jboss.tools.runtime.core.model.DownloadRuntime;
import org.jboss.tools.runtime.ui.internal.wizard.DownloadRuntimesWizard;
import org.jboss.tools.runtime.ui.wizard.DownloadRuntimesTaskWizard;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanType;

public class RuntimeHomeComposite extends Composite {
	
	public static interface IRuntimeHomeCompositeListener {
		public void homeChanged();
	}
	
	
	private IWizardHandle handle;
	private TaskModel tm;
	protected Label homeDirLabel;
	protected Text homeDirText;
	protected Button homeDirButton;
	protected Link downloadAndInstallButton;
	protected Composite downloadAndInstallButtonWrapper;
	protected String homeDir;
	private IRuntimeHomeCompositeListener listener;

	public RuntimeHomeComposite(Composite parent, int style, IWizardHandle handle, TaskModel tm) {
		super(parent, style);
		this.handle = handle;
		this.tm = tm;
		createWidgets();
		fireInitialWidgetUpdates();
	}
	
	public void setListener(IRuntimeHomeCompositeListener listener) {
		this.listener = listener;
	}
	
	public String getHomeDirectory() {
		return homeDir;
	}
	
	protected void homeDirChanged() {
		if( listener != null )
			listener.homeChanged();
	}
	
	protected void createWidgets() {
		// Create our composite
		setLayout(new FormLayout());

		// Create Internal Widgets
		homeDirLabel = new Label(this, SWT.NONE);
		homeDirLabel.setText(Messages.wf_HomeDirLabel);
		homeDirText = new Text(this, SWT.BORDER);
		homeDirButton = new Button(this, SWT.NONE);
		homeDirButton.setText(Messages.browse);

		downloadAndInstallButtonWrapper = new Composite(this, SWT.NONE);
		downloadAndInstallButtonWrapper.setLayout(new FillLayout());
		downloadAndInstallButton = new Link(downloadAndInstallButtonWrapper, SWT.NONE);
		downloadAndInstallButton.setText("<a href=\"\">" + Messages.rwf_DownloadRuntime + "</a>");
		downloadAndInstallButton.addSelectionListener(new DownloadAndInstallListener());
		downloadAndInstallButton.setEnabled(false);
		downloadAndInstallButtonWrapper.setToolTipText(Messages.rwf_downloadTooltipLoading);
		
		// Add listeners
		homeDirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				homeDir = homeDirText.getText();
				homeDirChanged();
			}
		});

		homeDirButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				browseHomeDirClicked();
			}

			public void widgetSelected(SelectionEvent e) {
				browseHomeDirClicked();
			}

		});

		// Set Layout Data
		homeDirLabel.setLayoutData(UIUtil.createFormData2(null,0,homeDirText,-5,0,5,null,0));
		homeDirText.setLayoutData(UIUtil.createFormData2(homeDirLabel,5,null,0,0,5,homeDirButton,-5));
		homeDirButton.setLayoutData(UIUtil.createFormData2(homeDirLabel,5,null,0,null,0,100,0));
		downloadAndInstallButtonWrapper.setLayoutData(UIUtil.createFormData2(0,0,homeDirButton,-5,null,0,100,-10));
	}

	protected class DownloadAndInstallListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent se) {
			IRuntimeType type = getRuntimeFromTaskModel().getRuntimeType();
			final DownloadRuntimesTaskWizard wizard = new DownloadRuntimesWizard(downloadAndInstallButton.getShell(), 
					new JBossASDownloadRuntimeFilter(type));
			wizard.getTaskModel().putObject(DownloadRuntimesTaskWizard.SUPPRESS_RUNTIME_CREATION, new Boolean(true));
			WizardDialog dialog = new WizardDialog(downloadAndInstallButton.getShell(), wizard);
			dialog.open();
			final Job j = (Job)wizard.getTaskModel().getObject(DownloadRuntimesWizard.DOWNLOAD_JOB);
			if( j != null ) {
				// The job has been launched in the background
				// We will run in the wizard another task that simply waits for the background
				// job to complete. If the user cancels this runnable in the new server
				// wizard, it will not cancel the background job. 
				handle.setMessage(Messages.rwf_downloadWarning, IMessageProvider.INFORMATION);
				handle.update();
				try {
					handle.run(true, true, new IRunnableWithProgress() {
						@Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException,
								InterruptedException {
							// We get the delegating wrapper from the wizard
							DelegatingProgressMonitor delMon = (DelegatingProgressMonitor)wizard.getTaskModel().getObject(DownloadRuntimesTaskWizard.DOWNLOAD_JOB_DELEGATING_PROGRESS_MONITOR);
							delMon.add(new ProgressMonitorWrapper(monitor) {
								// We override isCanceled here, so that if the user
								// cancels the action in the new server wizard,
								// it will not also cancel the download
								public boolean isCanceled() {
									return false;
								}
							});
							
							// We now wait for the real download job to complete, or for us to be canceled. 
							final Boolean[] barrier = new Boolean[1];
							JobChangeAdapter jobChange = new JobChangeAdapter() {
								@Override
								public void done(IJobChangeEvent event) {
									barrier[0] = true;
								}
							};
							j.addJobChangeListener(jobChange);
							
							while( barrier[0] == null && !monitor.isCanceled()) {
								try {
									Thread.sleep(500);
								} catch(InterruptedException ie) {
									// Ignore
								}
							}
							
							if( monitor.isCanceled()) {
								j.removeJobChangeListener(jobChange);
								return;
							}
							
							// So the download job finished. Now we have to update the 
							// server home with the newly unzipped path. 
							final String newHomeDir = (String)wizard.getTaskModel().getObject(DownloadRuntimesTaskWizard.UNZIPPED_SERVER_HOME_DIRECTORY);
							if( newHomeDir != null ) {
								Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										homeDirText.setText(newHomeDir);
									}
								});
							}
						}
					});
				} catch(InvocationTargetException ite) {
					// Ignore
				} catch( InterruptedException ie ) {
					// Ignore
				}
			}
		}
	}
	

	protected IRuntime getRuntimeFromTaskModel() {
		IRuntime r = (IRuntime) tm.getObject(TaskModel.TASK_RUNTIME);
		if( r == null ) {
			r = (IRuntime) tm.getObject(ServerProfileWizardFragment.TASK_CUSTOM_RUNTIME);
		}
		return r;
	}
	
	
	protected void browseHomeDirClicked() {
		File file = homeDir == null ? null : new File(homeDir);
		if (file != null && !file.exists()) {
			file = null;
		}

		File directory = getDirectory(file, getShell());
		if (directory != null) {
			homeDir = directory.getAbsolutePath();
			homeDirText.setText(homeDir);
		}
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

	public void fillHomeDir(IRuntime rt) {
		if( rt.getLocation() == null ) {
			// new runtime creation
			IEclipsePreferences prefs2 = InstanceScope.INSTANCE.getNode(JBossServerUIPlugin.PLUGIN_ID);
			String value = prefs2.get(IPreferenceKeys.RUNTIME_HOME_PREF_KEY_PREFIX + rt.getRuntimeType().getId(), null);
			homeDir = (value != null && value.length() != 0) ? value : "";
		} else {
			// old runtime, load from it
			homeDir = rt.getLocation().toOSString();
		}
		homeDirText.setText(homeDir);
		
		boolean isWC = rt instanceof IRuntimeWorkingCopy;
		if( isWC ) {
			((IRuntimeWorkingCopy)rt).setLocation(new Path(homeDir));
		} 
		homeDirText.setEnabled(isWC);
		homeDirButton.setEnabled(isWC);
	}
	
	private void fireInitialWidgetUpdates() {
		new Job("Update Download Runtimes Hyperlink") {
			protected IStatus run(IProgressMonitor monitor) {
				final DownloadRuntime[] downloads = DownloadRuntimeServerUtil.getDownloadRuntimes(getRuntimeFromTaskModel().getRuntimeType());
				Display.getDefault().asyncExec(new Runnable() { 
					public void run() {
						if( downloads != null && downloads.length > 0 ) {
							try {
								if( !downloadAndInstallButton.isDisposed())
									downloadAndInstallButton.setEnabled(true);
								if( !downloadAndInstallButtonWrapper.isDisposed())
									downloadAndInstallButtonWrapper.setToolTipText(null);
							} catch(Throwable t) {
								t.printStackTrace();
							}
						} else {
							if( !downloadAndInstallButtonWrapper.isDisposed())
								downloadAndInstallButtonWrapper.setToolTipText(Messages.rwf_downloadTooltipEmpty);
						}
					}
				});
				return Status.OK_STATUS;
			}
		}.schedule();
	}
	

	public boolean isHomeValid() {
		if( homeDir == null  || homeDir.length() == 1 || !(new File(homeDir).exists())) 
			return false;
		ServerBeanLoader l = new ServerBeanLoader(new File(homeDir));
		if( l.getServerBeanType() == ServerBeanType.UNKNOWN) {
			return false;
		}
		return true;
	}
}
