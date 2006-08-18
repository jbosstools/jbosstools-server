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

package org.jboss.ide.eclipse.as.ui.actions;

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.StartServerJob;
import org.eclipse.wst.server.ui.internal.ImageResource;
import org.eclipse.wst.server.ui.internal.Messages;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
import org.eclipse.wst.server.ui.internal.actions.NewServerWizardAction;
import org.eclipse.wst.server.ui.internal.provisional.ManagedUIDecorator;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.wizards.NewMBeanWizard;

public class ServerPulldownDelegate implements IWorkbenchWindowPulldownDelegate {

	private static final String DEFAULT_JBOSS_SERVER = "_DEFAULT_JBOSS_SERVER_";
	private static final String DEFAULT_JBOSS_SERVER_UNSET = "_DEFAULT_JBOSS_SERVER_UNSET_";
	
    private IWorkbench workbench;
    private ISelection selection;
    private Menu fMenu;
    
    private Image serverImage;

    private int fillMenuCurrentPos = 0;
    
    /** Return a menu which launches the various wizards */
    public Menu getMenu(Control parent) {
		setMenu(new Menu(parent));
		//fillMenu(fMenu);
		initMenu();
		return fMenu;
    }

	private void setMenu(Menu menu) {
		if (fMenu != null) {
			fMenu.dispose();
		}
		fMenu = menu;
	}

