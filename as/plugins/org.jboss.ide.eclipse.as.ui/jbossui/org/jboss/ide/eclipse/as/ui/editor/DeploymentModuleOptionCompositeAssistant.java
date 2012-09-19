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
package org.jboss.ide.eclipse.as.ui.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.ServerUICore;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentModulePrefs;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentPreferences;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.UIUtil;

public class DeploymentModuleOptionCompositeAssistant implements PropertyChangeListener {
	public static interface IDeploymentPageCallback {
		public void propertyChange(PropertyChangeEvent evt, DeploymentModuleOptionCompositeAssistant composite);
		public String getServerLocation(IServerWorkingCopy wc);
		public String getServerConfigName(IServerWorkingCopy wc);
	}
	
	private static HashMap<String, IDeploymentPageCallback> callbackMappings;
	static {
		callbackMappings = new HashMap<String, IDeploymentPageCallback>();
		callbackMappings.put(LocalPublishMethod.LOCAL_PUBLISH_METHOD, new LocalDeploymentPageCallback());
	}
	
	public static void addMapping(String mode, IDeploymentPageCallback callback) {
		callbackMappings.put(mode, callback);
	}

	
	public static class LocalDeploymentPageCallback implements IDeploymentPageCallback {
		public String getServerLocation(IServerWorkingCopy wc) {
			IJBossServerRuntime jbsrt = (IJBossServerRuntime)wc.getRuntime().loadAdapter(IJBossServerRuntime.class, null);
			return jbsrt.getConfigLocation();
		}

		public String getServerConfigName(IServerWorkingCopy wc) {
			IJBossServerRuntime jbsrt = (IJBossServerRuntime)wc.getRuntime().loadAdapter(IJBossServerRuntime.class, null);
			return jbsrt.getJBossConfiguration();
		}

		public void propertyChange(PropertyChangeEvent evt,
				DeploymentModuleOptionCompositeAssistant composite) {
			// TODO Auto-generated method stub
			
		}
	}
	

	public static interface IBrowseBehavior {
		public String openBrowseDialog(ModuleDeploymentPage page, String original);
	}
	public static HashMap<String, IBrowseBehavior> browseBehaviorMap = new HashMap<String, IBrowseBehavior>();
	static {
		browseBehaviorMap.put(LocalPublishMethod.LOCAL_PUBLISH_METHOD, new IBrowseBehavior() { 
			public String openBrowseDialog(ModuleDeploymentPage page, String original) {
				DirectoryDialog d = new DirectoryDialog(new Shell());
				d.setFilterPath(page.makeGlobal(original));
				return d.open();
			} 
		});
	}
		
	protected String openBrowseDialog(String original) {
		String mode = getServer().getAttributeHelper().getAttribute(IDeployableServer.SERVER_MODE, LocalPublishMethod.LOCAL_PUBLISH_METHOD);
		IBrowseBehavior beh = browseBehaviorMap.get(mode);
		if( beh == null )
			beh = browseBehaviorMap.get(LocalPublishMethod.LOCAL_PUBLISH_METHOD); 
		return beh.openBrowseDialog(page, original);
	}

	// Combo strings - TODO extract to messages
	private static final String ALL = "All";
	private static final String DEPLOYED = "Deployed";
	private static final String BY_MODNAME = "By Module Name";
	private static final String BY_MODTYPE = "By Module Type";
	
	
	protected static final String COLUMN_NAME = IJBossToolingConstants.LOCAL_DEPLOYMENT_NAME;
	protected static final String COLUMN_LOC = IJBossToolingConstants.LOCAL_DEPLOYMENT_LOC;
	protected static final String COLUMN_TEMP_LOC = IJBossToolingConstants.LOCAL_DEPLOYMENT_TEMP_LOC;
	protected static final String OUTPUT_NAME = IJBossToolingConstants.LOCAL_DEPLOYMENT_OUTPUT_NAME;
	protected static final String MAIN = LocalPublishMethod.LOCAL_PUBLISH_METHOD;
	private ModuleDeploymentPage page;
	private DeploymentPreferences preferences;
	private TreeViewer viewer;
	private Text deployText, tempDeployText;
	private Button metadataRadio, serverRadio, customRadio, currentSelection;
	private Button deployButton, tempDeployButton;
	private ModifyListener deployListener, tempDeployListener;
	private SelectionListener radioListener, zipListener;
	private Button zipDeployWTPProjects;
	private String lastCustomDeploy, lastCustomTemp;
	
