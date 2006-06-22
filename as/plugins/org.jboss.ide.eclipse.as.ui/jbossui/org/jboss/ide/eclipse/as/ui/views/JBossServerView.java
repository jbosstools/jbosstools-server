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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;
import org.eclipse.wst.server.ui.internal.view.servers.ServersView;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin.ServerViewProvider;


public class JBossServerView extends ServersView {

	private static final String TAG_SASHFORM_HEIGHT = "sashformHeight"; 
	private static final String TAG_SASHFORM_PROPERTIES = "sashformProperties"; 
	
	
	protected int[] sashRows; // For the sashform sashRows
	protected int[] sashCols; // for the properties sashform
	
	protected SashForm form;
	protected SashForm propertiesForm;
	
	protected JBossServerTableViewer jbViewer;
	protected Tree jbTreeTable;
	protected TreeViewer propertiesViewer;
	
	
	protected Action deleteModuleAction, republishModuleAction;
	
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
		addPropertyViewer(form);
		form.setWeights(sashRows);
		propertiesForm.setWeights(sashCols);
		
		addListeners();
		
		doMenuStuff(parent);
	}

	private void addServerViewer(Composite form) {
		Composite child1 = new Composite(form,SWT.NONE);
		child1.setLayout(new GridLayout());
		super.createPartControl(child1);

	}
	
	private void addSecondViewer(Composite form) {
		Composite child2 = new Composite(form,SWT.NONE);
		child2.setLayout(new FillLayout());
		jbTreeTable = new Tree(child2, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		jbViewer = new JBossServerTableViewer(jbTreeTable);
	}
	
	private void addPropertyViewer(Composite form) {
		propertiesForm = new SashForm(form, SWT.HORIZONTAL);
		propertiesForm.setLayout(new FillLayout());
		
		Composite c1 = new Composite(propertiesForm, SWT.NONE);
		c1.setLayout(new FillLayout());
		Tree tTable = new Tree(c1, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.NONE);
		tTable.setHeaderVisible(true);
		tTable.setLinesVisible(true);
		tTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		tTable.setFont(c1.getFont());

		TreeColumn column = new TreeColumn(tTable, SWT.SINGLE);
		column.setText("Property");
		column.setWidth(50);
		
		TreeColumn column2 = new TreeColumn(tTable, SWT.SINGLE);
		column2.setText("Value");
		column2.setWidth(50);

		propertiesViewer = new TreeViewer(tTable);
		
		Composite c2 = new Composite(propertiesForm, SWT.NONE);
		c2.setLayout(new FillLayout());
		Text t2 = new Text(c2, SWT.BORDER);
		t2.setText("Box 2");
	}
	
	
	
	public void addListeners() {
		
		jbViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection sel = event.getSelection();
				if( sel instanceof IStructuredSelection ) {
					Object o = ((IStructuredSelection)sel).getFirstElement();
					if( o != null ) {
						ServerViewProvider provider = jbViewer.getParentViewProvider(o);
						if( provider != null ) {
							int propertiesType = provider.getDelegate().selectedObjectViewType(o);
							if( propertiesType == JBossServerViewExtension.PROPERTIES ||  
									propertiesType == JBossServerViewExtension.PROPERTIES_AND_TEXT ) {
								propertiesViewer.setContentProvider(provider.getDelegate().getPropertiesContentProvider());
								propertiesViewer.setLabelProvider(provider.getDelegate().getPropertiesLabelProvider());
								propertiesViewer.setInput(o);
								propertiesViewer.refresh();
							}
						} else {
							propertiesViewer.setInput(null);
						}
					}
					
				}
			} 
			
		});
		
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
				
				if( jbViewer.getInput() != server )  {
					jbViewer.setInput(server);
				}
			} 
			
		});


	}
	protected void doMenuStuff(Composite parent) {
		MenuManager menuManager = new MenuManager("#PopupMenu"); 
		menuManager.setRemoveAllWhenShown(true);
		final Shell shell = jbTreeTable.getShell();
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				jbViewer.fillJBContextMenu(shell, mgr);
			}
		});
		Menu menu = menuManager.createContextMenu(parent);
		jbTreeTable.setMenu(menu);

	}
	
	
	/**
	 * Save some state
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		ServerUIPlugin.getPreferences().setShowOnActivity(false);
		int sum = 0;
		sashRows = new int[3];
		for (int i = 0; i < sashRows.length; i++) {
			sashRows[i] = (i == sashRows.length-1 ? 100-sum : 50);
			if (memento != null) {
				Integer in = memento.getInteger(TAG_SASHFORM_HEIGHT + i);
				if (in != null && in.intValue() > 5)
					sashRows[i] = in.intValue();
			}
			sum += sashRows[i];
		}
		
		sashCols = new int[2];
		sashCols[0] = 100;
		sashCols[1] = 0;
		
	}

	public void saveState(IMemento memento) {
		int[] weights = form.getWeights();
		for (int i = 0; i < weights.length; i++) {
			if (weights[i] != 0)
				memento.putInteger(TAG_SASHFORM_HEIGHT + i, weights[i]);
		}
		
		weights = propertiesForm.getWeights();
		for( int i = 0; i < weights.length; i++ ) {
			if (weights[i] != 0)
				memento.putInteger(TAG_SASHFORM_PROPERTIES + i, weights[i]);

		}
		
	}

}