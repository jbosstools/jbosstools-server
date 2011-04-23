package org.jboss.ide.eclipse.as.core.server.internal.v7;

import java.text.MessageFormat;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class JBoss7ManagentServiceProxy extends ServiceTracker<IJBoss7ManagementService, IJBoss7ManagementService>
		implements IJBoss7ManagementService {

	private static final String FILTER_EXPRESSION =
			"(&(objectClass=" + IJBoss7ManagementService.class.getCanonicalName() + ")(as.version={0}))"; //$NON-NLS-1$ //$NON-NLS-2$

	public JBoss7ManagentServiceProxy(BundleContext context, String asVersion) {
		super(context, MessageFormat.format(FILTER_EXPRESSION, asVersion), null);
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
