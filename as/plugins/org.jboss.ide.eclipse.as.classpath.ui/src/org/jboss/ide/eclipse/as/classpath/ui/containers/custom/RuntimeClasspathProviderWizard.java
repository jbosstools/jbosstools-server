/*******************************************************************************
 * Copyright (c) 2104 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.ui.containers.custom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.server.core.IRuntimeType;
import org.jboss.ide.eclipse.as.classpath.core.runtime.IRuntimePathProvider;
import org.jboss.ide.eclipse.as.classpath.core.runtime.path.internal.LayeredProductPathProvider;
import org.jboss.ide.eclipse.as.classpath.core.runtime.path.internal.RuntimePathProviderFileset;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.tools.foundation.core.tasks.TaskModel;
import org.jboss.tools.foundation.ui.xpl.taskwizard.IWizardHandle;
import org.jboss.tools.foundation.ui.xpl.taskwizard.TaskWizard;
import org.jboss.tools.foundation.ui.xpl.taskwizard.WizardFragment;



public class RuntimeClasspathProviderWizard extends TaskWizard {
	
	static final String RUNTIME_TYPE = "rtType"; //$NON-NLS-1$
	static final String PROVIDER_TYPE = "pathProviderType"; //$NON-NLS-1$
	static final String CREATED_PATH_PROVIDER = "createdProvider"; //$NON-NLS-1$
	
	
	public RuntimeClasspathProviderWizard(IRuntimeType rtType) {
		super("New Default Classpath Entry", new RootFragment());
		getTaskModel().putObject(RUNTIME_TYPE, rtType);
	}
	
	protected static class RootFragment extends WizardFragment {
		private SelectFilesetTypeFragment selectTypeFragment = null;
		protected void createChildFragments(List<WizardFragment> list) {
			if(selectTypeFragment == null) {
				selectTypeFragment = new SelectFilesetTypeFragment();
			}
			list.add(selectTypeFragment);
		}
	}

	private static RuntimePathProviderType[] getTypesForRuntime(TaskModel tm) {
		ArrayList<RuntimePathProviderType> c = new ArrayList<RuntimePathProviderType>();
		for( int i = 0; i < types.length; i++ ) {
			if( types[i].isValidFor((IRuntimeType)tm.getObject(RUNTIME_TYPE)))
				c.add(types[i]);
		}
		return c.toArray(new RuntimePathProviderType[c.size()]);
	}
	
	private static RuntimePathProviderType[] types = {
		new FilesetRuntimePathProviderTypeImpl(),
		new LayeredRuntimePathProviderTypeImpl()
	};
	
	private static interface RuntimePathProviderType {
		public boolean isValidFor(IRuntimeType type);
		public String getName();
	}
	
	private static class FilesetRuntimePathProviderTypeImpl implements RuntimePathProviderType {
		public boolean isValidFor(IRuntimeType type) {
			return true;
		}
		public String getName() {
			return "Standard Fileset";
		}
	}
	private static class LayeredRuntimePathProviderTypeImpl implements RuntimePathProviderType {
		public boolean isValidFor(IRuntimeType type) {
			ServerExtendedProperties props = type == null ? null : 
			(ServerExtendedProperties)Platform.getAdapterManager().getAdapter(type, ServerExtendedProperties.class);
			return props != null && props.getFileStructure() == props.FILE_STRUCTURE_CONFIG_DEPLOYMENTS;
		}
		public String getName() {
			return "Layered Product Module Folder";
		}
	}
	
	protected static class SelectFilesetTypeFragment extends WizardFragment {
		private IWizardHandle handle;
		private ITreeContentProvider contentProvider;
		private TreeViewer viewer;
		public boolean hasComposite() {
			return true;
		}

		/**
		 * Creates the composite associated with this fragment.
		 * This method is only called when hasComposite() returns true.
		 * 
		 * @param parent a parent composite
		 * @param handle a wizard handle
		 * @return the created composite
		 */
		public Composite createComposite(Composite parent, IWizardHandle handle) {
			this.handle = handle;
			handle.setTitle("Select a classpath entry type");
			handle.setDescription("Please select one of the classpath entry types below.");
			handle.setImageDescriptor( AbstractUIPlugin.imageDescriptorFromPlugin( "org.eclipse.wst.common.modulecore.ui", "icons/assembly-banner.png" ) ); //$NON-NLS-1$
			Composite c = new Composite(parent, SWT.NONE);
			c.setLayout(new GridLayout());
			viewer = new TreeViewer(c, SWT.SINGLE | SWT.BORDER);
			viewer.getTree().setLayoutData(new GridData( GridData.FILL_BOTH ));
			viewer.setLabelProvider(new LabelProvider() {
				public Image getImage(Object element) {
					return null;
				}
				public String getText(Object element) {
					return element == null ? "" : ((RuntimePathProviderType)element).getName();//$NON-NLS-1$
				}
			});
			viewer.setContentProvider(getContentProvider());
			viewer.setComparator( new ViewerComparator() );
			viewer.setInput(ResourcesPlugin.getWorkspace());
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					viewerSelectionChanged();
				}
			});
			viewer.addDoubleClickListener(new IDoubleClickListener() {
				public void doubleClick(DoubleClickEvent event) {
					advanceToNextPageOrFinish();
				}
			});
			return c;

		}
		protected void viewerSelectionChanged() {
			IStructuredSelection sel = ((IStructuredSelection)viewer.getSelection()); 
			Object o = sel == null ? null : sel.getFirstElement();
			getTaskModel().putObject(PROVIDER_TYPE, o);
			setComplete(o != null);
			handle.update();
		}
		protected ITreeContentProvider getContentProvider() {
			if( contentProvider == null ) {
				contentProvider = new ITreeContentProvider() {
					public Object[] getElements(Object inputElement) {
						return getTypesForRuntime(getTaskModel());
					}
					public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
					}
					public void dispose() {
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
				};
			}
			return contentProvider;
		}
		

		private LayeredPathProviderFragment layeredFragment = null;
		private FilesetClasspathProviderFragment filesetFragment = null;
		
		protected void createChildFragments(List<WizardFragment> list) {
			if( getTaskModel() != null ) {
				RuntimePathProviderType t = (RuntimePathProviderType)getTaskModel().getObject(PROVIDER_TYPE);
				if(t instanceof LayeredRuntimePathProviderTypeImpl) {
					if( layeredFragment == null ) {
						layeredFragment = new LayeredPathProviderFragment();
					}
					list.add(layeredFragment);
				} else if( t instanceof FilesetRuntimePathProviderTypeImpl) {
					if( filesetFragment == null ) {
						filesetFragment = new FilesetClasspathProviderFragment();
					}
					list.add(filesetFragment);
				}
			}
		}
	}

	public IRuntimePathProvider getRuntimePathProvider() {
		return (IRuntimePathProvider)getTaskModel().getObject(CREATED_PATH_PROVIDER);
	}
}