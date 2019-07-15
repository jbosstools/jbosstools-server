/******************************************************************************* 
* Copyright (c) 2011-2013 Red Hat, Inc. 
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
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
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModule2;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.ServerUICore;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.UIUtil;
import org.jboss.ide.eclipse.as.ui.editor.internal.ChangeModuleDeploymentPropertyCommand;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel;
import org.jboss.ide.eclipse.as.wtp.ui.util.FormDataUtility;
import org.jboss.tools.as.core.internal.modules.DeploymentModulePrefs;
import org.jboss.tools.as.core.internal.modules.DeploymentPreferences;
import org.jboss.tools.as.core.server.controllable.systems.AbstractJBossDeploymentOptionsController;

/**
 * 
 * This class represents primarily a tree viewer with columns capable of 
 * customizing deployment locations on a per-module basis for a given server. 
 * 
 * @since 3.0
 */
public class ModuleDeploymentOptionsComposite extends Composite implements PropertyChangeListener {
	protected static final String ALL = Messages.EditorDeploymentPageFilterAll;
	protected static final String DEPLOYABLE = Messages.EditorDeploymentPageFilterDeployable;
	protected static final String DEPLOYED = Messages.EditorDeploymentPageFilterDeployed;
	
	
	protected static final String COLUMN_NAME = IJBossToolingConstants.LOCAL_DEPLOYMENT_NAME;
	protected static final String COLUMN_ZIP = IJBossToolingConstants.LOCAL_DEPLOYMENT_ZIP;
	protected static final String COLUMN_LOC = IJBossToolingConstants.LOCAL_DEPLOYMENT_LOC;
	protected static final String COLUMN_TEMP_LOC = IJBossToolingConstants.LOCAL_DEPLOYMENT_TEMP_LOC;
	protected static final String OUTPUT_NAME = IJBossToolingConstants.LOCAL_DEPLOYMENT_OUTPUT_NAME;
	protected static final String MAIN = ServerProfileModel.DEFAULT_SERVER_PROFILE;
	
	
	private static String[] ZIP_STRINGS = new String[]{"Default","Yes","No"};
	
	private DeploymentPreferences preferences;
	private TreeViewer viewer;
	private Combo filterCombo; 
	private Text filterText;
	private Button refreshLink;
	private DeploymentPage partner;
	
	private IServerWorkingCopy lastWC;
	protected ArrayList<IModule> possibleModules;
	private FormToolkit tk;
	
	public ModuleDeploymentOptionsComposite(Composite parent, DeploymentPage partner, FormToolkit tk, DeploymentPreferences prefs) {
		super(parent, SWT.NONE);
		this.partner = partner;
		this.preferences = prefs;
		lastWC = partner.getServer();
		lastWC.addPropertyChangeListener(this);
		this.tk = tk;
		refreshPossibleModules();
		createViewerPortion(this);
	}

	public void dispose() {
		lastWC.removePropertyChangeListener(this);
	}
	
	/*
	 * This part adds the viewer for customizing on a per-module basis 
	 */
	protected Composite createViewerPortion(Composite root) {
		setLayout(new FormLayout());

		tk.adapt(root);
		Composite filterComposite = createFilterComposite(root);
		viewer = createTreeViewer(root);
		
		if( filterComposite == null ) {
			FormData treeData = FormDataUtility.createFormData2(0, 5, null,-5, 0,5,100,-5);
			viewer.getTree().setLayoutData(treeData);
		} else {
			FormData filterData = FormDataUtility.createFormData2(0, 5, viewer.getTree(),-5, 0,5,100,-5);
			filterComposite.setLayoutData(filterData);
			
			FormData treeData = FormDataUtility.createFormData2(filterComposite, 10, 100,-5, 0,5,100,-5);
			viewer.getTree().setLayoutData(treeData);
		}
		return root;
	}
	
