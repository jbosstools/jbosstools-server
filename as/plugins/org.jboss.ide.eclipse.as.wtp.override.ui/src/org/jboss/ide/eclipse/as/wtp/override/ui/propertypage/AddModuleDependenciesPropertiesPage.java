/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *	   David Schneider, david.schneider@unisys.com - [142500] WTP properties pages fonts don't follow Eclipse preferences
 *     Stefan Dimov, stefan.dimov@sap.com - bugs 207826, 222651
 *******************************************************************************/
package org.jboss.ide.eclipse.as.wtp.override.ui.propertypage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.BuildPathDialogAccess;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.jst.j2ee.internal.AvailableJ2EEComponentsForEARContentProvider;
import org.eclipse.jst.j2ee.internal.IJ2EEDependenciesControl;
import org.eclipse.jst.j2ee.internal.ManifestUIResourceHandler;
import org.eclipse.jst.j2ee.internal.common.J2EEVersionUtil;
import org.eclipse.jst.j2ee.internal.plugin.J2EEUIMessages;
import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPlugin;
import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider;


public abstract class AddModuleDependenciesPropertiesPage implements Listener, IJ2EEDependenciesControl {
	
	protected final String PATH_SEPARATOR = ComponentDependencyContentProvider.PATH_SEPARATOR;	
	protected boolean showRuntimePath;
	protected final IProject project;
	protected final J2EEDependenciesPage propPage; 
	protected IVirtualComponent rootComponent = null;
	protected Text componentNameText;
	protected Label availableModules;
	protected CheckboxTableViewer availableComponentsViewer;
	protected Button selectAllButton;
	protected Button deselectAllButton;
	protected Button projectButton;
	protected Button projectJarButton;
	protected Button externalJarButton;
	protected Button addVariableButton;
	protected Composite buttonColumn;
	protected static final IStatus OK_STATUS = IDataModelProvider.OK_STATUS;
	protected Listener tableListener;
	protected Listener labelListener;
	

	protected HashMap<IVirtualComponent, String> oldComponentToRuntimePath = new HashMap<IVirtualComponent, String>();
	
	// This should keep a list of all elements currently in the list, even if unchecked
	protected HashMap<Object, String> objectToRuntimePath = new HashMap<Object, String>();
	
	
	//[Bug 238264] the cached list elements that are new and need to be manually added to the viewer
	// Can be an IProject or IVirtualComponent
	protected ArrayList<Object> addedElements= new ArrayList<Object>();

	/**
	 * Constructor for AddModulestoEARPropertiesControl.
	 */
	public AddModuleDependenciesPropertiesPage(final IProject project, final J2EEDependenciesPage page) { 
		this.project = project;
		this.propPage = page;
		rootComponent = ComponentCore.createComponent(project);
		this.showRuntimePath = true;
	}

	protected boolean getShowRuntimePath() {
		return this.showRuntimePath;
	}
	
	protected void setShowRuntimePath(boolean bool) {
		this.showRuntimePath = bool;
	}	
	
	/*
	 * UI Creation Methods
	 */
	
	public Composite createContents(final Composite parent) { 
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        J2EEDependenciesPage.createDescriptionComposite(composite, ManifestUIResourceHandler.EAR_Modules_Desc);
		createListGroup(composite);
		refresh();
	    Dialog.applyDialogFont(parent);
		return composite;
	}

	protected void createListGroup(Composite parent) {
		Composite listGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		listGroup.setLayout(layout);
		GridData gData = new GridData(GridData.FILL_BOTH);
		gData.horizontalIndent = 5;
		listGroup.setLayoutData(gData);

		availableModules = new Label(listGroup, SWT.NONE);
		gData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		availableModules.setText(J2EEUIMessages.getResourceString("AVAILABLE_J2EE_COMPONENTS")); //$NON-NLS-1$ = "Available dependent JARs:"
		availableModules.setLayoutData(gData);
		createTableComposite(listGroup);
	}
	
