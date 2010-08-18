package org.jboss.ide.eclipse.as.rse.core;

import org.jboss.ide.eclipse.as.core.server.internal.JBossServerBehavior;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class RSECorePlugin implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		RSECorePlugin.context = bundleContext;
		JBossServerBehavior.addDelegateMapping(RSEPublishMethod.RSE_ID, RSEBehaviourDelegate.class);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		RSECorePlugin.context = null;
	}

}
