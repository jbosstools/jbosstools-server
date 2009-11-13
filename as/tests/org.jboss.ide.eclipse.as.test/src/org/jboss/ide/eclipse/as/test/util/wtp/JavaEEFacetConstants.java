package org.jboss.ide.eclipse.as.test.util.wtp;

import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;

public class JavaEEFacetConstants {

	public static final IProjectFacet APP_CLIENT_FACET = ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_APPCLIENT_MODULE); //$NON-NLS-1$
	public static final IProjectFacetVersion APP_CLIENT_12 = APP_CLIENT_FACET.getVersion("1.2"); //$NON-NLS-1$
	public static final IProjectFacetVersion APP_CLIENT_13 = APP_CLIENT_FACET.getVersion("1.3"); //$NON-NLS-1$
	public static final IProjectFacetVersion APP_CLIENT_14 = APP_CLIENT_FACET.getVersion("1.4"); //$NON-NLS-1$
	public static final IProjectFacetVersion APP_CLIENT_5 = APP_CLIENT_FACET.getVersion("5.0"); //$NON-NLS-1$
	//public static final IProjectFacetVersion APP_CLIENT_6 = APP_CLIENT_FACET.getVersion("6.0"); //$NON-NLS-1$
	
	public static final IProjectFacet EJB_FACET = ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_EJB_MODULE); //$NON-NLS-1$
	public static final IProjectFacetVersion EJB_11 = EJB_FACET.getVersion("1.1"); //$NON-NLS-1$
	public static final IProjectFacetVersion EJB_2 = EJB_FACET.getVersion("2.0"); //$NON-NLS-1$
	public static final IProjectFacetVersion EJB_21 = EJB_FACET.getVersion("2.1"); //$NON-NLS-1$
	public static final IProjectFacetVersion EJB_3 = EJB_FACET.getVersion("3.0"); //$NON-NLS-1$
	//public static final IProjectFacetVersion EJB_31 = EJB_FACET.getVersion("3.1"); //$NON-NLS-1$
	
	public static final IProjectFacet WEB_FACET = ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_WEB_MODULE); //$NON-NLS-1$
	public static final IProjectFacetVersion WEB_22 = WEB_FACET.getVersion("2.2"); //$NON-NLS-1$
	public static final IProjectFacetVersion WEB_23 = WEB_FACET.getVersion("2.3"); //$NON-NLS-1$
	public static final IProjectFacetVersion WEB_24 = WEB_FACET.getVersion("2.4"); //$NON-NLS-1$
	public static final IProjectFacetVersion WEB_25 = WEB_FACET.getVersion("2.5"); //$NON-NLS-1$
	//public static final IProjectFacetVersion WEB_30 = WEB_FACET.getVersion("3.0"); //$NON-NLS-1$
	
	public static final IProjectFacet CONNECTOR_FACET = ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_CONNECTOR_MODULE); //$NON-NLS-1$
	public static final IProjectFacetVersion CONNECTOR_1 = CONNECTOR_FACET.getVersion("1.0"); //$NON-NLS-1$
	public static final IProjectFacetVersion CONNECTOR_15 = CONNECTOR_FACET.getVersion("1.5"); //$NON-NLS-1$
	//public static final IProjectFacetVersion CONNECTOR_16 = CONNECTOR_FACET.getVersion("1.6"); //$NON-NLS-1$
	
	public static final IProjectFacet EAR_FACET = ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_EAR_MODULE); //$NON-NLS-1$
	public static final IProjectFacetVersion EAR_12 = EAR_FACET.getVersion("1.2"); //$NON-NLS-1$
	public static final IProjectFacetVersion EAR_13 = EAR_FACET.getVersion("1.3"); //$NON-NLS-1$
	public static final IProjectFacetVersion EAR_14 = EAR_FACET.getVersion("1.4"); //$NON-NLS-1$
	public static final IProjectFacetVersion EAR_5 = EAR_FACET.getVersion("5.0"); //$NON-NLS-1$
	//public static final IProjectFacetVersion EAR_6 = EAR_FACET.getVersion("6.0"); //$NON-NLS-1$
	
	public static final IProjectFacet JAVA_FACET = ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_JAVA); //$NON-NLS-1$
	public static final IProjectFacetVersion JAVA_13 = JAVA_FACET.getVersion("1.3"); //$NON-NLS-1$
	public static final IProjectFacetVersion JAVA_14 = JAVA_FACET.getVersion("1.4"); //$NON-NLS-1$
	public static final IProjectFacetVersion JAVA_5 = JAVA_FACET.getVersion("5.0"); //$NON-NLS-1$
	//public static final IProjectFacetVersion JAVA_6 = JAVA_FACET.getVersion("6.0"); //$NON-NLS-1$
}
