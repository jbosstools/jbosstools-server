package org.jboss.ide.eclipse.as.test;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ASTest extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.ide.eclipse.as.test";

	// The shared instance
	private static ASTest plugin;
	
	/**
	 * The constructor
	 */
	public ASTest() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ASTest getDefault() {
		return plugin;
	}

	
	
	// set some constants for wide-use
	public static final String TOMCAT_RUNTIME_55 = "org.eclipse.jst.server.tomcat.runtime.55";
	public static final String JBOSS_RUNTIME_32 = "org.jboss.ide.eclipse.as.runtime.32";
	public static final String JBOSS_RUNTIME_40 = "org.jboss.ide.eclipse.as.runtime.40";
	public static final String JBOSS_RUNTIME_42 = "org.jboss.ide.eclipse.as.runtime.42";
	public static final String JBOSS_SERVER_32 = "org.jboss.ide.eclipse.as.32";
	public static final String JBOSS_SERVER_40 = "org.jboss.ide.eclipse.as.40";
	public static final String JBOSS_SERVER_42 = "org.jboss.ide.eclipse.as.42";
	public static final String JBOSS_AS_32_HOME = System.getProperty("jbosstools.test.jboss.home.3.2", "C:\\apps\\jboss\\jboss-3.2.8.SP1\\");
	public static final String JBOSS_AS_40_HOME = System.getProperty("jbosstools.test.jboss.home.4.0", "C:\\apps\\jboss\\jboss-4.0.5.GA\\");
	public static final String JBOSS_AS_42_HOME = System.getProperty("jbosstools.test.jboss.home.4.2", "C:\\apps\\jboss\\jboss-4.2.1.GA\\");

	public static final String JBOSS_AS_HOME = System.getProperty("jbosstools.test.jboss.home", JBOSS_AS_42_HOME);
}
