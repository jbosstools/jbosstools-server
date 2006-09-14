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
/*******************************************************************************
 * Portions of this file (specifically the JBossWorkingDirectoryBlock class) are  
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.jboss.ide.eclipse.as.ui.config;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.VMArgumentsBlock;
import org.eclipse.jdt.internal.debug.ui.launcher.WorkingDirectoryBlock;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.JBossLaunchConfigurationDelegate;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.JBossServerBehavior;
import org.jboss.ide.eclipse.as.core.server.runtime.IJBossServerRuntimeDelegate;
import org.jboss.ide.eclipse.as.core.server.runtime.JBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.ASDebug;
import org.jboss.ide.eclipse.as.core.util.RuntimeConfigUtil;



public class JBossLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

	private IServer server;
	private JBossServer jbServer;
	private JBossServerBehavior jbServerBehavior;
	private JBossServerRuntime jbRuntime;
	
	private ILaunchConfiguration launchConfiguration;
	private ILaunchConfigurationWorkingCopy launchWC;

	
	
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[10];
		int i = 0;
		tabs[i] = new JavaArgumentsTab2();
		tabs[i++].setLaunchConfigurationDialog(dialog);
		tabs[i] = new JBossJavaArgumentsTab(JBossJavaArgumentsTab.STOP);
		tabs[i++].setLaunchConfigurationDialog(dialog);
		tabs[i] = new JBossJavaArgumentsTab(JBossJavaArgumentsTab.TWIDDLE);
		tabs[i++].setLaunchConfigurationDialog(dialog);
		tabs[i] = new JavaClasspathTab2();
		tabs[i++].setLaunchConfigurationDialog(dialog);
		tabs[i] = new SourceLookupTab();
		tabs[i++].setLaunchConfigurationDialog(dialog);
		tabs[i] = new EnvironmentTab();
		tabs[i++].setLaunchConfigurationDialog(dialog);
		tabs[i] = new JavaJRETab();
		tabs[i++].setLaunchConfigurationDialog(dialog);	 
		tabs[i] = new CommonTab();
		tabs[i++].setLaunchConfigurationDialog(dialog);

		
		ILaunchConfigurationTab[] tabs2 = new ILaunchConfigurationTab[i];
		System.arraycopy(tabs, 0, tabs2, 0, i);
		setTabs(tabs2);
	}
	
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			server = ServerUtil.getServer(configuration);
			jbServer = server.loadAdapter(JBossServer.class, null) == null ? null : ((JBossServer)server.loadAdapter(JBossServer.class, null));
			jbRuntime = jbServer.getJBossRuntime();
		} catch(Exception e) {
		}

		ILaunchConfigurationTab[] tabs = getTabs();
		for (int i = 0; i < tabs.length; i++) {
			tabs[i].initializeFrom(configuration);
		}		
	}

	
	protected class JBossJavaArgumentsTab extends JavaArgumentsTab {
		public static final int START   = 0;
		public static final int STOP    = 1;
		public static final int TWIDDLE = 2;
			
		
		
		private int type;
		private String suffix;
		protected String programArgsKey;
		protected String vmArgsKey;
		protected String workingDirKey;
		
		public JBossJavaArgumentsTab(int type) {
			super();
			this.type = type;
			if( this.type == START ) suffix = JBossLaunchConfigurationDelegate.PRGM_ARGS_START_SUFFIX;
			if( this.type == STOP ) suffix = JBossLaunchConfigurationDelegate.PRGM_ARGS_STOP_SUFFIX;
			if( this.type == TWIDDLE ) suffix = JBossLaunchConfigurationDelegate.PRGM_ARGS_TWIDDLE_SUFFIX;
			
			programArgsKey = IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS + suffix;
			vmArgsKey = IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS + suffix;
			workingDirKey = IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY + suffix;
		}
		
		public void setDefaults(ILaunchConfigurationWorkingCopy config) {
			config.setAttribute(programArgsKey, (String)null);
			fVMArgumentsBlock.setDefaults(config);
			fWorkingDirectoryBlock.setDefaults(config);
		}
		
		protected VMArgumentsBlock createVMArgsBlock() {
			return new JBossVMArgumentsBlock();
		}

		protected WorkingDirectoryBlock createWorkingDirBlock() {
			return new JBossWorkingDirectoryBlock();
		}

		public String getName() {
			switch (type) {
			case START: return "Start Args";
			case STOP: return "Stop Args";
			case TWIDDLE: return "Twiddle Args";
			default:
				return super.getName();
			}
		}	

		
		/**
		 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
		 */
		public void initializeFrom(ILaunchConfiguration configuration) {
			try {
				String prgmArgs = configuration.getAttribute(programArgsKey, (String)null);
				if( prgmArgs == null && jbServer != null && jbRuntime != null ) {
					if( type == STOP ) prgmArgs = jbRuntime.getVersionDelegate().getStopArgs(jbServer);
					else if( type == TWIDDLE ) {
				 		int jndiPort = jbServer.getDescriptorModel().getJNDIPort();
						String host = jbServer.getServer().getHost();
						prgmArgs = "-s " + host + ":" + jndiPort +  " -a jmx/rmi/RMIAdaptor "; 
					}
				}
				fPrgmArgumentsText.setText(prgmArgs);
				fVMArgumentsBlock.initializeFrom(configuration);
				fWorkingDirectoryBlock.initializeFrom(configuration);
			} catch (CoreException e) {
				setErrorMessage(LauncherMessages.JavaArgumentsTab_Exception_occurred_reading_configuration___15 + e.getStatus().getMessage()); 
				JDIDebugUIPlugin.log(e);
			}
		}

		/**
		 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
		 */
		public void performApply(ILaunchConfigurationWorkingCopy configuration) {
			configuration.setAttribute(programArgsKey, getAttributeValueFrom(fPrgmArgumentsText));
			fVMArgumentsBlock.performApply(configuration);
			fWorkingDirectoryBlock.performApply(configuration);
		}

		protected class JBossVMArgumentsBlock extends VMArgumentsBlock {
			public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
				configuration.setAttribute(vmArgsKey, (String)null);		
			}

			public void initializeFrom(ILaunchConfiguration configuration) {
				try {
					fVMArgumentsText.setText(configuration.getAttribute(vmArgsKey, "")); //$NON-NLS-1$
				} catch (CoreException e) {
					setErrorMessage(LauncherMessages.JavaArgumentsTab_Exception_occurred_reading_configuration___15 + e.getStatus().getMessage()); 
					JDIDebugUIPlugin.log(e);			
				}
			}

			public void performApply(ILaunchConfigurationWorkingCopy configuration) {
				configuration.setAttribute(vmArgsKey, getAttributeValueFrom(fVMArgumentsText));
			}
		}
		
		
		/* 100% lifted from jdt because they didnt make anything PROTECTED! >:O */
		public class JBossWorkingDirectoryBlock extends WorkingDirectoryBlock {
			
			// Local directory
			private Button fWorkspaceButton;
			private Button fFileSystemButton;
			private Button fVariablesButton;
			
			//bug 29565 fix
			private Button fUseDefaultDirButton = null;
			private Button fUseOtherDirButton = null;
			private Text fOtherWorkingText = null;
			private Text fWorkingDirText;
			
			/**
			 * The last launch config this tab was initialized from
			 */
			private ILaunchConfiguration fLaunchConfiguration;
			
			/**
			 * A listener to update for text changes and widget selection
			 */
			private class WidgetListener extends SelectionAdapter implements ModifyListener {
				public void modifyText(ModifyEvent e) {
					updateLaunchConfigurationDialog();
				}//end modifyText
				public void widgetSelected(SelectionEvent e) {
					Object source= e.getSource();
					if (source == fWorkspaceButton) {
						handleWorkspaceDirBrowseButtonSelected();
					}//end if
					else if (source == fFileSystemButton) {
						handleWorkingDirBrowseButtonSelected();
					}//end if 
					else if (source == fVariablesButton) {
						handleWorkingDirVariablesButtonSelected();
					}//end if 
					else if(source == fUseDefaultDirButton) {
						//only perform the action if this is the button that was selected
						if(fUseDefaultDirButton.getSelection()) {
							setDefaultWorkingDir();
						}//end if
					}//end if 
					else if(source == fUseOtherDirButton) {
						//only perform the action if this is the button that was selected
						if(fUseOtherDirButton.getSelection()) {
							handleUseOtherWorkingDirButtonSelected();
						}//end if
					}//end if
				}//end widgetSelected
			}//end WidgetListener class
			
			private WidgetListener fListener = new WidgetListener();
			
			/* (non-Javadoc)
			 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
			 */
			public void createControl(Composite parent) {
				Font font = parent.getFont();
						
				Group group = new Group(parent, SWT.NONE);
				PlatformUI.getWorkbench().getHelpSystem().setHelp(group, IJavaDebugHelpContextIds.WORKING_DIRECTORY_BLOCK);		
				GridLayout workingDirLayout = new GridLayout();
				workingDirLayout.numColumns = 2;
				workingDirLayout.makeColumnsEqualWidth = false;
				group.setLayout(workingDirLayout);
				GridData gd = new GridData(GridData.FILL_HORIZONTAL);
				group.setLayoutData(gd);
				group.setFont(font);
				setControl(group);
				
				group.setText(LauncherMessages.WorkingDirectoryBlock_12); 
				
				fUseDefaultDirButton = new Button(group, SWT.RADIO);
				fUseDefaultDirButton.setText(LauncherMessages.WorkingDirectoryBlock_18);
				fUseDefaultDirButton.setFont(font);
				fUseDefaultDirButton.addSelectionListener(fListener);
				fWorkingDirText = new Text(group, SWT.SINGLE | SWT.BORDER);
				gd = new GridData(GridData.FILL_HORIZONTAL);
				fWorkingDirText.setLayoutData(gd);
				fWorkingDirText.setFont(font);
				fWorkingDirText.addModifyListener(fListener);
				fWorkingDirText.setEnabled(false);
				
				fUseOtherDirButton = new Button(group, SWT.RADIO);
				fUseOtherDirButton.setText(LauncherMessages.WorkingDirectoryBlock_19);
				fUseOtherDirButton.setFont(font);
				fUseOtherDirButton.addSelectionListener(fListener);
				fOtherWorkingText = new Text(group, SWT.SINGLE | SWT.BORDER);
				gd = new GridData(GridData.FILL_HORIZONTAL);
				fOtherWorkingText.setLayoutData(gd);
				fOtherWorkingText.setFont(font);
				fOtherWorkingText.addModifyListener(fListener);
				
				Composite buttonComp = new Composite(group, SWT.NONE);
				GridLayout layout = new GridLayout(3, false);
				layout.marginHeight = 0;
				layout.marginWidth = 0;
				buttonComp.setLayout(layout);
				gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
				gd.horizontalSpan = 2;
				buttonComp.setLayoutData(gd);
				buttonComp.setFont(font);		
				fWorkspaceButton = createPushButton(buttonComp, LauncherMessages.WorkingDirectoryBlock_0, null); 
				fWorkspaceButton.addSelectionListener(fListener);
				
				fFileSystemButton = createPushButton(buttonComp, LauncherMessages.WorkingDirectoryBlock_1, null); 
				fFileSystemButton.addSelectionListener(fListener);
				
				fVariablesButton = createPushButton(buttonComp, LauncherMessages.WorkingDirectoryBlock_17, null); 
				fVariablesButton.addSelectionListener(fListener);
			}
							
			/* (non-Javadoc)
			 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
			 */
			public void dispose() {}
				
			/**
			 * Show a dialog that lets the user select a working directory
			 */
			private void handleWorkingDirBrowseButtonSelected() {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.setMessage(LauncherMessages.WorkingDirectoryBlock_7); 
				String currentWorkingDir = getWorkingDirectoryText();
				if (!currentWorkingDir.trim().equals("")) { //$NON-NLS-1$
					File path = new File(currentWorkingDir);
					if (path.exists()) {
						dialog.setFilterPath(currentWorkingDir);
					}//end if		
				}//end if
				String selectedDirectory = dialog.open();
				if (selectedDirectory != null) {
					fOtherWorkingText.setText(selectedDirectory);
				}//end if		
			}//end handleQWrokingDirBrowseBUttonSelected

			/**
			 * Show a dialog that lets the user select a working directory from 
			 * the workspace
			 */
			private void handleWorkspaceDirBrowseButtonSelected() {
			    IContainer currentContainer= getContainer();
				if (currentContainer == null) {
				    currentContainer = ResourcesPlugin.getWorkspace().getRoot();
				}//end if 
				ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), currentContainer, false,	LauncherMessages.WorkingDirectoryBlock_4); 
				dialog.showClosedProjects(false);
				dialog.open();
				Object[] results = dialog.getResult();		
				if ((results != null) && (results.length > 0) && (results[0] instanceof IPath)) {
					IPath path = (IPath)results[0];
					String containerName = path.makeRelative().toString();
					setOtherWorkingDirectoryText("${workspace_loc:" + containerName + "}"); //$NON-NLS-1$ //$NON-NLS-2$
				}//end if			
			}//end handleWorkspaceDirBrowseButtonSelected
			
			/**
			 * Returns the selected workspace container,or <code>null</code>
			 */
			protected IContainer getContainer() {
				String path = getWorkingDirectoryText();
				if (path.length() > 0) {
				    IResource res = null;
				    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				    if (path.startsWith("${workspace_loc:")) { //$NON-NLS-1$
				        IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
					    try {
		                    path = manager.performStringSubstitution(path, false);
		                    IContainer[] containers = root.findContainersForLocation(new Path(path));
		                    if (containers.length > 0) {
		                        res = containers[0];
		                    }//end if
		                }//end try 
					    catch (CoreException e) {}
					}//end if 
				    else {	    
						res = root.findMember(path);
					}//end else
					if (res instanceof IContainer) {
						return (IContainer)res;
					}//end if
				}//end if
				return null;
			}//end getContainer
				
			/**
			 * The default working dir radio button has been selected.
			 */
			private void handleUseDefaultWorkingDirButtonSelected() {
				fWorkspaceButton.setEnabled(false);
				fOtherWorkingText.setEnabled(false);
				fVariablesButton.setEnabled(false);
				fFileSystemButton.setEnabled(false);
				fUseOtherDirButton.setSelection(false);
			}

			/**
			 * The other working dir radio button has been selected
			 * 
			 * @since 3.2
			 */
			private void handleUseOtherWorkingDirButtonSelected() {
				fOtherWorkingText.setEnabled(true);
				fWorkspaceButton.setEnabled(true);
				fVariablesButton.setEnabled(true);
				fFileSystemButton.setEnabled(true);
				updateLaunchConfigurationDialog();
			}

			/**
			 * The working dir variables button has been selected
			 */
			private void handleWorkingDirVariablesButtonSelected() {
				StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
				dialog.open();
				String variableText = dialog.getVariableExpression();
				if (variableText != null) {
					fOtherWorkingText.insert(variableText);
				}//end if
			}//end handleWorkingDirVariablesButtonSelected
			
			/**
			 * Sets the default working directory
			 */
			protected void setDefaultWorkingDir() {
				try {
					ILaunchConfiguration config = getLaunchConfiguration();
					if (config != null) {
						IJavaProject javaProject = JavaRuntime.getJavaProject(config);
						if (javaProject != null) {
							setDefaultWorkingDirectoryText("${workspace_loc:" + javaProject.getPath().makeRelative().toOSString() + "}");  //$NON-NLS-1$//$NON-NLS-2$
							return;
						}//end if
					}//end if
				}//end try 
				catch (CoreException ce) {}
				setDefaultWorkingDirectoryText(System.getProperty("user.dir")); //$NON-NLS-1$
			}

			/* (non-Javadoc)
			 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
			 */
			public boolean isValid(ILaunchConfiguration config) {
				setErrorMessage(null);
				setMessage(null);
				// if variables are present, we cannot resolve the directory
				String workingDirPath = getWorkingDirectoryText();
				if (workingDirPath.indexOf("${") >= 0) { //$NON-NLS-1$
					IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
					try {
						manager.validateStringVariables(workingDirPath);
					}//end try
					catch (CoreException e) {
						setErrorMessage(e.getMessage());
						return false;
					}//end catch
				}//end if 
				else if (workingDirPath.length() > 0) {
					IContainer container = getContainer();
					if (container == null) {
						File dir = new File(workingDirPath);
						if (dir.isDirectory()) {
							return true;
						}//end if
						setErrorMessage(LauncherMessages.WorkingDirectoryBlock_10); 
						return false;
					}//end if
				} else if (workingDirPath.length() == 0) {
					setErrorMessage(LauncherMessages.WorkingDirectoryBlock_20);
				}
				return true;
			}//end isValid

			/**
			 * Defaults are empty.
			 * 
			 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
			 */
			public void setDefaults(ILaunchConfigurationWorkingCopy config) {
				config.setAttribute(workingDirKey, (String)null);
			}

			/* (non-Javadoc)
			 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
			 */
			public void initializeFrom(ILaunchConfiguration configuration) {
				setLaunchConfiguration(configuration);
				try {			
					String wd = configuration.getAttribute(workingDirKey, (String)null);
					setDefaultWorkingDir();
					if (wd != null) {
						setOtherWorkingDirectoryText(wd);
					}//end else
				}//end try 
				catch (CoreException e) {
					setErrorMessage(LauncherMessages.JavaArgumentsTab_Exception_occurred_reading_configuration___15 + e.getStatus().getMessage()); 
					JDIDebugUIPlugin.log(e);
				}//end catch
			}

			/* (non-Javadoc)
			 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
			 */
			public void performApply(ILaunchConfigurationWorkingCopy configuration) {
				if(fUseDefaultDirButton.getSelection()) {
					configuration.setAttribute(workingDirKey, (String)null);
				}//end if
				else {
					configuration.setAttribute(workingDirKey, getWorkingDirectoryText());
				}//end else
			}//end performApply
			
			/* (non-Javadoc)
			 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
			 */
			public String getName() {
				return LauncherMessages.WorkingDirectoryBlock_Working_Directory_8; 
			}//end getName
			
			/**
			 * gets the path from the text box that is selected
			 * @return the working directory the user wishes to use
			 * @since 3.2
			 */
			protected String getWorkingDirectoryText() {
				if(fUseDefaultDirButton.getSelection()) {
					return fWorkingDirText.getText().trim();
				}//end if
				return fOtherWorkingText.getText().trim();
			}//end getWorkingDirectoryPath
			
			/**
			 * sets the default working directory text
			 * @param dir the dir to set the widget to
			 * @since 3.2
			 */
			protected void setDefaultWorkingDirectoryText(String dir) {
				if(dir != null) {
					fWorkingDirText.setText(dir);
					fUseDefaultDirButton.setSelection(true);
					handleUseDefaultWorkingDirButtonSelected();
				}//end if
			}//setDefaultWorkingDirectoryText
			
			/**
			 * sets the other dir text
			 * @param dir the new text
			 * @since 3.2
			 */
			protected void setOtherWorkingDirectoryText(String dir) {
				if(dir != null) {
					fOtherWorkingText.setText(dir);
					fUseDefaultDirButton.setSelection(false);
					fUseOtherDirButton.setSelection(true);
					handleUseOtherWorkingDirButtonSelected();
				}//end if
			}//end setOtherWorkingDirectoryText
			
			/**
			 * Sets the java project currently specified by the
			 * given launch config, if any.
			 */
			protected void setLaunchConfiguration(ILaunchConfiguration config) {
				fLaunchConfiguration = config;
			}	
			
			/**
			 * Returns the current java project context
			 */
			protected ILaunchConfiguration getLaunchConfiguration() {
				return fLaunchConfiguration;
			}
			
			/**
			 * Allows this entire block to be enabled/disabled
			 * @param enabled whether to enable it or not
			 */
			protected void setEnabled(boolean enabled) {
				fUseDefaultDirButton.setEnabled(enabled);
				fUseOtherDirButton.setEnabled(enabled);
				if(fOtherWorkingText.isEnabled()) {
					fOtherWorkingText.setEnabled(enabled);
					fWorkspaceButton.setEnabled(enabled);
					fVariablesButton.setEnabled(enabled);
					fFileSystemButton.setEnabled(enabled);
				}//end if
				// in the case where the 'other' text is selected and we want to enable
				if(fUseOtherDirButton.getSelection() && enabled == true) {
					fOtherWorkingText.setEnabled(enabled);
				}//end if
			}//end setEnabled
			
		}//end class

	}
	
	
	private class JavaArgumentsTab2 extends JBossJavaArgumentsTab {
		public JavaArgumentsTab2() {
			super(START);
		}
		private String originalProgramArgs;
		public void initializeFrom(ILaunchConfiguration configuration) {
			try {
				originalProgramArgs = configuration.getAttribute(programArgsKey, "");
			} catch( CoreException ce ) {
			}
			super.initializeFrom(configuration);
		}
		
		public boolean canSave() {
			return isHostAndConfigValid();
		}

		// Host requirement has been removed
		private boolean isHostAndConfigValid() {
			String progArgs = fPrgmArgumentsText.getText();
			String workingConfig = RuntimeConfigUtil.getCommandArgument(progArgs, "-c", "--configuration");

			String oldConfig = RuntimeConfigUtil.getCommandArgument(originalProgramArgs, "-c", "--configuration");
			
			if( oldConfig != null && !workingConfig.equals(oldConfig)) return false;
			return true;
		}
		public boolean isValid(ILaunchConfiguration config) {
			boolean sup = super.isValid(config);
			if( !sup ) return false;
			
			try {
				
				boolean valid = isHostAndConfigValid();
				if( !valid ) {
					setErrorMessage("You may not change the configuration.");
					//getLaunchConfigurationDialog().updateButtons();
					return false;
				}
			} catch( Exception ce) {
				ce.printStackTrace();
			}
			
			setErrorMessage(null);
			return true;
		}
	}
	
	
	
	public class JavaClasspathTab2 extends JavaClasspathTab {
		public void performApply(ILaunchConfigurationWorkingCopy configuration) {
			super.performApply(configuration);
			try {
				
				server = ServerUtil.getServer(configuration);
				jbServer = (JBossServer)server.getAdapter(JBossServer.class);
				if (jbServer == null) {
					jbServer = (JBossServer) server.loadAdapter(JBossServer.class, new NullProgressMonitor());
				}
				jbServerBehavior = (JBossServerBehavior) server.getAdapter(JBossServerBehavior.class);
				jbRuntime = jbServer.getJBossRuntime();
				
				
				if( configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, (List)null) == null ) {
					// back to default. Now set our default.
					List classpath = jbRuntime.getVersionDelegate().getRuntimeClasspath(jbServer, IJBossServerRuntimeDelegate.ACTION_START);
					configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
					configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath);
					configuration.setAttribute(JBossServerBehavior.LAUNCH_CONFIG_DEFAULT_CLASSPATH, true);
				} else {
					// its not default... so set our string as no
					configuration.setAttribute(JBossServerBehavior.LAUNCH_CONFIG_DEFAULT_CLASSPATH, false);
				}
			} catch( CoreException ce ) {
				ce.printStackTrace();
			}
		}
		
		public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
			ASDebug.p("Setting defaults", this);
			try {
				if( jbRuntime == null ) {
					server = ServerUtil.getServer(configuration);
					jbServer = (JBossServer)server.getAdapter(JBossServer.class);
					if (jbServer == null) {
						jbServer = (JBossServer) server.loadAdapter(JBossServer.class, new NullProgressMonitor());
					}
					jbServerBehavior = (JBossServerBehavior) server.getAdapter(JBossServerBehavior.class);
					jbRuntime = jbServer.getJBossRuntime();
				}
	
				List classpath = jbRuntime.getVersionDelegate().getRuntimeClasspath(jbServer, IJBossServerRuntimeDelegate.ACTION_START);
				configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
				configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath);
			} catch ( CoreException ce ) {
				ce.printStackTrace();
			}
		}
		

	}


}
