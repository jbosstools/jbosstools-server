package org.jboss.ide.eclipse.as.core.client;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.JBossServer;

/**
 * This is merely here to label classes by the interface so that
 * the Publish client shows up in the list of possible clients.
 * 
 * The Publish client should take no action and let the previous
 * call to publish() do all the work.
 * 
 * @author rstryker
 *
 */
public interface ICopyAsLaunch {
	public boolean supportsDeploy(IServer server, String launchMode);
}
