package org.jboss.ide.eclipse.as.wtp.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.ide.eclipse.as.wtp.override.ui.messages"; //$NON-NLS-1$
	public static String AddModuleDependenciesPropertiesPage_AddProjectButton;
	public static String AddModuleDependenciesPropertiesPage_DeployPathColumn;
	public static String AddModuleDependenciesPropertiesPage_RemoveSelectedButton;
	public static String AddModuleDependenciesPropertiesPage_SelectAProjectTitle;
	public static String AddModuleDependenciesPropertiesPage_SourceColumn;
	public static String J2EEDependenciesPage_Description;
	public static String J2EEDependenciesPage_ErrorCheckingFacets;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
