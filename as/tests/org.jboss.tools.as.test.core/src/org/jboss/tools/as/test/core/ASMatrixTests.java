package org.jboss.tools.as.test.core;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.as.test.core.internal.utils.wtp.ProjectUtility;
import org.jboss.tools.test.util.JobUtils;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ASMatrixTests extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.as.test.core"; //$NON-NLS-1$

	// The shared instance
	private static ASMatrixTests plugin;
	
	/**
	 * The constructor
	 */
	public ASMatrixTests() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
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
	public static ASMatrixTests getDefault() {
		return plugin;
	}

	public static void clearStateLocation() {
		IPath state = ASMatrixTests.getDefault().getStateLocation();
		if( state.toFile().exists()) {
			File[] children = state.toFile().listFiles();
			for( int i = 0; i < children.length; i++ ) {
				FileUtil.safeDelete(children[i]);
			}
		}
	}
	public static void cleanup() throws Exception {
		JobUtils.waitForIdle();
		ServerCreationTestUtils.deleteAllServersAndRuntimes();
		ProjectUtility.deleteAllProjects();
		ASMatrixTests.clearStateLocation();
		JobUtils.waitForIdle();
	}
}