	private Combo filterCombo; 
	private Text filterText;
	

	
	private IServerWorkingCopy lastWC;
	
	public DeploymentModuleOptionCompositeAssistant() {
	}

	public ModuleDeploymentPage getPage() {
		return page;
	}
	
	public void setDeploymentPage(ModuleDeploymentPage page) {
		this.page = page;
	}

	public void setDeploymentPrefs(DeploymentPreferences prefs) {
		this.preferences = prefs;
	}

	protected ServerAttributeHelper getHelper() {
		return page.getHelper();
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
		return currentSelection;
	}
	
	public boolean showMetadataRadio() {
		return true;
	}

	public boolean showCustomRadio() {
		return true;
	}

	public boolean showServerRadio() {
		return true;
	}

	public boolean enableMetadataRadio() {
		String mode = getHelper().getAttribute(IDeployableServer.SERVER_MODE, LocalPublishMethod.LOCAL_PUBLISH_METHOD); 
		if(!LocalPublishMethod.LOCAL_PUBLISH_METHOD.equals(mode))
			return false;
		IServer s = page.getServer().getOriginal();
		ServerExtendedProperties props = (ServerExtendedProperties)s.loadAdapter(ServerExtendedProperties.class, null);
		if( props == null )
			return true;
		return props.getMultipleDeployFolderSupport() != ServerExtendedProperties.DEPLOYMENT_SCANNER_NO_SUPPORT;
	}
	
	public boolean showTempAndDeployTexts() {
		return true;
	}

