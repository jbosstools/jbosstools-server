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
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.ServerUICore;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentModulePrefs;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentPreferences;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.ui.Messages;

public class DeploymentModuleOptionCompositeAssistant implements PropertyChangeListener {
	public static interface IDeploymentPageCallback {
		public boolean metadataEnabled();
		public String getServerLocation(IServerWorkingCopy wc);
		public String getServerConfigName(IServerWorkingCopy wc);
		public void propertyChange(PropertyChangeEvent evt, DeploymentModuleOptionCompositeAssistant composite);
	}
	
	public static class LocalDeploymentPageCallback implements IDeploymentPageCallback {
		public boolean metadataEnabled() {
			return true;
		}

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
	
	private static HashMap<String, IDeploymentPageCallback> callbackMappings;
	static {
		callbackMappings = new HashMap<String, IDeploymentPageCallback>();
		callbackMappings.put(LocalPublishMethod.LOCAL_PUBLISH_METHOD, new LocalDeploymentPageCallback());
	}
	
	public static void addMapping(String mode, IDeploymentPageCallback callback) {
		callbackMappings.put(mode, callback);
	}
	
	private ModuleDeploymentPage page;
	private DeploymentPreferences preferences;
	private TreeViewer viewer;
	protected static final String COLUMN_NAME = IJBossToolingConstants.LOCAL_DEPLOYMENT_NAME;
	protected static final String COLUMN_LOC = IJBossToolingConstants.LOCAL_DEPLOYMENT_LOC;
	protected static final String COLUMN_TEMP_LOC = IJBossToolingConstants.LOCAL_DEPLOYMENT_TEMP_LOC;
	protected static final String OUTPUT_NAME = IJBossToolingConstants.LOCAL_DEPLOYMENT_OUTPUT_NAME;
	protected String currentDeployType = LocalPublishMethod.LOCAL_PUBLISH_METHOD;
	
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
	
	private Text deployText, tempDeployText;
	private Button metadataRadio, serverRadio, customRadio, currentSelection;
	private Button deployButton, tempDeployButton;
	private ModifyListener deployListener, tempDeployListener;
	private SelectionListener radioListener, zipListener;
	private Button zipDeployWTPProjects;
	private String lastCustomDeploy, lastCustomTemp;
	
	public Button getServerRadio() {
		return serverRadio;
	}

	public Button getCurrentSelection() {
		return currentSelection;
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
		Composite inner = toolkit.createComposite(composite);
		inner.setLayout(new GridLayout(1, false));

		if( getShowRadios() ) {
			metadataRadio = toolkit.createButton(inner,
					Messages.EditorUseWorkspaceMetadata, SWT.RADIO);
			serverRadio = toolkit.createButton(inner,
					Messages.EditorUseServersDeployFolder, SWT.RADIO);
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
			metadataRadio.addSelectionListener(radioListener);
			serverRadio.addSelectionListener(radioListener);
			customRadio.addSelectionListener(radioListener);
		}
		lastWC = page.getServer();
		lastWC.addPropertyChangeListener(this);
		
		FormData radios = new FormData();
		radios.top = new FormAttachment(descriptionLabel, 5);
		radios.left = new FormAttachment(0, 5);
		radios.right = new FormAttachment(100, -5);
		inner.setLayoutData(radios);
		top = inner;

		Label label = toolkit.createLabel(composite,
				Messages.swf_DeployDirectory);
		label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		deployText = toolkit.createText(composite, getDeployDir(), SWT.BORDER);
		deployListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				page.execute(new SetDeployDirCommand());
			}
		};
		deployText.addModifyListener(deployListener);

