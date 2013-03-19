/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.ide.eclipse.as.classpath.core.messages"; //$NON-NLS-1$
	public static String AbstractClasspathContainer_error_loading_container;
	public static String ClasspathDecorationsManager_unexpected_exception;
	public static String ClientAllRuntimeClasspathProvider_wrong_runtime_type;
	public static String DirectoryLibraryContainerInitializer_libraries_found_in_dir;
	public static String DirectoryLibraryContainerInitializer_libraries_found_no_dir;
	public static String EJB3ClasspathContainer_could_not_determine_home;
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