	protected Composite createDefaultComposite(Composite parent) {

		FormToolkit toolkit = new FormToolkit(parent.getDisplay());

		Section section = toolkit.createSection(parent,
				ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED
						| ExpandableComposite.TITLE_BAR);
		section.setText(Messages.swf_DeployEditorHeading);
		section.setToolTipText(Messages.swf_DeploymentDescription);
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_FILL));

		Composite composite = toolkit.createComposite(section);

		composite.setLayout(new FormLayout());

		Label descriptionLabel = toolkit.createLabel(composite,
				Messages.swf_DeploymentDescriptionLabel);
		descriptionLabel.setToolTipText(Messages.swf_DeploymentDescription);
		Control top = descriptionLabel;
		FormData descriptionLabelData = new FormData();
		descriptionLabelData.left = new FormAttachment(0, 5);
		descriptionLabelData.top = new FormAttachment(0, 5);
		descriptionLabel.setLayoutData(descriptionLabelData);
		if( getShowRadios() ) {
			Composite inner = addRadios(toolkit, composite);
			FormData radios = new FormData();
			radios.top = new FormAttachment(descriptionLabel, 5);
			radios.left = new FormAttachment(0, 5);
			radios.right = new FormAttachment(100, -5);
			inner.setLayoutData(radios);
			top = inner;
		}
		lastWC = page.getServer();
		lastWC.addPropertyChangeListener(this);
		
		if( showTempAndDeployTexts() ) {
			top = addTempAndDeployTexts(toolkit, composite, top);
		}
		
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
				page.execute(new SetZipCommand());
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		};
		zipDeployWTPProjects.addSelectionListener(zipListener);

		toolkit.paintBordersFor(composite);
		section.setClient(composite);
		page.getSaveStatus();
		updateWidgets();
		return section;
	}
	
	protected Composite addRadios(FormToolkit toolkit, Composite parent) {
		Composite inner = toolkit.createComposite(parent);
		inner.setLayout(new GridLayout(1, false));

		if( showMetadataRadio() )
			metadataRadio = toolkit.createButton(inner,
					Messages.EditorUseWorkspaceMetadata, SWT.RADIO);
		if( showServerRadio())
			serverRadio = toolkit.createButton(inner,
					Messages.EditorUseServersDeployFolder, SWT.RADIO);
		if( showCustomRadio())
			customRadio = toolkit.createButton(inner,
					Messages.EditorUseCustomDeployFolder, SWT.RADIO);

		radioListener = new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				radioSelected(e.getSource());
			}
		};
		if( showMetadataRadio())
			metadataRadio.addSelectionListener(radioListener);
		if( showServerRadio())
			serverRadio.addSelectionListener(radioListener);
		if( showCustomRadio()) 
			customRadio.addSelectionListener(radioListener);
		
		metadataRadio.setEnabled(enableMetadataRadio());
		return inner;
	}
	
	protected Control addTempAndDeployTexts(FormToolkit toolkit, Composite parent, Control top) {
		Label label = toolkit.createLabel(parent,
				Messages.swf_DeployDirectory);
		label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		
		deployText = toolkit.createText(parent, getDeployDir(), SWT.BORDER);
		deployListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				page.execute(new SetDeployDirCommand());
			}
		};
		deployText.addModifyListener(deployListener);

		deployButton = toolkit.createButton(parent, Messages.browse,
				SWT.PUSH);
		deployButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				String x = openBrowseDialog(deployText.getText());
				if (x != null) {
					deployText.setText(page.makeRelative(x));
				}
			}
		});

		Label tempDeployLabel = toolkit.createLabel(parent,
				Messages.swf_TempDeployDirectory);
		tempDeployLabel.setForeground(toolkit.getColors().getColor(
				IFormColors.TITLE));

		tempDeployText = toolkit.createText(parent, getTempDeployDir(),
				SWT.BORDER);
		tempDeployListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				page.execute(new SetTempDeployDirCommand());
			}
		};
		tempDeployText.addModifyListener(tempDeployListener);

		tempDeployButton = toolkit.createButton(parent, Messages.browse,
				SWT.PUSH);
		tempDeployButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				String x = openBrowseDialog(tempDeployText.getText());
				if (x != null)
					tempDeployText.setText(page.makeRelative(x));
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

		// second row
		FormData tempLabelData = new FormData();
		tempLabelData.left = new FormAttachment(0, 5);
		tempLabelData.right = new FormAttachment(deployText, -5);
		tempLabelData.top = new FormAttachment(deployText, 5);
		tempDeployLabel.setLayoutData(tempLabelData);

		FormData tempTextData = new FormData();
		tempTextData.left = new FormAttachment(tempDeployButton, -305);
		tempTextData.top = new FormAttachment(deployText, 5);
		tempTextData.right = new FormAttachment(tempDeployButton, -5);
		tempDeployText.setLayoutData(tempTextData);

		FormData tempButtonData = new FormData();
		tempButtonData.right = new FormAttachment(100, -5);
		tempButtonData.left = new FormAttachment(100, -100);
		tempButtonData.top = new FormAttachment(deployText, 5);
		tempDeployButton.setLayoutData(tempButtonData);
		return tempDeployText;
	}
	
	protected boolean getShowRadios() {
		IRuntime rt = getServer().getServer().getRuntime();
		boolean showRadios = true;
		if( rt == null || rt.loadAdapter(IJBossServerRuntime.class, null) == null)
			showRadios = false;
		return showRadios;
	}
		
	private void updateWidgets() {
		if( getShowRadios()) {
			if( showMetadataRadio())
				metadataRadio.setSelection(getDeployType().equals(
						IDeployableServer.DEPLOY_METADATA));
			if( showServerRadio())
				serverRadio.setSelection(getDeployType().equals(
						IDeployableServer.DEPLOY_SERVER));
			if( showCustomRadio())
				customRadio.setSelection(getDeployType().equals(
						IDeployableServer.DEPLOY_CUSTOM));
			
			currentSelection = metadataRadio != null && metadataRadio.getSelection() ? metadataRadio
					: serverRadio != null && serverRadio.getSelection() ? serverRadio : customRadio;
		}
		
		
		if(showTempAndDeployTexts()) {
			IJBossServer jbs = ServerConverter.getJBossServer(page.getServer().getOriginal());
			String newDir = getHelper().getAttribute(IDeployableServer.DEPLOY_DIRECTORY, 
					jbs == null ? "" : jbs.getDeployFolder()); //$NON-NLS-1$
			String newTemp = getHelper().getAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, 
					jbs == null ? "" : jbs.getTempDeployFolder()); //$NON-NLS-1$
			newDir = ServerUtil.makeRelative(page.getServer().getRuntime(), new Path(newDir)).toString();
			newTemp = ServerUtil.makeRelative(page.getServer().getRuntime(), new Path(newTemp)).toString();
			deployText.removeModifyListener(deployListener);
			if( !deployText.getText().equals(newDir))
				deployText.setText(newDir);
			deployText.addModifyListener(deployListener);
			tempDeployText.removeModifyListener(tempDeployListener);
			if( !tempDeployText.getText().equals(newTemp))
				tempDeployText.setText(newTemp);
			tempDeployText.addModifyListener(tempDeployListener);
			
			deployText.setEnabled(getDeployType().equals(IDeployableServer.DEPLOY_CUSTOM));
			tempDeployText.setEnabled(getDeployType().equals(IDeployableServer.DEPLOY_CUSTOM));
			deployButton.setEnabled(getDeployType().equals(IDeployableServer.DEPLOY_CUSTOM));
			tempDeployButton.setEnabled(getDeployType().equals(IDeployableServer.DEPLOY_CUSTOM));
		}
	}
	
	public void radioSelected(Object c) {
		if (c == currentSelection)
			return; // do nothing
		page.execute(new RadioClickedCommand((Button)c, currentSelection));
		currentSelection = (Button)c;
	}
	
	protected boolean isZippedPublisherAvailable() {
		/*
		 * Maybe use IJBossServerPublishMethodType type = DeploymentPreferenceLoader.getCurrentDeploymentMethodType(getServer());
		 * But this class has no reference to the server, and it also might not want to go by stored data,
		 * but rather the combo in the ModuleDeploymentPage somehow? 
		 */

		// String method = DeploymentPreferenceLoader.getCurrentDeploymentMethodType(getServer()).getId();
		String method = LocalPublishMethod.LOCAL_PUBLISH_METHOD;
		IJBossServerPublisher[] publishers = 
			ExtensionManager.getDefault().getZippedPublishers();
		for( int i = 0; i < publishers.length; i++ ) {
			if( publishers[i].accepts(method, getServer().getServer(), null))
				return true;
		}
		return false;
	}
	
	public class SetDeployDirCommand extends ServerCommand {
		private String oldDir;
		private String newDir;
		private Text text;
		private ModifyListener listener;
		public SetDeployDirCommand() {
			super(page.getServer(), Messages.EditorSetDeployLabel);
			this.text = deployText;
			this.newDir = deployText.getText();
			this.listener = deployListener;
			this.oldDir = getHelper().getAttribute(IDeployableServer.DEPLOY_DIRECTORY, ""); //$NON-NLS-1$
		}
		public void execute() {
			getHelper().setAttribute(IDeployableServer.DEPLOY_DIRECTORY, newDir);
			lastCustomDeploy = newDir;
			updateWidgets();
			page.getSaveStatus();
		}
		public void undo() {
			getHelper().setAttribute(IDeployableServer.DEPLOY_DIRECTORY, oldDir);
			updateWidgets();
			page.getSaveStatus();
		}
	}

	public class SetZipCommand extends ServerCommand {
		boolean oldVal;
		boolean newVal;
		public SetZipCommand() {
			super(page.getServer(), Messages.EditorZipDeployments);
			oldVal = getHelper().getAttribute(IDeployableServer.ZIP_DEPLOYMENTS_PREF, false);
			newVal = zipDeployWTPProjects.getSelection();
		}
		public void execute() {
			getHelper().setAttribute(IDeployableServer.ZIP_DEPLOYMENTS_PREF, newVal);
			page.getSaveStatus();
		}
		public void undo() {
			zipDeployWTPProjects.removeSelectionListener(zipListener);
			zipDeployWTPProjects.setSelection(oldVal);
			getHelper().setAttribute(IDeployableServer.ZIP_DEPLOYMENTS_PREF, oldVal);
			zipDeployWTPProjects.addSelectionListener(zipListener);
			page.getSaveStatus();
		}
	}
	
	public class SetTempDeployDirCommand extends ServerCommand {
		private String oldDir;
		private String newDir;
		private Text text;
		private ModifyListener listener;
		public SetTempDeployDirCommand() {
			super(page.getServer(), Messages.EditorSetTempDeployLabel);
			text = tempDeployText;
			newDir = tempDeployText.getText();
			listener = tempDeployListener;
			oldDir = getHelper().getAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, ""); //$NON-NLS-1$
		}
		public void execute() {
			getHelper().setAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, newDir);
			lastCustomTemp = newDir;
			updateWidgets();
			page.getSaveStatus();
		}
		public void undo() {
			getHelper().setAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, oldDir);
			updateWidgets();
			page.getSaveStatus();
		}
	}
	
	public class RadioClickedCommand extends ServerCommand {
		private Button newSelection, oldSelection;
		private String oldDir, newDir;
		private String oldTemp, newTemp;
		private String id;
		public RadioClickedCommand(Button clicked, Button previous) {
			super(page.getServer(), Messages.EditorSetRadioClicked);
			newSelection = clicked;
			oldSelection = previous;
			id = server.getId();
		}
		public void execute() {
			oldDir = deployText.getText();
			oldTemp = tempDeployText.getText();
			String newType = newSelection == customRadio ? IDeployableServer.DEPLOY_CUSTOM :
	 			newSelection == serverRadio ? IDeployableServer.DEPLOY_SERVER :
	 				IDeployableServer.DEPLOY_METADATA;
			discoverNewFolders();
			ServerAttributeHelper helper = getHelper();
			helper.setAttribute(IDeployableServer.DEPLOY_DIRECTORY, newDir);
			helper.setAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, newTemp);
			helper.setAttribute(IDeployableServer.DEPLOY_DIRECTORY_TYPE, newType);
			updateWidgets();
			page.getSaveStatus();
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
				String mode = getHelper().getAttribute(IDeployableServer.SERVER_MODE, LocalPublishMethod.LOCAL_PUBLISH_METHOD); 
				newDir = getServerRadioNewDeployDir();
				newTemp = getServerRadioNewTempDeployDir();
				if( mode.equals(LocalPublishMethod.LOCAL_PUBLISH_METHOD))
					new File(newTemp).mkdirs();
			}
		}
		
		protected String getServerRadioNewDeployDir() {
			String mode = getHelper().getAttribute(IDeployableServer.SERVER_MODE, LocalPublishMethod.LOCAL_PUBLISH_METHOD); 
			if( ServerUtil.isJBoss7(page.getServer().getServerType())) {
				return new Path(IJBossRuntimeResourceConstants.AS7_STANDALONE)
				.append(IJBossRuntimeResourceConstants.AS7_DEPLOYMENTS)
				.makeRelative().toString();
			} else {
				IDeploymentPageCallback cb = callbackMappings.get(mode);
				String loc = cb.getServerLocation(page.getServer());
				String config = cb.getServerConfigName(page.getServer());
				return new Path(loc)
					.append(config)
					.append(IJBossRuntimeResourceConstants.DEPLOY).toString();
			}
		}
		
		protected String getServerRadioNewTempDeployDir() {
			String mode = getHelper().getAttribute(IDeployableServer.SERVER_MODE, LocalPublishMethod.LOCAL_PUBLISH_METHOD); 
			if( ServerUtil.isJBoss7(page.getServer().getServerType())) {
				return new Path(IJBossRuntimeResourceConstants.AS7_STANDALONE)
				.append(IJBossRuntimeResourceConstants.FOLDER_TMP)
				.makeRelative().toString();
			} else {
				IDeploymentPageCallback cb = callbackMappings.get(mode);
				String loc = cb.getServerLocation(page.getServer());
				String config = cb.getServerConfigName(page.getServer());
				return new Path(loc).append(config)
				.append(IJBossToolingConstants.TMP)
				.append(IJBossToolingConstants.JBOSSTOOLS_TMP).toString();
			}
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
			page.getSaveStatus();
		}
	}
	
	private String getDeployType() {
		return getServer().getDeployLocationType();
	}

	private String getDeployDir() {
		if( page.getServer().getRuntime() == null )
			return "";//$NON-NLS-1$
		return ModuleDeploymentPage.makeRelative(getServer().getDeployFolder(), 
					page.getServer().getRuntime());
	}

	private String getTempDeployDir() {
		if( page.getServer().getRuntime() == null)
			return "";//$NON-NLS-1$
		return 
			ModuleDeploymentPage.makeRelative(getServer().getTempDeployFolder(), 
					page.getServer().getRuntime());
	}

	private IDeployableServer getServer() {
		return (IDeployableServer) page.getServer().loadAdapter(
				IDeployableServer.class, new NullProgressMonitor());
	}
