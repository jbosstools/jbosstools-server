package org.jboss.ide.eclipse.as.ui.editor;

import java.io.File;

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
import org.eclipse.wst.server.ui.ServerUICore;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentModulePrefs;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentPreferences;
import org.jboss.ide.eclipse.as.ui.Messages;

public class LocalDeploymentModuleTab implements IDeploymentEditorTab {
	private ModuleDeploymentPage page;
	private DeploymentPreferences preferences;

	public LocalDeploymentModuleTab() {
	}

	public String getTabName() {
		return Messages.EditorLocalDeployment;
	}

	public void setDeploymentPage(ModuleDeploymentPage page) {
		this.page = page;
	}

	public void setDeploymentPrefs(DeploymentPreferences prefs) {
		this.preferences = prefs;
	}

	private TreeViewer viewer;
	private static final String LOCAL_COLUMN_NAME = IJBossToolingConstants.LOCAL_DEPLOYMENT_NAME;
	private static final String LOCAL_COLUMN_LOC = IJBossToolingConstants.LOCAL_DEPLOYMENT_LOC;
	private static final String LOCAL_COLUMN_TEMP_LOC = IJBossToolingConstants.LOCAL_DEPLOYMENT_TEMP_LOC;

	public Control createControl(Composite parent) {
		helper = new ServerAttributeHelper(page.getServer().getOriginal(), page.getServer());

		Composite random = new Composite(parent, SWT.NONE);
		GridData randomData = new GridData(GridData.FILL_BOTH);
		random.setLayoutData(randomData);
		random.setLayout(new FormLayout());

		Composite defaultComposite = createDefaultComposite(random);
		FormData fd = new FormData();
		fd.left = new FormAttachment(0, 5);
		fd.top = new FormAttachment(0, 5);
		fd.right = new FormAttachment(100, -5);
		defaultComposite.setLayoutData(fd);
		
		Composite viewComposite = createViewerPortion(random);
		fd = new FormData();
		fd.left = new FormAttachment(0, 5);
		fd.top = new FormAttachment(defaultComposite, 5);
		fd.right = new FormAttachment(100, -5);
		fd.bottom = new FormAttachment(100, -5);
		viewComposite.setLayoutData(fd);

		return random;
	}

	private Text deployText, tempDeployText;
	private Button metadataRadio, serverRadio, customRadio, currentSelection;
	private Button deployButton, tempDeployButton;
	private ModifyListener deployListener, tempDeployListener;
	private SelectionListener radioListener, zipListener;
	private ServerAttributeHelper helper;
	private Button zipDeployWTPProjects;
	private String lastCustomDeploy, lastCustomTemp;