	private void initMenu() {
		fMenu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				Menu m = (Menu) e.widget;
				MenuItem[] items = m.getItems();
				for (int i = 0; i < items.length; i++) {
					items[i].dispose();
				}
				fillMenu(m);
			}
		});
    }
    
	protected void fillMenu(Menu menu) {
		fillMenuCurrentPos = 0;
		fillMenuServerItems(menu);
		fillMenuNewTypes(menu);
	}
	
	protected void fillMenuServerItems(Menu menu) {
		MenuItem startInRunMode = new MenuItem(menu, SWT.NONE);
		startInRunMode.setText("Start Server");
		startInRunMode.setImage(getStateImage(IServer.STATE_STARTED, ILaunchManager.RUN_MODE));
		fillMenuCurrentPos++;
		
		MenuItem startInDebugMode = new MenuItem(menu, SWT.NONE);
		startInDebugMode.setText("Start Server (Debug Mode)");
		startInDebugMode.setImage(getStateImage(IServer.STATE_STARTED, ILaunchManager.DEBUG_MODE));
		fillMenuCurrentPos++;
		
		MenuItem stopServer = new MenuItem(menu, SWT.NONE);
		stopServer.setText("Stop Server");
		stopServer.setImage(getStateImage(IServer.STATE_STOPPED, ILaunchManager.RUN_MODE));
		fillMenuCurrentPos++;
		
		// if default server is unset OR if it doesnt exist anymore (deleted)
		if( DEFAULT_JBOSS_SERVER_UNSET.equals(getCurrentDefaultServer()) || 
				ServerCore.findServer(getCurrentDefaultServer()) == null) {
			startInRunMode.setEnabled(false);
			startInDebugMode.setEnabled(false);
			stopServer.setEnabled(false);
		} else {
			// we have a server set. Depending on state, disable some options
			String defaultServerID = getCurrentDefaultServer();
			IServer defaultServer = ServerCore.findServer(defaultServerID);
			int state = defaultServer.getServerState();
			if( state == IServer.STATE_STARTED || state == IServer.STATE_STARTING ) {
				startInRunMode.setEnabled(false);
				startInDebugMode.setEnabled(false);
			} else if( state == IServer.STATE_STOPPED ) {
				stopServer.setEnabled(false);
			}
		}
		
		
		new Separator().fill(menu, fillMenuCurrentPos);
		fillMenuCurrentPos++;

		
		JBossServer[] servers = JBossServerCore.getAllJBossServers();
		if( servers.length == 0 ) {
			MenuItem selectDefaultServer = new MenuItem(menu, SWT.NONE);
			selectDefaultServer.setText("Set Default Server");
			selectDefaultServer.setEnabled(false);
			fillMenuCurrentPos++;
		} else { 
			MenuItem selectDefaultServer = new MenuItem(menu, SWT.CASCADE);
			selectDefaultServer.setText("Set Default Server");
			Menu subMenu = new Menu(menu);
			selectDefaultServer.setMenu(subMenu);
			fillMenuCurrentPos++;
			for( int i = 0; i < servers.length; i++ ) {
				MenuItem server= new MenuItem(subMenu, SWT.CHECK);
				server.setText(servers[i].getServer().getName());
				server.setImage(JBossServerUISharedImages.getImage(JBossServerUISharedImages.IMG_JBOSS));
				
				if( servers[i].getServer().getId().equals(getCurrentDefaultServer())) {
					server.setSelection(true);
				} else {
					server.setSelection(false);
				}
				
				final IServer myServer = servers[i].getServer();

				// upon selection, update the default
				server.addSelectionListener(new SelectionListener() {

					public void widgetDefaultSelected(SelectionEvent e) {
					}

					public void widgetSelected(SelectionEvent e) {
						if( myServer.getId().equals(getCurrentDefaultServer())) {
							JBossServerUIPlugin.getDefault().getPreferenceStore().setValue(DEFAULT_JBOSS_SERVER, DEFAULT_JBOSS_SERVER_UNSET);
						} else {
							JBossServerUIPlugin.getDefault().getPreferenceStore().setValue(DEFAULT_JBOSS_SERVER, myServer.getId());
						}
					} 
					
				});
			}
		}

		// Add listeners for the three start / debug / stop options
		startInRunMode.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				if( !DEFAULT_JBOSS_SERVER_UNSET.equals(getCurrentDefaultServer())) {
					IServer s = ServerCore.findServer(getCurrentDefaultServer());
					StartServerJob startJob = new StartServerJob(s, ILaunchManager.RUN_MODE);
					startJob.schedule();
				}
			} 
		} );
		startInDebugMode.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				if( !DEFAULT_JBOSS_SERVER_UNSET.equals(getCurrentDefaultServer())) {
					IServer s = ServerCore.findServer(getCurrentDefaultServer());
					StartServerJob startJob = new StartServerJob(s, ILaunchManager.DEBUG_MODE);
					startJob.schedule();
				}
			} 
		} );
		stopServer.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				if( !DEFAULT_JBOSS_SERVER_UNSET.equals(getCurrentDefaultServer())) {
					final IServer s = ServerCore.findServer(getCurrentDefaultServer());
					final Shell shell = new Shell();
					ServerUIPlugin.addTerminationWatch(shell, s, ServerUIPlugin.STOP);
					
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							MessageDialog dialog = new MessageDialog(shell, Messages.defaultDialogTitle, null,
									NLS.bind(Messages.dialogStoppingServer, s.getName()), MessageDialog.INFORMATION, new String[0], 0);
							dialog.setBlockOnOpen(false);
							dialog.open();
							s.stop(false);
							dialog.close();
						}
					});
				}
			} 
		} );
		
	}

	
	protected void fillMenuNewTypes(Menu menu) {
		new Separator().fill(menu, fillMenuCurrentPos);
		fillMenuCurrentPos++;
		
		MenuItem newMenuItem = new MenuItem(menu, SWT.CASCADE);
		newMenuItem.setText("New");
		Menu subMenu = new Menu(menu);
		newMenuItem.setMenu(subMenu);
		fillMenuCurrentPos++;
		
		MenuItem mbeanStubsMenuItem = new MenuItem(subMenu, SWT.NONE);
		mbeanStubsMenuItem.setText("New MBean Stubs");
		// TODO: get an image
		
		mbeanStubsMenuItem.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				NewMBeanWizard newMBeanWizard = new NewMBeanWizard();
				if( selection instanceof IStructuredSelection )
					newMBeanWizard.init(workbench, (IStructuredSelection)selection);
				else 
					newMBeanWizard.init(workbench, null);
					
				WizardDialog dlg = new WizardDialog(Display.getDefault().getActiveShell(), newMBeanWizard);
			    int ret = dlg.open();

			} 
		});
		
		
		MenuItem newServerItem = new MenuItem(subMenu, SWT.NONE);
		newServerItem.setText("New Server");
		newServerItem.setImage(getServerImage());
		
		newServerItem.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				IAction newServerAction = new NewServerWizardAction();
				newServerAction.run();
			} 
		});
	}
	
    public void init(IWorkbenchWindow window) {
        workbench = window.getWorkbench();
    }

    public void run(IAction action) {
    }

    public void selectionChanged(IAction action, ISelection selection) {
    	this.selection = selection;
    }

	public void dispose() {
		if( fMenu != null && !fMenu.isDisposed()) fMenu.dispose();
		if( serverImage != null && !serverImage.isDisposed()) serverImage.dispose();
	}
	
	protected Image getServerImage() {
		if( serverImage == null ) {
			ImageDescriptor id = ImageResource.getImageDescriptor(ImageResource.IMG_CTOOL_NEW_SERVER);
			serverImage = id.createImage();
		}
		return serverImage;
	}
	protected Image getStateImage(int state, String mode) {
		return new ManagedUIDecorator().getStateImage(state, mode, 0);
	}

	protected String getCurrentDefaultServer() {
		return JBossServerUIPlugin.getDefault().getPreferenceStore().getString(DEFAULT_JBOSS_SERVER);
	}
}