/*
 * 
 * 
 * 
 * This is where the second half goes
 * 
 * 
 * 
 */
	protected Composite createViewerPortion(Composite random) {
		Composite root = new Composite(random, SWT.NONE);
		root.setLayout(new FormLayout());

		page.getFormToolkit(random).adapt(root);

		viewer = new TreeViewer(root, SWT.BORDER);
		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);
		TreeColumn moduleColumn = new TreeColumn(viewer.getTree(), SWT.NONE);
		TreeColumn publishLocColumn = new TreeColumn(viewer.getTree(), SWT.NONE);
		TreeColumn publishTempLocColumn = new TreeColumn(viewer.getTree(),
				SWT.NONE);
		moduleColumn.setText(Messages.EditorModule);
		publishLocColumn.setText(Messages.EditorSetDeployLabel);
		publishTempLocColumn.setText(Messages.EditorSetTempDeployLabel);

		moduleColumn.setWidth(200);
		publishLocColumn.setWidth(200);
		publishTempLocColumn.setWidth(200);

		viewer.setContentProvider(new ModulePageContentProvider());

		viewer.setLabelProvider(new ModulePageLabelProvider());
		viewer.setColumnProperties(new String[] { COLUMN_NAME,
				COLUMN_LOC, COLUMN_TEMP_LOC });
		viewer.setInput("");  //$NON-NLS-1$
		CellEditor[] editors = new CellEditor[] {
				new TextCellEditor(viewer.getTree()),
				new TextCellEditor(viewer.getTree()),
				new TextCellEditor(viewer.getTree()) };
		viewer.setCellModifier(new LocalDeploymentCellModifier());
		viewer.setCellEditors(editors);
		
		Link link = new Link(root, SWT.DEFAULT);
		link.setText("<a>" + Messages.EditorRefreshViewer + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
		link.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				refreshViewer();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		FormData linkData = UIUtil.createFormData2(null, 0, 100,-10, 0,5,null,0);
		link.setLayoutData(linkData);
		
		FormData treeData = UIUtil.createFormData2(0, 5, link,-5, 0,5,100,-5);
		viewer.getTree().setLayoutData(treeData);

		// Newer stuff
		Label comboLabel = new Label(root, SWT.NULL);
		comboLabel.setText("Filter by:");
		filterCombo = new Combo(root, SWT.READ_ONLY);
		filterCombo.setItems(new String[]{ALL, DEPLOYED, BY_MODNAME});
		filterCombo.select(0);
		
		filterText = new Text(root, SWT.SINGLE |SWT.BORDER);
		
		comboLabel.setLayoutData(UIUtil.createFormData2(null,0,100,-8,link,5,null,0));
		filterCombo.setLayoutData(UIUtil.createFormData2(null,0,100,-3,comboLabel,5,null,0));
		filterText.setLayoutData(UIUtil.createFormData2(null,0,100,-3,filterCombo,5,100,-5));
		
		ModifyListener ml =new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				resetFilterTextState();
				viewer.setInput(""); //$NON-NLS-1$
			}
		};
		filterCombo.addModifyListener(ml);
		filterText.addModifyListener(ml);
		filterCombo.select(1); // select DEPLOYED
		return root;
	}
	
	public void resetFilterTextState() {
		int ind = filterCombo.getSelectionIndex();
		boolean enabled = ind != -1 && 
				filterCombo.getItem(ind).equals(BY_MODNAME);
		filterText.setEnabled(enabled);
	}
	
	private void refreshViewer() {
		page.refreshPossibleModules();
		viewer.setInput("");  //$NON-NLS-1$
	}
	
	private class LocalDeploymentCellModifier implements ICellModifier {
		public boolean canModify(Object element, String property) {
			if( property == COLUMN_NAME)
				return false;
			return true;
		}

		public Object getValue(Object element, String property) {
			DeploymentModulePrefs p = preferences.getOrCreatePreferences(MAIN)
					.getOrCreateModulePrefs((IModule) element);
			if (property == COLUMN_LOC) {
				return getOutputFolderAndName(p, (IModule)element);
			}
			if (property == COLUMN_TEMP_LOC) {
				String ret = p.getProperty(COLUMN_TEMP_LOC);
				return ret == null ? "" : ret; //$NON-NLS-1$
			}

			return ""; //$NON-NLS-1$
		}

		public void modify(Object element, String property, Object value) {

			IModule module = (IModule) ((TreeItem) element).getData();
			DeploymentModulePrefs p = preferences.getOrCreatePreferences(MAIN)
					.getOrCreateModulePrefs(module);
			if (property == COLUMN_LOC) {
				String outputName, outPath;
				if( value == null || ((String)value).equals("")) { //$NON-NLS-1$
					outputName = ""; //$NON-NLS-1$
					outPath = ""; //$NON-NLS-1$
				} else {
					outputName = new Path(((String)value)).lastSegment();
					outPath = ((String)value).substring(0, ((String)value).length()-outputName.length());
				}
				page.firePropertyChangeCommand(p, 
						new String[]{COLUMN_LOC, OUTPUT_NAME},
						new String[]{outPath,outputName},
						Messages.EditorEditDeployLocCommand);
				viewer.refresh();
			} else if (property == COLUMN_TEMP_LOC) {
				page.firePropertyChangeCommand(p, COLUMN_TEMP_LOC,
						(String) value, Messages.EditorEditDeployLocCommand);
				viewer.refresh();
			}
		}
	}

	private class ModulePageContentProvider implements ITreeContentProvider {
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object inputElement) {
			return getFilteredModules();
		}

		public boolean hasChildren(Object element) {
			return false;
		}

		public Object getParent(Object element) {
			return null;
		}

		public Object[] getChildren(Object parentElement) {
			return null;
		}
	}

	private Object[] getFilteredModules(){
		if( filterCombo == null )
			return page.getPossibleModules();
		if( filterCombo.getItem(filterCombo.getSelectionIndex()).equals(ALL))
			return page.getPossibleModules();
		if( filterCombo.getItem(filterCombo.getSelectionIndex()).equals(DEPLOYED))
			return page.getServer().getModules();
		if( filterCombo.getItem(filterCombo.getSelectionIndex()).equals(BY_MODNAME)) {
			IModule[] mods = page.getPossibleModules();
			String txt = filterText.getText();
			ArrayList<IModule> result = new ArrayList<IModule>();
			for( int i = 0; i < mods.length; i++) {
				if( mods[i].getName().startsWith(txt)) {
					result.add(mods[i]);
				}
			}
			return result.toArray(new IModule[result.size()]);
		}
		return new Object[]{};
	}
	
	private class ModulePageLabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			if (element instanceof IModule && columnIndex == 0) {
				ILabelProvider labelProvider = ServerUICore.getLabelProvider();
				Image image = labelProvider.getImage((IModule) element);
				labelProvider.dispose();
				return image;
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof IModule) {
				IModule m = (IModule) element;
				if (columnIndex == 0)
					return m.getName();
				if (columnIndex == 1) {
					DeploymentModulePrefs modPref = preferences
							.getOrCreatePreferences(MAIN)
							.getOrCreateModulePrefs(m);
					return getOutputFolderAndName(modPref, m);
				}
				if (columnIndex == 2) {
					DeploymentModulePrefs modPref = preferences
							.getOrCreatePreferences(MAIN)
							.getOrCreateModulePrefs(m);
					String result = modPref.getProperty(COLUMN_TEMP_LOC);
					if (result != null)
						return result;
					modPref.setProperty(COLUMN_TEMP_LOC, ""); //$NON-NLS-1$
					return ""; //$NON-NLS-1$
				}
			}
			return element.toString();
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}
	}

	public void updateListeners() {
		// server has been saved. Remove property change listener from last wc and add to newest
		if( lastWC != null )
			lastWC.removePropertyChangeListener(this);
		lastWC = page.getServer();
		if( lastWC != null )
			lastWC.addPropertyChangeListener(this);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if( getShowRadios() && evt.getPropertyName().equals( IDeployableServer.SERVER_MODE)) { 
			if( showMetadataRadio() ) {
				metadataRadio.setEnabled(enableMetadataRadio());
				if(!metadataRadio.isEnabled() && metadataRadio.getSelection()) {
					page.execute(new RadioClickedCommand(serverRadio, currentSelection));
				}
			}
		} 
		updateWidgets();
		
	}
	
	
	public static String getDefaultOutputName(IModule module) {
		return new Path(module.getName()).lastSegment() + PublishUtil.getSuffix(module.getModuleType().getId());
	}
	
	protected static String getOutputFolderAndName(DeploymentModulePrefs modPref, IModule m) {
		String folder = modPref.getProperty(COLUMN_LOC);
		String outputName = modPref.getProperty(OUTPUT_NAME);
		outputName = outputName == null || outputName.length() == 0
			? getDefaultOutputName(m) : outputName;
			
		if (folder != null)
			return new Path(folder).append(outputName).toPortableString();
		return outputName;
	}

	public void setEnabled(boolean enabled) {
		Control[] c = new Control[] { 
				viewer.getTree(), deployText, tempDeployText, 
				metadataRadio, serverRadio, customRadio, currentSelection, 
				deployButton, tempDeployButton,zipDeployWTPProjects				
		};
		System.out.println("Setting enablement to " + enabled);
		for( int i = 0; i < c.length; i++ ) {
			if( c[i] != null && !c[i].isDisposed()) {
				c[i].setEnabled(enabled);
			}
		}
	}
}
