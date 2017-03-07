/******************************************************************************* 
 * Copyright (c) 2017 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.jmx.ui.internal.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.jboss.tools.foundation.ui.xpl.taskwizard.IWizardHandle;
import org.jboss.tools.foundation.ui.xpl.taskwizard.WizardFragment;
import org.jboss.tools.jmx.core.ExtensionManager;
import org.jboss.tools.jmx.core.IConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.ui.ConnectionWizardPage;
import org.jboss.tools.jmx.ui.Messages;
import org.jboss.tools.jmx.ui.UIExtensionManager;
import org.jboss.tools.jmx.ui.UIExtensionManager.ConnectionProviderUI;

public class JMXConnectionTypeFragment extends WizardFragment {
	private TreeViewer viewer;
	private IWizardHandle handle;
	private Map<String, ConnectionProviderUI> providerMap;
	private Map<String, WizardFragment> childFragmentMap;
	private IConnectionProvider selected = null;
	
	public JMXConnectionTypeFragment() {
		providerMap = UIExtensionManager.getConnectionUIElements();
		childFragmentMap = new HashMap<>();
	}
	
	@Override
	public boolean hasComposite() {
		return true;
	}
	@Override
	public Composite createComposite(Composite parent, IWizardHandle handle) {
		this.handle = handle;
		getPage().setTitle(Messages.NewConnectionWizard_CreateNewConnection);
		getPage().setDescription(Messages.NewConnectionWizard_CreateNewConnection);
		return createControl(parent);
	}
	
	public Composite createControl(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new FillLayout());
		viewer = new TreeViewer(main);
		viewer.setContentProvider(new FirstPageContentProvider());
		viewer.setLabelProvider(new FirstPageLabelProvider());
		viewer.setInput(this);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				viewerSelectionChanged();
			}
		});
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				TreeItem item = viewer.getTree().getItems()[0];
				viewer.setSelection(new StructuredSelection(item.getData()));
			}
		});
		return main;
	}

	@Override
	protected void createChildFragments(List<WizardFragment> list) {
		if( selected != null ) {
			WizardFragment all = childFragmentMap.get(selected.getId());
			list.addAll(Arrays.asList(all));
		}
	}
	
	private void viewerSelectionChanged() {
		IStructuredSelection ssel = (IStructuredSelection) viewer.getSelection();
		IConnectionProvider cp = (IConnectionProvider) ssel.getFirstElement();
		selected = cp;
		if( selected != null ) {
			String cpId = cp.getId();
			if( childFragmentMap.get(cpId) == null ) {
				ConnectionProviderUI ui = providerMap.get(cpId);
				childFragmentMap.put(cpId, ui.createFragments());
			}
		}
		
		handle.update();
	}

	@Override
	public void performFinish(IProgressMonitor monitor) throws CoreException {
		if( selected != null ) {
			WizardFragment typeRoot = childFragmentMap.get(selected.getId());
			ConnectionWizardPage wp = (ConnectionWizardPage)typeRoot;
			IConnectionWrapper wrap = wp.getConnection();
			if( wrap != null ) {
				wrap.getProvider().addConnection(wrap);
			}
		}
	}
	
	private class FirstPageContentProvider implements ITreeContentProvider {
		public Object[] getElements(Object inputElement) {
			List<IConnectionProvider> providers = new ArrayList<>();
			Map<String, ConnectionProviderUI> map = UIExtensionManager.getConnectionUIElements();
			Set<String> keys = map.keySet();
			Iterator<String> i = keys.iterator();
			while (i.hasNext()) {
				String id = i.next();
				if (ExtensionManager.getProvider(id) != null && ExtensionManager.getProvider(id).canCreate())
					providers.add(ExtensionManager.getProvider(id));
			}

			return providers.toArray(new IConnectionProvider[providers.size()]);
		}

		public void dispose() {
			// no need
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// no need
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
	}

	private class FirstPageLabelProvider extends LabelProvider {
		private HashMap<String, Image> images = new HashMap<String, Image>();

		public void dispose() {
			for (Iterator<Image> i = images.values().iterator(); i.hasNext();)
				i.next().dispose();
			super.dispose();
		}

		public Image getImage(Object element) {
			if (element instanceof IConnectionProvider) {
				ConnectionProviderUI ui = UIExtensionManager
						.getConnectionProviderUI(((IConnectionProvider) element).getId());
				if (ui != null) {
					String id = ui.getId();
					if (images.containsKey(id))
						return images.get(id);
					images.put(id, ui.getImageDescriptor().createImage());
					return images.get(id);
				}
			}
			return null;
		}

		public String getText(Object element) {
			if (element instanceof IConnectionProvider) {
				ConnectionProviderUI ui = UIExtensionManager
						.getConnectionProviderUI(((IConnectionProvider) element).getId());
				if (ui != null) {
					return ui.getName();
				}
			}
			return element == null ? "" : element.toString();//$NON-NLS-1$
		}

	}
}
