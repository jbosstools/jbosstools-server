/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
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
	public static final JBossServerType EAP61 = new ServerBeanTypeEAP61();
	public static final JBossServerType WILDFLY80 = new ServerBeanTypeWildfly80();
	public static final JBossServerType JPP6 = new ServerBeanTypeJPP6();
	/**
	 * @since 3.0 (actually 2.4.101)
	 */
	public static final JBossServerType JPP61 = new ServerBeanTypeJPP61();
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
	 */
	public static final JBossServerType[] KNOWN_TYPES =
		{
		JBossServerType.AS, 
		JBossServerType.WILDFLY80, 
		JBossServerType.EAP61,
		JBossServerType.SOA6,
		JBossServerType.JPP61, 
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
	protected JBossServerType(String id, String name, String jbossSystemJarPath, String[] versions, Condition condition) {
		super(id, name, jbossSystemJarPath, condition);
		this.versions = versions;
	}
	
	@Deprecated
	public String[] getVersions() {
		return versions;
	}
	
	
	@Deprecated
	static interface Condition extends ICondition {
	}
	
	@Deprecated
	public static abstract class AbstractCondition 
		extends org.jboss.ide.eclipse.as.core.server.bean.AbstractCondition 
		implements Condition {

		public String getFullVersion(File location, File systemFile) {
			return AbstractCondition.getFullServerVersionFromZipLegacy(systemFile, 
					getJBossManifestAttributes());
		}
		
		private static String[] getJBossManifestAttributes() {
			return new String[]{
					"JBossEAP-Release-Version",
					"Specification-Version",
					"Implementation-Version"
			};
		}
	}
	

	
	@Deprecated
	public static class EAPStandaloneServerTypeCondition extends ServerBeanTypeEAPStandalone.EAPStandaloneServerTypeCondition {}
	@Deprecated
	public static class ASServerTypeCondition extends ServerBeanTypeAS.ASServerTypeCondition {}
	@Deprecated
	public static class AS7ServerTypeCondition extends ServerBeanTypeAS7.AS7ServerTypeCondition {}
	@Deprecated
	public static class AS72ServerTypeCondition extends ServerBeanTypeAS72.AS72ServerTypeCondition {}
	@Deprecated
	public static class SOAPServerTypeCondition extends ServerBeanTypeSOAP.SOAPServerTypeCondition{}
	@Deprecated
	public static class SOAPStandaloneServerTypeCondition extends ServerBeanTypeSOAPStandalone.SOAPStandaloneServerTypeCondition {}
	@Deprecated
	public static class EWPTypeCondition extends ServerBeanTypeEWP.EWPTypeCondition {}
	@Deprecated
	public static class EPPTypeCondition extends ServerBeanTypeEPP.EPPTypeCondition {}
	@Deprecated
	public static class EAPServerTypeCondition extends ServerBeanTypeEAP.EAPServerTypeCondition {}
	@Deprecated
	public static class EAP6ServerTypeCondition extends ServerBeanTypeEAP6.EAP6ServerTypeCondition{}
	@Deprecated
	public static class EAP61ServerTypeCondition extends ServerBeanTypeEAP61.EAP61ServerTypeCondition {};
	@Deprecated
	public static class JPP6ServerTypeCondition extends ServerBeanTypeJPP6.JPP6ServerTypeCondition{};
	@Deprecated
	public static class AS7GateInServerTypeCondition extends ServerBeanTypeAS7GateIn.AS7GateInServerTypeCondition {}
	
	
	/* Deprecated methods which are poorly defined  */
	@Deprecated
	public static boolean isEAP(File systemJarFile) {
		String title = getJarProperty(systemJarFile, IMPLEMENTATION_TITLE);
		return title != null && title.contains("EAP"); //$NON-NLS-1$
	}
	
	@Deprecated
	public static boolean isEAP6(File systemJarFile) {
		String value = getJarProperty(systemJarFile, JBEAP_RELEASE_VERSION);
		if( value != null && value.trim().startsWith("6.")) //$NON-NLS-1$
				return true;
		return false;
	}

	@Deprecated
	protected static boolean checkAS7StyleVersion(File location, String mainFolder, String property, String propPrefix) {
		return scanFolderJarsForManifestProp(location, mainFolder, property, propPrefix);
	}
		
	/**
	 * This method is almost impossible to return accurately.
	 * AS7 and AS-5 both have the same JBossServerType.name value, "AS", 
	 * so if a user wishes to get the JBossServerType which corresponds
	 * to AS7, it is impossible for him to do so. 
	 * 
	 * This method really should be deprecated or fixed. 
	 * 
	 * @param name
	 * @return
	 */
	@Deprecated
	public static JBossServerType getType(String name) {
		if(AS.name.equals(name)) {
			return AS;
		} else if(EAP.name.equals(name)) {
			return EAP;
		} else if(SOAP.name.equals(name)) {
			return SOAP;
		} else if(SOAP_STD.name.equals(name)) {
			return SOAP_STD;
		} else if(EWP.name.equals(name)) {
			return EWP;
		} else if(EPP.name.equals(name)) {
			return EPP;
		}
		// TODO externalize
		throw new IllegalArgumentException("Name '" + name + "' cannot be converted to ServerType"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
