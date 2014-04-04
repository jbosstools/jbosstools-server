/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.editor;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeLifecycleListener;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.core.util.SocketUtil;
import org.eclipse.wst.server.ui.editor.ServerEditorOverviewPageModifier;
import org.eclipse.wst.server.ui.internal.ContextIds;
import org.eclipse.wst.server.ui.internal.Messages;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
import org.eclipse.wst.server.ui.internal.Trace;
import org.eclipse.wst.server.ui.internal.command.SetServerRuntimeCommand;
import org.eclipse.wst.server.ui.internal.editor.OverviewEditorPart;
import org.eclipse.wst.server.ui.internal.wizard.TaskWizard;
import org.eclipse.wst.server.ui.internal.wizard.WizardTaskUtil;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;

/**
 * The large majority of this class has been copied and modified from the 
 * {@link OverviewEditorPart} class, and is intended to be 
 * a way for us to re-add the runtime combo to the editor for server types
 * that don't require a runtime but do allow for one, such as most JBT runtimes
 */
public class AddRuntimeComboOverviewPageModifier extends
		ServerEditorOverviewPageModifier {

	private IRuntime[] runtimes;
	private Combo runtimeCombo;
	private IRuntimeLifecycleListener runtimeListener;
	protected boolean updating = false;

	@Override
	public void handlePropertyChanged(PropertyChangeEvent event) {
		// TODO Auto-generated method stub

	}
	
	private boolean isJBTServerType() {
		return true;
	}
	
	@Override
	public void createControl(UI_LOCATION location, Composite parent) {
		addControl(parent, null);
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				ServerCore.removeRuntimeLifecycleListener(runtimeListener);
			}
		});
	}


	private void addControl(Composite composite, FormToolkit toolkit) {
		int decorationWidth = FieldDecorationRegistry.getDefault().getMaximumDecorationWidth(); 
		IWorkbenchHelpSystem whs = PlatformUI.getWorkbench().getHelpSystem();
		
		// runtime
		if (serverWc != null && serverWc.getServerType() != null) {
			final Hyperlink link = new Hyperlink(composite, SWT.NONE);
			link.setText(Messages.serverEditorOverviewRuntime);
			
			// The following 2 lines would be unnecessary if we had a reference to the server editor's formtoolkit
			link.setUnderlined(true);
			link.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
			
			final IServerWorkingCopy server2 = serverWc;
			link.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					IRuntime runtime = server2.getRuntime();
					if (runtime != null && ServerUIPlugin.hasWizardFragment(server2.getServerType().getRuntimeType().getId()))
						editRuntime(runtime);
				}
			});
			
			final IRuntime runtime = serverWc.getRuntime();
			if (runtime == null || !ServerUIPlugin.hasWizardFragment(serverWc.getServerType().getRuntimeType().getId()))
				link.setEnabled(false);
			
			IRuntimeType runtimeType = serverWc.getServerType().getRuntimeType();
			runtimes = ServerUIPlugin.getRuntimes(runtimeType);
			
			runtimeCombo = new Combo(composite, SWT.READ_ONLY);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalIndent = decorationWidth;
			runtimeCombo.setLayoutData(data);
			updateRuntimeCombo();
			
			int size = runtimes.length;
			for (int i = 0; i < size; i++) {
				if (runtimes[i].equals(runtime))
					runtimeCombo.select(i);
			}
			
			runtimeCombo.addSelectionListener(runtimeComboSelectionListener(link));
			whs.setHelp(runtimeCombo, ContextIds.EDITOR_RUNTIME);
			
			// add runtime listener
			runtimeListener = runtimeLifecycleListener(runtime);
			ServerCore.addRuntimeLifecycleListener(runtimeListener);
		}
	}

	protected IRuntimeLifecycleListener runtimeLifecycleListener(final IRuntime originalRuntime) {
		return new IRuntimeLifecycleListener() {
			public void runtimeChanged(final IRuntime runtime2) {
				// may be name change of current runtime
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						if (runtime2.equals(serverWc.getRuntime())) {
							try {
								if (updating)
									return;
								updating = true;
								execute(new SetServerRuntimeCommand(serverWc, runtime2));
								updating = false;
							} catch (Exception ex) {
								// ignore
							}
						}
						
						if (runtimeCombo != null && !runtimeCombo.isDisposed()) {
							updateRuntimeCombo();
							
							int size2 = runtimes.length;
							for (int i = 0; i < size2; i++) {
								if (runtimes[i].equals(originalRuntime))
									runtimeCombo.select(i);
							}
						}
					}
				});
			}

			public void runtimeAdded(final IRuntime runtime2) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						if (runtimeCombo != null && !runtimeCombo.isDisposed()) {
							updateRuntimeCombo();
							
							int size2 = runtimes.length;
							for (int i = 0; i < size2; i++) {
								if (runtimes[i].equals(originalRuntime))
									runtimeCombo.select(i);
							}
						}
					}
				});
			}

			public void runtimeRemoved(IRuntime runtime2) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						if (runtimeCombo != null && !runtimeCombo.isDisposed()) {
							updateRuntimeCombo();
							
							int size2 = runtimes.length;
							for (int i = 0; i < size2; i++) {
								if (runtimes[i].equals(originalRuntime))
									runtimeCombo.select(i);
							}
						}
					}
				});
			}
		};
	}
	protected SelectionListener runtimeComboSelectionListener(final Hyperlink link) {
		return (new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				try {
					if (updating)
						return;
					updating = true;
					IRuntime newRuntime = runtimes[runtimeCombo.getSelectionIndex()];
					execute(new SetServerRuntimeCommand(serverWc, newRuntime));
					link.setEnabled(newRuntime != null && ServerUIPlugin.hasWizardFragment(newRuntime.getRuntimeType().getId()));
					updating = false;
				} catch (Exception ex) {
					// ignore
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
	}
	
	protected void updateRuntimeCombo() {
		IRuntimeType runtimeType = serverWc.getServerType().getRuntimeType();
		runtimes = ServerUIPlugin.getRuntimes(runtimeType);
		
		if (SocketUtil.isLocalhost(serverWc.getHost())) {
			List<IRuntime> runtimes2 = new ArrayList<IRuntime>();
			int size = runtimes.length;
			for (int i = 0; i < size; i++) {
				IRuntime runtime2 = runtimes[i];
				if (!runtime2.isStub())
					runtimes2.add(runtime2);
			}
			runtimes = new IRuntime[runtimes2.size()];
			runtimes2.toArray(runtimes);
		}
		
		int size = runtimes.length;
		String[] items = new String[size];
		for (int i = 0; i < size; i++)
			items[i] = runtimes[i].getName();
		
		runtimeCombo.setItems(items);
	}
	

	protected void editRuntime(IRuntime runtime) {
		IRuntimeWorkingCopy runtimeWorkingCopy = runtime.createWorkingCopy();
		if (showWizard(runtimeWorkingCopy) != Window.CANCEL) {
			try {
				runtimeWorkingCopy.save(false, null);
			} catch (Exception ex) {
				// ignore
			}
		}
	}
	

	protected int showWizard(final IRuntimeWorkingCopy runtimeWorkingCopy) {
		String title = Messages.wizEditRuntimeWizardTitle;
		final WizardFragment fragment2 = ServerUIPlugin.getWizardFragment(runtimeWorkingCopy.getRuntimeType().getId());
		if (fragment2 == null)
			return Window.CANCEL;
		
		TaskModel taskModel = new TaskModel();
		taskModel.putObject(TaskModel.TASK_RUNTIME, runtimeWorkingCopy);

		WizardFragment fragment = new WizardFragment() {
			protected void createChildFragments(List<WizardFragment> list) {
				list.add(fragment2);
				list.add(WizardTaskUtil.SaveRuntimeFragment);
			}
		};
		
		TaskWizard wizard = new TaskWizard(title, fragment, taskModel);
		wizard.setForcePreviousAndNextButtons(true);
		WizardDialog dialog = new WizardDialog(runtimeCombo.getShell(), wizard);
		return dialog.open();
	}
	
	public void execute(IUndoableOperation operation) {
		// We do not have access to a command manager. This is a bug
		// Please file upstream bug
		//commandManager.execute(operation);
		

		try {
			IAdaptable adaptable = new IAdaptable() {
				public Object getAdapter(Class adapter) {
					if (Shell.class.equals(adapter))
						return runtimeCombo.getShell();
					return null;
				}
			};
			IStatus status = operation.execute(new NullProgressMonitor(), adaptable);
			if (status != null && !status.isOK())
				MessageDialog.openError(runtimeCombo.getShell(), Messages.editorServerEditor, status.getMessage());
		} catch (ExecutionException ce) {
			if (Trace.SEVERE) {
				Trace.trace(Trace.STRING_SEVERE, "Error executing command", ce);
			}
			return;
		}
	}
}
