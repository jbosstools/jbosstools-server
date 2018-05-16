/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
//import org.eclipse.ui.internal.browser.ImageResource;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IModuleType;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.ILaunchableAdapter;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.IURLProvider;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;
import org.jboss.ide.eclipse.as.core.server.internal.JBossLaunchAdapter.JBTCustomHttpLaunchable;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties.GetWelcomePageURLException;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.actions.ServerActionMessages;
import org.jboss.ide.eclipse.as.ui.launch.JBTWebLaunchableClient;

public class ShowInWelcomePageActionProvider extends CommonActionProvider {

	private Action action;
	private ICommonActionExtensionSite actionSite;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.ICommonActionExtensionSite)
	 */
	@Override
	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		actionSite = aSite;
		ICommonViewerSite site = aSite.getViewSite();
		if( site instanceof ICommonViewerWorkbenchSite ) {
			StructuredViewer v = aSite.getStructuredViewer();
			if( v instanceof CommonViewer ) {
				ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite)site;
				createActions((CommonViewer)v, wsSite.getSelectionProvider());
			}
		}
	}

	public void createActions(CommonViewer tableViewer, ISelectionProvider provider) {
		action = new Action() {
			@Override
			public void run() {
				final Object sel = getSelection();
				new Job("Fetching Welcome Page URL") {
					public IStatus run(IProgressMonitor monitor) {
						// Get the url in a background thread to not freeze the UI
						String url2 = null;
						try {
							url2 = getUrl(sel);
						} catch(CoreException ce) {
							return ce.getStatus();
						}
						final String url = url2;
						if(url!=null) {
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									JBTWebLaunchableClient.checkedCreateInternalBrowser(url, getServer().getName(), JBossServerUIPlugin.PLUGIN_ID, JBossServerUIPlugin.getDefault().getLog());
								}
							});
						}
						return Status.OK_STATUS;
					}
				}.schedule();
			}
		};
		action.setText(ServerActionMessages.OpenWithBrowser);
		action.setDescription(ServerActionMessages.OpenWithBrowserDescription);
		//action.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_INTERNAL_BROWSER));
	}

	private boolean hasURL() {
		IServer server = getServer();
		ServerExtendedProperties props = (ServerExtendedProperties)server.loadAdapter(ServerExtendedProperties.class, new NullProgressMonitor());
		// Server types that have not opted in get no welcome page support
		if( props == null )
			return false;
		
		try {
			if( getModuleServer() != null ) {
				return getUrl(getSelection()) != null;
			} else {
				return props.hasWelcomePage(); 
			}
		} catch(CoreException ce) {
			// Ignore, expected
		}
		return false;
	}
	
	private static JBTCustomHttpLaunchable getCustomLaunchable(IServer server, final IModule[] module) throws CoreException {
		IModule mod = module == null || module.length == 0 ? null : module[module.length-1];
		IModuleArtifact artifact = null;
		if( mod != null ) {
			IProject p = mod.getProject();
			if( p != null ) {
				final IModuleArtifact[] moduleArtifacts = ServerPlugin.getModuleArtifacts(p);
				if( moduleArtifacts != null && moduleArtifacts.length > 0 ) {
					artifact = moduleArtifacts[0];
				}
			}
		}
		
		if( artifact == null ) {
			// create a stub
			artifact = new IModuleArtifact() {
				public IModule getModule() {
					return module == null || module.length == 0 ? null : module[module.length-1];
				}
			};
		}
		return getLaunchable(server, artifact);
	}
	
	private static JBTCustomHttpLaunchable getLaunchable(IServer server, IModuleArtifact moduleArtifact) throws CoreException {
		ILaunchableAdapter[] adapters = ServerPlugin.getLaunchableAdapters();
		IStatus lastStatus = null;
		if (adapters != null) {
			int size2 = adapters.length;
			for (int j = 0; j < size2; j++) {
				try {
					Object launchable2 = adapters[j].getLaunchable(server, moduleArtifact);
					if (launchable2 != null && launchable2 instanceof JBTCustomHttpLaunchable)
						return (JBTCustomHttpLaunchable)launchable2;
				} catch (CoreException ce) {
					lastStatus = ce.getStatus();
				}
			}
			if (lastStatus != null)
				throw new CoreException(lastStatus);
		}
		return null;
	}

	
	private String getUrl(Object sel) throws CoreException {
		String urlString = null;
		IServer server = getServer(sel);
		if(server!=null && server.getServerState() == IServer.STATE_STARTED) {
			ModuleServer ms = getModuleServer(sel);
			if(ms!=null) {
				urlString = getWelcomePageURL(ms);
			} else {
				// When no module is selected,use welcome page url
				ServerExtendedProperties props = (ServerExtendedProperties)server.loadAdapter(ServerExtendedProperties.class, new NullProgressMonitor());
				if( props != null ) {
					try {
						urlString = props.getWelcomePageUrl();
					} catch (final GetWelcomePageURLException e) {
						if(e.getCause() != null) {
							JBossServerUIPlugin.log(new Status(IStatus.WARNING, JBossServerUIPlugin.PLUGIN_ID, "No URL found for current selection '" + server.getName() + "'."));
						}
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								MessageDialog.openWarning(
										PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
										"No URL found for current selection", e.getMessage());
							}
						});
					}
				}
			}
		}
		return urlString;
	}

	private String getWelcomePageURL(ModuleServer ms) throws CoreException {
		// When a module is selected, behave as you would during run-on-server for project-level selection
		if(ms!=null) {
			// Go through the wtp framework to find the proper launchable adapter for the project
			JBTCustomHttpLaunchable launchable = getCustomLaunchable(getServer(), ms.getModule());
			// IF its one we provide, return its url directly
			if( launchable != null ){
				return (launchable).getURL().toString();
			} 
				
			//Otherwise, do the magic we did in the past to try our best to come up with a url
			IModule[] mss = ms.getModule();
			IModule m = getWebModule(mss);
			if(m!=null) {
				IServer s = getServer();
				Object o = s.loadAdapter(IURLProvider.class, null);
				if(o instanceof IURLProvider) {
					URL url = ((IURLProvider)o).getModuleRootURL(m);
					if(url!=null) {
						return url.toString();
					}
				}
			}
		}
		return null;
	}


	private IModule getWebModule(IModule[] m) {
		if(m.length>0) {
			IModule module = m[m.length-1];
			if(isWebModule(module)) {
				return module;
			} else {
				IServer s = getServer();
				IModule[] mms = s.getChildModules(m, null);
				for (IModule child : mms) {
					if(isWebModule(child)) {
						return child;
					}
				}
			}
		}
		return null;
	}

	private boolean isWebModule(IModule module) {
		IModuleType type = module.getModuleType();
		return "jst.web".equals(type.getId()); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	public void fillContextMenu(IMenuManager menu) {
		if( getModuleServer() != null || getServer()!=null ) {
			IContributionItem menuItem = CommonActionProviderUtils.getShowInQuickMenu(menu, true);
			if (menuItem instanceof MenuManager) {
				((MenuManager) menuItem).add(action);
				action.setEnabled(hasURL() && getServer().getServerState() == IServer.STATE_STARTED);
			}
		}
	}

	public IServer getServer() {
		Object sel = getSelection();
		return getServer(sel);
	}

	public ModuleServer getModuleServer() {
		Object sel = getSelection();
		return getModuleServer(sel);
	}

	public IServer getServer(Object o) {
		if (o instanceof IServer) {
			return ((IServer)o);
		}
		if( o instanceof ModuleServer) { 
			return ((ModuleServer) o).server;
		}
		return null;
	}

	public ModuleServer getModuleServer(Object o) {
		if(o instanceof ModuleServer) { 
			return ((ModuleServer) o);
		}
		return null;
	}

	protected Object getSelection() {
		ICommonViewerSite site = actionSite.getViewSite();
		IStructuredSelection selection = null;
		if (site instanceof ICommonViewerWorkbenchSite) {
			ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite)site;
			selection = (IStructuredSelection) wsSite.getSelectionProvider().getSelection();
			if( selection.size() == 1 ) {
				return selection.getFirstElement();
			}
		}
		return null;
	}	
}
