/**
 * JBoss by Red Hat
 * Copyright 2006-2009, Red Hat Middleware, LLC, and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.ui.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.Messages;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class DeploySection extends ServerEditorSection {

	private Text deployText, tempDeployText;
	private Button metadataRadio, serverRadio, customRadio, currentSelection;
	private Button deployButton, tempDeployButton;
	private ModifyListener deployListener, tempDeployListener;
	private SelectionListener radioListener, zipListener;
	private ServerAttributeHelper helper;
	private Button zipDeployWTPProjects;
	private String lastCustomDeploy, lastCustomTemp;
	public DeploySection() {
	}

	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		helper = new ServerAttributeHelper(server.getOriginal(), server);
	}
	
	public void createSection(Composite parent) {
		super.createSection(parent);
		
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		
		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE|ExpandableComposite.EXPANDED|ExpandableComposite.TITLE_BAR);
		section.setText(Messages.swf_DeployEditorHeading);
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
		
		Composite composite = toolkit.createComposite(section);

		composite.setLayout(new FormLayout());
		
		Label descriptionLabel = toolkit.createLabel(composite, Messages.swf_DeploymentDescription);
		Control top = descriptionLabel;
		Composite inner = toolkit.createComposite(composite);
		inner.setLayout(new GridLayout(1, false));
		metadataRadio = toolkit.createButton(inner, Messages.EditorUseWorkspaceMetadata, SWT.RADIO);
		serverRadio = toolkit.createButton(inner, Messages.EditorUseServersDeployFolder, SWT.RADIO);
		customRadio = toolkit.createButton(inner, Messages.EditorUseCustomDeployFolder, SWT.RADIO);
		
		metadataRadio.setSelection(getDeployType().equals(IDeployableServer.DEPLOY_METADATA));
		serverRadio.setSelection(getDeployType().equals(IDeployableServer.DEPLOY_SERVER));
		customRadio.setSelection(getDeployType().equals(IDeployableServer.DEPLOY_CUSTOM));
		currentSelection = metadataRadio.getSelection() ? metadataRadio :
							serverRadio.getSelection() ? serverRadio : 
								customRadio;
		
		radioListener = new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			public void widgetSelected(SelectionEvent e) {
				if( e.getSource() == currentSelection )
					return; // do nothing
				execute(new RadioClickedCommand((Button)e.getSource(), currentSelection));
				currentSelection = (Button)e.getSource();
			} };
		metadataRadio.addSelectionListener(radioListener);
		serverRadio.addSelectionListener(radioListener);
		customRadio.addSelectionListener(radioListener);
		
		FormData radios = new FormData();
		radios.top = new FormAttachment(descriptionLabel,5);
		radios.left = new FormAttachment(0,5);
		radios.right = new FormAttachment(100,-5);
		inner.setLayoutData(radios);
		top = inner;
		
		
		Label label = toolkit.createLabel(composite, Messages.swf_DeployDirectory);
		label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		deployText = toolkit.createText(composite, getDeployDir(), SWT.BORDER);
		deployListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				execute(new SetDeployDirCommand());
			}
		};
		deployText.addModifyListener(deployListener);

		deployButton = toolkit.createButton(composite, Messages.browse, SWT.PUSH);
		deployButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog d = new DirectoryDialog(new Shell());
				d.setFilterPath(makeGlobal(deployText.getText()));
				String x = d.open();
				if( x != null ) {
					deployText.setText(makeRelative(x));
				}
			} 
		});
		
		Label tempDeployLabel = toolkit.createLabel(composite, Messages.swf_TempDeployDirectory);
		tempDeployLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));

		tempDeployText = toolkit.createText(composite, getTempDeployDir(), SWT.BORDER);
		tempDeployListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				execute(new SetTempDeployDirCommand());
			}
		};
		tempDeployText.addModifyListener(tempDeployListener);

		tempDeployButton = toolkit.createButton(composite, Messages.browse, SWT.PUSH);
		tempDeployButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog d = new DirectoryDialog(new Shell());
				d.setFilterPath(makeGlobal(tempDeployText.getText()));
				String x = d.open();
				if( x != null ) 
					tempDeployText.setText(makeRelative(x));
			} 
		});
		
		deployText.setEnabled(customRadio == null || customRadio.getSelection());
		tempDeployText.setEnabled(customRadio == null || customRadio.getSelection());

		FormData descriptionLabelData = new FormData();
		descriptionLabelData.left = new FormAttachment(0,5);
		descriptionLabelData.top = new FormAttachment(0,5);
		descriptionLabel.setLayoutData(descriptionLabelData);

		// first row
		FormData labelData = new FormData();
		labelData.left = new FormAttachment(0,5);
		labelData.right = new FormAttachment(deployText,-5);
		labelData.top = new FormAttachment(top,5);
		label.setLayoutData(labelData);
		
		FormData textData = new FormData();
		textData.left = new FormAttachment(deployButton, -305);
		textData.top = new FormAttachment(top,5);
		textData.right = new FormAttachment(deployButton, -5);
		deployText.setLayoutData(textData);
		
		FormData buttonData = new FormData();
		buttonData.right = new FormAttachment(100,-5);
		buttonData.left = new FormAttachment(100, -100);
		buttonData.top = new FormAttachment(top,2);
		deployButton.setLayoutData(buttonData);
		
		// second row
		FormData tempLabelData = new FormData();
		tempLabelData.left = new FormAttachment(0,5);
		tempLabelData.right = new FormAttachment(deployText, -5);
		tempLabelData.top = new FormAttachment(deployText,5);
		tempDeployLabel.setLayoutData(tempLabelData);
		
		FormData tempTextData = new FormData();
		tempTextData.left = new FormAttachment(tempDeployButton, -305);
		tempTextData.top = new FormAttachment(deployText,5);
		tempTextData.right = new FormAttachment(tempDeployButton, -5);
		tempDeployText.setLayoutData(tempTextData);
		
		FormData tempButtonData = new FormData();
		tempButtonData.right = new FormAttachment(100,-5);
		tempButtonData.left = new FormAttachment(100,-100);
		tempButtonData.top = new FormAttachment(deployText,5);
		tempDeployButton.setLayoutData(tempButtonData);
		
		zipDeployWTPProjects = toolkit.createButton(composite, Messages.EditorZipDeployments, SWT.CHECK);
		boolean publisherAvailable = ExtensionManager.getDefault().getZippedPublisher() != null;
		boolean value = getServer().zipsWTPDeployments();
		zipDeployWTPProjects.setEnabled(publisherAvailable);
		zipDeployWTPProjects.setSelection(publisherAvailable && value);

		FormData zipButtonData = new FormData();
		zipButtonData.right = new FormAttachment(100,-5);
		zipButtonData.left = new FormAttachment(0,5);
		zipButtonData.top = new FormAttachment(tempDeployText,5);
		zipDeployWTPProjects.setLayoutData(zipButtonData);
		
		zipListener = new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				execute(new SetZipCommand());
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		};
		zipDeployWTPProjects.addSelectionListener(zipListener);
		
		toolkit.paintBordersFor(composite);
		section.setClient(composite);
		getSaveStatus();
	}
	
	private String getDeployType() {
		return getServer().getDeployLocationType();
	}
	
	private String getDeployDir() {
		return server.getRuntime() == null ? "" : makeRelative(getServer().getDeployFolder()); //$NON-NLS-1$
	}
	private String getTempDeployDir() {
		return server.getRuntime() == null ? "" : makeRelative(getServer().getTempDeployFolder()); //$NON-NLS-1$
	}
	
	private IDeployableServer getServer() {
		return (IDeployableServer)server.loadAdapter(IDeployableServer.class, new NullProgressMonitor());
	}
	
	public IStatus[] getSaveStatus() {
		String error = ""; //$NON-NLS-1$
		List<Status> status = new ArrayList<Status>();
		if( getServer() instanceof JBossServer && getRuntime() == null ) {
			String msg = Messages.EditorNoRuntimeSelected;
			status.add(new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, msg));
			error = error + msg + "\n"; //$NON-NLS-1$
		}
		if(!new File(makeGlobal(deployText.getText())).exists()) {
			String msg = NLS.bind(Messages.EditorDeployDNE, deployText.getText()); 
			status.add(new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, msg));
			error = error + msg + "\n";  //$NON-NLS-1$
		}
		
		if(!new File(makeGlobal(tempDeployText.getText())).exists()) {
			String msg = NLS.bind(Messages.EditorTempDeployDNE, tempDeployText.getText());
			status.add(new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, msg));
			error = error + msg + "\n"; //$NON-NLS-1$
		}
		setErrorMessage(error.equals("") ? null : error); //$NON-NLS-1$
		return status.size() == 0 ? null : status.toArray(new IStatus[status.size()]);
	}


	
	public class SetDeployDirCommand extends ServerCommand {
		private String oldDir;
		private String newDir;
		private Text text;
		private ModifyListener listener;
		public SetDeployDirCommand() {
			super(DeploySection.this.server, Messages.EditorSetDeployLabel);
			this.text = deployText;
			this.newDir = deployText.getText();
			this.listener = deployListener;
			this.oldDir = helper.getAttribute(IDeployableServer.DEPLOY_DIRECTORY, ""); //$NON-NLS-1$
		}
		public void execute() {
			helper.setAttribute(IDeployableServer.DEPLOY_DIRECTORY, newDir);
			lastCustomDeploy = newDir;
			getSaveStatus();
		}
		public void undo() {
			text.removeModifyListener(listener);
			helper.setAttribute(IDeployableServer.DEPLOY_DIRECTORY, oldDir);
			text.setText(oldDir);
			text.addModifyListener(listener);
			getSaveStatus();
		}
	}

	public class SetZipCommand extends ServerCommand {
		boolean oldVal;
		boolean newVal;
		public SetZipCommand() {
			super(DeploySection.this.server, Messages.EditorZipDeployments);
			oldVal = helper.getAttribute(IDeployableServer.ZIP_DEPLOYMENTS_PREF, false);
			newVal = zipDeployWTPProjects.getSelection();
		}
		public void execute() {
			helper.setAttribute(IDeployableServer.ZIP_DEPLOYMENTS_PREF, newVal);
			getSaveStatus();
		}
		public void undo() {
			zipDeployWTPProjects.removeSelectionListener(zipListener);
			zipDeployWTPProjects.setSelection(oldVal);
			helper.setAttribute(IDeployableServer.ZIP_DEPLOYMENTS_PREF, oldVal);
			zipDeployWTPProjects.addSelectionListener(zipListener);
			getSaveStatus();
		}
	}
	
	public class SetTempDeployDirCommand extends ServerCommand {
		private String oldDir;
		private String newDir;
		private Text text;
		private ModifyListener listener;
		public SetTempDeployDirCommand() {
			super(DeploySection.this.server, Messages.EditorSetTempDeployLabel);
			text = tempDeployText;
			newDir = tempDeployText.getText();
			listener = tempDeployListener;
			oldDir = helper.getAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, ""); //$NON-NLS-1$
		}
		public void execute() {
			helper.setAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, newDir);
			lastCustomTemp = newDir;
			getSaveStatus();
		}
		public void undo() {
			text.removeModifyListener(listener);
			helper.setAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, oldDir);
			text.setText(oldDir);
			text.addModifyListener(listener);
			getSaveStatus();
		}
	}
	
	public class RadioClickedCommand extends ServerCommand {
		private Button newSelection, oldSelection;
		private String oldDir, newDir;
		private String oldTemp, newTemp;
		private String id;
		public RadioClickedCommand(Button clicked, Button previous) {
			super(DeploySection.this.server, Messages.EditorSetRadioClicked);
			newSelection = clicked;
			oldSelection = previous;
			id = DeploySection.this.server.getId();
		}
		public void execute() {
			boolean custom = newSelection == customRadio;
			deployText.setEnabled(custom);
			tempDeployText.setEnabled(custom);
			deployButton.setEnabled(custom);
			tempDeployButton.setEnabled(custom);
			oldDir = deployText.getText();
			oldTemp = tempDeployText.getText();
			
			String type = null;
			String oldType = oldSelection == customRadio ? IDeployableServer.DEPLOY_CUSTOM :
	 			oldSelection == serverRadio ? IDeployableServer.DEPLOY_SERVER :
	 				IDeployableServer.DEPLOY_METADATA;
			
			if( newSelection == metadataRadio  ) {
				newDir = JBossServerCorePlugin.getServerStateLocation(id)
					.append(IJBossServerConstants.DEPLOY).makeAbsolute().toString();
				newTemp = JBossServerCorePlugin.getServerStateLocation(id)
					.append(IJBossServerConstants.TEMP_DEPLOY).makeAbsolute().toString();
				type = IDeployableServer.DEPLOY_METADATA;
				new File(newDir).mkdirs();
				new File(newTemp).mkdirs();
			} else if( newSelection == serverRadio ) {
				IRuntime rt = DeploySection.this.server.getRuntime();
				if( rt != null ) {
					IJBossServerRuntime jbsrt = (IJBossServerRuntime)rt.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
					if( jbsrt != null ) {
						String config = jbsrt.getJBossConfiguration();
						newDir = new Path(IJBossServerConstants.SERVER)
							.append(config)
							.append(IJBossServerConstants.DEPLOY).makeRelative().toString();
						newTemp = new Path(IJBossServerConstants.SERVER).append(config)
							.append(IJBossServerConstants.TMP)
							.append(IJBossServerConstants.JBOSSTOOLS_TMP).makeRelative().toString();
						new File(newTemp).mkdirs();
						type = IDeployableServer.DEPLOY_SERVER;
					}
				}
			} else {
				newDir = lastCustomDeploy;
				newTemp = lastCustomTemp;
				type = IDeployableServer.DEPLOY_CUSTOM;
			}
			
			if( !newSelection.getSelection() ) {
				// REDO, so no one actually clicked the radio. UGH!
				oldSelection.removeSelectionListener(radioListener);
				oldSelection.setSelection(false);
				oldSelection.addSelectionListener(radioListener);
				
				newSelection.removeSelectionListener(radioListener);
				newSelection.setSelection(true);
				newSelection.addSelectionListener(radioListener);
			}
			
			type = type == null ? oldType : type;
			newDir = newDir == null ? oldDir : newDir;
			newTemp = newTemp == null ? oldTemp : newTemp; 
			
			deployText.removeModifyListener(deployListener);
			helper.setAttribute(IDeployableServer.DEPLOY_DIRECTORY, newDir);
			deployText.setText(newDir);
			deployText.addModifyListener(deployListener);

			tempDeployText.removeModifyListener(tempDeployListener);
			helper.setAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, newTemp);
			tempDeployText.setText(newTemp);
			tempDeployText.addModifyListener(tempDeployListener);
			
			helper.setAttribute(IDeployableServer.DEPLOY_DIRECTORY_TYPE, type);
			getSaveStatus();
		}
		public void undo() {
			deployText.removeModifyListener(deployListener);
			helper.setAttribute(IDeployableServer.DEPLOY_DIRECTORY, oldDir);
			deployText.setText(oldDir);
			deployText.addModifyListener(deployListener);

			tempDeployText.removeModifyListener(tempDeployListener);
			helper.setAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, oldTemp);
			tempDeployText.setText(oldTemp);
			tempDeployText.addModifyListener(tempDeployListener);
			
			oldSelection.removeSelectionListener(radioListener);
			oldSelection.setSelection(true);
			oldSelection.addSelectionListener(radioListener);
			
			newSelection.removeSelectionListener(radioListener);
			newSelection.setSelection(false);
			newSelection.addSelectionListener(radioListener);
			
			deployText.setEnabled(customRadio.getSelection());
			tempDeployText.setEnabled(customRadio.getSelection());
			
			String oldType = oldSelection == customRadio ? IDeployableServer.DEPLOY_CUSTOM :
				 			oldSelection == serverRadio ? IDeployableServer.DEPLOY_SERVER :
				 				IDeployableServer.DEPLOY_METADATA;
			helper.setAttribute(IDeployableServer.DEPLOY_DIRECTORY_TYPE, oldType);
			getSaveStatus();
		}
	}
	
	public void dispose() {
		// ignore
	}
	
	private String makeGlobal(String path) {
		return ServerUtil.makeGlobal(getRuntime(), new Path(path)).toString();
	}
	
	private String makeRelative(String path) {
		if (getRuntime() == null) {
			return path;
		}
		return ServerUtil.makeRelative(getRuntime(), new Path(path)).toString();
	}
	
	private IJBossServerRuntime getRuntime() {
		IRuntime r = server.getRuntime();
		IJBossServerRuntime ajbsrt = null;
		if (r != null) {
			ajbsrt = (IJBossServerRuntime) r
					.loadAdapter(IJBossServerRuntime.class,
							new NullProgressMonitor());
		}
		return ajbsrt;
	}
}
