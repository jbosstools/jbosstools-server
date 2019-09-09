/******************************************************************************* 
 * Copyright (c) 2013-2019 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.bean;

import java.io.File;

import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class JBossServerType extends ServerBeanType implements IJBossToolingConstants {
	
	public static final String JBOSS_AS_PATH = "jboss-as"; //$NON-NLS-1$
	public static final String BIN_PATH = "bin"; //$NON-NLS-1$
	public static final String RUN_JAR_NAME = "run.jar"; //$NON-NLS-1$
	public static final String IMPLEMENTATION_TITLE = "Implementation-Title"; //$NON-NLS-1$
	private static final String JBEAP_RELEASE_VERSION = "JBossEAP-Release-Version"; //$NON-NLS-1$
	

	// NEW_SERVER_ADAPTER
	public static final JBossServerType AS = new ServerBeanTypeAS();
	public static final JBossServerType AS7 = new ServerBeanTypeAS7();
	public static final JBossServerType EAP_STD = new ServerBeanTypeEAPStandalone();
	public static final JBossServerType EAP = new ServerBeanTypeEAP();
	public static final JBossServerType EAP6 = new ServerBeanTypeEAP6();
	public static final JBossServerType UNKNOWN_AS72_PRODUCT = new ServerBeanTypeUnknownAS72Product();
	public static final JBossServerType AS72 = new ServerBeanTypeAS72();
	public static final JBossServerType WILDFLY80 = new ServerBeanTypeWildfly80();
	public static final JBossServerType WILDFLY90 = new ServerBeanTypeWildfly90();
	public static final JBossServerType WILDFLY90_WEB = new ServerBeanTypeWildfly90Web();
	public static final JBossServerType WILDFLY100 = new ServerBeanTypeWildfly100();
	public static final JBossServerType WILDFLY100_WEB = new ServerBeanTypeWildfly100Web();
	public static final JBossServerType WILDFLY110 = new ServerBeanTypeWildfly110();
	public static final JBossServerType WILDFLY110_WEB = new ServerBeanTypeWildfly110Web();
	public static final JBossServerType WILDFLY120 = new ServerBeanTypeWildfly120();
	public static final JBossServerType WILDFLY120_WEB = new ServerBeanTypeWildfly120Web();

	public static final JBossServerType WILDFLY130 = new ServerBeanTypeWildfly130();
	public static final JBossServerType WILDFLY130_WEB = new ServerBeanTypeWildfly130Web();
	public static final JBossServerType WILDFLY140 = new ServerBeanTypeWildfly140();
	public static final JBossServerType WILDFLY140_WEB = new ServerBeanTypeWildfly140Web();
	public static final JBossServerType WILDFLY150 = new ServerBeanTypeWildfly150();
	public static final JBossServerType WILDFLY150_WEB = new ServerBeanTypeWildfly150Web();
	public static final JBossServerType WILDFLY160 = new ServerBeanTypeWildfly160();
	public static final JBossServerType WILDFLY160_WEB = new ServerBeanTypeWildfly160Web();
	public static final JBossServerType WILDFLY170 = new ServerBeanTypeWildfly170();
	public static final JBossServerType WILDFLY170_WEB = new ServerBeanTypeWildfly170Web();
	public static final JBossServerType WILDFLY180 = new ServerBeanTypeWildfly180();
	public static final JBossServerType WILDFLY180_WEB = new ServerBeanTypeWildfly180Web();

	public static final JBossServerType EAP70 = new ServerBeanTypeEAP70();
	public static final JBossServerType EAP71 = new ServerBeanTypeEAP71();
	public static final JBossServerType EAP72 = new ServerBeanTypeEAP72();
	public static final JBossServerType EAP73 = new ServerBeanTypeEAP73();
	
	public static final JBossServerType JPP6 = new ServerBeanTypeJPP6();
	
	/**
	 * @since 3.0 (actually 2.4.101)
	 */
	public static final JBossServerType JPP61 = new ServerBeanTypeJPP61();
	public static final JBossServerType DV6 = new DataVirtualization6ServerBeanType();
	public static final JBossServerType FSW6 = new ServerBeanTypeFSW6();
	public static final JBossServerType EAP61 = new ServerBeanTypeEAP61();
	public static final JBossServerType UNKNOWN_AS71_PRODUCT = new ServerBeanTypeUnknownAS71Product();	
	public static final JBossServerType SOA6 = new ServerBeanTypeSOA6();; 
	public static final JBossServerType SOAP = new ServerBeanTypeSOAP(); 
	public static final JBossServerType SOAP_STD = new ServerBeanTypeSOAPStandalone();
	public static final JBossServerType EWP = new ServerBeanTypeEWP();
	public static final JBossServerType EPP = new ServerBeanTypeEPP();
	public static final JBossServerType AS7GateIn = new ServerBeanTypeAS7GateIn();
	/* Any reason the unknown type needs so many versions? */
	public static final JBossServerType UNKNOWN = (JBossServerType)ServerBeanType.UNKNOWN;

	
	/**
	 * This public variable duplicates the hidden one. 
	 * We shouldn't have to update this in multiple places.
	 * 
	 * 	// NEW_SERVER_ADAPTER
	 */
	public static final JBossServerType[] KNOWN_TYPES =
		{
		JBossServerType.AS, 
		JBossServerType.EAP70,
		JBossServerType.EAP71,
		JBossServerType.EAP72,
		JBossServerType.EAP73,
		JBossServerType.WILDFLY90, 
		JBossServerType.WILDFLY90_WEB,
		JBossServerType.WILDFLY100,
		JBossServerType.WILDFLY100_WEB,
		JBossServerType.WILDFLY110,
		JBossServerType.WILDFLY110_WEB,
		JBossServerType.WILDFLY120,
		JBossServerType.WILDFLY120_WEB,
		JBossServerType.WILDFLY130,JBossServerType.WILDFLY130_WEB,
		JBossServerType.WILDFLY140,JBossServerType.WILDFLY140_WEB,
		JBossServerType.WILDFLY150,JBossServerType.WILDFLY150_WEB,
		JBossServerType.WILDFLY160,JBossServerType.WILDFLY160_WEB,
		JBossServerType.WILDFLY170,JBossServerType.WILDFLY170_WEB,
		JBossServerType.WILDFLY180,JBossServerType.WILDFLY180_WEB,
		JBossServerType.WILDFLY80, 
		JBossServerType.FSW6,
		JBossServerType.EAP61,
		JBossServerType.SOA6,
		JBossServerType.JPP61, 
		JBossServerType.DV6, 
		JBossServerType.UNKNOWN_AS72_PRODUCT,
		JBossServerType.AS72, 
		JBossServerType.JPP6, 
		JBossServerType.EAP6, 
		JBossServerType.AS7GateIn, 
		JBossServerType.UNKNOWN_AS71_PRODUCT,
		JBossServerType.AS7, JBossServerType.EAP_STD, 
		JBossServerType.SOAP, JBossServerType.SOAP_STD, 
		JBossServerType.EPP, JBossServerType.EAP, 
		JBossServerType.EWP
	};

	@Deprecated
	protected String[] versions = new String[0];
	protected JBossServerType(String id, String name, String jbossSystemJarPath, String[] versions, ICondition condition) {
		super(id, name, jbossSystemJarPath, condition);
		this.versions = versions;
	}
	
	@Deprecated
	public String[] getVersions() {
		return versions;
	}
	
	@Override
	public String getServerBeanName(File root) {
		return getServerTypeBaseName() + " " + ServerBeanLoader.getMajorMinorVersion(getFullVersion(root));
	}
	
	protected String getServerTypeBaseName() {
		return "JBoss " + getId();
	}
}
