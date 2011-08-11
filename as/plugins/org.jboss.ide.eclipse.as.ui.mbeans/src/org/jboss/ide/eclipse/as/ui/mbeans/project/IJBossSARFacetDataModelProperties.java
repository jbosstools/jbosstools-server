/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.mbeans.project;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;

public interface IJBossSARFacetDataModelProperties extends IFacetDataModelProperties {

	public static final String USER_DEFINED_LOCATION = "IProjectCreationPropertiesNew.USER_DEFINED_LOCATION"; //$NON-NLS-1$
	public static final String DEFAULT_LOCATION = "IProjectCreationPropertiesNew.DEFAULT_LOCATION"; //$NON-NLS-1$
	public static final String USE_DEFAULT_LOCATION = "IProjectCreationPropertiesNew.USE_DEFAULT_LOCATION"; //$NON-NLS-1$
	public static final String PROJECT_LOCATION = "IProjectCreationPropertiesNew.PROJECT_LOCATION"; //$NON-NLS-1$
	
	public static final String SAR_CONTENT_FOLDER = "JBoss.Project.Content_Folder"; //$NON-NLS-1$
	public static final String SAR_SOURCE_FOLDER = "JBoss.Project.Src_Folder"; //$NON-NLS-1$
	
	public static final String SAR_CONFIG_VERSION = "JBoss.Project.Config_Version"; //$NON-NLS-1$
	
	public static final QualifiedName QNAME_SAR_CONTENT_FOLDER = new QualifiedName("jboss", SAR_CONTENT_FOLDER); //$NON-NLS-1$
	public static final QualifiedName QNAME_SAR_SRC_FOLDER = new QualifiedName("jboss", SAR_SOURCE_FOLDER); //$NON-NLS-1$
	public static final String SAR_PROJECT_VERSION = "jboss.sar.project.project.version"; //$NON-NLS-1$

	
	
	public static final String JBOSS_SAR_FACET_ID = "jst.jboss.sar"; //$NON-NLS-1$
	public static final String RUNTIME_DEPLOY = "jboss.deploy";	 //$NON-NLS-1$
	public static final String QUALIFIEDNAME_IDENTIFIER = "jboss.tools";	 //$NON-NLS-1$
	public static final String RUNTIME_IS_SERVER_SUPPLIED = "jboss.is.server.supplied"; //$NON-NLS-1$
	public static final String RUNTIME_ID = "jboss.runtime_id"; //$NON-NLS-1$
	
	public static final String PERSISTENT_PROPERTY_IS_SERVER_SUPPLIED_RUNTIME = "is.server.supplied.runtime"; //$NON-NLS-1$
	public static final String RUNTIME_HOME = "jboss.runtime.home"; //$NON-NLS-1$
	public static final String DEFAULT_VALUE_IS_SERVER_SUPPLIED = "1"; //$NON-NLS-1$
	static QualifiedName PERSISTENCE_PROPERTY_QNAME_RUNTIME_NAME = new QualifiedName(QUALIFIEDNAME_IDENTIFIER,
			RUNTIME_ID);
	static QualifiedName PERSISTENCE_PROPERTY_RNTIME_LOCATION = new QualifiedName(QUALIFIEDNAME_IDENTIFIER,
			RUNTIME_HOME);
	static QualifiedName PERSISTENCE_PROPERTY_SERVER_SUPPLIED_RUNTIME = new QualifiedName(
			QUALIFIEDNAME_IDENTIFIER,
			PERSISTENT_PROPERTY_IS_SERVER_SUPPLIED_RUNTIME);
	
	
	
	
	
	/*
	 * Other constants
	 */
	public final static String SAR_EXTENSION = ".sar";//$NON-NLS-1$
	public final static String SAR_PROJECT_FACET = "jst.jboss.sar";//$NON-NLS-1$
	public final static String SAR_PROJECT_FACET_TEMPLATE = "template.jst.jboss.sar";//$NON-NLS-1$
	public final static String BUILD_CLASSES = "build/classes";//$NON-NLS-1$
	public final static String META_INF = "META-INF";//$NON-NLS-1$
	
	public final static String DEFAULT_SAR_CONFIG_RESOURCE_FOLDER = "sarcontent";//$NON-NLS-1$
	public final static String DEFAULT_SAR_SOURCE_FOLDER = "src";//$NON-NLS-1$
	
	public final static String SAR_PROJECT_NATURE = "org.jboss.tools.esb.project.SARNature";//$NON-NLS-1$
	
	
}