		deployButton = toolkit.createButton(composite, Messages.browse,
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

		Label tempDeployLabel = toolkit.createLabel(composite,
				Messages.swf_TempDeployDirectory);
		tempDeployLabel.setForeground(toolkit.getColors().getColor(
				IFormColors.TITLE));

		tempDeployText = toolkit.createText(composite, getTempDeployDir(),
				SWT.BORDER);
		tempDeployListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				page.execute(new SetTempDeployDirCommand());
			}
		};
		tempDeployText.addModifyListener(tempDeployListener);

		tempDeployButton = toolkit.createButton(composite, Messages.browse,
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

		FormData descriptionLabelData = new FormData();
		descriptionLabelData.left = new FormAttachment(0, 5);
		descriptionLabelData.top = new FormAttachment(0, 5);
		descriptionLabel.setLayoutData(descriptionLabelData);

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

		zipDeployWTPProjects = toolkit.createButton(composite,
				Messages.EditorZipDeployments, SWT.CHECK);
		boolean zippedPublisherAvailable = isZippedPublisherAvailable(); 
		boolean value = getServer().zipsWTPDeployments();
		zipDeployWTPProjects.setEnabled(zippedPublisherAvailable);
		zipDeployWTPProjects.setSelection(zippedPublisherAvailable && value);

		FormData zipButtonData = new FormData();
		zipButtonData.right = new FormAttachment(100, -5);
		zipButtonData.left = new FormAttachment(0, 5);
		zipButtonData.top = new FormAttachment(tempDeployText, 5);
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
	
	protected boolean getShowRadios() {
		IRuntime rt = getServer().getServer().getRuntime();
		boolean showRadios = true;
		if( rt == null || rt.loadAdapter(IJBossServerRuntime.class, null) == null)
			showRadios = false;
		if( ServerUtil.isJBoss7(getServer().getServer()))
			showRadios = false;
		return showRadios;
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
		
	private void updateWidgets() {
		if( getShowRadios()) {
			metadataRadio.setSelection(getDeployType().equals(
					IDeployableServer.DEPLOY_METADATA));
			serverRadio.setSelection(getDeployType().equals(
					IDeployableServer.DEPLOY_SERVER));
			customRadio.setSelection(getDeployType().equals(
					IDeployableServer.DEPLOY_CUSTOM));
			currentSelection = metadataRadio.getSelection() ? metadataRadio
					: serverRadio.getSelection() ? serverRadio : customRadio;
			
			String mode = page.getServer().getAttribute(IDeployableServer.SERVER_MODE, LocalPublishMethod.LOCAL_PUBLISH_METHOD);
			boolean metaEnabled = callbackMappings.get(mode).metadataEnabled();
			metadataRadio.setEnabled(metaEnabled);
		}
		
		
		JBossServer jbs = ServerConverter.getJBossServer(page.getServer().getOriginal());
		String newDir = getHelper().getAttribute(IDeployableServer.DEPLOY_DIRECTORY, 
				jbs == null ? "" : jbs.getDeployFolder()); //$NON-NLS-1$
		String newTemp = getHelper().getAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, 
				jbs == null ? "" : jbs.getTempDeployFolder()); //$NON-NLS-1$
		deployText.removeModifyListener(deployListener);
		deployText.setText(newDir);
		deployText.addModifyListener(deployListener);
		tempDeployText.removeModifyListener(tempDeployListener);
		tempDeployText.setText(newTemp);
		tempDeployText.addModifyListener(tempDeployListener);
		
		deployText.setEnabled(getDeployType().equals(IDeployableServer.DEPLOY_CUSTOM));
		tempDeployText.setEnabled(getDeployType().equals(IDeployableServer.DEPLOY_CUSTOM));
		deployButton.setEnabled(getDeployType().equals(IDeployableServer.DEPLOY_CUSTOM));
		tempDeployButton.setEnabled(getDeployType().equals(IDeployableServer.DEPLOY_CUSTOM));
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
		
		protected void discoverNewFolders() {
			// Discover the new folders
			if( newSelection == metadataRadio  ) {
				newDir = JBossServerCorePlugin.getServerStateLocation(id)
					.append(IJBossRuntimeResourceConstants.DEPLOY).makeAbsolute().toString();
				newTemp = JBossServerCorePlugin.getServerStateLocation(id)
					.append(IJBossToolingConstants.TEMP_DEPLOY).makeAbsolute().toString();
				new File(newDir).mkdirs();
				new File(newTemp).mkdirs();
			} else if( newSelection == serverRadio ) {
				if( server.getRuntime() != null && 
						server.getRuntime().loadAdapter(IJBossServerRuntime.class, null) != null) {
					String loc, config;
					loc = config = null;
					String mode = getHelper().getAttribute(IDeployableServer.SERVER_MODE, LocalPublishMethod.LOCAL_PUBLISH_METHOD); 
					IDeploymentPageCallback cb = callbackMappings.get(mode);
					loc = cb.getServerLocation(page.getServer());
					config = cb.getServerConfigName(page.getServer());
					newDir = new Path(loc)
						.append(config)
						.append(IJBossRuntimeResourceConstants.DEPLOY).toString();
					newTemp = new Path(loc).append(config)
						.append(IJBossToolingConstants.TMP)
						.append(IJBossToolingConstants.JBOSSTOOLS_TMP).toString();
					if( mode.equals(LocalPublishMethod.LOCAL_PUBLISH_METHOD))
						new File(newTemp).mkdirs();
				}
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
		if( page.getServer().getRuntime() == null || ServerUtil.isJBoss7(page.getServer().getOriginal()))
			return "";//$NON-NLS-1$
		return ModuleDeploymentPage.makeRelative(getServer().getDeployFolder(), 
					page.getServer().getRuntime());
	}

	private String getTempDeployDir() {
		if( page.getServer().getRuntime() == null || ServerUtil.isJBoss7(page.getServer().getOriginal()))
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
		FormData linkData = new FormData();
		linkData.bottom = new FormAttachment(100,-5);
		linkData.left = new FormAttachment(0, 5);
		link.setLayoutData(linkData);
		
		FormData treeData = new FormData();
		treeData.top = new FormAttachment(0, 5);
		treeData.bottom = new FormAttachment(link, -5);
		treeData.left = new FormAttachment(0, 5);
		treeData.right = new FormAttachment(100, -5);
		viewer.getTree().setLayoutData(treeData);

		return root;
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
			DeploymentModulePrefs p = preferences.getOrCreatePreferences(currentDeployType)
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
			DeploymentModulePrefs p = preferences.getOrCreatePreferences(currentDeployType)
					.getOrCreateModulePrefs(module);
			if (property == COLUMN_LOC) {
				String outputName = new Path(((String)value)).lastSegment();
				String outPath = ((String)value).substring(0, ((String)value).length()-outputName.length());
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
			return page.getPossibleModules();
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
							.getOrCreatePreferences(currentDeployType)
							.getOrCreateModulePrefs(m);
					return getOutputFolderAndName(modPref, m);
				}
				if (columnIndex == 2) {
					DeploymentModulePrefs modPref = preferences
							.getOrCreatePreferences(currentDeployType)
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
			String mode = page.getServer().getAttribute(IDeployableServer.SERVER_MODE, LocalPublishMethod.LOCAL_PUBLISH_METHOD);
			metadataRadio.setEnabled(callbackMappings.get(mode).metadataEnabled());
//			String originalDeployLocation = page.getServer().getOriginal().getAttribute(IDeployableServer.DEPLOY_DIRECTORY_TYPE, IDeployableServer.DEPLOY_CUSTOM);
//			String wcDeployLocation = page.getServer().getAttribute(IDeployableServer.DEPLOY_DIRECTORY_TYPE, IDeployableServer.DEPLOY_CUSTOM);
			if(!metadataRadio.isEnabled() && metadataRadio.getSelection()) {
				page.execute(new RadioClickedCommand(serverRadio, currentSelection));
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
}