	protected Composite createDefaultComposite(Composite parent) {

		FormToolkit toolkit = new FormToolkit(parent.getDisplay());

		Section section = toolkit.createSection(parent,
				ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED
						| ExpandableComposite.TITLE_BAR);
		section.setText(Messages.swf_DeployEditorHeading);
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_FILL));

		Composite composite = toolkit.createComposite(section);

		composite.setLayout(new FormLayout());

		Label descriptionLabel = toolkit.createLabel(composite,
				Messages.swf_DeploymentDescription);
		Control top = descriptionLabel;
		Composite inner = toolkit.createComposite(composite);
		inner.setLayout(new GridLayout(1, false));

		IRuntime rt = getServer().getServer().getRuntime();
		boolean showRadios = true;
		if( rt == null )
			showRadios = false;
		else {
			IJBossServerRuntime jbsrt = (IJBossServerRuntime)rt.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
			if( jbsrt == null )
				showRadios = false;
		}

		if( showRadios ) {
			metadataRadio = toolkit.createButton(inner,
					Messages.EditorUseWorkspaceMetadata, SWT.RADIO);
			serverRadio = toolkit.createButton(inner,
					Messages.EditorUseServersDeployFolder, SWT.RADIO);
			customRadio = toolkit.createButton(inner,
					Messages.EditorUseCustomDeployFolder, SWT.RADIO);
	
			metadataRadio.setSelection(getDeployType().equals(
					IDeployableServer.DEPLOY_METADATA));
			serverRadio.setSelection(getDeployType().equals(
					IDeployableServer.DEPLOY_SERVER));
			customRadio.setSelection(getDeployType().equals(
					IDeployableServer.DEPLOY_CUSTOM));
			currentSelection = metadataRadio.getSelection() ? metadataRadio
					: serverRadio.getSelection() ? serverRadio : customRadio;
	
			radioListener = new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
	
				public void widgetSelected(SelectionEvent e) {
					if (e.getSource() == currentSelection)
						return; // do nothing
					page.execute(new RadioClickedCommand((Button) e.getSource(),
							currentSelection));
					currentSelection = (Button) e.getSource();
				}
			};
			metadataRadio.addSelectionListener(radioListener);
			serverRadio.addSelectionListener(radioListener);
			customRadio.addSelectionListener(radioListener);
		}
		
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
				DirectoryDialog d = new DirectoryDialog(new Shell());
				d.setFilterPath(page.makeGlobal(deployText.getText()));
				String x = d.open();
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
				DirectoryDialog d = new DirectoryDialog(new Shell());
				d.setFilterPath(page.makeGlobal(tempDeployText.getText()));
				String x = d.open();
				if (x != null)
					tempDeployText.setText(page.makeRelative(x));
			}
		});

		deployText
				.setEnabled(customRadio == null || customRadio.getSelection());
		tempDeployText.setEnabled(customRadio == null
				|| customRadio.getSelection());

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
		boolean zippedPublisherAvailable = isLocalZippedPublisherAvailable(); 
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
		return section;
	}

	protected boolean isLocalZippedPublisherAvailable() {
		IJBossServerPublisher[] publishers = 
			ExtensionManager.getDefault().getZippedPublishers();
		for( int i = 0; i < publishers.length; i++ ) {
			if( publishers[i].accepts(LocalPublishMethod.LOCAL_PUBLISH_METHOD, getServer().getServer(), null))
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
			this.oldDir = helper.getAttribute(IDeployableServer.DEPLOY_DIRECTORY, ""); //$NON-NLS-1$
		}
		public void execute() {
			helper.setAttribute(IDeployableServer.DEPLOY_DIRECTORY, newDir);
			lastCustomDeploy = newDir;
			page.getSaveStatus();
		}
		public void undo() {
			text.removeModifyListener(listener);
			helper.setAttribute(IDeployableServer.DEPLOY_DIRECTORY, oldDir);
			text.setText(oldDir);
			text.addModifyListener(listener);
			page.getSaveStatus();
		}
	}

	public class SetZipCommand extends ServerCommand {
		boolean oldVal;
		boolean newVal;
		public SetZipCommand() {
			super(page.getServer(), Messages.EditorZipDeployments);
			oldVal = helper.getAttribute(IDeployableServer.ZIP_DEPLOYMENTS_PREF, false);
			newVal = zipDeployWTPProjects.getSelection();
		}
		public void execute() {
			helper.setAttribute(IDeployableServer.ZIP_DEPLOYMENTS_PREF, newVal);
			page.getSaveStatus();
		}
		public void undo() {
			zipDeployWTPProjects.removeSelectionListener(zipListener);
			zipDeployWTPProjects.setSelection(oldVal);
			helper.setAttribute(IDeployableServer.ZIP_DEPLOYMENTS_PREF, oldVal);
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
			oldDir = helper.getAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, ""); //$NON-NLS-1$
		}
		public void execute() {
			helper.setAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, newDir);
			lastCustomTemp = newDir;
			page.getSaveStatus();
		}
		public void undo() {
			text.removeModifyListener(listener);
			helper.setAttribute(IDeployableServer.TEMP_DEPLOY_DIRECTORY, oldDir);
			text.setText(oldDir);
			text.addModifyListener(listener);
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
				IRuntime rt = server.getRuntime();
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
			page.getSaveStatus();
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
			page.getSaveStatus();
		}
	}
	
	
	private String getDeployType() {
		return getServer().getDeployLocationType();
	}

	private String getDeployDir() {
		return page.getServer().getRuntime() == null ? "" : //$NON-NLS-1$
			page.makeRelative(getServer().getDeployFolder()); 
	}

	private String getTempDeployDir() {
		return page.getServer().getRuntime() == null ? "" : //$NON-NLS-1$
			page.makeRelative(getServer().getTempDeployFolder()); 
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

		FormData treeData = new FormData();
		treeData.top = new FormAttachment(0, 5);
		treeData.bottom = new FormAttachment(100, -5);
		treeData.left = new FormAttachment(0, 5);
		treeData.right = new FormAttachment(100, -5);
		viewer.getTree().setLayoutData(treeData);
		viewer.setContentProvider(new ModulePageContentProvider());

		viewer.setLabelProvider(new ModulePageLabelProvider());
		viewer.setColumnProperties(new String[] { LOCAL_COLUMN_NAME,
				LOCAL_COLUMN_LOC, LOCAL_COLUMN_TEMP_LOC });
		viewer.setInput(""); // irrelevent
		CellEditor[] editors = new CellEditor[] {
				new TextCellEditor(viewer.getTree()),
				new TextCellEditor(viewer.getTree()),
				new TextCellEditor(viewer.getTree()) };
		viewer.setCellModifier(new LocalDeploymentCellModifier());
		viewer.setCellEditors(editors);

		return root;
	}

	private class LocalDeploymentCellModifier implements ICellModifier {
		public boolean canModify(Object element, String property) {
			if( property == LOCAL_COLUMN_NAME)
				return false;
			return true;
		}

		public Object getValue(Object element, String property) {
			DeploymentModulePrefs p = preferences.getPreferences("local")
					.getModulePrefs((IModule) element);
			if (property == LOCAL_COLUMN_LOC) {
				String ret = p.getProperty(LOCAL_COLUMN_LOC);
				return ret == null ? "" : ret;
			}
			if (property == LOCAL_COLUMN_TEMP_LOC) {
				String ret = p.getProperty(LOCAL_COLUMN_TEMP_LOC);
				return ret == null ? "" : ret;
			}

			return "";
		}

		public void modify(Object element, String property, Object value) {

			IModule module = (IModule) ((TreeItem) element).getData();
			DeploymentModulePrefs p = preferences.getPreferences("local")
					.getModulePrefs(module);
			if (property == LOCAL_COLUMN_LOC) {
				page.firePropertyChangeCommand(p, LOCAL_COLUMN_LOC,
						(String) value, Messages.EditorEditDeployLocCommand);
				viewer.refresh();
			} else if (property == LOCAL_COLUMN_TEMP_LOC) {
				page.firePropertyChangeCommand(p, LOCAL_COLUMN_TEMP_LOC,
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
							.getOrCreatePreferences("local")
							.getOrCreateModulePrefs(m);
					String result = modPref.getProperty(LOCAL_COLUMN_LOC);
					if (result != null)
						return result;
					modPref.setProperty(LOCAL_COLUMN_LOC, "");
					return "";
				}
				if (columnIndex == 2) {
					DeploymentModulePrefs modPref = preferences
							.getOrCreatePreferences("local")
							.getOrCreateModulePrefs(m);
					String result = modPref.getProperty(LOCAL_COLUMN_TEMP_LOC);
					if (result != null)
						return result;
					modPref.setProperty(LOCAL_COLUMN_TEMP_LOC, "");
					return "";
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

}
