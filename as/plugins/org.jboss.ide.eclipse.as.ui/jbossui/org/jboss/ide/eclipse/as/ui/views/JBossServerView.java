/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.ui.views;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;
import org.eclipse.wst.server.ui.internal.view.servers.ServersView;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin.ServerViewProvider;


public class JBossServerView extends ServersView {

	private static final String TAG_SASHFORM_HEIGHT = "sashformHeight"; 
		
	protected int[] sashRows; // For the sashform sashRows
	
	protected SashForm form;
	
	protected JBossServerTableViewer jbViewer;
	protected Tree jbTreeTable;
	
	
	
	public static JBossServerView instance;
	public static JBossServerView getDefault() {
		return instance;
	}
	
	
	public JBossServerView() {
		super();
		instance = this;		
	}
	
	public void createPartControl(Composite parent) {
				
		form = new SashForm(parent, SWT.VERTICAL);
		form.setLayout(new FillLayout());
		
		addServerViewer(form);
		addSecondViewer(form);
		form.setWeights(sashRows);
		createActions();
		addListeners();
		doMenuStuff(parent);
	}

	private void addServerViewer(Composite form) {
		Composite child1 = new Composite(form,SWT.NONE);
		child1.setLayout(new GridLayout());
		super.createPartControl(child1);
		
		tableViewer.setContentProvider(new TrimmedServerContentProvider());
	}
	
	/**
	 * Only shows the servers in the top part of the sashform
	 * @author rstryker
	 *
	 */
	public class TrimmedServerContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		public Object[] getElements(Object element) {
			return JBossServerCore.getIServerJBossServers();
		}

		public void inputChanged(Viewer theViewer, Object oldInput, Object newInput) {
			// do nothing
		}
		
		public void dispose() {
			// do nothing
		}

		public Object[] getChildren(Object element) {
			return new Object[0];
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
				return false;
		}
	}
	
	private void addSecondViewer(Composite form) {
		Composite child2 = new Composite(form,SWT.NONE);
		child2.setLayout(new FillLayout());
		jbTreeTable = new Tree(child2, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		jbViewer = new JBossServerTableViewer(jbTreeTable);
		getSite().setSelectionProvider(jbViewer);
	}
	
	
	public void createActions() {
	}
	
	public IServer getSelectedServer() {
		return (IServer)jbViewer.getInput();
	}
	
	public void refreshJBTree(Object obj) {
		final Object obj2 = obj;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					// If it's null, refresh the whole thing
					if( obj2 == null ) { 
						Object[] expanded = JBossServerView.this.jbViewer.getExpandedElements();
						JBossServerView.this.jbViewer.refresh();
						JBossServerView.this.jbViewer.setExpandedElements(expanded);
					} else {
						Object[] expanded = JBossServerView.this.jbViewer.getExpandedElements();
						JBossServerView.this.jbViewer.refresh(obj2);
						JBossServerView.this.jbViewer.setExpandedElements(expanded);
					}
				} catch (Exception e) {
					// ignore
				}
			}
		});
	}
	
	public void addListeners() {
		
		/*
		 * Handles the selection of the server viewer which is embedded in my sashform
		 */
		
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object current = jbViewer.getInput();
				Object selection = ((TreeSelection)event.getSelection()).getFirstElement();
				Object server = selection;
				if( selection instanceof ModuleServer ) {
					server = ((ModuleServer)selection).server;
				}

				if( selection == null ) return;
				
				//if( server != jbViewer.getInput())
					jbViewer.setInput(server);
			} 
			
		});
	}
	protected void doMenuStuff(Composite parent) {
		MenuManager menuManager = new MenuManager("#PopupMenu"); 
		menuManager.setRemoveAllWhenShown(true);
		final Shell shell = jbTreeTable.getShell();
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				jbViewer.fillSelectedContextMenu(shell, mgr);
				mgr.add(new Separator());
				jbViewer.fillJBContextMenu(shell, mgr);
			}
		});
		Menu menu = menuManager.createContextMenu(parent);
		jbTreeTable.setMenu(menu);
	}
	
	/**
	 * Save / Load some state  (width / height of boxes)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		ServerUIPlugin.getPreferences().setShowOnActivity(false);
		int sum = 0;
		sashRows = new int[2];
		for (int i = 0; i < sashRows.length; i++) {
			sashRows[i] = 50;
			if (memento != null) {
				Integer in = memento.getInteger(TAG_SASHFORM_HEIGHT + i);
				if (in != null && in.intValue() > 5)
					sashRows[i] = in.intValue();
			}
			sum += sashRows[i];
		}
	}

	public void saveState(IMemento memento) {
		int[] weights = form.getWeights();
		for (int i = 0; i < weights.length; i++) {
			if (weights[i] != 0)
				memento.putInteger(TAG_SASHFORM_HEIGHT + i, weights[i]);
		}
	}

	
	
	public Object getAdapter(Class adaptor) {
		//ASDebug.p("It wants an adaptor: " + adaptor.getCanonicalName(), this);
		if( adaptor == IPropertySheetPage.class) {
			return jbViewer.getPropertySheet();
		}
		return super.getAdapter(adaptor);
	}
	
    public void dispose() {
    	super.dispose();
    	jbViewer.dispose();
    }


	public JBossServerTableViewer getJbViewer() {
		return jbViewer;
	}


}