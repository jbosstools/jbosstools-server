package org.jboss.ide.eclipse.as.classpath.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.ide.eclipse.as.classpath.core.messages"; //$NON-NLS-1$
	public static String AbstractClasspathContainer_error_loading_container;
	public static String ClasspathDecorationsManager_unexpected_exception;
	public static String ClientAllRuntimeClasspathProvider_wrong_runtime_type;
	public static String DirectoryLibraryContainerInitializer_libraries_found_in_dir;
	public static String DirectoryLibraryContainerInitializer_libraries_found_no_dir;
	public static String EJB3ClasspathContainer_ejb3_description;
	public static String EJB3ClasspathContainer_ejb30_description;
	public static String J2EE13ClasspathContainerInitializer_description;
	public static String J2EE14ClasspathContainerInitializer_description;
	public static String J2EE50ClasspathContainerInitializer_description;
	public static String ProjectRuntimeClasspathProvider_all_jboss_libraries_description;
	public static String ProjectRuntimeClasspathProvider_runtime_does_not_exist;
	public static String WebtoolsProjectJBossClasspathContainerInitializer_jboss_runtimes;
	public static String WebtoolsProjectJBossClasspathContainerInitializer_jboss_runtimes_path;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
