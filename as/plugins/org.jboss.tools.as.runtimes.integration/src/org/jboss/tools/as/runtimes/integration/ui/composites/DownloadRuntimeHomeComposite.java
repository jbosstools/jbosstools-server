/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.runtimes.integration.ui.composites;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.jboss.ide.eclipse.as.wtp.ui.composites.RuntimeHomeComposite;
import org.jboss.ide.eclipse.as.wtp.ui.util.FormDataUtility;
import org.jboss.tools.as.runtimes.integration.Messages;
import org.jboss.tools.as.runtimes.integration.ui.wizard.JBossASDownloadRuntimeFilter;
import org.jboss.tools.as.runtimes.integration.util.DownloadRuntimeServerUtil;
import org.jboss.tools.foundation.core.jobs.DelegatingProgressMonitor;
import org.jboss.tools.foundation.ui.xpl.taskwizard.TaskWizardDialog;
import org.jboss.tools.runtime.core.model.DownloadRuntime;
import org.jboss.tools.runtime.ui.internal.wizard.DownloadRuntimesWizard;
import org.jboss.tools.runtime.ui.wizard.DownloadRuntimesTaskWizard;

public class DownloadRuntimeHomeComposite extends RuntimeHomeComposite {
	
	protected Link downloadAndInstallButton;
	protected Composite downloadAndInstallButtonWrapper;
	public DownloadRuntimeHomeComposite(Composite parent, int style, IWizardHandle handle, TaskModel tm) {
		super(parent, style, handle, tm);
		fireInitialWidgetUpdates();
	}
	
	protected void createWidgets() {
		super.createWidgets();

		downloadAndInstallButtonWrapper = new Composite(this, SWT.NONE);
		downloadAndInstallButtonWrapper.setLayout(new FillLayout());
		downloadAndInstallButton = new Link(downloadAndInstallButtonWrapper, SWT.NONE);
		downloadAndInstallButton.setText("<a href=\"\">" + Messages.rwf_DownloadRuntime + "</a>");
		downloadAndInstallButton.setEnabled(false);
		downloadAndInstallButtonWrapper.setToolTipText(Messages.rwf_downloadTooltipLoading);
		downloadAndInstallButtonWrapper.setLayoutData(FormDataUtility.createFormData2(0,0,homeDirButton,-5,null,0,100,-10));
	}

	protected class DownloadAndInstallListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent se) {
			IRuntimeType type = getRuntimeFromTaskModel().getRuntimeType();
			final DownloadRuntimesTaskWizard wizard = new DownloadRuntimesWizard(downloadAndInstallButton.getShell(), 
					new JBossASDownloadRuntimeFilter(type));
			wizard.getTaskModel().putObject(DownloadRuntimesTaskWizard.SUPPRESS_RUNTIME_CREATION, new Boolean(true));
			WizardDialog dialog = new TaskWizardDialog(downloadAndInstallButton.getShell(), wizard);
			dialog.open();
			final Job j = (Job)wizard.getTaskModel().getObject(DownloadRuntimesWizard.DOWNLOAD_JOB);
			if( j != null ) {
				// The job has been launched in the background
				// We will run in the wizard another task that simply waits for the background
				// job to complete. If the user cancels this runnable in the new server
				// wizard, it will not cancel the background job. 
				getWizardHandle().setMessage(Messages.rwf_downloadWarning, IMessageProvider.INFORMATION);
				getWizardHandle().update();
				try {
					getWizardHandle().run(true, true, new IRunnableWithProgress() {
						@Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException,
								InterruptedException {
							// We get the delegating wrapper from the wizard
							DelegatingProgressMonitor delMon = (DelegatingProgressMonitor)wizard.getTaskModel().getObject(DownloadRuntimesTaskWizard.DOWNLOAD_JOB_DELEGATING_PROGRESS_MONITOR);
							ProgressMonitorWrapper wrapped =
								new ProgressMonitorWrapper(monitor) {
									// We override isCanceled here, so that if the user
									// cancels the action in the new server wizard,
									// it will not also cancel the download
									public boolean isCanceled() {
										return false;
									}
								};
							delMon.add(wrapped);
							
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
								delMon.remove(wrapped);
								return;
							}
							
							// So the download job finished. Now we have to update the 
							// server home with the newly unzipped path. 
							final String newHomeDir = (String)wizard.getTaskModel().getObject(DownloadRuntimesTaskWizard.UNZIPPED_SERVER_HOME_DIRECTORY);
							if( newHomeDir != null && !homeDirText.isDisposed()) {
								new Job("Update wizard buttons") {
									protected IStatus run( IProgressMonitor monitor) {
										Display.getDefault().asyncExec(new Runnable() {
											public void run() {
												homeDirText.setText(newHomeDir);
												getWizardHandle().update();
											}
										});
										return Status.OK_STATUS;
									}
								}.schedule(800);
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

	private void fireInitialWidgetUpdates() {
		new Job("Update Download Runtimes Hyperlink") {
			protected IStatus run(IProgressMonitor monitor) {
				final DownloadRuntime[] downloads = DownloadRuntimeServerUtil.getDownloadRuntimes(getRuntimeFromTaskModel().getRuntimeType());
				Display.getDefault().asyncExec(new Runnable() { 
					public void run() {
						if( downloads != null && downloads.length > 0 ) {
							try {
								if( !downloadAndInstallButton.isDisposed()) {
									downloadAndInstallButton.setEnabled(true);
									downloadAndInstallButton.addSelectionListener(new DownloadAndInstallListener());
								}
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
}
