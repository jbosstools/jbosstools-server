package org.jboss.ide.eclipse.as.core.server.internal.v7;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.ide.eclipse.as.core.server.IJBoss7ManagerService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

public class JBoss7ManagerServiceProxy extends ServiceTracker<IJBoss7ManagerService, IJBoss7ManagerService>
		implements IJBoss7ManagerService {

	public JBoss7ManagerServiceProxy(BundleContext context, String asVersion) throws InvalidSyntaxException {
		super(
				context,
				context.createFilter(MessageFormat
						.format("(&(objectClass={0})(as.version={1}))", IJBoss7ManagerService.class.getCanonicalName(), asVersion)), null); //$NON-NLS-1$
	}

	public IJBoss7DeploymentResult deployAsync(String host, int port, String deploymentName, File file,
			IProgressMonitor monitor) throws Exception {
		return checkedGetService().deployAsync(host, port, deploymentName, file, monitor);
	}

	public IJBoss7DeploymentResult deploySync(String host, int port, String deploymentName, File file,
			IProgressMonitor monitor) throws Exception {
		return checkedGetService().deployAsync(host, port, deploymentName, file, monitor);
	}

	public IJBoss7DeploymentResult undeployAsync(String host, int port, String deploymentName, boolean removeFile,
			IProgressMonitor monitor) throws Exception {
		return checkedGetService().undeployAsync(host, port, deploymentName, removeFile, monitor);
	}

	public IJBoss7DeploymentResult syncUndeploy(String host, int port, String deploymentName, boolean removeFile,
			IProgressMonitor monitor) throws Exception {
		return checkedGetService().syncUndeploy(host, port, deploymentName, removeFile, monitor);
	}

	public JBoss7DeploymentState getDeploymentState(String host, int port, String deploymentName) throws Exception {
		return checkedGetService().getDeploymentState(host, port, deploymentName);
	}

	public JBoss7ServerState getServerState(String host, int port) throws Exception {
		return checkedGetService().getServerState(host, port);
	}

	public boolean isRunning(String host, int port) throws Exception {
		try {
			return checkedGetService().isRunning(host, port);
		} catch (Exception e) {
			return false;
		}
	}

	@Deprecated
	public JBoss7ServerState getServerState(String host) throws Exception {
		return checkedGetService().getServerState(host);
	}

	public void stop(String host, int port) throws Exception {
		checkedGetService().stop(host, port);
	}

	@Deprecated
	public void stop(String host) throws Exception {
		checkedGetService().stop(host);
	}

	private IJBoss7ManagerService checkedGetService() throws JBoss7ManangerException {
		IJBoss7ManagerService service = getService();
		if (service == null) {
			throw new JBoss7ManangerException("Could not acquire JBoss Management service"); //$NON-NLS-1$
		}
		return service;
	}

	public void dispose() {
		close();
	}

}
