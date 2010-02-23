package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.PublishServerJob;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.modules.SingleDeployableFactory.SingleDeployableModuleDelegate;
import org.jboss.ide.eclipse.as.core.publishers.JstPublisher;
import org.jboss.ide.eclipse.as.core.publishers.SingleFilePublisher;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.util.ModuleUtil;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.actions.ExploreUtils;
import org.jboss.tools.as.wst.server.ui.xpl.ServerActionProvider;

public class ModuleActionProvider extends CommonActionProvider {
	private Action deleteModuleAction, fullPublishModuleAction, incrementalPublishModuleAction;
	private Action exploreAction;
	private ModuleServer[] selection;

	private ICommonActionExtensionSite actionSite;
	public ModuleActionProvider() {
		super();
	}
	
	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		this.actionSite = aSite;
		createActions();
	}
	public void fillContextMenu(IMenuManager menu) {
		ICommonViewerSite site = actionSite.getViewSite();
		IStructuredSelection selection = null;
		if (site instanceof ICommonViewerWorkbenchSite) {
			ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite) site;
			selection = (IStructuredSelection) wsSite.getSelectionProvider()
					.getSelection();
		}

		if( allAre(selection, ModuleServer.class)) {
			Object[] arr = selection.toArray();
			ModuleServer[] ms = new ModuleServer[arr.length];
			for( int i = 0; i < arr.length; i++ ) 
				ms[i] = (ModuleServer)arr[i];
			this.selection = ms;
			
			IContributionItem sep = menu.find(ServerActionProvider.CONTROL_MODULE_SECTION_END_SEPARATOR);
			if( sep == null ) {
				sep = new Separator(ServerActionProvider.CONTROL_MODULE_SECTION_END_SEPARATOR);
				sep.setVisible(false);
				menu.add(sep);
			}
			menu.insertBefore(ServerActionProvider.CONTROL_MODULE_SECTION_END_SEPARATOR, incrementalPublishModuleAction);
			menu.insertBefore(ServerActionProvider.CONTROL_MODULE_SECTION_END_SEPARATOR, fullPublishModuleAction);
			if( selection.size() > 1 ) 
				menu.insertBefore(ServerActionProvider.CONTROL_MODULE_SECTION_END_SEPARATOR, deleteModuleAction);
			if (selection.size() == 1) {
				ModuleServer moduleServer = (ModuleServer) selection.getFirstElement();
				IServer server = moduleServer.getServer();
				if (ExploreUtils.canExplore(server)) {
					if (getDeployPath() != null) {
						menu.insertBefore(ServerActionProvider.CONTROL_MODULE_SECTION_END_SEPARATOR, exploreAction);
					}
				}
			}
		}
	}
	
	protected boolean allAre(IStructuredSelection sel, Class c) {
		if( sel == null || sel.isEmpty())
			return false;
		
		Iterator i = sel.iterator();
		while(i.hasNext()) 
			if(!i.next().getClass().equals(c))
				return false;
		return true;
	}
	
	
	protected void createActions() {
		deleteModuleAction = new Action() {
			public void run() {
				deleteModule();
			}
		};
		deleteModuleAction.setText(Messages.DeleteModuleText);
		deleteModuleAction.setDescription(Messages.DeleteModuleDescription);
		deleteModuleAction.setImageDescriptor(JBossServerUISharedImages.getImageDescriptor(JBossServerUISharedImages.UNPUBLISH_IMAGE));
		
		fullPublishModuleAction = new Action() {
			public void run() {
				actionPublish(IServer.PUBLISH_STATE_FULL);
			}
		};
		fullPublishModuleAction.setText(Messages.FullPublishModuleText);
		fullPublishModuleAction.setDescription(Messages.PublishModuleDescription);
		fullPublishModuleAction.setImageDescriptor(JBossServerUISharedImages.getImageDescriptor(JBossServerUISharedImages.PUBLISH_IMAGE));

	
		incrementalPublishModuleAction = new Action() {
			public void run() {
				actionPublish(IServer.PUBLISH_STATE_INCREMENTAL);
			}
		};
		incrementalPublishModuleAction.setText(Messages.IncrementalPublishModuleText);
		incrementalPublishModuleAction.setDescription(Messages.PublishModuleDescription);
		incrementalPublishModuleAction.setImageDescriptor(JBossServerUISharedImages.getImageDescriptor(JBossServerUISharedImages.PUBLISH_IMAGE));

		exploreAction = new Action() {
			public void run() {
				IPath path = getDeployPath();
				if (path != null) {
					File file = path.toFile();
					if (file.exists()) {
						ExploreUtils.explore(file.getAbsolutePath());
					}
				}
			}
		};
		exploreAction.setText(ExploreUtils.EXPLORE);
		exploreAction.setDescription(ExploreUtils.EXPLORE_DESCRIPTION);
		exploreAction.setImageDescriptor(JBossServerUISharedImages.getImageDescriptor(JBossServerUISharedImages.EXPLORE_IMAGE));
	}

	protected void actionPublish(int type) {
		// Assumption: Anything selected is already on the server, or it wouldnt be in the view.
		if( selection != null && selection.length > 0 ) {
			Server s = ((Server)selection[0].server);
			for( int i = 0; i < selection.length; i++ ) {
				IModule[] mod = selection[i].module;
				s.setModulePublishState(mod, type);
				ArrayList<IModule[]> allChildren = ModuleUtil.getDeepChildren(s, mod);
				for( int j = 0; j < allChildren.size(); j++ ) {
					s.setModulePublishState((IModule[])allChildren.get(j), type);
				}
			}
			new PublishServerJob(s, IServer.PUBLISH_INCREMENTAL, true).schedule();
		}
	}

	protected void deleteModule() {
		if (MessageDialog.openConfirm(new Shell(), Messages.ServerDialogHeading, Messages.DeleteModuleConfirm)) {
			Thread t = new Thread() { public void run() { 
				try {
					if( selection.length > 0 && selection[0].server != null ) {
						IServer server = selection[0].server;
						ArrayList topModsToRemove = new ArrayList();
						IModule topModTmp;
						for( int i = 0; i < selection.length; i++ ) {
							if( !topModsToRemove.contains(selection[i].module[0]))
								topModsToRemove.add(selection[i].module[0]);
						}
						IServerWorkingCopy serverWC = server.createWorkingCopy();
						IModule[] modsToRemove = 
							(IModule[]) topModsToRemove.toArray(new IModule[topModsToRemove.size()]);
						ServerUtil.modifyModules(serverWC, new IModule[0], modsToRemove, new NullProgressMonitor());
						IServer server2 = serverWC.save(true, null);
						new PublishServerJob(server2, IServer.PUBLISH_INCREMENTAL, true).schedule();
					}
				} catch (CoreException e) {
					// ignore
				}
			}};
			t.start();
		}
	}
	
	private IPath getDeployPath() {
		ModuleServer ms = selection[0];
		IModule[] module = ms.module;
		IJBossServerPublisher publisher = ExtensionManager.getDefault()
				.getPublisher(ms.getServer(), module, "local");
		IPath path = null;
		IDeployableServer deployableServer = ServerConverter
				.getDeployableServer(ms.server);
		if (deployableServer != null) {
			if (publisher instanceof JstPublisher) {
				path = ExploreUtils.getDeployPath(deployableServer,
						module);
			} else if (publisher instanceof SingleFilePublisher) {
				SingleDeployableModuleDelegate delegate = (SingleDeployableModuleDelegate)module[0].loadAdapter(SingleDeployableModuleDelegate.class, new NullProgressMonitor());
				if (delegate != null) {
					IPath sourcePath = delegate.getGlobalSourcePath();
					IPath destFolder = new Path(deployableServer.getDeployFolder());
					path = destFolder.append(sourcePath.lastSegment());
				} else {
					path = new Path(deployableServer.getDeployFolder());
				}
			} else {
				path = new Path(deployableServer.getDeployFolder());
			}
		}
		return path;
	}
}
