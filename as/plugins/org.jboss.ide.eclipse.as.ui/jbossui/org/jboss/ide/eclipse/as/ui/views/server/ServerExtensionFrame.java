/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.ui.views.server;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;
import org.jboss.ide.eclipse.as.ui.views.server.JBossServerView.IServerViewFrame;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class ServerExtensionFrame extends Composite implements IServerViewFrame {
		
		private Tree jbTreeTable;
		private ExtensionTableViewer jbViewer;
		private JBossServerView view;
		public ServerExtensionFrame(Composite parent, JBossServerView view) {
			super(parent, SWT.NONE);
			this.view = view;
			setLayout(new FillLayout());
			jbTreeTable = new Tree(this, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			jbViewer = new ExtensionTableViewer(jbTreeTable);
			view.getSite().setSelectionProvider(jbViewer);
			addListeners();
			doMenuStuff(parent);
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
		
		public ExtensionTableViewer getViewer() {
			return jbViewer;
		}

		public void refresh() {
			jbViewer.refresh();
		}
		
		public void addListeners() {
			
			/*
			 * Handles the selection of the server viewer which is embedded in my sashform
			 */
			view.getServerFrame().getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					Object selection = ((TreeSelection)event.getSelection()).getFirstElement();
					Object server = selection;
					if( selection instanceof ModuleServer ) {
						server = ((ModuleServer)selection).server;
					}

					if( selection == null ) return;
					if( server != jbViewer.getInput()) {
						// find the newest copy of the server
						IServer tmp = ServerCore.findServer(((IServer)server).getId());
						jbViewer.setInput(tmp); 
					} else {
						jbViewer.refresh();
					}
				} 
				
			});
		}


		public IAction[] getActionBarActions() {
			return new IAction[] {}; // none
		}

		public int getDefaultSize() {
			return 0;
		}
	}
