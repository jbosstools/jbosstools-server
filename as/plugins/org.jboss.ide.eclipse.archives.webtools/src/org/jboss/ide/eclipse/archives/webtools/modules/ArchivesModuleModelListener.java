/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.webtools.modules;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IPublishListener;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.ModuleFactory;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.jboss.ide.eclipse.archives.core.model.AbstractBuildListener;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModelListener;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNodeDelta;
import org.jboss.ide.eclipse.archives.webtools.IntegrationPlugin;
import org.jboss.ide.eclipse.archives.webtools.Messages;
import org.jboss.ide.eclipse.archives.webtools.modules.PackageModuleFactory.PackagedModuleDelegate;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.FileUtil;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class ArchivesModuleModelListener extends AbstractBuildListener implements IArchiveModelListener {

	public static ArchivesModuleModelListener instance;
	public static final String DEPLOY_SERVERS = "org.jboss.ide.eclipse.as.core.model.PackagesListener.DeployServers";//$NON-NLS-1$
	public static final String DEPLOY_AFTER_BUILD = "org.jboss.ide.eclipse.as.core.model.PackagesListener.DeployAfterBuild";//$NON-NLS-1$

	public static ArchivesModuleModelListener getInstance() {
		if( instance == null ) {
			instance = new ArchivesModuleModelListener();
		}
		return instance;
	}

	private IPublishListener publishListener;
	private ArrayList<IServer> publishing;
	public ArchivesModuleModelListener() {
		ArchivesModel.instance().addModelListener(this);
		ArchivesModel.instance().addBuildListener(this);
		publishing = new ArrayList<IServer>();
		publishListener = new IPublishListener() {
			public void publishFinished(IServer server, IStatus status) {
				publishing.remove(server);
			}
			public void publishStarted(IServer server) {
				publishing.add(server);
			} 
		};
	}

	// If we're supposed to auto-deploy, get on it
	public void finishedBuildingArchive(IArchive pkg) {
		if( pkg.isTopLevel() && new Boolean(pkg.getProperty(DEPLOY_AFTER_BUILD)).booleanValue()) {
			String servers = pkg.getProperty(ArchivesModuleModelListener.DEPLOY_SERVERS);
			publish(pkg, servers, IServer.PUBLISH_INCREMENTAL);
		}
	}

	public void publish(IArchive pkg, String servers, int publishType) {
		IModule[] module = getModule(pkg);
		if( module[0] == null ) return;
		IServer[] servers2 = ArchivesModuleModelListener.getServers(servers);
		if( servers2 != null ) {
			for( int i = 0; i < servers2.length; i++ ) {
				if( !publishing.contains(servers2[i])) {
					try {
						servers2[i].addPublishListener(publishListener);
						publish(servers2[i], publishType, module );
					} finally {
						servers2[i].removePublishListener(publishListener);
					}
				}
			}
		}
	}

	protected IStatus publish(IServer server, int publishType, IModule[] module ) {
		try {
			IServerWorkingCopy copy = server.createWorkingCopy();
			copy.modifyModules(module, new IModule[0], new NullProgressMonitor());
			IServer saved = copy.save(false, new NullProgressMonitor());
			saved.publish(IServer.PUBLISH_INCREMENTAL, new NullProgressMonitor());
		} catch( CoreException ce ) {
			return new Status(Status.ERROR, IntegrationPlugin.PLUGIN_ID,
					NLS.bind(Messages.ExceptionCannotDeployFile, module[0].getName()), ce);
		}
		return Status.OK_STATUS;

	}
	protected IModule[] getModule(IArchive node) {
		ModuleFactory factory = ServerPlugin.findModuleFactory(PackageModuleFactory.FACTORY_TYPE_ID);
		IModule mod = factory.findModule(PackageModuleFactory.getId(node), new NullProgressMonitor());
		return new IModule[] { mod };
	}
	protected PackagedModuleDelegate getModuleDelegate(IArchive node) {
		IModule mod = getModule(node)[0];
		return (PackagedModuleDelegate)mod.loadAdapter(PackagedModuleDelegate.class, new NullProgressMonitor());
	}

	protected IDeployableServer getDeployableServerFromBehavior(IServer server) {
		IDeployableServer ids = (IDeployableServer)server.loadAdapter(IDeployableServer.class, new NullProgressMonitor());
		return ids;
	}

	public static IServer[] getServers(String servers) {
		if( servers == null || "".equals(servers))//$NON-NLS-1$
			return null;
		ArrayList<IServer> list = new ArrayList<IServer>();
		String[] byId = servers.split(",");//$NON-NLS-1$
		for( int i = 0; i < byId.length; i++ ) {
			IServer server = ServerCore.findServer(byId[i]);
			if( server != null ) {
				list.add(server);
			}
		}
		return list.toArray(new IServer[list.size()]);
	}

	/*
	 * If a node is changing from exploded to imploded, or vice versa
	 * make sure to delete the pre-existing file or folder on the server.
	 */
	public void packageBuildTypeChanged(IArchive topLevelPackage, boolean isExploded) {
		String servers = topLevelPackage.getProperty(ArchivesModuleModelListener.DEPLOY_SERVERS);
		IServer[] servers2 = ArchivesModuleModelListener.getServers(servers);
		if( servers2 != null ) {
			IPath sourcePath, destPath;
			IDeployableServer depServer;
			for( int i = 0; i < servers2.length; i++ ) {
				sourcePath = topLevelPackage.getArchiveFilePath();
				depServer = getDeployableServerFromBehavior(servers2[i]);
				destPath = new Path(depServer.getDeployFolder()).append(sourcePath.lastSegment());
				FileUtil.safeDelete(destPath.toFile());
				FileUtil.fileSafeCopy(sourcePath.toFile(), destPath.toFile());
			}
		}
	}

	public void modelChanged(IArchiveNodeDelta delta) {
		IPath p ;
		if( delta.getPreNode() == null )
			p = delta.getPostNode().getProjectPath();
		else
			p = delta.getPreNode().getProjectPath();

		PackageModuleFactory.getFactory().refreshProject(p);
	}
}
