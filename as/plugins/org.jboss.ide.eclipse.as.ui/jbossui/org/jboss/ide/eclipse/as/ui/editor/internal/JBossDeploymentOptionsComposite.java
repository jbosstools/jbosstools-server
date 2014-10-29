/******************************************************************************* 
* Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.editor.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.IServerModeDetails;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.editor.DeploymentPage;
import org.jboss.ide.eclipse.as.ui.subsystems.IBrowseBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel;
import org.jboss.tools.as.core.server.controllable.systems.IDeploymentOptionsController;

/**
 * This class is an internal class for displaying
 * deployment options for a JBoss server. 
 * 
 */
public class JBossDeploymentOptionsComposite extends Composite implements PropertyChangeListener {
	
	protected static final String MAIN = ServerProfileModel.DEFAULT_SERVER_PROFILE;
	
	private StandardDeploymentPageController controller;
	private Text deployText, tempDeployText;
	private Button metadataRadio, serverRadio, customRadio, currentRadioSelection;
	private Button deployButton, tempDeployButton;
	private ModifyListener deployListener, tempDeployListener;
	private SelectionListener radioListener, zipListener;
	private Button zipDeployWTPProjects;
	private String lastCustomDeploy, lastCustomTemp;
	
	
	// Poorly named, but, after certain events, any widget in this list
	// may have its 'enabled' status tweaked based on shouldEnableControl
	private ArrayList<Control> enableDisableWidgets = new ArrayList<Control>();

	
	private IServerWorkingCopy lastWC;
	
	public JBossDeploymentOptionsComposite(Composite parent, StandardDeploymentPageController page) {
		super(parent, SWT.NONE);
		this.controller = page;
		setLayout(new GridLayout(1,true));
		createDefaultComposite(this);
	}

	protected String openBrowseDialog(String original) {
		IControllableServerBehavior csb = JBossServerBehaviorUtils.getControllableBehavior(lastWC);
		IBrowseBehavior beh = null;
		if( csb != null ) {
			try {
				beh = (IBrowseBehavior)csb.getController(IBrowseBehavior.SYSTEM_ID);
				return beh.openBrowseDialog(controller.getPage().getServer(), original);
			} catch(CoreException ce) {
				JBossServerUIPlugin.log(ce.getStatus());
			}
		}
		return null;
	}

	
	public DeploymentPage getPage() {
		return controller.getPage();
	}

	protected ServerAttributeHelper getHelper() {
		return getPage().getHelper();
	}
	
	public Button getServerRadio() {
		return serverRadio;
	}

	public Button getCustomRadio() {
		return customRadio;
	}

	public Button getMetadataRadio() {
		return metadataRadio;
	}

	public Button getSelectedRadio() {
		return currentRadioSelection;
	}
	
	protected boolean shouldCreateMetadataRadio() {
		return true;
	}

	protected boolean shouldCreateCustomRadio() {
		return true;
	}

	protected boolean shouldCreateServerRadio() {
		return true;
	}

	protected boolean shouldEnableMetadataRadio() {
		if( !shouldCreateMetadataRadio())
			return false;
		
		String mode = ServerProfileModel.getProfile(getPage().getServer());
		if(!ServerProfileModel.DEFAULT_SERVER_PROFILE.equals(mode))
			return false;
		IServer s = getPage().getServer().getOriginal();
		ServerExtendedProperties props = (ServerExtendedProperties)s.loadAdapter(ServerExtendedProperties.class, null);
		if( props == null )
			return true;
		return props.getMultipleDeployFolderSupport() != ServerExtendedProperties.DEPLOYMENT_SCANNER_NO_SUPPORT;
	}
	
	protected boolean showTempDeployText() {
		return true;
	}
	protected boolean showDeployText() {
		return true;
	}
	
