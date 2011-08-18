package org.jboss.ide.eclipse.as.egit.core.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


public class EGitCoreActivator implements BundleActivator {

	public static final String PLUGIN_ID = "org.jboss.ide.eclipse.as.egit.core";
	
	private static BundleContext context;

	public static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		EGitCoreActivator.context = bundleContext;
	}


	public void stop(BundleContext bundleContext) throws Exception {
		EGitCoreActivator.context = null;
	}
}
