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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
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
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.ui.ServerUICore;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.UIUtil;
import org.jboss.ide.eclipse.as.ui.editor.internal.ChangeModuleDeploymentPropertyCommand;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel;
import org.jboss.tools.as.core.internal.modules.DeploymentModulePrefs;
import org.jboss.tools.as.core.internal.modules.DeploymentPreferences;

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
	protected static final String BY_MODNAME = Messages.EditorDeploymentPageFilterModuleName;
	
	
	protected static final String COLUMN_NAME = IJBossToolingConstants.LOCAL_DEPLOYMENT_NAME;
	protected static final String COLUMN_LOC = IJBossToolingConstants.LOCAL_DEPLOYMENT_LOC;
	protected static final String COLUMN_TEMP_LOC = IJBossToolingConstants.LOCAL_DEPLOYMENT_TEMP_LOC;
	protected static final String OUTPUT_NAME = IJBossToolingConstants.LOCAL_DEPLOYMENT_OUTPUT_NAME;
	protected static final String MAIN = ServerProfileModel.DEFAULT_SERVER_PROFILE;
	
	private DeploymentPreferences preferences;
	private TreeViewer viewer;
	private Combo filterCombo; 
	private Text filterText;
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
		viewer = createTreeViewer(root);
		Composite filterComposite = createFilterComposite(root);
		
		FormData treeData = UIUtil.createFormData2(0, 5, filterComposite,-5, 0,5,100,-5);
		viewer.getTree().setLayoutData(treeData);

		if( filterComposite != null ) {
			FormData filterData = UIUtil.createFormData2(null, 0, 100,-5, 0,5,100,-5);
			filterComposite.setLayoutData(filterData);
		}
		
		return root;
	}
	
	protected Composite createFilterComposite(Composite root) {
		
		Composite wrapper = new Composite(root, SWT.NONE);
		wrapper.setLayout(new FormLayout());
		
		Link link = new Link(wrapper, SWT.DEFAULT);
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

		// Newer stuff
		Label comboLabel = new Label(wrapper, SWT.NULL);
		comboLabel.setText(Messages.EditorDeploymentPageFilterBy);
		filterCombo = new Combo(wrapper, SWT.READ_ONLY);
		filterCombo.setItems(getViewerFilterTypes());
		filterCombo.select(0);
		
		filterText = new Text(wrapper, SWT.SINGLE |SWT.BORDER);
		
		comboLabel.setLayoutData(UIUtil.createFormData2(null,0,100,-8,link,5,null,0));
		filterCombo.setLayoutData(UIUtil.createFormData2(null,0,100,-3,comboLabel,5,null,0));
		filterText.setLayoutData(UIUtil.createFormData2(null,0,100,-3,filterCombo,5,100,-5));
		
		ModifyListener ml =new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				refreshPossibleModules();
				resetFilterTextState();
				viewer.setInput(""); //$NON-NLS-1$
			}
		};
		filterCombo.addModifyListener(ml);
		filterText.addModifyListener(ml);
		filterCombo.select(1); // select DEPLOYABLE
		
		return wrapper;
	}
	
	protected TreeViewer createTreeViewer(Composite root) {
		TreeViewer viewer = new TreeViewer(root, SWT.BORDER);
		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);
		viewer.setContentProvider(createViewerContentProvider());
		viewer.setLabelProvider(createViewerLabelProvider());
		
		boolean showTemp = showTemporaryColumn();
		TreeColumn moduleColumn = new TreeColumn(viewer.getTree(), SWT.NONE);
		TreeColumn publishLocColumn = new TreeColumn(viewer.getTree(), SWT.NONE);
		TreeColumn publishTempLocColumn = null;
		moduleColumn.setText(Messages.EditorModule);
		publishLocColumn.setText(Messages.EditorSetDeployLabel);
		moduleColumn.setWidth(200);
		publishLocColumn.setWidth(200);
		
		String[] colNames = null;
		CellEditor[] editors = null;
		if( !showTemp ) {
			colNames = new String[] { COLUMN_NAME, COLUMN_LOC, COLUMN_TEMP_LOC };
			editors = new CellEditor[] {
					new TextCellEditor(viewer.getTree()),
					new TextCellEditor(viewer.getTree()),
					new TextCellEditor(viewer.getTree())};
		} else {
			publishTempLocColumn = new TreeColumn(viewer.getTree(),SWT.NONE);
			publishTempLocColumn.setText(Messages.EditorSetTempDeployLabel);
			publishTempLocColumn.setWidth(200);
			colNames = new String[] { COLUMN_NAME, COLUMN_LOC};
			editors = new CellEditor[] {
					new TextCellEditor(viewer.getTree()),
					new TextCellEditor(viewer.getTree())};
		}

		viewer.setColumnProperties(colNames);
		viewer.setInput("");  //$NON-NLS-1$
		viewer.setCellEditors(editors);
		viewer.setCellModifier(createViewerCellModifier());
		return viewer;
	}
	
	protected boolean showTemporaryColumn() {
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
		return new String[]{ALL, DEPLOYABLE, DEPLOYED, BY_MODNAME};
	}
	
	public void resetFilterTextState() {
		int ind = filterCombo.getSelectionIndex();
		boolean enabled = ind != -1 && usesViewerFilterText(filterCombo.getItem(ind));
		filterText.setEnabled(enabled);
	}
	
	/*
	 * Subclasses with custom filter types may override this method.
	 * Any filter mechanism which makes use of the text field should return true here
	 */
	protected boolean usesViewerFilterText(String comboItem) {
		return BY_MODNAME.equals(comboItem);
	}
	
	private void refreshViewer() {
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
		if( filterCombo.getItem(filterCombo.getSelectionIndex()).equals(ALL))
			return mods;
		if( filterCombo.getItem(filterCombo.getSelectionIndex()).equals(DEPLOYED))
			return partner.getServer().getModules();
		else {
			ArrayList<IModule> result = new ArrayList<IModule>();
			if( filterCombo.getItem(filterCombo.getSelectionIndex()).equals(DEPLOYABLE)) {
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
			}
			if( filterCombo.getItem(filterCombo.getSelectionIndex()).equals(BY_MODNAME)) {
				String txt = filterText.getText();
				for( int i = 0; i < mods.length; i++) {
					if( mods[i].getName().contains(txt)) {
						result.add(mods[i]);
					}
				}
			}
			return result.toArray(new IModule[result.size()]);
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
							.getOrCreatePreferences()
							.getOrCreateModulePrefs(m);
					return getOutputFolderAndName(modPref, m);
				}
				if (columnIndex == 2) {
					DeploymentModulePrefs modPref = preferences
							.getOrCreatePreferences()
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
		lastWC = partner.getServer();
		if( lastWC != null )
			lastWC.addPropertyChangeListener(this);
	}

	/* Subclasses can override */
	public void propertyChange(PropertyChangeEvent evt) {
		// Update widgets for the property change. Subclasses may override
	}
	
	
	protected String getDefaultOutputName(IModule module) {
		String lastSegment = new Path(module.getName()).lastSegment();
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

}
