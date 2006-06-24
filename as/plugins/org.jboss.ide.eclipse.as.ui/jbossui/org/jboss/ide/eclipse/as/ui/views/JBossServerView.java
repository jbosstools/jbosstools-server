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

import java.util.Properties;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
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
import org.eclipse.swt.widgets.Display;
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
import org.eclipse.wst.server.ui.internal.view.servers.ServerTableViewer;
import org.eclipse.wst.server.ui.internal.view.servers.ServersView;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.util.ASDebug;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin.ServerViewProvider;
import org.jboss.ide.eclipse.as.ui.viewproviders.ISimplePropertiesHolder;
import org.jboss.ide.eclipse.as.ui.viewproviders.JBossServerViewExtension;
import org.jboss.ide.eclipse.as.ui.viewproviders.SimplePropertiesContentProvider;


public class JBossServerView extends ServersView {

	private static final String TAG_SASHFORM_HEIGHT = "sashformHeight"; 
	private static final String TAG_SASHFORM_PROPERTIES = "sashformProperties"; 
	private static final String PROPERTIES_COLUMNS = "propertiesColumns"; 
		
	protected int[] sashRows; // For the sashform sashRows
	protected int[] sashCols; // for the properties sashform
	protected int[] propertyCols; // For the property columns
	
	protected SashForm form;
	protected SashForm propertiesForm;
	
	protected JBossServerTableViewer jbViewer;
	protected Tree jbTreeTable;
	protected TreeViewer propertiesViewer;
	protected Text propertiesText;
	
	
	protected SimplePropertiesContentProvider topLevelPropertiesProvider;
	
	
	protected Action hidePropertiesAction, hideTextAction, hidePropertiesAndTextAction;
	
	public static JBossServerView instance;
	public static JBossServerView getDefault() {
		return instance;
	}
	
	
	public JBossServerView() {
		super();
		instance = this;		
	}
	
	public void createPartControl(Composite parent) {
				
		form = new SashForm(parent, SWT.HORIZONTAL);
		form.setLayout(new FillLayout());
		
		addServerViewer(form);
		addSecondViewer(form);
		addPropertyViewer(form);
		form.setWeights(sashRows);
		propertiesForm.setWeights(sashCols);
		createActions();
		addListeners();
		topLevelPropertiesProvider = new SimplePropertiesContentProvider(new TopLevelProperties());
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
		column.setText(Messages.property);
		column.setWidth(propertyCols[0]);
		
		TreeColumn column2 = new TreeColumn(tTable, SWT.SINGLE);
		column2.setText(Messages.value);
		column2.setWidth(propertyCols[1]);

		propertiesViewer = new TreeViewer(tTable);
		
		Composite c2 = new Composite(propertiesForm, SWT.NONE);
		c2.setLayout(new FillLayout());
		propertiesText = new Text(c2, SWT.BORDER);
	}
	
	public void createActions() {
		hidePropertiesAction = new Action() {
			public void run() {
				sashCols = propertiesForm.getWeights();
				if( sashCols[1] == 0 ) { 
					hidePropertiesAndText();
				} else {
					sashCols[1] = 100;
					sashCols[0] = 0;
					propertiesForm.setWeights(sashCols);
				}
			}
		};
		hidePropertiesAction.setText(Messages.HidePropertiesAction);
		
		hideTextAction = new Action() {
			public void run() {
				sashCols = propertiesForm.getWeights();
				if( sashCols[0] == 0 ) { 
					hidePropertiesAndText();
				} else {
					sashCols[0] = 100;
					sashCols[1] = 0;
					propertiesForm.setWeights(sashCols);
				}
			}
		};
		hideTextAction.setText(Messages.HideTextAction);
		
		hidePropertiesAndTextAction = new Action() {
			public void run() {
				hidePropertiesAndText();
			}
		};
		hidePropertiesAndTextAction.setText(Messages.HideLowerFrameAction);
	}
	
