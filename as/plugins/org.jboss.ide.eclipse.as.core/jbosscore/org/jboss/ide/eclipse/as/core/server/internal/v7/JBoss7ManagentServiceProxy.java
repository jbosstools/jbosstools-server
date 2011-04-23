package org.jboss.ide.eclipse.as.core.server.internal.v7;

import java.text.MessageFormat;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

public class JBoss7ManagentServiceProxy extends ServiceTracker<IJBoss7ManagementService, IJBoss7ManagementService>
		implements IJBoss7ManagementService {

	public JBoss7ManagentServiceProxy(BundleContext context, String asVersion) throws InvalidSyntaxException {
		super(
				context,
				context.createFilter(MessageFormat
						.format("(&(objectClass={0})(as.version={1}))", IJBoss7ManagementService.class.getCanonicalName(), asVersion)), null); //$NON-NLS-1$
	}

	public IJBoss7DeploymentManager getDeploymentManager() throws JBoss7ManangementException {

		return checkedGetService().getDeploymentManager();
	}

	public IJBoss7ManagementInterface getManagementInterface() throws JBoss7ManangementException {
		return checkedGetService().getManagementInterface();
	}

	private IJBoss7ManagementService checkedGetService() throws JBoss7ManangementException {
		IJBoss7ManagementService service = getService();
		if (service == null) {
			throw new JBoss7ManangementException("Could not acquire JBoss Management service"); //$NON-NLS-1$
		}
		return service;
	}
}
