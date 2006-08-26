/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.ui.wizards;

import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.jst.server.ui.internal.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
import org.eclipse.wst.server.ui.internal.wizard.ClosableWizardDialog;
import org.eclipse.wst.server.ui.internal.wizard.NewServerWizard;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.runtime.JBossProjectRuntime;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;

public class ProjectRuntimeWizardFragment extends WizardFragment {
	protected DynWebProjectRuntimeComposite comp;
	
	public ProjectRuntimeWizardFragment() {
	}

	public boolean hasComposite() {
		return true;
	}

	public Composite createComposite(Composite parent, IWizardHandle wizard) {
		comp = new DynWebProjectRuntimeComposite(parent, wizard);
		return comp;
	}

	public boolean isComplete() {
		IRuntimeWorkingCopy runtime = (IRuntimeWorkingCopy) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
		
		if (runtime == null)
			return false;
		IStatus status = runtime.validate(null);
		return (status != null && status.isOK());
	}

	public void enter() {
		if (comp != null) {
			IRuntimeWorkingCopy runtime = (IRuntimeWorkingCopy) getTaskModel().getObject(TaskModel.TASK_RUNTIME);
			comp.setRuntime(runtime);
		}
	}
	
	protected class DynWebProjectRuntimeComposite extends Composite {

		
		
		protected IRuntimeWorkingCopy runtimeWC;
		protected JBossProjectRuntime runtime;
		
		protected IWizardHandle wizard;
		
		protected Group runtimeGroup, serverGroup;
		protected Combo serverCombo, jreCombo;
		protected Text projRuntimeName, runtimeName, serverName;
		protected Text homeText, jreText, configName;
		protected String[] jreNames;
		protected ArrayList installedJREs;
		protected JBossServer[] jbservers;
		private Button newServerButton;
		
		
		private boolean repopulateRequired = false;
		
		/**
		 * GenericRuntimeComposite constructor comment.
		 */
		protected DynWebProjectRuntimeComposite(Composite parent, IWizardHandle wizard) {
			super(parent, SWT.NONE);
			this.wizard = wizard;
			
			wizard.setTitle("Create JBoss Project Runtime");
			wizard.setImageDescriptor(JBossServerUISharedImages.getImageDescriptor(JBossServerUISharedImages.IMG_JBOSS));
			
			createControl();
		}

		protected void setRuntime(IRuntimeWorkingCopy newRuntime) {
			if (newRuntime == null) {
				runtimeWC = null;
				runtime = null;
			} else {
				runtimeWC = newRuntime;
				runtime = (JBossProjectRuntime) newRuntime.getAdapter(JBossProjectRuntime.class);
			}
			
			init();
			validate();
		}

		/**
		 * Provide a wizard page to change the root directory.
		 */
		protected void createControl() {
			FillLayout layout = new FillLayout();
			setLayout(layout);
			setLayoutData(new GridData(GridData.FILL_BOTH));

			
			Composite main = new Composite(this, SWT.BORDER);
			main.setLayout(new FormLayout());
			
			Label serverLabel = new Label(main, SWT.NONE);
			serverLabel.setText("Server: ");
			FormData serverLabelData = new FormData();
			serverLabelData.left = new FormAttachment(0,5);
			serverLabelData.top = new FormAttachment(0,7);
			serverLabel.setLayoutData(serverLabelData);
			
			serverCombo = new Combo(main, SWT.READ_ONLY);
			FormData serverComboData = new FormData();
			serverComboData.top = new FormAttachment(0,5);
			serverComboData.right = new FormAttachment(70,5);
			serverComboData.left = new FormAttachment(serverLabel, 5);
			serverCombo.setLayoutData(serverComboData);
			
			newServerButton = new Button(main, SWT.NONE);
			newServerButton.setText("New Server...");
			FormData newServerButtonData = new FormData();
			newServerButtonData.top = new FormAttachment(0,5);
			newServerButtonData.left = new FormAttachment(serverCombo, 5);
			newServerButtonData.right = new FormAttachment(100, -5);
			newServerButton.setLayoutData(newServerButtonData);
			
			repopulateServerCombo();
			
			createRuntimeGroup(main);
			createServerGroup(main);
			
			addListeners();
			
		}
		
