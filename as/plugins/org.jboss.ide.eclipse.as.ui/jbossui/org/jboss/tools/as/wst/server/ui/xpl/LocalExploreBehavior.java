package org.jboss.tools.as.wst.server.ui.xpl;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ui.actions.ExploreUtils;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities;
import org.jboss.tools.as.wst.server.ui.xpl.ExploreActionProvider.IExploreBehavior;

public class LocalExploreBehavior implements IExploreBehavior {
	public boolean canExplore(IServer server, IModule[] module) {
		if( module != null )
			return ExploreUtils.canExplore(server, module);
		return ExploreUtils.canExplore(server);
	}
	public void openExplorer(IServer server, IModule[] module) {
		if( module != null ) 
			runExploreModuleServer(server, module);
		else
			runExploreServer(server);
	}
	public void runExploreServer(IServer server) {
		String deployDirectory = ExploreUtils.getDeployDirectory(server);
		if (deployDirectory != null && deployDirectory.length() > 0) {
			ExploreUtils.explore(deployDirectory);
		} 
	}
	
	public void runExploreModuleServer(IServer server, IModule[] module) {
		IPath path = getModuleDeployPath(server, module);
		if (path != null) {
			File file = path.toFile();
			if (file.exists()) {
				ExploreUtils.explore(file.getAbsolutePath());
			}
		}
	}
	private IPath getModuleDeployPath(IServer server, IModule[] module) {
		IDeployableServer deployableServer = ServerConverter.getDeployableServer(server);
		if( deployableServer != null )
			return getDeployPath(deployableServer, module);
		return null;
	}
	
	private static IPath getDeployPath(IDeployableServer server,IModule[] moduleTree) {
		IPath fullPath = server.getDeploymentLocation(moduleTree, true);
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			return fullPath;
		}
		if( !ServerModelUtilities.isBinaryModule(moduleTree)) {
			return fullPath;
		}
		return fullPath.removeLastSegments(1);
	}

}
