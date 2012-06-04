/*******************************************************************************
 * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.internal.ImageResource;
import org.eclipse.wst.server.ui.internal.wizard.NewServerWizard;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;


public class SelectServerActionDelegate extends AbstractServerActionDelegate 
	implements IWorkbenchWindowPulldownDelegate {
	
	private ArrayList images = new ArrayList();
	
	protected void safeSelectionChanged(IAction action, ISelection selection) {
		this.action = action;
		update();
	}
	
	protected void update() {
		if (action == null)	return;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				update0();
			}
		});
	}
	
	private void update0() {
		if(action == null) return;
		IServer server = ServerManager.getInstance().getSelectedServer();
		if( server != null && server.getServerType() != null ) {
			ImageDescriptor id = ImageResource.getImageDescriptor(server.getServerType().getId());
			action.setImageDescriptor(id);
		}
		action.setText(null);
		action.setToolTipText(ServerActionMessages.SELECT_A_SERVER);
	}
	
	protected void doRun() {
		getMenu(window.getShell()).setVisible(true);
	}
	
	public void dispose() {
		action = null;
	}
	
	public Menu getMenu(Control parent) {
		cleanImages();
		
		Menu menu = new Menu(parent);
		IServer[] servers = ServerManager.getInstance().getServers();
		for (int i = 0; i < servers.length; i++) {
			createServerItem(menu, servers[i]);
		}
		new MenuItem(menu, SWT.SEPARATOR);
		createNewServerItem(menu);
		return menu;
	}
	
	// prevent memory leaks
	protected void cleanImages() {
		Iterator<Image> i = images.iterator();
		while(i.hasNext()) 
			((Image)i.next()).dispose();
		images.clear();
	}
	
	private void createServerItem(Menu menu, final IServer server) {
		MenuItem item = new MenuItem(menu, SWT.RADIO);
		if(server == ServerManager.getInstance().getSelectedServer()) {
			item.setSelection(true);
		}
		
		item.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				ServerManager.getInstance().setSelectedServer(server.getId());
				update();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		item.setText(server.getName());
		ImageDescriptor id = ImageResource.getImageDescriptor(server.getServerType().getId());
		Image i = id.createImage();
		images.add(i);
		item.setImage(i);
	}
	
	private void createNewServerItem(Menu menu) {
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText(ServerActionMessages.SelectServerActionDelegate_NewServerMenuItem);
		item.setImage(JBossServerUISharedImages.getImage(JBossServerUISharedImages.GENERIC_SERVER_IMAGE));
		item.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				newServer();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				newServer();
				widgetSelected(e);
			}
		});
	}
	
	private void newServer() {
		NewServerWizard wizard = new NewServerWizard();
		WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
		if (dialog.open() != Window.CANCEL) {
			IServer server = (IServer)wizard.getRootFragment().getTaskModel().getObject(TaskModel.TASK_SERVER);
			if(server != null) {
				ServerManager.getInstance().setSelectedServer(server.getId());
				update();
			}
		}
	}
}
