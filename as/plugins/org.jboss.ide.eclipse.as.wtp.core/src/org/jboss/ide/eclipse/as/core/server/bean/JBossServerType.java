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
	public static final JBossServerType WILDFLY100 = new ServerBeanTypeWildfly10Plus("10.0", IJBossToolingConstants.SERVER_WILDFLY_100);
	public static final JBossServerType WILDFLY100_WEB = new ServerBeanTypeWildfly10PlusWeb("10.0", IJBossToolingConstants.SERVER_WILDFLY_100);
	public static final JBossServerType WILDFLY110 = new ServerBeanTypeWildfly10Plus("11.0", IJBossToolingConstants.SERVER_WILDFLY_110);
	public static final JBossServerType WILDFLY110_WEB = new ServerBeanTypeWildfly10PlusWeb("11.0", IJBossToolingConstants.SERVER_WILDFLY_110);
	public static final JBossServerType WILDFLY120 = new ServerBeanTypeWildfly10Plus("12.0", IJBossToolingConstants.SERVER_WILDFLY_120);
	public static final JBossServerType WILDFLY120_WEB = new ServerBeanTypeWildfly10PlusWeb("12.0", IJBossToolingConstants.SERVER_WILDFLY_120);
	public static final JBossServerType WILDFLY130 = new ServerBeanTypeWildfly10Plus("13.0", IJBossToolingConstants.SERVER_WILDFLY_130);
	public static final JBossServerType WILDFLY130_WEB = new ServerBeanTypeWildfly10PlusWeb("13.0", IJBossToolingConstants.SERVER_WILDFLY_130);
	public static final JBossServerType WILDFLY140 = new ServerBeanTypeWildfly10Plus("14.0", IJBossToolingConstants.SERVER_WILDFLY_140);
	public static final JBossServerType WILDFLY140_WEB = new ServerBeanTypeWildfly10PlusWeb("14.0", IJBossToolingConstants.SERVER_WILDFLY_140);
	public static final JBossServerType WILDFLY150 = new ServerBeanTypeWildfly10Plus("15.0", IJBossToolingConstants.SERVER_WILDFLY_150);
	public static final JBossServerType WILDFLY150_WEB = new ServerBeanTypeWildfly10PlusWeb("15.0", IJBossToolingConstants.SERVER_WILDFLY_150);
	public static final JBossServerType WILDFLY160 = new ServerBeanTypeWildfly10Plus("16.0", IJBossToolingConstants.SERVER_WILDFLY_160);
	public static final JBossServerType WILDFLY160_WEB = new ServerBeanTypeWildfly10PlusWeb("16.0", IJBossToolingConstants.SERVER_WILDFLY_160);
	public static final JBossServerType WILDFLY170 = new ServerBeanTypeWildfly10Plus("17.0", IJBossToolingConstants.SERVER_WILDFLY_170);
	public static final JBossServerType WILDFLY170_WEB = new ServerBeanTypeWildfly10PlusWeb("17.0", IJBossToolingConstants.SERVER_WILDFLY_170);
	public static final JBossServerType WILDFLY180 = new ServerBeanTypeWildfly10Plus("18.0", IJBossToolingConstants.SERVER_WILDFLY_180);
	public static final JBossServerType WILDFLY180_WEB = new ServerBeanTypeWildfly10PlusWeb("18.0", IJBossToolingConstants.SERVER_WILDFLY_180);
	public static final JBossServerType WILDFLY190 = new ServerBeanTypeWildfly10Plus("19.0", IJBossToolingConstants.SERVER_WILDFLY_190, "main");
	public static final JBossServerType WILDFLY190_WEB = new ServerBeanTypeWildfly10PlusWeb("19.0", IJBossToolingConstants.SERVER_WILDFLY_190);
	public static final JBossServerType WILDFLY200 = new ServerBeanTypeWildfly10Plus("20.0", IJBossToolingConstants.SERVER_WILDFLY_200, "main");
	public static final JBossServerType WILDFLY200_WEB = new ServerBeanTypeWildfly10PlusWeb("20.0", IJBossToolingConstants.SERVER_WILDFLY_200);
	public static final JBossServerType WILDFLY210 = new ServerBeanTypeWildfly10Plus("21.0", IJBossToolingConstants.SERVER_WILDFLY_210, "main");
	public static final JBossServerType WILDFLY210_WEB = new ServerBeanTypeWildfly10PlusWeb("21.0", IJBossToolingConstants.SERVER_WILDFLY_210);

	public static final JBossServerType EAP70 = new ServerBeanTypeEAP7Plus("7.0", IJBossToolingConstants.SERVER_EAP_70);
	public static final JBossServerType EAP71 = new ServerBeanTypeEAP7Plus("7.1", IJBossToolingConstants.SERVER_EAP_71);
	public static final JBossServerType EAP72 = new ServerBeanTypeEAP7Plus("7.2", IJBossToolingConstants.SERVER_EAP_72);
	public static final JBossServerType EAP73 = new ServerBeanTypeEAP7Plus("7.3", IJBossToolingConstants.SERVER_EAP_73);
	public static final JBossServerType WILDFLY220 = new ServerBeanTypeWildfly10Plus("22.0", IJBossToolingConstants.SERVER_WILDFLY_220, "main");
	public static final JBossServerType WILDFLY220_WEB = new ServerBeanTypeWildfly10PlusWeb("22.0", IJBossToolingConstants.SERVER_WILDFLY_220);
	public static final JBossServerType WILDFLY230 = new ServerBeanTypeWildfly10Plus("23.0", IJBossToolingConstants.SERVER_WILDFLY_230, "main");
	public static final JBossServerType WILDFLY230_WEB = new ServerBeanTypeWildfly10PlusWeb("23.0", IJBossToolingConstants.SERVER_WILDFLY_230);
	public static final JBossServerType EAP74 = new ServerBeanTypeEAP7Plus("7.4", IJBossToolingConstants.SERVER_EAP_74);
	public static final JBossServerType WILDFLY240 = new ServerBeanTypeWildfly10Plus("24.0", IJBossToolingConstants.SERVER_WILDFLY_240, "main");
	public static final JBossServerType WILDFLY240_WEB = new ServerBeanTypeWildfly10PlusWeb("24.0", IJBossToolingConstants.SERVER_WILDFLY_240);
	// AUTOGEN_SERVER_ADAPTER_CHUNK








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
		JBossServerType.EAP74, 
		// AUTOGEN_SERVER_ADAPTER_CHUNK




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
		JBossServerType.WILDFLY190,JBossServerType.WILDFLY190_WEB,
		JBossServerType.WILDFLY200,JBossServerType.WILDFLY200_WEB,
		JBossServerType.WILDFLY210,JBossServerType.WILDFLY210_WEB,
		JBossServerType.WILDFLY220,JBossServerType.WILDFLY220_WEB,
		JBossServerType.WILDFLY230,JBossServerType.WILDFLY230_WEB,
		JBossServerType.WILDFLY240,JBossServerType.WILDFLY240_WEB,
		// AUTOGEN_SERVER_ADAPTER_CHUNK







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
		