	public void hidePropertiesAndText() {
		sashRows = form.getWeights();
		sashRows[2] = 0;
		form.setWeights(sashRows);
	}
	
	public void refreshJBTree(ServerViewProvider provider) {
		ASDebug.p("Refreshing the tree", this);
		final ServerViewProvider provider2 = provider;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					JBossServerView.this.jbViewer.refresh(provider2);
					ISelection sel = JBossServerView.this.jbViewer.getSelection();
					JBossServerView.this.jbViewer.setSelection(sel);
				} catch (Exception e) {
					// ignore
				}
			}
		});

	}
	
	public void addListeners() {
		
		jbViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				
				// If the properties aren't showing, dont bother
				if( form.getWeights()[2] == 0 ) 
					return;
				
				ISelection sel = event.getSelection();
				if( sel instanceof IStructuredSelection ) {
					Object selected = ((IStructuredSelection)sel).getFirstElement();
					if( selected != null ) {
						ServerViewProvider provider = jbViewer.getParentViewProvider(selected);
						if( provider != null ) {
							// We have a provider (extension) and a selected object. 
							// Maintain the properties / text boxes.
							updatePropertiesTextViewers(provider, selected);
						} else if( selected instanceof JBossServer || selected instanceof ServerViewProvider ){
							updatePropertiesTopLevel(selected);
						} else {
							clearPropertiesTextViewers();
						}
					} else {
						clearPropertiesTextViewers();
					}
					propertiesForm.setWeights(sashCols);
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
				
				//if( jbViewer.getInput() != server )  {
					jbViewer.setInput(server);
				//}
			} 
			
		});
	}
	
	public void clearPropertiesTextViewers() {
		try {
			propertiesViewer.setInput(null);
		} catch( Exception e) {}
		
		propertiesText.setText("");	
		sashCols[0] = 100;
		sashCols[1] = 0;
	}

	public void updatePropertiesTextViewers(ServerViewProvider provider, Object selected) {
		// Surround the entire method
		try {
			
			int propertiesType = provider.getDelegate().selectedObjectViewType(selected);
			if( propertiesType == JBossServerViewExtension.PROPERTIES ||  
					propertiesType == JBossServerViewExtension.PROPERTIES_AND_TEXT ) {
							
				propertiesViewer.setContentProvider(provider.getDelegate().getPropertiesContentProvider());
				propertiesViewer.setLabelProvider(provider.getDelegate().getPropertiesLabelProvider());
				propertiesViewer.setInput(selected);
				propertiesViewer.refresh();
				
				// set weights
				if( propertiesType == JBossServerViewExtension.PROPERTIES) {
					sashCols[0] = 100;
					sashCols[1] = 0;
				} else {
					sashCols[0] = 50;
					sashCols[1] = 50;
				}
			} else {
				propertiesText.setText(provider.getDelegate().getPropertiesText(selected));
				sashCols[0] = 0;
				sashCols[1] = 100;
			}
			
		} catch( Exception e ) {
			clearPropertiesTextViewers();
		}

	}

	public void updatePropertiesTopLevel(Object selected) {
		propertiesViewer.setContentProvider(topLevelPropertiesProvider);
		propertiesViewer.setLabelProvider(topLevelPropertiesProvider);
		propertiesViewer.setInput(selected);
		propertiesViewer.refresh();
		sashCols[0] = 100;
		sashCols[1] = 0;
		
	}
	protected void doMenuStuff(Composite parent) {
		MenuManager menuManager = new MenuManager("#PopupMenu"); 
		menuManager.setRemoveAllWhenShown(true);
		final Shell shell = jbTreeTable.getShell();
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				ISelection sel = jbViewer.getSelection();
				if( sel instanceof IStructuredSelection ) {
					Object selected = ((IStructuredSelection)sel).getFirstElement();
					if( selected != null ) {
						ServerViewProvider provider = jbViewer.getParentViewProvider(selected);
						if( provider != null ) 
							provider.getDelegate().fillContextMenu(shell, mgr, selected);
					}
				}
				mgr.add(new Separator());
				jbViewer.fillJBContextMenu(shell, mgr);
			}
		});
		Menu menu = menuManager.createContextMenu(parent);
		jbTreeTable.setMenu(menu);
		
		
		
		MenuManager menuManager2 = new MenuManager("#PopupMenu"); 
		menuManager2.setRemoveAllWhenShown(true);
		final Shell shell2 = propertiesForm.getShell();
		menuManager2.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillPropertiesContextMenu(shell2, mgr);
			}
		});
		Menu menu2 = menuManager2.createContextMenu(parent);
		propertiesViewer.getTree().setMenu(menu2);
		propertiesText.setMenu(menu2);
	}
	
	public void fillPropertiesContextMenu(Shell shell, IMenuManager mgr) {
		sashCols = propertiesForm.getWeights();
		if( sashCols[0] != 0 ) 
			mgr.add(hidePropertiesAction);
		if( sashCols[1] != 0 ) 
			mgr.add(hideTextAction);
		
		// If only one of the two are present, do not show the 'hide both' action
		if( !(sashCols[0] != 0 && sashCols[1] == 0 || sashCols[1] != 0 && sashCols[0] == 0 )) 
			mgr.add(hidePropertiesAndTextAction);
	}
	/**
	 * Save / Load some state  (width / height of boxes)
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
		
		
		propertyCols = new int[2];
		for (int i = 0; i < propertyCols.length; i++) {
			propertyCols[i] = 100;
			if (memento != null) {
				Integer in = memento.getInteger(PROPERTIES_COLUMNS + i);
				if (in != null && in.intValue() > 5)
					propertyCols[i] = in.intValue();
			}
		}
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
		
		Tree propTree = propertiesViewer.getTree();
		TreeColumn[] columns = propTree.getColumns();
		for( int i = 0; i < columns.length; i++ ) {
			memento.putInteger(PROPERTIES_COLUMNS + i, columns[i].getWidth());
		}
		
	}

	
	
	/**
	 * Properties for the top level elements 
	 *    (a server or a category / extension point 
	 * @author rstryker
	 *
	 */
	class TopLevelProperties implements ISimplePropertiesHolder {
		public Properties getProperties(Object selected) {
			Properties ret = new Properties();
			if( selected instanceof ServerViewProvider ) {
				ServerViewProvider provider = (ServerViewProvider)selected;
				ret.setProperty(Messages.ExtensionID, provider.getId());
				ret.setProperty(Messages.ExtensionName, provider.getName());
				ret.setProperty(Messages.ExtensionDescription, provider.getDescription());
				ret.setProperty(Messages.ExtensionProviderClass, provider.getDelegateName());
			}
			
			if( selected instanceof JBossServer) {
				JBossServer server = (JBossServer)selected;
				String home = server.getRuntimeConfiguration().getServerHome();
				
				ret.setProperty(Messages.ServerRuntimeVersion, server.getJBossRuntime().getVersionDelegate().getId());
				ret.setProperty(Messages.ServerHome, home);
				ret.setProperty(Messages.ServerConfigurationName, server.getRuntimeConfiguration().getJbossConfiguration());
				ret.setProperty(Messages.ServerDeployDir, 
						server.getRuntimeConfiguration().getDeployDirectory().replace(home, "(home)"));
			}
			return ret;
		}


		public String[] getPropertyKeys(Object selected) {
			if( selected instanceof ServerViewProvider) {
				return new String[] { 
						Messages.ExtensionName, Messages.ExtensionDescription, 
						Messages.ExtensionID, Messages.ExtensionProviderClass
				};
				
			}
			if( selected instanceof JBossServer ) {
				return new String[] {
						Messages.ServerRuntimeVersion, Messages.ServerHome, 
						Messages.ServerConfigurationName, Messages.ServerDeployDir,
				};
			}
			return new String[] {};
		}
	}


}