	protected Composite createFilterComposite(Composite root) {
		
		Composite wrapper = new Composite(root, SWT.NONE);
		wrapper.setLayout(new GridLayout(20, true));
		tk.adapt(wrapper);

		Composite wrapper1 = new Composite(wrapper, SWT.NONE);
		wrapper1.setLayout(new GridLayout(3, true));
		tk.adapt(wrapper1);
		
		
		// Newer stuff
		Label comboLabel = new Label(wrapper1, SWT.NULL);
		comboLabel.setText(Messages.EditorDeploymentPageFilterBy);
		filterCombo = new Combo(wrapper1, SWT.READ_ONLY);
		String[] viewerFilterTypes = getViewerFilterTypes();
		filterCombo.setItems(viewerFilterTypes);
		List<String> filterTypes = Arrays.asList(viewerFilterTypes);
		filterCombo.select(filterTypes.indexOf(DEPLOYED));
		
		filterText = new Text(wrapper, SWT.SINGLE |SWT.BORDER);
		
		refreshLink = new Button(wrapper, SWT.PUSH);
		refreshLink.setText(Messages.EditorRefreshViewer);
		


		// Organize widgets
		comboLabel.setLayoutData(  GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span( 1, 1).grab(true, false).create());
		filterCombo.setLayoutData( GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span( 2, 1).grab(true, false).create());
		
		wrapper1.setLayoutData(    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span( 5, 1).grab(true, false).create());
		filterText.setLayoutData(  GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(12, 1).grab(true, false).create());
		refreshLink.setLayoutData( GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span( 3, 1).grab(true, false).create());
		
//		comboLabel.setLayoutData(UIUtil.createFormData2(null,0,100,-9,0,5,null,0));
//		FormData comboData = UIUtil.createFormData2(null,0,100,-5,comboLabel,5,null,0);
//		filterCombo.setLayoutData(comboData);
//		filterText.setLayoutData(UIUtil.createFormData2(0,3,100,-2,filterCombo,5,70,-5));
//		FormData linkData = UIUtil.createFormData2(null, 0, 100,-5, null,0,100,-5);
//		refreshLink.setLayoutData(linkData);

		
		// Add the listeners
		ModifyListener ml =new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				refreshPossibleModules();
				resetFilterTextState();
				viewer.setInput(""); //$NON-NLS-1$
			}
		};
		filterCombo.addModifyListener(ml);
		filterText.addModifyListener(ml);
		
		refreshLink.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				refreshViewer();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		return wrapper;
	}
	
	protected TreeViewer createTreeViewer(Composite root) {
		TreeViewer viewer = new TreeViewer(root, SWT.BORDER);
		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);
		viewer.setContentProvider(createViewerContentProvider());
		viewer.setLabelProvider(createViewerLabelProvider());
		
		ArrayList<String> columnIDs = new ArrayList<String>();
		ArrayList<CellEditor> cellEditors = new ArrayList<CellEditor>();
		
		// Module column
		TreeColumn moduleColumn = new TreeColumn(viewer.getTree(), SWT.NONE);
		moduleColumn.setText(Messages.EditorModule);
		moduleColumn.setWidth(200);
		columnIDs.add(COLUMN_NAME);
		cellEditors.add(new TextCellEditor(viewer.getTree()));
		
		// Zip Column
		boolean showZip = showZipColumn();
		TreeColumn moduleZip = null;
		if( showZip ) {
			moduleZip = new TreeColumn(viewer.getTree(), SWT.NONE);
			moduleZip.setText("Zip Module"); // TODO externalize
			moduleZip.setWidth(100);
			columnIDs.add(COLUMN_ZIP);
			cellEditors.add(new ComboBoxCellEditor(viewer.getTree(), ZIP_STRINGS, SWT.READ_ONLY));
		}
		
		// Publish location column
		TreeColumn publishLocColumn = new TreeColumn(viewer.getTree(), SWT.NONE);
		publishLocColumn.setText(Messages.EditorSetDeployLabel);
		publishLocColumn.setWidth(200);
		columnIDs.add(COLUMN_LOC);
		cellEditors.add(new TextCellEditor(viewer.getTree()));
		
		
		// Temporary location
		boolean showTemp = showTemporaryColumn();
		TreeColumn publishTempLocColumn = null;
		if( showTemp ) {
			publishTempLocColumn = new TreeColumn(viewer.getTree(),SWT.NONE);
			publishTempLocColumn.setText(Messages.EditorSetTempDeployLabel);
			publishTempLocColumn.setWidth(200);
			columnIDs.add(COLUMN_TEMP_LOC);
			cellEditors.add(new TextCellEditor(viewer.getTree()));
		}
		
		
		String[] colNames = (String[]) columnIDs.toArray(new String[columnIDs.size()]);
		CellEditor[] editors = (CellEditor[]) cellEditors.toArray(new CellEditor[cellEditors.size()]);

		viewer.setColumnProperties(colNames);
		viewer.setInput("");  //$NON-NLS-1$
		viewer.setCellEditors(editors);
		viewer.setCellModifier(createViewerCellModifier());
		return viewer;
	}
	
	protected boolean showTemporaryColumn() {
		return true;
	}
	protected boolean showZipColumn() {
		return true;
	}
	
	protected ITableLabelProvider createViewerLabelProvider() {
		return new ModulePageLabelProvider();
	}
	
	protected ITreeContentProvider createViewerContentProvider() {
		return new ModulePageContentProvider();
	}
	protected ICellModifier createViewerCellModifier() {
		return new ModuleDeploymentCellModifier(this);
	}
	
	/*
	 * Extenders can override this to provide additional filter types
	 */
	protected String[] getViewerFilterTypes() {
		return new String[]{ALL, DEPLOYABLE, DEPLOYED};
	}
	
	public void resetFilterTextState() {
		int ind = filterCombo.getSelectionIndex();
		boolean enabled = ind != -1 && usesViewerFilterText(filterCombo.getItem(ind));
		filterText.setEnabled(enabled);
	}
	
	/*
	 * By default all mechanisms can be filtered now
	 */
	protected boolean usesViewerFilterText(String comboItem) {
		return true;
	}
	
	public void refreshViewer() {
		refreshPossibleModules();
		viewer.setInput("");  //$NON-NLS-1$
	}
	
	/*
	 * Reload the list of possible modules from the most recent working copy of the server
	 */
	protected void refreshPossibleModules() {
		ArrayList<IModule> possibleChildren = new ArrayList<IModule>();
		IModule[] modules2 = org.eclipse.wst.server.core.ServerUtil.getModules(partner.getServer().getServerType().getRuntimeType().getModuleTypes());
		if (modules2 != null) {
			int size = modules2.length;
			for (int i = 0; i < size; i++) {
				IModule module = modules2[i];
				IStatus status = partner.getServer().canModifyModules(new IModule[] { module }, null, null);
				if (status != null && status.getSeverity() != IStatus.ERROR)
					possibleChildren.add(module);
			}
		}
		this.possibleModules = possibleChildren;
	}
	

	public IModule[] getPossibleModules() {
		return (IModule[]) possibleModules.toArray(new IModule[possibleModules.size()]);
	}
	
	
	
	protected static class ModuleDeploymentCellModifier implements ICellModifier {
		private ModuleDeploymentOptionsComposite composite;
		public ModuleDeploymentCellModifier(ModuleDeploymentOptionsComposite composite) {
			this.composite = composite;
		}
		
		protected ModuleDeploymentOptionsComposite getComposite() {
			return composite;
		}
		protected DeploymentPreferences getPreferences() {
			return composite.getPreferences();
		}
		protected TreeViewer getViewer() {
			return composite.getViewer();
		}
		public boolean canModify(Object element, String property) {
			if( property == COLUMN_NAME)
				return false;
			return true;
		}

		public Object getValue(Object element, String property) {
			DeploymentModulePrefs p = getPreferences().getOrCreatePreferences()
					.getOrCreateModulePrefs((IModule) element);
			if (property == COLUMN_LOC) {
				return composite.getOutputFolderAndName(p, (IModule)element);
			}
			if (property == COLUMN_TEMP_LOC) {
				String ret = p.getProperty(COLUMN_TEMP_LOC);
				return ret == null ? "" : ret; //$NON-NLS-1$
			}
			if( property == COLUMN_ZIP) {
				String ret = p.getProperty(COLUMN_ZIP);
				try {
					return Integer.valueOf(ret);
				} catch(NumberFormatException nfe) {
				}
				return AbstractJBossDeploymentOptionsController.ZIP_DEFAULT;
			}

			return ""; //$NON-NLS-1$
		}

		public void modify(Object element, String property, Object value) {

			IModule module = (IModule) ((TreeItem) element).getData();
			DeploymentModulePrefs p = getPreferences().getOrCreatePreferences()
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
				getComposite().firePropertyChangeCommand(p, 
						new String[]{COLUMN_LOC, OUTPUT_NAME},
						new String[]{outPath,outputName},
						Messages.EditorEditDeployLocCommand);
				getComposite().getViewer().refresh();
			} else if (property == COLUMN_TEMP_LOC) {
				getComposite().firePropertyChangeCommand(p, 
						new String[] { COLUMN_TEMP_LOC },
						new String[] {(String) value}, 
						Messages.EditorEditDeployLocCommand);
				getComposite().getViewer().refresh();
			} else if( property == COLUMN_ZIP ) {
				getComposite().firePropertyChangeCommand(p, 
						new String[] { COLUMN_ZIP },
						new String[] {value.toString()},
						"Edit Deployment Zipped"); // TODO externalize
				getComposite().getViewer().refresh();
			}
		}
	}
	
	protected DeploymentPreferences getPreferences() {
		return preferences;
	}
	protected TreeViewer getViewer() {
		return viewer;
	}

	public void firePropertyChangeCommand(DeploymentModulePrefs p, String[] keys, String[] vals, String cmdName) {
		partner.execute(new ChangeModuleDeploymentPropertyCommand(partner, preferences, p, keys,vals,cmdName, viewer));
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

	/*
	 * Subclasses with a custom filter type may override this method
	 * This returns an array of objects which survive the filter. 
	 */
	protected Object[] getFilteredModules(){
		IModule[] mods = getPossibleModules();
		if( filterCombo == null )
			return mods;
		String item = filterCombo.getItem(filterCombo.getSelectionIndex()); 
		if( item.equals(DEPLOYED)) {
			mods = partner.getHelper().getServer().getModules();
		} else if( item.equals(DEPLOYABLE)) {
			ArrayList<IModule> result = new ArrayList<IModule>();
			for( int i = 0; i < mods.length; i++) {
				try {
					IModule[] parent = partner.getServer().getRootModules(mods[i], new NullProgressMonitor());
					if( parent.length == 1 && parent[0] == mods[i]) {
						result.add(mods[i]);
					}
				} catch(CoreException ce) {
					// getRootModules only throws CE if modules cannot be modified.
					// If they cannot be modified, they will not get added to result list.
					// No logging is needed
				}
			}
			mods = (IModule[]) result.toArray(new IModule[result.size()]);
		}
		
		// Now apply filters
		if( filterText != null ) {
			String txt = filterText.getText();
			if( !txt.isEmpty()) {
				ArrayList<IModule> collector = new ArrayList(Arrays.asList(mods));
				Iterator<IModule> it = collector.iterator();
				while(it.hasNext()) {
					IModule t = it.next();
					if( !t.getName().toLowerCase().contains(txt.toLowerCase())) {
						it.remove();
					}
				}
				return collector.toArray(new IModule[collector.size()]);
			}
		}
		return mods;
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
				int moduleIndex, zipIndex, locIndex, tempLocIndex;
				zipIndex = tempLocIndex = -1;
				moduleIndex = 0;
				zipIndex = showZipColumn() ? 1 : -1;
				locIndex = (zipIndex == -1 ? moduleIndex+1 : zipIndex+1);
				tempLocIndex = showTemporaryColumn() ? locIndex+1 : -1;
				
				IModule m = (IModule) element;
				if (columnIndex == moduleIndex)
					return m.getName();
				
				DeploymentModulePrefs modPref = preferences
						.getOrCreatePreferences()
						.getOrCreateModulePrefs(m);

				if( columnIndex == zipIndex ) {
					String ret = modPref.getProperty(COLUMN_ZIP);
					int retInd = 0;
					if( ret != null ) {
						try {
							retInd = Integer.parseInt(ret);
						} catch(NumberFormatException nfe) {
						}
					}
					if( retInd > 2 || retInd < 0) 
						retInd = 0;
					return ZIP_STRINGS[retInd];
				}
				if (columnIndex == locIndex) {
					return getOutputFolderAndName(modPref, m);
				}
				if (columnIndex == tempLocIndex) {
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
		lastWC = partner.getServer();
		if( lastWC != null )
			lastWC.addPropertyChangeListener(this);
	}

	/* Subclasses can override */
	public void propertyChange(PropertyChangeEvent evt) {
		if(IDeployableServer.ZIP_DEPLOYMENTS_PREF.equals(evt.getPropertyName())) {
			viewer.refresh();
		}
	}
	
	
	protected String getDefaultOutputName(IModule module) {
		String tmpName = null;
		if( module instanceof IModule2) {
			tmpName = ((IModule2)module).getProperty(IModule2.PROP_DEPLOY_NAME);
		}
		if( tmpName == null ) {
			tmpName = module.getName();
		}
		String lastSegment = new Path(tmpName).lastSegment();
		String suffix = PublishUtil.getSuffix(module.getModuleType().getId());
		String ret = lastSegment.endsWith(suffix) ? lastSegment : lastSegment + suffix;
		return  ret;
	}
	
	protected String getOutputFolderAndName(DeploymentModulePrefs modPref, IModule m) {
		String folder = modPref.getProperty(COLUMN_LOC);
		String outputName = modPref.getProperty(OUTPUT_NAME);
		outputName = outputName == null || outputName.length() == 0
			? getDefaultOutputName(m) : outputName;
			
		if (folder != null)
			return new Path(folder).append(outputName).toPortableString();
		return outputName;
	}
	
	public IStatus[] validate() {
		return new IStatus[0];
	}
	
	public Control[] getEnablementImmuneWidgets() {
		return new Control[]{filterCombo, filterText, refreshLink};
	}
}