		protected void createRuntimeGroup(Composite main) {
			runtimeGroup = new Group(main, SWT.NONE);
			runtimeGroup.setText("Runtime Information");
			FormData runtimeGroupData = new FormData();
			runtimeGroupData.top = new FormAttachment(serverCombo, 5);
			runtimeGroupData.left = new FormAttachment(0,5);
			runtimeGroupData.right = new FormAttachment(100,-5);
			runtimeGroup.setLayoutData(runtimeGroupData);
			
			runtimeGroup.setLayout(new FormLayout());
			
			// Name
			
			Label runtimeNameLabel = new Label(runtimeGroup, SWT.NONE);
			runtimeNameLabel.setText("Runtime Name: ");
			FormData runtimeNameLabelData = new FormData();
			runtimeNameLabelData.left = new FormAttachment(0,5);
			runtimeNameLabelData.top = new FormAttachment(0,7);
			runtimeNameLabel.setLayoutData(runtimeNameLabelData);
			
			runtimeName = new Text(runtimeGroup, SWT.BORDER);
			FormData runtimeNameData = new FormData();
			runtimeNameData.left = new FormAttachment(30,5);
			runtimeNameData.top = new FormAttachment(0,5);
			runtimeNameData.right = new FormAttachment(100, -5);
			runtimeName.setLayoutData(runtimeNameData);
			runtimeName.setEditable(false);
			
			
			// Home
			Label homeLabel = new Label(runtimeGroup, SWT.NONE);
			homeLabel.setText("Home Directory: ");
			FormData homeLabelData = new FormData();
			homeLabelData.left = new FormAttachment(0,5);
			homeLabelData.top = new FormAttachment(runtimeName,5);
			homeLabel.setLayoutData(homeLabelData);

			homeText = new Text(runtimeGroup, SWT.BORDER);
			FormData homeTextData = new FormData();
			homeTextData.left = new FormAttachment(30,5);
			homeTextData.top = new FormAttachment(runtimeName,5);
			homeTextData.right = new FormAttachment(100, -5);
			homeText.setLayoutData(homeTextData);
			homeText.setEditable(false);		

			// JRE
			Label jreLabel = new Label(runtimeGroup, SWT.NONE);
			jreLabel.setText("JRE: ");
			FormData jreLabelData = new FormData();
			jreLabelData.left = new FormAttachment(0,5);
			jreLabelData.top = new FormAttachment(homeText,5);
			jreLabel.setLayoutData(jreLabelData);

			jreText = new Text(runtimeGroup, SWT.BORDER);
			FormData jreTextData = new FormData();
			jreTextData.left = new FormAttachment(30,5);
			jreTextData.top = new FormAttachment(homeText,5);
			jreTextData.right = new FormAttachment(100, -5);
			jreText.setLayoutData(jreTextData);
			jreText.setEditable(false);	
		}
		protected void createServerGroup(Composite main) {
			serverGroup = new Group(main, SWT.NONE);
			serverGroup.setText("Server Information");
			FormData serverGroupData = new FormData();
			serverGroupData.top = new FormAttachment(runtimeGroup, 5);
			serverGroupData.left = new FormAttachment(0,5);
			serverGroupData.right = new FormAttachment(100,-5);
			serverGroup.setLayoutData(serverGroupData);
			
			serverGroup.setLayout(new FormLayout());
			
			// Name
			
			Label serverNameLabel = new Label(serverGroup, SWT.NONE);
			serverNameLabel.setText("Server Name: ");
			FormData serverNameLabelData = new FormData();
			serverNameLabelData.left = new FormAttachment(0,5);
			serverNameLabelData.top = new FormAttachment(0,5);
			serverNameLabel.setLayoutData(serverNameLabelData);
			
			serverName = new Text(serverGroup, SWT.BORDER);
			FormData serverNameData = new FormData();
			serverNameData.left = new FormAttachment(30,5);
			serverNameData.top = new FormAttachment(0,5);
			serverNameData.right = new FormAttachment(100, -5);
			serverName.setLayoutData(serverNameData);
			serverName.setEditable(false);
			
			// Configuration
			Label configLabel = new Label(serverGroup, SWT.NONE);
			configLabel.setText("Server Configuration: ");
			FormData configLabelData = new FormData();
			configLabelData.left = new FormAttachment(0,5);
			configLabelData.top = new FormAttachment(serverName,5);
			configLabel.setLayoutData(configLabelData);
			
			configName = new Text(serverGroup, SWT.BORDER);
			FormData configNameData = new FormData();
			configNameData.left = new FormAttachment(30,5);
			configNameData.top = new FormAttachment(serverName,5);
			configNameData.right = new FormAttachment(100, -5);
			configName.setLayoutData(configNameData);
			configName.setEditable(false);


		}
		protected void addListeners() {
			serverCombo.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}
				public void widgetSelected(SelectionEvent e) {
					serverSelectionChanged();
					detailsChanged();
				} 
			});
			
			newServerButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}
				public void widgetSelected(SelectionEvent e) {
					NewServerWizard wizard = null;
					wizard = new NewServerWizard();
					ClosableWizardDialog dialog = new ClosableWizardDialog(new Shell(), wizard);
					int result = dialog.open();
					if( result == Window.OK) {
						int currentItems = serverCombo.getItems().length;
						int jbossServers = JBossServerCore.getAllJBossServers().length;
						if( jbossServers == currentItems + 1 ) {
							repopulateServerCombo();
						} else {
							repopulateRequired = true;
						}
					}
					
				} 
			});
		}
		
		protected void detailsChanged() {
			JBossServer jbs = getSelectedServer();
			if( jbs != null ) {
				//runtimeWC.setLocation(path)
				runtimeWC.setLocation(new Path(jbs.getAttributeHelper().getConfigurationPath()));
				((RuntimeWorkingCopy)runtimeWC).setAttribute(JBossProjectRuntime.SERVER_ID, jbs.getServer().getId());
			}
			// alert the wizard to check again
			wizard.setMessage(null, IMessageProvider.NONE);
		}
		protected void repopulateServerCombo() {
			jbservers = JBossServerCore.getAllJBossServers();
			String[] jbserverNames = new String[jbservers.length];
			for( int i = 0; i < jbservers.length; i++ ) {
				jbserverNames[i] = jbservers[i].getServer().getName();
			}
			serverCombo.setItems(jbserverNames);
		}
		protected void serverSelectionChanged() {
			JBossServer selected = getSelectedServer();
			if( selected != null ) {
				IServer server = selected.getServer();
				IRuntime runtime = server.getRuntime();
				
				configName.setText(selected.getAttributeHelper().getJbossConfiguration());
				serverName.setText(server.getName());
				jreText.setText(selected.getJBossRuntime().getVM().getName());
				homeText.setText(runtime.getLocation().toOSString());
				runtimeName.setText(runtime.getName());
			}
			if( repopulateRequired ) {
				repopulateRequired = false;
				repopulateServerCombo();
			}
		}
		
		public JBossServer getSelectedServer() {
			int index = serverCombo.getSelectionIndex();
			if( index != -1 ) {
				String name = serverCombo.getItem(index);
				for( int i = 0; i < jbservers.length; i++ ) { 
					if( jbservers[i].getServer().getName().equals(name)) {
						return jbservers[i];
					}
				}
			}
			return null;
		}
		
		protected void updateJREs() {
			// get all installed JVMs
			installedJREs = new ArrayList();
			IVMInstallType[] vmInstallTypes = JavaRuntime.getVMInstallTypes();
			int size = vmInstallTypes.length;
			for (int i = 0; i < size; i++) {
				IVMInstall[] vmInstalls = vmInstallTypes[i].getVMInstalls();
				int size2 = vmInstalls.length;
				for (int j = 0; j < size2; j++) {
					installedJREs.add(vmInstalls[j]);
				}
			}
			
			// get names
			size = installedJREs.size();
			jreNames = new String[size+1];
			jreNames[0] = Messages.runtimeTypeDefaultJRE;
			for (int i = 0; i < size; i++) {
				IVMInstall vmInstall = (IVMInstall) installedJREs.get(i);
				jreNames[i+1] = vmInstall.getName();
			}
		}

		
		protected void init() {
		}

		protected void validate() {
		}

	}

}