	protected void createTableComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridData gData = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gData);
		fillComposite(composite);
	}

	public void fillComposite(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		parent.setLayout(layout);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		createTable(parent);
		createButtonColumn(parent);
	}

	protected void createButtonColumn(Composite parent) {
		buttonColumn = createButtonColumnComposite(parent);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END);
		buttonColumn.setLayoutData(data);
		createPushButtons();
	}

	// TODO add a project button here
	protected void createPushButtons() {
		String SELECT_ALL_BUTTON = ManifestUIResourceHandler.Select_All_3; 
		String DE_SELECT_ALL_BUTTON = ManifestUIResourceHandler.Deselect_All_4; 

		selectAllButton = createPushButton(SELECT_ALL_BUTTON);
		deselectAllButton = createPushButton(DE_SELECT_ALL_BUTTON);
		projectButton = createPushButton("Add Project...");
		projectJarButton = createPushButton(J2EEUIMessages.getResourceString(J2EEUIMessages.PROJECT_JAR));
		externalJarButton = createPushButton(J2EEUIMessages.getResourceString(J2EEUIMessages.EXTERNAL_JAR));
		addVariableButton = createPushButton(J2EEUIMessages.getResourceString(J2EEUIMessages.ADDVARIABLE));
	}

	protected Button createPushButton(String label) {
		Button aButton = new Button(buttonColumn, SWT.PUSH);
		aButton.setText(label);
		aButton.addListener(SWT.Selection, this);
		aButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return aButton;
	}

	public Composite createButtonColumnComposite(Composite parent) {
		Composite aButtonColumn = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		aButtonColumn.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		aButtonColumn.setLayoutData(data);
		return aButtonColumn;
	}

	public Group createGroup(Composite parent) {
		return new Group(parent, SWT.NULL);
	}

	protected void createTable(Composite parent) {
		availableComponentsViewer = createAvailableComponentsViewer(parent);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		availableComponentsViewer.getTable().setLayoutData(gd);

		if (rootComponent != null) {
			ComponentDependencyContentProvider provider = createProvider();
			provider.setRuntimePaths(objectToRuntimePath);
			availableComponentsViewer.setContentProvider(provider);
			availableComponentsViewer.setLabelProvider(provider);
			availableComponentsViewer.setFilters(new ViewerFilter[]{provider});
			addTableListeners();
		}
	}
	
	/**
	 *  Subclasses should over-ride this and extend the class
	 */
	protected ComponentDependencyContentProvider createProvider() {
		int j2eeVersion = J2EEVersionUtil.convertVersionStringToInt(rootComponent);
		return new ComponentDependencyContentProvider(rootComponent);
	}

	/*
	 * Listeners of various events
	 */
	
	protected void addTableListeners() {
		addCheckStateListener();
		addHoverHelpListeners();
		addDoubleClickListener();
	}
	
	protected void addHoverHelpListeners() {
		final Table table = availableComponentsViewer.getTable();				
		createLabelListener(table);
		createTableListener(table);
		table.addListener(SWT.Dispose, tableListener);
		table.addListener(SWT.KeyDown, tableListener);
		table.addListener(SWT.MouseMove,	 tableListener);
		table.addListener(SWT.MouseHover, tableListener);		
	}
	
	protected void createLabelListener(final Table table) {
		labelListener = new Listener () {
			public void handleEvent (Event event) {
				Label label = (Label)event.widget;
				Shell shell = label.getShell ();
				switch (event.type) {
					case SWT.MouseDown:
						Event e = new Event ();
						e.item = (TableItem) label.getData ("_TABLEITEM");
						table.setSelection (new TableItem [] {(TableItem) e.item});
						table.notifyListeners (SWT.Selection, e);
						shell.dispose ();
						table.setFocus();
						break;
					case SWT.MouseExit:
						shell.dispose ();
						break;
				}
			}
		};
	}
	
	protected void createTableListener(final Table table) {
		tableListener = new Listener () {
			Shell tip = null;
			Label label = null;
			public void handleEvent (Event event) {
				switch (event.type) {
					case SWT.Dispose:
					case SWT.KeyDown:
					case SWT.MouseMove: {
						if (tip == null) break;
						tip.dispose ();
						tip = null;
						label = null;
						break;
					}
					case SWT.MouseHover: {
						TableItem item = table.getItem (new Point (event.x, event.y));
						if (item != null) {
							if (!item.getGrayed())
								return;
							if (tip != null  && !tip.isDisposed ()) tip.dispose ();
							tip = new Shell (PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					                .getShell(), SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL);
							tip.setBackground (Display.getDefault().getSystemColor (SWT.COLOR_INFO_BACKGROUND));
							FillLayout layout = new FillLayout ();
							layout.marginWidth = 2;
							tip.setLayout (layout);
							label = new Label (tip, SWT.WRAP);
							label.setForeground (Display.getDefault().getSystemColor (SWT.COLOR_INFO_FOREGROUND));
							label.setBackground (Display.getDefault().getSystemColor (SWT.COLOR_INFO_BACKGROUND));
							label.setData ("_TABLEITEM", item); //$NON-NLS-1$
							label.setText (J2EEUIMessages.getResourceString(J2EEUIMessages.HOVER_HELP_FOR_DISABLED_LIBS));
							label.addListener (SWT.MouseExit, labelListener);
							label.addListener (SWT.MouseDown, labelListener);
							Point size = tip.computeSize (SWT.DEFAULT, SWT.DEFAULT);
							Rectangle rect = item.getBounds (0);
							Point pt = table.toDisplay (rect.x, rect.y);
							tip.setBounds (pt.x, pt.y - size.y, size.x, size.y);
							tip.setVisible (true);
						}
					}
				}
			}
		};		
	}

	protected void addDoubleClickListener() {
		availableComponentsViewer.setColumnProperties(new String[] {"a", "b", "c"});
		CellEditor[] editors = new CellEditor[]{new TextCellEditor(), new TextCellEditor(), new TextCellEditor(availableComponentsViewer.getTable())};
		availableComponentsViewer.setCellEditors(editors);
		availableComponentsViewer.setCellModifier(new RuntimePathCellModifier());
	}
	
	private class RuntimePathCellModifier implements ICellModifier {

		public boolean canModify(Object element, String property) {
			int columnIndex = Arrays.asList(availableComponentsViewer.getColumnProperties()).indexOf(property);
			if( columnIndex == 2 ) { 
				if( element instanceof VirtualArchiveComponent ) {
					try {
						boolean sameProject = 
							((VirtualArchiveComponent)element).getWorkspaceRelativePath() != null 
							&& ((VirtualArchiveComponent)element).getWorkspaceRelativePath().segment(0)
								.equals(rootComponent.getProject().getName());
						return !(sameProject && isPhysicallyAdded(((VirtualArchiveComponent)element)));
					} catch( IllegalArgumentException iae) {}
				}
				return true;
			}
			return false;
		}

		public Object getValue(Object element, String property) {
			return objectToRuntimePath.get(element) == null ? 
					new Path("/").toString() : 
					objectToRuntimePath.get(element);
		}

		public void modify(Object element, String property, Object value) {
			if( element instanceof TableItem ) {
				TableItem item = (TableItem)element;
				item.setText(2, (String)value);
				objectToRuntimePath.put(item.getData(), (String)value);
			}
		}
		
	}

	public void handleEvent(Event event) {
		if (event.widget == selectAllButton)
			handleSelectAllButtonPressed();
		else if (event.widget == deselectAllButton)
			handleDeselectAllButtonPressed();
		else if(event.widget == projectButton) 
			handleSelectProjectButton();
		else if(event.widget == projectJarButton)
			handleSelectProjectJarButton();
		else if(event.widget == externalJarButton)
			handleSelectExternalJarButton();
		else if(event.widget == addVariableButton)
			handleSelectVariableButton();
	}
	
	private void handleSelectAllButtonPressed() {
        TableItem[] children = availableComponentsViewer.getTable().getItems();
        for (int i = 0; i < children.length; i++) {
            TableItem item = children[i];
            if( !item.getGrayed())
            	item.setChecked(true);
        }
	}

	private void handleDeselectAllButtonPressed() {
        TableItem[] children = availableComponentsViewer.getTable().getItems();
        for (int i = 0; i < children.length; i++) {
            TableItem item = children[i];
            if( !item.getGrayed())
            	item.setChecked(false);
        }
	}
	
	private void handleSelectProjectButton() {
		SelectProjectDialog d = new SelectProjectDialog(
				new Shell(), getProjectLabelProvider(), getProjectContentProvider());
		if( d.open() == Window.OK) {
			IProject selected = (IProject)d.getFirstResult();
			addedElements.add(selected);
			refresh();
			TableItem[] items = availableComponentsViewer.getTable().getItems();
			for( int i = 0; i < items.length; i++ )
				if( items[i].getData().equals(selected))
					items[i].setChecked(true);
		}
	}
	
	protected class SelectProjectDialog extends ElementTreeSelectionDialog {
		public SelectProjectDialog(Shell parent, ILabelProvider labelProvider,
				ITreeContentProvider contentProvider) {
			super(parent, labelProvider, contentProvider);
			setAllowMultiple(false);
			setTitle("Select a Project");
			setInput(ResourcesPlugin.getWorkspace());
		}
	}
	protected ILabelProvider getProjectLabelProvider() {
		return new LabelProvider() {
				public Image getImage(Object element) {
					if( element instanceof IProject ) 
						return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_PROJECT);
					return null;
				}
				public String getText(Object element) {
					if( element instanceof IProject )
						return ((IProject)element).getName();
					return element == null ? "" : element.toString();//$NON-NLS-1$
				}
		};
	}
	protected ITreeContentProvider getProjectContentProvider() {
		return new ITreeContentProvider() {
			public Object[] getElements(Object inputElement) {
				return ResourcesPlugin.getWorkspace().getRoot().getProjects();
			}
			public Object[] getChildren(Object parentElement) {
				return null;
			}
			public Object getParent(Object element) {
				return null;
			}
			public boolean hasChildren(Object element) {
				return false;
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
			
		};
	}
	
	protected IProject[] filterAddProjectList(IProject[] list) {
		return list;
	}
	
	
	/**
	 * [Bug 238264]
	 * Add an archive as a potential new reference for this.earComponent
	 * NOTE1: the given archive will not be added as a potential reference if there is already a reference to it
	 * NOTE2: the given archive will only be added as an actual reference when this.performOk is invoked
	 * 
	 * @param archive the archive to add as a potential new reference in this.earComponent
	 * 			
	 */
	private void addPotentialNewReference(IVirtualComponent archive) {
		//check to see if a reference to the given archive already exists
		IVirtualReference [] existingRefs = rootComponent.getReferences();
		IVirtualComponent referencedComponent;
		boolean refAlreadyExists = false;
		for(int i=0;i<existingRefs.length && !refAlreadyExists;i++){
			referencedComponent = existingRefs[i].getReferencedComponent();
			refAlreadyExists = referencedComponent.equals(archive);
		}
		
		// verify it's not already in the list... ugh
		TableItem[] allItems = availableComponentsViewer.getTable().getItems();
		for( int i = 0; i < allItems.length; i++ ) {
			if( allItems[i].getData().equals(archive)) {
				allItems[i].setChecked(true);
				refAlreadyExists = true;
			}
		}
		
		// only add the archive as a potentialy new reference if it does not already exist
		// also force check it
		if(!refAlreadyExists) {
			this.objectToRuntimePath.put(archive, new Path("/").toString());
			this.addedElements.add(archive);
			availableComponentsViewer.add(archive);
			TableItem[] items = availableComponentsViewer.getTable().getItems();
			for( int i = 0; i < items.length; i++ ) {
				if(items[i].getData().equals(archive))
					items[i].setChecked(true);
			}
			
		} else {
			//TODO should inform user that they selected an already referenced archive?
		}
	}
	
	private void handleSelectExternalJarButton(){
		IPath[] selected= BuildPathDialogAccess.chooseExternalJAREntries(propPage.getShell());

		if (selected != null) {
			for (int i= 0; i < selected.length; i++) {
				
				String type = VirtualArchiveComponent.LIBARCHIVETYPE + IPath.SEPARATOR;
				IVirtualComponent archive = ComponentCore.createArchiveComponent( rootComponent.getProject(), type +
							selected[i].toString());
				
				this.addPotentialNewReference(archive);
			}
			refresh();
		}
		
	}

	private void handleSelectVariableButton(){
		IPath existingPath[] = new Path[0];
		IPath[] paths =  BuildPathDialogAccess.chooseVariableEntries(propPage.getShell(), existingPath);
		
		if (paths != null) {
			refresh();
			for (int i = 0; i < paths.length; i++) {
				IPath resolvedPath= JavaCore.getResolvedVariablePath(paths[i]);

				java.io.File file = new java.io.File(resolvedPath.toOSString());
				if( file.isFile() && file.exists()){
					String type = VirtualArchiveComponent.VARARCHIVETYPE + IPath.SEPARATOR;
					
					IVirtualComponent archive = ComponentCore.createArchiveComponent( rootComponent.getProject(), type +
								paths[i].toString());
					
					this.addPotentialNewReference(archive);
				}else{
					//display error
				}
			}
			refresh();
		}	
	}
	


	// TODO FIX THIS
	protected void addCheckStateListener() {
		availableComponentsViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				CheckboxTableViewer vr = (CheckboxTableViewer)event.getSource();
				Object element = event.getElement();
				if (vr.getGrayed(element)) 
					vr.setChecked(element, !vr.getChecked(element));
				Object o = event.getSource();
				
				// TODO : check for conflict + dependency res
			}
		});
	}
	


	public CheckboxTableViewer createAvailableComponentsViewer(Composite parent) {
		int flags = SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI;

		Table table = new Table(parent, flags);
		availableComponentsViewer = new CheckboxTableViewer(table);

		// set up table layout
		TableLayout tableLayout = new org.eclipse.jface.viewers.TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(300, true));
		tableLayout.addColumnData(new ColumnWeightData(300, true));
		if (showRuntimePath) 
			tableLayout.addColumnData(new ColumnWeightData(200, true));
		table.setLayout(tableLayout);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		availableComponentsViewer.setSorter(null);

		// table columns
		TableColumn fileNameColumn = new TableColumn(table, SWT.NONE, 0);
		fileNameColumn.setText(ManifestUIResourceHandler.JAR_Module_UI_); 
		fileNameColumn.setResizable(true);

		TableColumn projectColumn = new TableColumn(table, SWT.NONE, 1);
		projectColumn.setText(ManifestUIResourceHandler.Project_UI_); 
		projectColumn.setResizable(true);
		
		if (showRuntimePath) {
			TableColumn bndColumn = new TableColumn(table, SWT.NONE, 2);
			bndColumn.setText(ManifestUIResourceHandler.Packed_In_Lib_UI_); 
			bndColumn.setResizable(true);			
		}
			
		tableLayout.layout(table, true);
		return availableComponentsViewer;

	}
	
	protected boolean isPhysicallyAdded(VirtualArchiveComponent component) {
		IPath p = null;
		try {
			p = component.getProjectRelativePath();
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}		
	private boolean secondShouldBeDisabled(IVirtualComponent component) {
		if(component.isBinary()) return false;
		if (JavaEEProjectUtilities.isApplicationClientComponent(component)) return true;
		if (JavaEEProjectUtilities.isEARProject(component.getProject()) && component.isBinary()) return false;
		if (JavaEEProjectUtilities.isEJBComponent(component)) return true;
		if (JavaEEProjectUtilities.isDynamicWebComponent(component)) return true;
		if (JavaEEProjectUtilities.isJCAComponent(component)) return true;
		if (JavaEEProjectUtilities.isStaticWebProject(component.getProject())) return true;
		if (JavaEEProjectUtilities.isProjectOfType(component.getProject(), IJ2EEFacetConstants.JAVA)) return false;
		return false;
	}
	

		
	private boolean isInLibDir(VirtualArchiveComponent comp) {
		IPath p = comp.getProjectRelativePath();
		if (p.segmentCount() == 2)
			return false;
		return true;
	}
	
	private boolean isConflict(Object lib) {
		IProject libProj = (lib instanceof IProject) ? (IProject)lib : ((IVirtualComponent)lib).getProject(); 
		IProject earProject = rootComponent.getProject();	
		try {			
			IVirtualComponent cmp = ComponentCore.createComponent(earProject);
			IProject[] earRefProjects = earProject.getReferencedProjects();
			for (int i = 0; i < earRefProjects.length; i++) {	
				if (!J2EEProjectUtilities.isEARProject(earRefProjects[i]) &&
						!earRefProjects[i].equals(libProj)) {
					IVirtualComponent cmp1 = ComponentCore.createComponent(earRefProjects[i]);
					IVirtualReference[] refs = cmp1.getReferences();
					for (int j = 0; j < refs.length; j++) {
						if (refs[j].getReferencedComponent().getProject().equals(libProj)) return true;
					}
				}	
			}
			return false;
		} catch (CoreException ce) {
			J2EEUIPlugin.logError(ce);
		}		
		return false;
	}	
	
	private void handleSelectProjectJarButton(){
		IPath[] selected= BuildPathDialogAccess.chooseJAREntries(propPage.getShell(), project.getLocation(), new IPath[0]);
	
		if (selected != null) {
			for (int i= 0; i < selected.length; i++) {
				//IPath fullPath = project.getFile(selected[i]).getFullPath();	
				String type = VirtualArchiveComponent.LIBARCHIVETYPE + IPath.SEPARATOR;
				IVirtualComponent archive = ComponentCore.createArchiveComponent( rootComponent.getProject(), type +
							selected[i].makeRelative().toString());
				
				this.addPotentialNewReference(archive);
			}
			refresh();
		}
		
	}
	private boolean hasInitialized = false;
	/**
	 * This should only be called on changes, such as adding a project
	 * reference, adding a lib reference etc.
	 * 
	 * It will reset the input, manually re-add missing elements, and do other tasks
	 */
	public void refresh() {
		TableItem[] items = availableComponentsViewer.getTable().getItems();
		HashMap<Object, Boolean> checked = cacheChecked(); 
		resetTableUI();
		if( !hasInitialized) {
			initialize();
			resetTableUI();
		}

		/*
		 * Re-check any added elements that were checked but now lost their check
		 */
		TableItem[] newItems = availableComponentsViewer.getTable().getItems();
		for( int i = 0; i < newItems.length; i++ ) {
			if( checked.containsKey(newItems[i].getData()))
				newItems[i].setChecked(checked.get(newItems[i].getData()));
		}
	}

	protected HashMap<Object, Boolean>  cacheChecked() {
		// preserve selections / check / etc of new (added) entities
		TableItem [] items = availableComponentsViewer.getTable().getItems();
		HashMap<Object, Boolean> checked = new HashMap<Object,Boolean>();
		if( addedElements != null ) {
			int j = 0;
			for( int i = 0; i < items.length; i++ ) {
				if( addedElements.contains(items[i].getData()))
					checked.put(items[i].getData(), items[i].getChecked());
			}
		}
		return checked;
	}

	protected void resetTableUI() {
		IWorkspaceRoot input = ResourcesPlugin.getWorkspace().getRoot();
		availableComponentsViewer.setInput(input);
		GridData data = new GridData(GridData.FILL_BOTH);
		int numlines = Math.min(10, availableComponentsViewer.getTable().getItemCount());
		data.heightHint = availableComponentsViewer.getTable().getItemHeight() * numlines;
		availableComponentsViewer.getTable().setLayoutData(data);
		GridData btndata = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		buttonColumn.setLayoutData(btndata);

		//[Bug 238264] for all the jars in the cache temparaly list them in the grid
		for(Object addedElement : this.addedElements) {
			availableComponentsViewer.add(addedElement);
		}
	}

	protected void initialize() {
		TableItem [] items = availableComponentsViewer.getTable().getItems();
		
		// First initialize the paths
		Object data;
		for( int i = 0; i < items.length; i++ ) {
			data = items[i].getData();
			if( data instanceof IVirtualComponent ) {
				IVirtualReference ref = rootComponent.getReference(((IVirtualComponent)data).getName());
				String val = ref == null ? new Path(PATH_SEPARATOR).toString() : ref.getRuntimePath().toString();
				objectToRuntimePath.put(data, val);
			}
		}
		
		// Then initialize the UI
		List forceCheck = new ArrayList();
		forceCheck.addAll(getChildrenComponents());
		forceCheck.addAll(getCPComponents());
		for( int i = 0; i < items.length; i++ ) {
			if( forceCheck.contains(items[i].getData()))
				items[i].setChecked(true);
			
			if( items[i].getData() instanceof VirtualArchiveComponent ) 
				items[i].setGrayed(isPhysicallyAdded(((VirtualArchiveComponent)items[i].getData())));
		}
		hasInitialized = true;
	}
	protected List getChildrenComponents() {
		List list = new ArrayList();
		IVirtualReference refs[] = rootComponent.getReferences();
		for( int i=0; i< refs.length; i++){
			if( isChildComponent(refs[i]))
				list.add(refs[i].getReferencedComponent());
		}
		return list;
	}
	
	protected boolean isChildComponent(IVirtualReference ref) {
//		if ((ref.getRuntimePath().isRoot() && !inLibFolder) ||
//		(!ref.getRuntimePath().isRoot() && inLibFolder) ||
//		!isVersion5) {
		return true;
	}
	
	protected List getCPComponents() {
		List list = new ArrayList();
		Map pathToComp = new HashMap();
		IVirtualReference refs[] = rootComponent.getReferences();
		for( int i=0; i< refs.length; i++){
			if( isChildComponent(refs[i])) {
				IVirtualComponent comp = refs[i].getReferencedComponent();
				addClasspathComponentDependencies(list, pathToComp, comp);
			}
		}
		return list;
	}

	protected void addClasspathComponentDependencies(final List componentList, final Map pathToComp, final IVirtualComponent referencedComponent) {
		AvailableJ2EEComponentsForEARContentProvider.addClasspathComponentDependencies(componentList, pathToComp, referencedComponent);
	}

	
	/*
	 *  Clean-up methods are below. These include 
	 *  performCancel, performDefaults, performOK, and 
	 *  any other methods that are called *only* by this one. 
	 */
	public void setVisible(boolean visible) {
	}
	
	public void performDefaults() {
	}
	
	public boolean performCancel() {
		return true;
	}
	
	public void dispose() {
		Table table = null;
		if (availableComponentsViewer != null) {
		     table = availableComponentsViewer.getTable();
		     if (table == null)
		    	 return;
		}
		table.removeListener(SWT.Dispose, tableListener);
		table.removeListener(SWT.KeyDown, tableListener);
		table.removeListener(SWT.MouseMove, tableListener);
		table.removeListener(SWT.MouseHover, tableListener);		
	}

	protected abstract boolean performOk(IProgressMonitor monitor);
	public boolean performOk() {
		NullProgressMonitor monitor = new NullProgressMonitor();
		boolean subResult = performOk(monitor);
		if( !subResult )
			return false;
		
//		removeModulesFromEAR(monitor);		
//		addModulesToEAR(monitor);
//		refresh();
		
		Iterator i = addedElements.iterator();
		while(i.hasNext()) {
			Object o = i.next();
			System.out.println("Adding " + o + " to path " + objectToRuntimePath.get(o));
		}
		return true;
	}

	
//	/*
//	 * Methods called directly from OK
//	 */
//	private IStatus addModulesToEAR(IProgressMonitor monitor) {
//		IStatus stat = OK_STATUS;
//		try {
//			if( earComponent != null ){
//				final List list = newJ2EEModulesToAdd(false);
//				final List bndList = newJ2EEModulesToAdd(true);
//				final boolean shouldRun = (list != null && !list.isEmpty()) || !javaProjectsList.isEmpty();
//				final boolean shouldBndRun = isVersion5 && 
//											((bndList != null && !bndList.isEmpty()) || !javaLibProjectsList.isEmpty());
//				if(shouldRun || shouldBndRun){
//					IWorkspaceRunnable runnable = new IWorkspaceRunnable(){
//
//						public void run(IProgressMonitor monitor) throws CoreException{
//							if (shouldRun) {
//								execAddOp(monitor, list, J2EEConstants.EAR_ROOT_DIR);
//								execAddOp1(monitor, javaProjectsList, j2eeComponentList, J2EEConstants.EAR_ROOT_DIR);								
//							} 
//							if (shouldBndRun) {
//								execAddOp(monitor, bndList, libDir);
//								execAddOp1(monitor, javaLibProjectsList, j2eeLibElementList, libDir);																
//							} 
//						}
//					};
//					J2EEUIPlugin.getWorkspace().run(runnable, monitor);
//				}
//			}
//		} catch (Exception e) {
//			J2EEUIPlugin.logError(e);
//		}
//		
//		//[Bug 238264] clear out the cache because they should all either be added as references now
//		//	or no longer checked and therefore not wanted by the user
//		this.addedJARComponents.clear();
//		
//		return OK_STATUS;
//	}
//
//	private List newJ2EEModulesToAdd(boolean inLibFolder){
//		if (inLibFolder && !isVersion5) return null;
//		List newComps = new ArrayList();
//		List comps = inLibFolder ? j2eeLibElementList : j2eeComponentList;
//		if (comps != null && !comps.isEmpty()){
//			for (int i = 0; i < comps.size(); i++){
//				IVirtualComponent handle = (IVirtualComponent)comps.get(i);
//				if (ClasspathDependencyUtil.isClasspathComponentDependency(handle)) {
//					continue;
//				}
//				if( !inEARAlready(handle))
//					newComps.add(handle);
//			}
//		}
//		return newComps;
//	}
//	
//	/**
//	 * 
//	 * @param componentHandle
//	 * @return
//	 * @description  returns true is a component is already in the EAR as a dependent
//	 */
//	protected boolean inEARAlready(IVirtualComponent component){
//		IVirtualReference refs[] = rootComponent.getReferences();
//		for( int i=0; i< refs.length; i++){
//			IVirtualReference ref = refs[i];
//			if  ( ref.getReferencedComponent().equals( component ))
//				return true;
//		}	
//		return false;
//	}
	
//	private void updateLibDir(IProgressMonitor monitor) {
//		if (libDir.equals(oldLibDir)) return;
//		final IEARModelProvider earModel = (IEARModelProvider)ModelProviderManager.getModelProvider(project);
//		final Application app = (Application)ModelProviderManager.getModelProvider(project).getModelObject();
//		oldLibDir = app.getLibraryDirectory();
//		if (oldLibDir == null) oldLibDir = J2EEConstants.EAR_DEFAULT_LIB_DIR;
//		earModel.modify(new Runnable() {
//			public void run() {			
//			app.setLibraryDirectory(libDir);
//		}}, null);
//	}
//
//	private void remComps(List list, String path) {
//		if( !list.isEmpty()){
//			try {
//				// remove the components from the EAR
//				IDataModelOperation op = removeComponentFromEAROperation(earComponent, list, path);
//				op.execute(null, null);
//				// if that succeeded, remove all EAR-scope J2EE dependencies on these components
//				J2EEComponentClasspathUpdater.getInstance().queueUpdateEAR(earComponent.getProject());
//			} catch (ExecutionException e) {
//				J2EEUIPlugin.logError(e);
//			}
//		}	
//	}
//	
//	private IStatus removeModulesFromEAR(IProgressMonitor monitor) {
//		IStatus stat = OK_STATUS;
//		if (!isVersion5) {
//			if(earComponent != null && j2eeComponentList != null) {
//				List list = getComponentsToRemove();
//				remComps(list, J2EEConstants.EAR_ROOT_DIR);
//			}			
//		} else {	
//			if( earComponent != null && j2eeComponentList != null) {
//				List[] list = getComponentsToRemoveUpdate(!libDir.equals(oldLibDir)); 
//				remComps(list[0], J2EEConstants.EAR_ROOT_DIR);
//				
//				remComps(list[1], oldLibDir);
//			}
//		}
//		return stat;
//	}		
//	
//	protected IDataModelOperation removeComponentFromEAROperation(IVirtualComponent sourceComponent, List targetComponentsHandles, String dir) {
//		IDataModel model = DataModelFactory.createDataModel(new RemoveComponentFromEnterpriseApplicationDataModelProvider());
//		model.setProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT, sourceComponent);
//		List modHandlesList = (List) model.getProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST);
//		modHandlesList.addAll(targetComponentsHandles);
//		model.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST, modHandlesList);
//        model.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENTS_DEPLOY_PATH, dir);		
//		return model.getDefaultOperation();
//	}
//	
//	protected List getComponentsToRemove(){
//		//j2eeComponentList = getCheckedJ2EEElementsAsList();
//		List list = new ArrayList();
//		if( earComponent != null && list != null ){
//			IVirtualReference[] oldrefs = earComponent.getReferences();
//			for (int j = 0; j < oldrefs.length; j++) {
//				IVirtualReference ref = oldrefs[j];
//				IVirtualComponent handle = ref.getReferencedComponent();
//				if(!j2eeComponentList.contains(handle) && (isVersion5 ? !j2eeLibElementList.contains(handle) : true)){
//					if ((handle instanceof VirtualArchiveComponent) && (isPhysicallyAdded((VirtualArchiveComponent)handle)))
//						continue;
//					list.add(handle);
//				}
//			}
//		}
//		return list;		
//	}
//	
//	// EAR5 case
//	protected List[] getComponentsToRemoveUpdate(boolean dirUpdated){
//		//j2eeComponentList = getCheckedJ2EEElementsAsList();
//		List[] list = new ArrayList[2];
//		list[0] = new ArrayList();
//		list[1] = new ArrayList();
//		if( earComponent != null && list != null ){
//			IVirtualReference[] oldrefs = earComponent.getReferences();
//			for (int j = 0; j < oldrefs.length; j++) {
//				IVirtualReference ref = oldrefs[j];
//				IVirtualComponent handle = ref.getReferencedComponent();
//				if (handle instanceof VirtualArchiveComponent) {
//					VirtualArchiveComponent comp = (VirtualArchiveComponent)handle;
//					if (isPhysicallyAdded(comp))
//						continue;
//				}
//				if(!j2eeComponentList.contains(handle) && ref.getRuntimePath().isRoot()) {
//					list[0].add(handle);
//				}
//				if((!j2eeLibElementList.contains(handle) || dirUpdated) &&
//						ref.getRuntimePath().toString().equals(oldLibDir)) {	
//					list[1].add(handle);
//				}
//			}
//		}
//		return list;		
//	}
//	
//	
//	/*
//	 * Operations called from OK
//	 */
//	
//	private void execAddOp(IProgressMonitor monitor, List componentList, String path) throws CoreException {
//		if (componentList == null || componentList.isEmpty()) return;
//		IDataModel dm = DataModelFactory.createDataModel(new AddComponentToEnterpriseApplicationDataModelProvider());
//		
//		dm.setProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT, earComponent);					
//		dm.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST, componentList);
//		
//		//[Bug 238264] the uri map needs to be manually set correctly
//		Map uriMap = new HashMap();
//		IVirtualComponent virtComp;
//		String virtCompURIMapName;
//		for(int i=0; i<componentList.size(); i++) {
//			virtComp = (IVirtualComponent)componentList.get(i);
//			virtCompURIMapName = getVirtualComponentNameWithExtension(virtComp);	
//			uriMap.put(virtComp, virtCompURIMapName);
//		}
//		dm.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENTS_TO_URI_MAP, uriMap);
//		
//        if (isVersion5) dm.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENTS_DEPLOY_PATH, path);
//
//		IStatus stat = dm.validateProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST);
//		if (stat != OK_STATUS)
//			throw new CoreException(stat);
//		try {
//			dm.getDefaultOperation().execute(monitor, null);
//		} catch (ExecutionException e) {
//			J2EEUIPlugin.logError(e);
//		}		
//	}
//	
//	private void execAddOp1(IProgressMonitor monitor, List jProjList, List j2eeCompList, String path)
//													throws CoreException {
//		if (!jProjList.isEmpty()) {
//			Set moduleProjects = new HashSet();
//			for (int i = 0; i < jProjList.size(); i++) {
//				try {
//					IProject proj = (IProject) jProjList.get(i);
//					moduleProjects.add(proj);
//					IDataModel migrationdm = DataModelFactory.createDataModel(new JavaProjectMigrationDataModelProvider());
//					migrationdm.setProperty(IJavaProjectMigrationDataModelProperties.PROJECT_NAME, proj.getName());
//					migrationdm.getDefaultOperation().execute(monitor, null);
//
//
//					IDataModel refdm = DataModelFactory.createDataModel(new CreateReferenceComponentsDataModelProvider());
//					List targetCompList = (List) refdm.getProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST);
//
//					IVirtualComponent targetcomponent = ComponentCore.createComponent(proj);
//					targetCompList.add(targetcomponent);
//
//					refdm.setProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT, earComponent);
//					refdm.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST, targetCompList);
//					if (isVersion5) refdm.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENTS_DEPLOY_PATH, path);
//					
//
//					// referenced java projects should have archiveName attribute
//					((Map)refdm.getProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENTS_TO_URI_MAP)).put(targetcomponent, proj.getName().replace(' ', '_') + IJ2EEModuleConstants.JAR_EXT);
//
//					refdm.getDefaultOperation().execute(monitor, null);
//					j2eeCompList.add(targetcomponent);
//				} catch (ExecutionException e) {
//					J2EEUIPlugin.logError(e);
//				}
//			}
//			EarFacetRuntimeHandler.updateModuleProjectRuntime(earComponent.getProject(), moduleProjects, new NullProgressMonitor());
//		} // end 
//		
//	}
//	
//
//	
//	/*
//	 * Methods called From the Operations (which are called from OK)
//	 */
//	
//	/**
//	 * Method returns the name of the given IVirtualComponent being sure the correct extension
//	 * is on the end of the name, this is important for internal projects. Added for [Bug 241509]
//	 * 
//	 * @param virtComp the IVirtualComponent to get the name of with the correct extension
//	 * @return the name of the given IVirtualComponent with the correct extension
//	 */
//	private String getVirtualComponentNameWithExtension(IVirtualComponent virtComp) {
//		String virtCompURIMapName = this.getURIMappingName(virtComp);
//		
//		boolean linkedToEAR = true;
//		try {
//			if(virtComp.isBinary()){
//				linkedToEAR = ((J2EEModuleVirtualArchiveComponent)virtComp).isLinkedToEAR();
//				((J2EEModuleVirtualArchiveComponent)virtComp).setLinkedToEAR(false);
//			}
//			if(JavaEEProjectUtilities.isDynamicWebComponent(virtComp)) {
//				if(!virtCompURIMapName.endsWith(IJ2EEModuleConstants.WAR_EXT)) {
//					//web module URIs need to end in WAR
//					virtCompURIMapName += IJ2EEModuleConstants.WAR_EXT;
//				}
//			} else if(JavaEEProjectUtilities.isJCAComponent(virtComp)) {
//				if(!virtCompURIMapName.endsWith(IJ2EEModuleConstants.RAR_EXT)) {
//					//connector module URIs need to end in RAR
//					virtCompURIMapName += IJ2EEModuleConstants.RAR_EXT;
//				}
//			} else if(!virtCompURIMapName.endsWith(IJ2EEModuleConstants.JAR_EXT)) {
//				//all other modules (EJB, AppClient, Utility) need to end in JAR
//				virtCompURIMapName += IJ2EEModuleConstants.JAR_EXT;
//			}
//		} finally {
//			if(virtComp.isBinary()){
//				((J2EEModuleVirtualArchiveComponent)virtComp).setLinkedToEAR(linkedToEAR);
//			}
//		}
//		return virtCompURIMapName;
//	}
//	
//	/**
//	 * [Bug 238264]
//	 * determines a unique URI mapping name for a given component
//	 * this is in case two components have the same name.
//	 * 
//	 * @return returns a valid (none duplicate) uri mapping name for the given component\
//	 */
//	private String getURIMappingName(IVirtualComponent archive) {
//		
//		//get the default uri map name for the given archive
//		IPath componentPath = Path.fromOSString(archive.getName());
//		String uriMapName = componentPath.lastSegment().replace(' ', '_');
//		
//		
//		//check to be sure this uri mapping is not already in use by another reference
//		boolean dupeArchiveName;
//		String refedCompName;
//		int lastDotIndex;
//		String increment;
//		IVirtualReference [] existingRefs = earComponent.getReferences();
//		for(int i=0;i<existingRefs.length;i++){
//			refedCompName = existingRefs[i].getReferencedComponent().getName();
//			
//			//if uri mapping names of the refed component and the given archive are the same
//			//  find a new uri map name for the given archive
//			if(existingRefs[i].getArchiveName().equals(uriMapName)){
//				dupeArchiveName = true;
//				//find a new uriMapName for the given component
//				for(int j=1; dupeArchiveName; j++){
//					lastDotIndex = uriMapName.lastIndexOf('.');
//					increment = "_"+j; //$NON-NLS-1$
//					
//					//create the new potential name
//					if(lastDotIndex != -1){
//						uriMapName = uriMapName.substring(0, lastDotIndex) + increment + uriMapName.substring(lastDotIndex);
//					} else {
//						uriMapName = uriMapName.substring(0)+increment;
//					}
//					
//					//determine if the new potential name is valid
//					for(int k=0; k<existingRefs.length; k++) {
//						dupeArchiveName = existingRefs[k].getArchiveName().equals(uriMapName);
//						if(dupeArchiveName) {
//							break;
//						}
//					}
//				}
//			}
//		}
//		
//		return uriMapName;
//	}
//	
//			
}
