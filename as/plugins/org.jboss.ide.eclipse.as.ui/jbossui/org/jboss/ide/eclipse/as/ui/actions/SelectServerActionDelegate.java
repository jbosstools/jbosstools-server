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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.ui.internal.ImageResource;
import org.eclipse.wst.server.ui.internal.view.servers.PublishAction;
import org.eclipse.wst.server.ui.internal.view.servers.StartAction;
import org.eclipse.wst.server.ui.internal.view.servers.StopAction;
import org.eclipse.wst.server.ui.internal.wizard.NewServerWizard;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.wtp.core.server.publish.FullPublishJobScheduler;


public class SelectServerActionDelegate implements IWorkbenchWindowPulldownDelegate {

	protected IWorkbenchWindow window;
	private boolean isInitialized = false;
	private Image startImage = ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_START).createImage();
	private Image debugImage = ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_START_DEBUG).createImage();
	private Image profileImage = ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_START_PROFILE).createImage();
	private Image stopImage = ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_STOP).createImage();
	private Image publishImage = ImageResource.getImageDescriptor(ImageResource.IMG_ELCL_PUBLISH).createImage();
	
	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
		loadServers();
	}
	
	private void loadServers() {
		new Thread("Loading Server Adapters") {
			public void run() {
				ServerCore.getServers();
				setInitialized(true);
			}
		}.start();
	}

	private void setInitialized(boolean status) {
		this.isInitialized = status;
	}
	
	@Override
	public void run(IAction action) {
		if(!isInitialized) {
			loadServers();
		} else {
			doRun();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing
	}
	
	protected void doRun() {
		getMenu(window.getShell()).setVisible(true);
	}
	
	public Menu getMenu(Control parent) {
		Menu menu = new Menu(parent);
		if( isInitialized ) {
			IServer[] servers = ServerCore.getServers();
			for (int i = 0; i < servers.length; i++) {
				createServerItem(menu, servers[i]);
			}
			new MenuItem(menu, SWT.SEPARATOR);
			createNewServerItem(menu);
		} else {
			MenuItem item = new MenuItem(menu, SWT.RADIO);
			item.setText("Loading...");
			item.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK));
		}
		return menu;
	}
	
	private void createServerItem(Menu menu, final IServer server) {
		Menu serverMenu = new Menu(menu);
		MenuItem serverItem = new MenuItem(menu, SWT.CASCADE);
		serverItem.setText(server.getName());
		serverItem.setMenu(serverMenu);
		
		MenuItem start = new MenuItem(serverMenu, SWT.NONE);
		start.setText("Start");
		start.setImage(startImage);
		MenuItem debugStart = new MenuItem(serverMenu, SWT.NONE);
		debugStart.setText("Debug");
		debugStart.setImage(debugImage);
		MenuItem profileStart = new MenuItem(serverMenu, SWT.NONE);
		profileStart.setText("Profile");
		profileStart.setImage(profileImage);

		if( server.getServerState() == IServer.STATE_STARTED) {
			start.setText("Restart");
			debugStart.setText("Restart in Debug");
			profileStart.setText("Restart in Profile");
		}
		
		new MenuItem(serverMenu, SWT.SEPARATOR);
	
		MenuItem stop = new MenuItem(serverMenu, SWT.NONE);
		stop.setText("Stop");
		stop.setImage(stopImage);

		new MenuItem(serverMenu, SWT.SEPARATOR);
		
		MenuItem publish = new MenuItem(serverMenu, SWT.NONE);
		publish.setText("Publish (Incremental)");
		publish.setImage(publishImage);
		MenuItem fullPublish = new MenuItem(serverMenu, SWT.NONE);
		fullPublish.setText("Publish (Full)");
		fullPublish.setImage(publishImage);
		
		
		// Set enablement
		
		start.setEnabled(server.canStart("run").isOK());
		debugStart.setEnabled(server.canStart("debug").isOK());
		profileStart.setEnabled(server.canStart("profile").isOK());
		stop.setEnabled(server.canStop().isOK());
		publish.setEnabled(server.canPublish().isOK());
		fullPublish.setEnabled(server.canPublish().isOK());
		
		
		// Add listeners
		
		start.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				StartAction.start(server, "run", window.getShell());
			}
		});
		debugStart.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				StartAction.start(server, "debug", window.getShell());
			}
		});
		profileStart.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				StartAction.start(server, "profile", window.getShell());
			}
		});
		
		stop.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				StopAction.stop(server, window.getShell());
			}
		});
		publish.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PublishAction.publish(server, window.getShell());
			}
		});
		fullPublish.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				new FullPublishJobScheduler(server, server.getModules()).schedule();
			}
		});
		
		serverItem.setImage(getImage(server.getServerType()));
		
	}
	
	private Map<String, Image> imageMap = new HashMap<String, Image>();
	private Image getImage(IServerType st) {
		if( st == null || st.getId() == null )
			return null;
		if( imageMap.get(st.getId()) == null ) {
			ImageDescriptor i = ImageResource.getImageDescriptor(st.getId());
			if( i != null ) 
				imageMap.put(st.getId(), i.createImage());
		}
		return imageMap.get(st.getId());
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
		dialog.open();
	}

	@Override
	public void dispose() {
		Iterator<Image> images = imageMap.values().iterator();
		while(images.hasNext()) {
			images.next().dispose();
		}
		debugImage.dispose();
		profileImage.dispose();
		publishImage.dispose();
		startImage.dispose();
		stopImage.dispose();
	}
}
