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
package org.jboss.ide.eclipse.as.classpath.core;


public interface ClasspathConstants {
	
	/* Server runtime types */
	public static final String AS_32 = "org.jboss.ide.eclipse.as.runtime.32"; //$NON-NLS-1$
	public static final String AS_40 = "org.jboss.ide.eclipse.as.runtime.40"; //$NON-NLS-1$
	public static final String AS_42 = "org.jboss.ide.eclipse.as.runtime.42"; //$NON-NLS-1$
	public static final String AS_50 = "org.jboss.ide.eclipse.as.runtime.50"; //$NON-NLS-1$
	public static final String AS_51 = "org.jboss.ide.eclipse.as.runtime.51"; //$NON-NLS-1$
	public static final String EAP_43 = "org.jboss.ide.eclipse.as.runtime.eap.43"; //$NON-NLS-1$
	public static final String EAP_50 = "org.jboss.ide.eclipse.as.runtime.eap.50"; //$NON-NLS-1$
	
	/* Version Strings */
	public static final String V3_0 = "3.0"; //$NON-NLS-1$
	public static final String V3_2 = "3.2"; //$NON-NLS-1$
	public static final String V4_0 = "4.0"; //$NON-NLS-1$
	public static final String V4_2 = "4.2"; //$NON-NLS-1$
	public static final String V5_0 = "5.0"; //$NON-NLS-1$
	public static final String V5_1 = "5.1"; //$NON-NLS-1$
	
	
	/* Files and folders that must be added to CP */
	public static final String SERVER = "server"; //$NON-NLS-1$
	public static final String CLIENT = "client"; //$NON-NLS-1$
	public static final String LIB = "lib"; //$NON-NLS-1$
	public static final String DEPLOY = "deploy"; //$NON-NLS-1$
	public static final String COMMON = "common"; //$NON-NLS-1$
	public static final String DEPLOYERS = "deployers"; //$NON-NLS-1$
	
	public static final String JSF_LIB = "jsf-libs"; //$NON-NLS-1$
	public static final String JBOSSWEB_TOMCAT55_SAR = "jbossweb-tomcat55.sar"; //$NON-NLS-1$
	public static final String JBOSSWEB_SAR = "jbossweb.sar"; //$NON-NLS-1$
	public static final String JBOSS_WEB_SERVICE_JAR = "jboss-web-service.jar";  //$NON-NLS-1$
	public static final String EJB3_DEPLOYER = "ejb3.deployer"; //$NON-NLS-1$
	public static final String AS5_AOP_DEPLOYER = "jboss-aop-jboss5.deployer"; //$NON-NLS-1$
	public static final String AOP_JDK5_DEPLOYER = "jboss-aop-jdk50.deployer"; //$NON-NLS-1$
	public static final String JBOSS_AOP_JDK5_JAR = "jboss-aop-jdk50.jar"; //$NON-NLS-1$
	public static final String JBOSS_WEB_DEPLOYER = "jboss-web.deployer"; //$NON-NLS-1$
	public static final String JSP_API_JAR = "jsp-api.jar"; //$NON-NLS-1$
	public static final String SERVLET_API_JAR = "servlet-api.jar"; //$NON-NLS-1$
	public static final String JSF_API_JAR = "jsf-api.jar"; //$NON-NLS-1$
	public static final String JSF_IMPL_JAR = "jsf-impl.jar"; //$NON-NLS-1$
	public static final String JBOSS_J2EE_JAR = "jboss-j2ee.jar"; //$NON-NLS-1$
	public static final String JBOSS_EJB3X_JAR = "jboss-ejb3x.jar"; //$NON-NLS-1$
	public static final String JBOSS_EJB3_JAR = "jboss-ejb3.jar"; //$NON-NLS-1$
	public static final String JBOSS_ANNOTATIONS_EJB3_JAR = "jboss-annotations-ejb3.jar"; //$NON-NLS-1$
	public static final String EJB3_PERSISTENCE_JAR = "ejb3-persistence.jar"; //$NON-NLS-1$
	public static final String JBOSS_ASPECT_LIBRARY_JDK5_0 = "jboss-aspect-library-jdk50.jar"; //$NON-NLS-1$
	public static final String HIBERNATE_CLIENT_JAR = "hibernate-client.jar";  //$NON-NLS-1$
	public static final String JBOSSALL_CLIENT_JAR = "jbossall-client.jar"; //$NON-NLS-1$
	public static final String JBOSSWEB_TOMCAT_50_SAR = "jbossweb-tomcat50.sar"; //$NON-NLS-1$
	
	public static final String JAVAX_SERVLET_JAR = "javax.servlet.jar"; //$NON-NLS-1$
	public static final String JAVAX_SERVLET_JSP_JAR = "javax.servlet.jsp.jar"; //$NON-NLS-1$
	
	
	// etc
	public static final String JAR_EXT = ".jar"; //$NON-NLS-1$
	
	/* Facet Names */
	public static final String FACET_JST_JAVA = "jst.java"; //$NON-NLS-1$
	public static final String FACET_WEB = "jst.web";//$NON-NLS-1$
	public static final String FACET_EJB = "jst.ejb";//$NON-NLS-1$
	public static final String FACET_EAR = "jst.ear";//$NON-NLS-1$
	public static final String FACET_UTILITY  = "jst.utility";//$NON-NLS-1$
	public static final String FACET_CONNECTOR  = "jst.connector";//$NON-NLS-1$
	public static final String FACET_APP_CLIENT = "jst.appclient";//$NON-NLS-1$
	
	
}