	/*
	 * Subclasses may override to change text strings
	 */
	protected Section createSection(FormToolkit toolkit, Composite parent) {
		Section section = toolkit.createSection(parent,
				ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED
						| ExpandableComposite.TITLE_BAR);
		section.setText(Messages.swf_DeployEditorHeading);
		section.setToolTipText(Messages.swf_DeploymentDescription);
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_FILL));
		return section;
	}
	
	/*
	 * Subclasses may override to change text labels
	 */
	protected Control createDescriptionLabel(FormToolkit toolkit, Composite composite) {
		Label descriptionLabel = toolkit.createLabel(composite,
				Messages.swf_DeploymentDescriptionLabel);
		descriptionLabel.setToolTipText(Messages.swf_DeploymentDescription);
		return descriptionLabel;
	}
	
	/*
	 * This creates the top half of the page. Specifically, 
	 * the deploy and tmp deploy fields, the radios (metadata, server, custom), 
	 * the 'should zip' checkbox, etc. 
	 */
	
	protected Composite createDefaultComposite(Composite parent) {
		lastWC = getPage().getServer();
		lastWC.addPropertyChangeListener(this);

		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		Section section = createSection(toolkit, parent);
		Composite composite = toolkit.createComposite(section);
		composite.setLayout(new FormLayout());

		createDefaultCompositeContents(toolkit, composite);

		toolkit.paintBordersFor(composite);
		section.setClient(composite);
		getPage().getSaveStatus();
		updateWidgets();
		return section;
	}
	
	protected void createDefaultCompositeContents(FormToolkit toolkit, Composite composite) {
		Control top = createDescriptionLabel(toolkit, composite);
		if( top != null ) {
			FormData fd1 = new FormData();
			fd1.left = new FormAttachment(0, 5);
			fd1.top = new FormAttachment(0, 5);
			top.setLayoutData(fd1);
		}
		
		if( getShowRadios() ) {
			Composite inner = addRadios(toolkit, composite);
			FormData radios = new FormData();
			radios.top = new FormAttachment(top, 5);
			radios.left = new FormAttachment(0, 5);
			radios.right = new FormAttachment(100, -5);
			inner.setLayoutData(radios);
			top = inner;
		}
		
		if( showDeployText()) {
			top = addDeployText(toolkit, composite, top);
		}
		if( showTempDeployText()) {
			top = addTempDeployText(toolkit, composite, top);
		}
		
		if( showZipWidgets()) {
			addZipWidgets(toolkit, composite, top);
		}
	}
	
	protected boolean showZipWidgets() {
		return true;
	}
	
	protected Control addZipWidgets(FormToolkit toolkit, Composite composite, Control top) {
		zipDeployWTPProjects = toolkit.createButton(composite,
				Messages.EditorZipDeployments, SWT.CHECK);
		boolean zippedPublisherAvailable = isZippedPublisherAvailable(); 
		boolean value = getServer().zipsWTPDeployments();
		zipDeployWTPProjects.setEnabled(zippedPublisherAvailable);
		zipDeployWTPProjects.setSelection(zippedPublisherAvailable && value);

		FormData zipButtonData = new FormData();
		zipButtonData.right = new FormAttachment(100, -5);
		zipButtonData.left = new FormAttachment(0, 5);
		zipButtonData.top = new FormAttachment(top, 5);
		zipDeployWTPProjects.setLayoutData(zipButtonData);

		zipListener = new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				getPage().execute(new SetZipCommand());
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		};
		zipDeployWTPProjects.addSelectionListener(zipListener);
		return zipDeployWTPProjects;
	}
	
	protected Composite addRadios(FormToolkit toolkit, Composite parent) {
		radioListener = new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				radioSelected(e.getSource());
			}
		};

		Composite inner = toolkit.createComposite(parent);
		inner.setLayout(new GridLayout(1, false));

		if( shouldCreateMetadataRadio() ) {
			metadataRadio = toolkit.createButton(inner,
					Messages.EditorUseWorkspaceMetadata, SWT.RADIO);
			metadataRadio.addSelectionListener(radioListener);
			metadataRadio.setEnabled(shouldEnableMetadataRadio());
			enableDisableWidgets.add(metadataRadio);
		}
		if( shouldCreateServerRadio()) {
			serverRadio = toolkit.createButton(inner,
					Messages.EditorUseServersDeployFolder, SWT.RADIO);
			serverRadio.addSelectionListener(radioListener);
			enableDisableWidgets.add(serverRadio);
		}
		if( shouldCreateCustomRadio()) {
			customRadio = toolkit.createButton(inner,
					Messages.EditorUseCustomDeployFolder, SWT.RADIO);
			customRadio.addSelectionListener(radioListener);
			enableDisableWidgets.add(customRadio);
		}
		return inner;
	}
	
	@Deprecated
	protected Control addTempAndDeployTexts(FormToolkit toolkit, Composite parent, Control top) {
		Control top2 = addDeployText(toolkit, parent, top);
		return addTempDeployText(toolkit, parent, top2);
	}
	
	protected Control addTempDeployText(FormToolkit toolkit, Composite parent, Control top) {
		
		Label tempDeployLabel = toolkit.createLabel(parent,
				Messages.swf_TempDeployDirectory);
		tempDeployLabel.setForeground(toolkit.getColors().getColor(
				IFormColors.TITLE));

		tempDeployText = toolkit.createText(parent, getTempDeployDir(), SWT.BORDER);
		enableDisableWidgets.add(tempDeployText);

		tempDeployListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getPage().execute(new SetTempDeployDirCommand());
			}
		};
		tempDeployText.addModifyListener(tempDeployListener);

		tempDeployButton = toolkit.createButton(parent, Messages.browse,SWT.PUSH);
		enableDisableWidgets.add(tempDeployButton);

		tempDeployButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				String x = openBrowseDialog(tempDeployText.getText());
				if (x != null)
					tempDeployText.setText(makeRelative(x));
			}
		});

		// second row
		FormData tempLabelData = new FormData();
		tempLabelData.left = new FormAttachment(0, 5);
		tempLabelData.right = new FormAttachment(top, -5);
		tempLabelData.top = new FormAttachment(top, 5);
		tempDeployLabel.setLayoutData(tempLabelData);

		FormData tempTextData = new FormData();
		tempTextData.left = new FormAttachment(tempDeployButton, -305);
		tempTextData.top = new FormAttachment(top, 5);
		tempTextData.right = new FormAttachment(tempDeployButton, -5);
		tempDeployText.setLayoutData(tempTextData);

		FormData tempButtonData = new FormData();
		tempButtonData.right = new FormAttachment(100, -5);
		tempButtonData.left = new FormAttachment(100, -100);
		tempButtonData.top = new FormAttachment(top, 5);
		tempDeployButton.setLayoutData(tempButtonData);
		return tempDeployText;		
	}
	protected Control addDeployText(FormToolkit toolkit, Composite parent, Control top) {
	
		Label label = toolkit.createLabel(parent,
				Messages.swf_DeployDirectory);
		label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		
		deployText = toolkit.createText(parent, getDeployDir(), SWT.BORDER);
		enableDisableWidgets.add(deployText);
		deployListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getPage().execute(new SetDeployDirCommand());
			}
		};
		deployText.addModifyListener(deployListener);

		deployButton = toolkit.createButton(parent, Messages.browse, SWT.PUSH);
		enableDisableWidgets.add(deployButton);

		deployButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				String x = openBrowseDialog(deployText.getText());
				if (x != null) {
					deployText.setText(makeRelative(x));
				}
			}
		});

		// first row
		FormData labelData = new FormData();
		labelData.left = new FormAttachment(0, 5);
		labelData.right = new FormAttachment(deployText, -5);
		labelData.top = new FormAttachment(top, 5);
		label.setLayoutData(labelData);

		FormData textData = new FormData();
		textData.left = new FormAttachment(deployButton, -305);
		textData.top = new FormAttachment(top, 5);
		textData.right = new FormAttachment(deployButton, -5);
		deployText.setLayoutData(textData);

		FormData buttonData = new FormData();
		buttonData.right = new FormAttachment(100, -5);
		buttonData.left = new FormAttachment(100, -100);
		buttonData.top = new FormAttachment(top, 2);
		deployButton.setLayoutData(buttonData);

		return deployText;
	}
	
	protected boolean getShowRadios() {
		return true;
	}
		
	private void updateWidgets() {
		if( getShowRadios()) {
			if( shouldCreateMetadataRadio())
				metadataRadio.setSelection(getDeployType().equals(
						IDeployableServer.DEPLOY_METADATA));
			if( shouldCreateServerRadio())
				serverRadio.setSelection(getDeployType().equals(
						IDeployableServer.DEPLOY_SERVER));
			if( shouldCreateCustomRadio())
				customRadio.setSelection(getDeployType().equals(
						IDeployableServer.DEPLOY_CUSTOM));
			
			currentRadioSelection = metadataRadio != null && metadataRadio.getSelection() ? metadataRadio
					: serverRadio != null && serverRadio.getSelection() ? serverRadio : customRadio;
		}
		
		
		if(showDeployText() || showTempDeployText()) {
			try {
				IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(getPage().getServer());
				IDeploymentOptionsController deployOpts = (IDeploymentOptionsController)beh.getWorkingCopyController(IDeploymentOptionsController.SYSTEM_ID, getHelper().getWorkingCopy());
				
				if( showDeployText()) {
					String newDir = deployOpts.getDeploymentsRootFolder(false);
					deployText.removeModifyListener(deployListener);
					if( !deployText.getText().equals(newDir))
						deployText.setText(newDir);
					deployText.addModifyListener(deployListener);
					deployText.setEnabled(shouldEnableControl(deployText));
					deployButton.setEnabled(shouldEnableControl(deployButton));
				}
				
				if( showTempDeployText()) {
					String newTemp = deployOpts.getDeploymentsTemporaryFolder(false);
					tempDeployText.removeModifyListener(tempDeployListener);
					if( !tempDeployText.getText().equals(newTemp))
						tempDeployText.setText(newTemp);
					tempDeployText.addModifyListener(tempDeployListener);
					
					tempDeployText.setEnabled(shouldEnableControl(tempDeployText));
					tempDeployButton.setEnabled(shouldEnableControl(tempDeployButton));
				}
			} catch(CoreException ce) {
				JBossServerUIPlugin.log(ce.getStatus());
			}
		}
	}
	
	public void radioSelected(Object c) {
		if (c == currentRadioSelection)
			return; // do nothing
		getPage().execute(new RadioClickedCommand((Button)c, currentRadioSelection));
		currentRadioSelection = (Button)c;
	}
	
	protected boolean isZippedPublisherAvailable() {
		// Zipped publisher is always available after rewrite.
		// ASTools now depends on archives and doesn't need to do weird logic
		// to find a zipped publisher
		return true;
	}
	
	public class SetDeployDirCommand extends ServerCommand {
		private String oldDir;
		private String newDir;
		private Text text;
		private ModifyListener listener;
		public SetDeployDirCommand() {
			super(getPage().getServer(), Messages.EditorSetDeployLabel);
			this.text = deployText;
			this.newDir = deployText.getText();
			this.listener = deployListener;
			this.oldDir = getHelper().getAttribute(IDeployableServer.DEPLOY_DIRECTORY, ""); //$NON-NLS-1$
		}
		public void execute() {
			getHelper().setAttribute(IDeployableServer.DEPLOY_DIRECTORY, newDir);
			lastCustomDeploy = newDir;
			updateWidgets();
			getPage().getSaveStatus();
		}
		public void undo() {
			getHelper().setAttribute(IDeployableServer.DEPLOY_DIRECTORY, oldDir);
			updateWidgets();
			getPage().getSaveStatus();
		}
	}

	public class SetZipCommand extends ServerCommand {
		boolean oldVal;
		boolean newVal;
		public SetZipCommand() {
			super(getPage().getServer(), Messages.EditorZipDeployments);
			oldVal = getHelper().getAttribute(IDeployableServer.ZIP_DEPLOYMENTS_PREF, false);
			newVal = zipDeployWTPProjects.getSelection();
		}
		public void execute() {
			getHelper().setAttribute(IDeployableServer.ZIP_DEPLOYMENTS_PREF, newVal);
			getPage().getSaveStatus();
		}
		public void undo() {
			zipDeployWTPProjects.removeSelectionListener(zipListener);
			zipDeployWTPProjects.setSelection(oldVal);
			getHelper().setAttribute(IDeployableServer.ZIP_DEPLOYMENTS_PREF, oldVal);
			zipDeployWTPProjects.addSelectionListener(zipListener);
			getPage().getSaveStatus();
		}
	}
	
	public class SetTempDeployDirCommand extends ServerCommand {
		private String oldDir;
		private String newDir;
		private Text text;
		private ModifyListener listener;
		public SetTempDeployDirCommand() {
			super(getPage().getServer(), Messages.EditorSetTempDeployLabel);
			text = tempDeployText;
			newDir = tempDeployText.getText();
			listener = tempDeployListener;
			oldDir = getHelper().getAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, ""); //$NON-NLS-1$
		}
		public void execute() {
			getHelper().setAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, newDir);
			lastCustomTemp = newDir;
			updateWidgets();
			getPage().getSaveStatus();
		}
		public void undo() {
			getHelper().setAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, oldDir);
			updateWidgets();
			getPage().getSaveStatus();
		}
	}
	
	public class RadioClickedCommand extends ServerCommand {
		private Button newSelection, oldSelection;
		private String oldDir, newDir;
		private String oldTemp, newTemp;
		private String id;
		public RadioClickedCommand(Button clicked, Button previous) {
			super(getPage().getServer(), Messages.EditorSetRadioClicked);
			newSelection = clicked;
			oldSelection = previous;
			id = server.getId();
		}
		public void execute() {
			oldDir = deployText == null ? null : deployText.getText();
			oldTemp = tempDeployText == null ? null : tempDeployText.getText();
			String newType = newSelection == customRadio ? IDeployableServer.DEPLOY_CUSTOM :
	 			newSelection == serverRadio ? IDeployableServer.DEPLOY_SERVER :
	 				IDeployableServer.DEPLOY_METADATA;
			discoverNewFolders();
			ServerAttributeHelper helper = getHelper();
			helper.setAttribute(IDeployableServer.DEPLOY_DIRECTORY, newDir);
			helper.setAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, newTemp);
			helper.setAttribute(IDeployableServer.DEPLOY_DIRECTORY_TYPE, newType);
			updateWidgets();
			getPage().getSaveStatus();
		}
		
		protected void handleMetadataRadioSelected() {
			newDir = JBossServerCorePlugin.getServerStateLocation(id)
					.append(IJBossRuntimeResourceConstants.DEPLOY).makeAbsolute().toString();
			newTemp = JBossServerCorePlugin.getServerStateLocation(id)
					.append(IJBossToolingConstants.TEMP_DEPLOY).makeAbsolute().toString();
			new File(newDir).mkdirs();
			new File(newTemp).mkdirs();
		}
		
		protected void handleServerRadioSelected() {
			if( server.getRuntime() != null && 
					server.getRuntime().loadAdapter(IJBossServerRuntime.class, null) != null) {
				newDir = getServerRadioNewDeployDir();
				newTemp = getServerRadioNewTempDeployDir();

				String mode = ServerProfileModel.getProfile(getPage().getServer());
				if(ServerProfileModel.DEFAULT_SERVER_PROFILE.equals(mode))
					new File(newTemp).mkdirs();
			}
		}
		
		protected String getServerRadioNewDeployDir() {
			IServerModeDetails det = (IServerModeDetails)Platform.getAdapterManager().getAdapter(server, IServerModeDetails.class); 
			if( det == null )
				return "";
			String s = det.getProperty(det.PROP_SERVER_DEPLOYMENTS_FOLDER_REL);
			return s;
		}
		
		protected String getServerRadioNewTempDeployDir() {
			IServerModeDetails det = (IServerModeDetails)Platform.getAdapterManager().getAdapter(server, IServerModeDetails.class); 
			if( det == null )
				return "";
			String s = det.getProperty(det.PROP_SERVER_TMP_DEPLOYMENTS_FOLDER_REL);
			return s;
		}
		
		protected void discoverNewFolders() {
			// Discover the new folders
			if( newSelection == metadataRadio  ) {
				handleMetadataRadioSelected();
			} else if( newSelection == serverRadio ) {
				handleServerRadioSelected();
			} else {
				newDir = lastCustomDeploy;
				newTemp = lastCustomTemp;
			}
			newDir = newDir == null ? oldDir : newDir;
			newTemp = newTemp == null ? oldTemp : newTemp; 
		}
		
		public void undo() {
			String oldType = oldSelection == customRadio ? IDeployableServer.DEPLOY_CUSTOM :
	 			oldSelection == serverRadio ? IDeployableServer.DEPLOY_SERVER :
	 				IDeployableServer.DEPLOY_METADATA;
			getHelper().setAttribute(IDeployableServer.DEPLOY_DIRECTORY, oldDir);
			getHelper().setAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, oldTemp);
			getHelper().setAttribute(IDeployableServer.DEPLOY_DIRECTORY_TYPE, oldType);
			updateWidgets();
			getPage().getSaveStatus();
		}
	}
	
	private String getDeployType() {
		return getServer().getDeployLocationType();
	}

	private String getDeployDir() {
		if( getPage().getServer().getRuntime() == null )
			return "";//$NON-NLS-1$
		return makeRelative(getServer().getDeployFolder(), 
				getPage().getServer().getRuntime());
	}

	private String getTempDeployDir() {
		if( getPage().getServer().getRuntime() == null)
			return "";//$NON-NLS-1$
		return 
			makeRelative(getServer().getTempDeployFolder(), 
					getPage().getServer().getRuntime());
	}

	private IDeployableServer getServer() {
		return (IDeployableServer) getPage().getServer().loadAdapter(
				IDeployableServer.class, new NullProgressMonitor());
	}
	
	public void updateListeners() {
		// server has been saved. Remove property change listener from last wc and add to newest
		if( lastWC != null )
			lastWC.removePropertyChangeListener(this);
		lastWC = getPage().getServer();
		if( lastWC != null )
			lastWC.addPropertyChangeListener(this);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if( getShowRadios() && ServerProfileModel.isProfileKey(evt.getPropertyName())) { 
			if( shouldCreateMetadataRadio() ) {
				metadataRadio.setEnabled(shouldEnableMetadataRadio());
				if(!metadataRadio.isEnabled() && metadataRadio.getSelection()) {
					getPage().execute(new RadioClickedCommand(serverRadio, currentRadioSelection));
				}
			}
		} 
		updateWidgets();
		
	}
	
	
	public static String getDefaultOutputName(IModule module) {
		String lastSegment = new Path(module.getName()).lastSegment();
		String suffix = PublishUtil.getSuffix(module.getModuleType().getId());
		String ret = lastSegment.endsWith(suffix) ? lastSegment : lastSegment + suffix;
		return  ret;
	}
	
	public void setEnabled(boolean enabled) {
		Iterator<Control> i = enableDisableWidgets.iterator();
		while(i.hasNext()) {
			Control c = i.next();
			if( shouldEnableControl(c)) {
				c.setEnabled(enabled);
			}
		}
	}
	
	/*
	 * This method may be overridden with specific logic for your list of radios
	 */
	protected boolean shouldEnableControl(Control c) {
		if( c == null || c.isDisposed())
			return false;
		
		if( c == deployText || c == tempDeployText || c == deployButton || c == tempDeployButton)
			return getDeployType().equals(IDeployableServer.DEPLOY_CUSTOM);
		if( c == metadataRadio)
			return shouldEnableMetadataRadio();
		return true;
	}
	
	
	private String makeRelative(String path) {
		return makeRelative(path, getPage().getServer().getRuntime());
	}
	
	private static String makeRelative(String path, IRuntime runtime) {
		return ServerUtil.makeRelative(runtime, new Path(path)).toString();
	}
	
	public IStatus[] validate() {
		return new IStatus[0];
	}
}
