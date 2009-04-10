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
package org.jboss.ide.eclipse.archives.jdt.integration.ui;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElement;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElementAttribute;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListLabelProvider;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPUserLibraryElement;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.jdt.integration.model.IArchiveLibFileSet;
import org.jboss.ide.eclipse.archives.ui.ArchivesUIMessages;

/**
 *
 * @author "Rob Stryker" <rob.stryker@redhat.com>
 *
 */
public class LibFilesetInfoWizardPage extends WizardPage {

	private IArchiveNode parentNode;
	private IArchiveLibFileSet fileset;
	private String projectName, id;
	private Composite mainComposite;
	private TreeViewer viewer;
	private ArrayList elements;
	public LibFilesetInfoWizardPage (Shell parent, IArchiveLibFileSet fileset, IArchiveNode parentNode) {
		super(ArchivesUIMessages.LibFilesetInfoWizardPage_new_title, ArchivesUIMessages.LibFilesetInfoWizardPage_new_title, null);

		if (fileset == null) {
			setTitle(ArchivesUIMessages.LibFilesetInfoWizardPage_new_title);
			setMessage(ArchivesUIMessages.LibFilesetInfoWizardPage_new_message);
		} else {
			setTitle(ArchivesUIMessages.LibFilesetInfoWizardPage_edit_title);
			setMessage(ArchivesUIMessages.LibFilesetInfoWizardPage_edit_message);
		}

		this.fileset = fileset;
		this.parentNode = parentNode;
		projectName = parentNode.getProjectName();
	}

	public void createControl (Composite parent) {
		mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		mainComposite.setLayout(new FillLayout());
		viewer = new TreeViewer(mainComposite, SWT.BORDER | SWT.SINGLE);
		viewer.setContentProvider(getContentProvider());
		viewer.setLabelProvider(new CPListLabelProvider());
		elements= getElementList(createPlaceholderProject());
		viewer.setInput(new Object());
		addListener();
		setControl(mainComposite);
	}
	
	protected void addListener() {
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if( getFirstElement() instanceof CPUserLibraryElement ) {
					id = ((CPUserLibraryElement)getFirstElement()).getName();
				}
				validate();
			} 
		});
	}
	
	protected Object getFirstElement() {
		IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
		return sel.getFirstElement();
	}
	
	protected ITreeContentProvider getContentProvider() {
		return new ITreeContentProvider() {
			public Object[] getChildren(Object element) {
				if (element instanceof CPUserLibraryElement) {
					CPUserLibraryElement elem= (CPUserLibraryElement) element;
					return elem.getChildren();
				} 
				return new Object[]{};
			}

			public Object getParent(Object element) {
				if (element instanceof CPListElementAttribute) {
					return ((CPListElementAttribute) element).getParent();
				} else if (element instanceof CPListElement) {
					return ((CPListElement) element).getParentContainer();
				} 
				return null;
			}

			public boolean hasChildren(Object element) {
				return getChildren(element).length > 0;
			}
			public Object[] getElements(Object inputElement) {
				return (Object[]) elements.toArray(new Object[elements.size()]);
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		};
	}
	
	
	protected ArrayList getElementList(IJavaProject fDummyProject) {
		String[] names= JavaCore.getUserLibraryNames();
		ArrayList elements = new ArrayList();
		for (int i= 0; i < names.length; i++) {
			IPath path= new Path(JavaCore.USER_LIBRARY_CONTAINER_ID).append(names[i]);
			try {
				IClasspathContainer container= JavaCore.getClasspathContainer(path, fDummyProject);
				elements.add(new CPUserLibraryElement(names[i], container, fDummyProject));
			} catch (JavaModelException e) {
			}
		}
		return elements;
	}
	private static IJavaProject createPlaceholderProject() {
		String name= "####internal"; //$NON-NLS-1$
		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		while (true) {
			IProject project= root.getProject(name);
			if (!project.exists()) {
				return JavaCore.create(project);
			}
			name += '1';
		}		
	}
	
	private FormData createFormData(Object topStart, int topOffset, Object bottomStart, int bottomOffset,
			Object leftStart, int leftOffset, Object rightStart, int rightOffset) {
		FormData data = new FormData();

		if( topStart != null ) {
			data.top = topStart instanceof Control ? new FormAttachment((Control)topStart, topOffset) :
				new FormAttachment(((Integer)topStart).intValue(), topOffset);
		}

		if( bottomStart != null ) {
			data.bottom = bottomStart instanceof Control ? new FormAttachment((Control)bottomStart, bottomOffset) :
				new FormAttachment(((Integer)bottomStart).intValue(), bottomOffset);
		}

		if( leftStart != null ) {
			data.left = leftStart instanceof Control ? new FormAttachment((Control)leftStart, leftOffset) :
				new FormAttachment(((Integer)leftStart).intValue(), leftOffset);
		}

		if( rightStart != null ) {
			data.right = rightStart instanceof Control ? new FormAttachment((Control)rightStart, rightOffset) :
				new FormAttachment(((Integer)rightStart).intValue(), rightOffset);
		}

		return data;
	}




	private boolean validate () {
		if( !( getFirstElement() instanceof CPUserLibraryElement )) {
			setPageComplete(false);
			return false;
		}
		setPageComplete(true);
		return true;
	}

	public String getId() {
		return id;
	}

	private void fillDefaults () {
		if (fileset != null) {
			id = fileset.getId();
		} else {
			id = null;
		}

	}

	protected double getDescriptorVersion() {
		return parentNode.getModelRootNode().getDescriptorVersion();
	}
}
