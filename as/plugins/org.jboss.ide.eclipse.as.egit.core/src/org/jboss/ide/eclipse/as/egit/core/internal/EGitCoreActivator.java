package org.jboss.ide.eclipse.as.egit.core.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class EGitCoreActivator implements BundleActivator {

	public static final String PLUGIN_ID = "org.jboss.ide.eclipse.as.egit.core";
	
	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		EGitCoreActivator.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		EGitCoreActivator.context = null;
	}

}
