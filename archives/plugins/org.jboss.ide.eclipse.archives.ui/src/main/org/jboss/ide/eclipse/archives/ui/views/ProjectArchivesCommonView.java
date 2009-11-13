/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.ui.views;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.ui.ArchivesSharedImages;
import org.jboss.ide.eclipse.archives.ui.ArchivesUIMessages;
import org.jboss.ide.eclipse.archives.ui.PrefsInitializer;
import org.jboss.ide.eclipse.archives.ui.PrefsInitializer.IArchivesPreferenceListener;
import org.jboss.ide.eclipse.archives.ui.actions.BuildAction;
import org.jboss.ide.eclipse.archives.ui.providers.ArchivesContentProviderDelegate.WrappedProject;

/**
 *
 * @author "Rob Stryker" <rob.stryker@redhat.com>
 *
 */
public class ProjectArchivesCommonView extends CommonNavigator implements IArchivesPreferenceListener {

	public static final String ID = "org.jboss.ide.eclipse.archives.ui.ProjectArchivesView"; //$NON-NLS-1$
	
	protected static ProjectArchivesCommonView instance;
	protected ISelectionListener selectionListener;
	protected IProject currentProject;
	public static ProjectArchivesCommonView getInstance() { return instance; }
	public ProjectArchivesCommonView() {
		super();
		instance = this;
		selectionListener = createSelectionListener();
	}
	protected IAdaptable getInitialInput() {
		currentProject = null;
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	public void createPartControl(Composite aParent) {
		super.createPartControl(aParent);
		addBuildActionToSite();
		PrefsInitializer.addListener(this);
	}

	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().addPostSelectionListener(selectionListener);
	}

    public void dispose() {
    	super.dispose();
    	PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().removePostSelectionListener(selectionListener);
		PrefsInitializer.removeListener(this);
    }

	protected ISelectionListener createSelectionListener() {
		return new INullSelectionListener() {
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				if( part == instance )
					return;
				if (selection == null || selection.isEmpty()) {
					getInitialInput();
					jiggleViewerInput();
				}
				if (!(selection instanceof IStructuredSelection))
					return;

				Object element = ((IStructuredSelection)selection).getFirstElement();
				IProject project = getProject(element);
				if( project != null && project != currentProject && project.isOpen()) {
					currentProject = project;
					jiggleViewerInput();
				}
			}

			public IProject getProject (Object element) {
				if( element instanceof IStructuredSelection)
					element = ((IStructuredSelection)element).getFirstElement();

				if (element instanceof IAdaptable) {
					IAdaptable adaptable = (IAdaptable)element;
					IResource resource = (IResource) adaptable.getAdapter(IResource.class);
					if( resource != null )
						return resource.getProject();
				}
				if( element instanceof WrappedProject )
					return ((WrappedProject)element).getElement();
				if( element instanceof IArchiveNode ) {
					String projName = ((IArchiveNode)element).getProjectName();
					return ResourcesPlugin.getWorkspace().getRoot().getProject(projName);
				}
				return null;
			}

		};
	}

	public IProject getCurrentProject() {
		return currentProject;
	}

	protected void jiggleViewerInput() {
		if( showProjectRoot()) {
			// if we're showing all projects, then the view is already set.
			if( !showAllProjects() ||  !getCommonViewer().getInput().equals(ResourcesPlugin.getWorkspace().getRoot()))
				getCommonViewer().setInput(ResourcesPlugin.getWorkspace().getRoot());
		} else {
			getCommonViewer().setInput(currentProject);
		}
	}

	private boolean showProjectRoot() {
		return PrefsInitializer.getBoolean(PrefsInitializer.PREF_SHOW_PROJECT_ROOT);
	}

	private boolean showAllProjects() {
		return PrefsInitializer.getBoolean(PrefsInitializer.PREF_SHOW_ALL_PROJECTS);
	}

	public void addBuildActionToSite() {
		Action buildAction = new Action(ArchivesUIMessages.BuildArchivesNode, ArchivesSharedImages.getImageDescriptor(ArchivesSharedImages.IMG_BUILD_PACKAGES)) {
			public void run() {
				buildSelection(getSelectedObject());
			}
		};
		IActionBars bars = ((IViewSite)getSite()).getActionBars();
		bars.getToolBarManager().add(buildAction);
	}

	private void buildSelection(final Object selected) {
		new BuildAction().run(selected);
	}

	private Object getSelectedObject() {
		IStructuredSelection selection = (IStructuredSelection)getCommonViewer().getSelection();
		if (selection != null && !selection.isEmpty())
			return selection.getFirstElement();
		return null;
	}

	public Object getAdapter(Class adapter) {
		if( adapter == IPropertySheetPage.class )
			return new PropertySheetPage();
		return super.getAdapter(adapter);
	}

	public void preferenceChanged(String key, boolean val) {
		jiggleViewerInput();
	}
	public void preferenceChanged(String key, String val) {
		jiggleViewerInput();
	}
}
