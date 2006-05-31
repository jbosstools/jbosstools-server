package org.jboss.ide.eclipse.as.core.client;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.ClientDelegate;
import org.jboss.ide.eclipse.as.core.util.ASDebug;

/**
 * This client will only copy to the deploy directory,
 * which is actually a side effect of the run-on-server action.
 * 
 * Therefore, this client does absolutely nothing.
 * 
 * It only shows up on the list for files that are deployable via 
 * copying into a deploy directory. 
 * 
 * @author rstryker
 *
 */
public class PublishClient extends ClientDelegate {

	public PublishClient() {
		super();
	}

	public boolean supports(IServer server, Object launchable, String launchMode) {
		if( launchable instanceof ICopyAsLaunch)
			return ((ICopyAsLaunch)launchable).supportsDeploy(server, launchMode);

		return false;
	}

	public IStatus launch(IServer server, Object launchable, String launchMode,
			ILaunch launch) {
		
		// Do nothing
		
		ASDebug.p("Published content!", this);
		return new Status(IStatus.OK, ServerPlugin.PLUGIN_ID, 0, "A-OK", null);
	}

}
