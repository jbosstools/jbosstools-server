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
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.wst.server.ui.internal.command.SetServerRuntimeCommand;
import org.eclipse.wst.server.ui.internal.editor.OverviewEditorPart;
import org.eclipse.wst.server.ui.internal.wizard.TaskWizard;
import org.eclipse.wst.server.ui.internal.wizard.WizardTaskUtil;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel.ServerProfile;

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
					if (runtime != null && ServerUIPlugin.hasWizardFragment(RuntimeUtils.getRuntimeTypeId(server2.getServerType())))
						editRuntime(runtime);
				}
			});
			
			final IRuntime runtime = serverWc.getRuntime();
			if (runtime == null || !ServerUIPlugin.hasWizardFragment(RuntimeUtils.getRuntimeTypeId(serverWc.getServerType())))
				link.setEnabled(false);
			
			IRuntimeType runtimeType = serverWc.getServerType().getRuntimeType();
			runtimes = ServerUIPlugin.getRuntimes(runtimeType);
			
			runtimeCombo = new Combo(composite, SWT.READ_ONLY);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalIndent = decorationWidth;
			runtimeCombo.setLayoutData(data);
			updateRuntimeCombo();
			
			if( runtime == null ) {
				if( !requiresRuntime()) {
					// we dont require, and found is -1, so select the last item (No Runtime)
					String[] items = runtimeCombo.getItems();
					if( items.length > 0 )
						runtimeCombo.select(items.length-1);
				}
			} else {
				int size = runtimes.length;
				for (int i = 0; i < size; i++) {
					if (runtimes[i].equals(runtime)) { 
						runtimeCombo.select(i);
						break;
					}
				}
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
								executeCommand(new SetServerRuntimeCommand(serverWc, runtime2));
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
					int selIndex = runtimeCombo.getSelectionIndex();
					IRuntime newRuntime = selIndex < runtimes.length ? runtimes[selIndex] : null;
					
					// are they both null
					boolean bothNull = ( newRuntime == null && serverWc.getRuntime() == null );
					// Which one is not null
					IRuntime notNull = (newRuntime == null ? serverWc.getRuntime() : newRuntime);
					// Which one is not equal to notNull
					IRuntime notNotNull = (notNull == newRuntime ? serverWc.getRuntime() : newRuntime);
					// if the values are equal, do not execute the command, as no change has been made
					if(!( bothNull || notNull.equals(notNotNull)) ){
						executeCommand(new SetServerRuntimeCommand(serverWc, newRuntime));
					}
					link.setEnabled(newRuntime != null && ServerUIPlugin.hasWizardFragment(RuntimeUtils.getRuntimeTypeId(newRuntime)));
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
		boolean requiresRuntime = requiresRuntime();
		int size2 = size + (requiresRuntime ? 0 : 1);
		String[] items = new String[size2];
		for (int i = 0; i < size; i++)
			items[i] = runtimes[i].getName();
		if( !requiresRuntime ) {
			// Add a "no runtime" option
			items[size2-1] = "(No Runtime)";
		}
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
	
	
	private boolean requiresRuntime() {
		String currentProfile = ServerProfileModel.getProfile(serverWc, ServerProfileModel.DEFAULT_SERVER_PROFILE);
		ServerProfile sp = ServerProfileModel.getDefault().getProfile(serverWc.getServerType().getId(), currentProfile);
		if( sp != null ) {
			boolean requires = ServerProfileModel.getDefault().profileRequiresRuntime(serverWc.getServerType().getId(), 
					sp.getId());
			return requires;
		}
		return false;
	}
	
}
