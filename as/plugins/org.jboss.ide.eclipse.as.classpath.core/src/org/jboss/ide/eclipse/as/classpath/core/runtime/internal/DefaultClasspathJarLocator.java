/*******************************************************************************
 * Copyright (c) 2011-2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.core.runtime.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.jboss.ide.eclipse.as.classpath.core.runtime.IRuntimePathProvider;
import org.jboss.ide.eclipse.as.classpath.core.runtime.RuntimeJarUtility;
import org.jboss.ide.eclipse.as.classpath.core.runtime.RuntimePathProviderFileset;
import org.jboss.ide.eclipse.as.core.resolvers.RuntimeVariableResolver;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.tools.foundation.core.expressions.ExpressionResolver;
import org.jboss.tools.foundation.core.expressions.IVariableResolver;

/**
 * This class is in charge of the default classpath entries when
 * the users have not overridden the settings on a per-runtime-type basis. 
 */
public class DefaultClasspathJarLocator implements IJBossToolingConstants, IJBossRuntimeResourceConstants {
	private static final String SEP = "/"; //$NON-NLS-1$
	private static final String EMPTY = ""; //$NON-NLS-1$
	private static final String CONFIG_DIR = RuntimeJarUtility.CONFIG_DIR_VAR_PATTERN;
	
	public IRuntimePathProvider[] getDefaultPathProviders(IRuntimeType type) {
		String rtID = type.getId();
		if(AS_32.equals(rtID)) 
			return getDefaultAS3Entries();
		if(AS_40.equals(rtID)) 
			return getDefaultAS40Entries();
		if(AS_42.equals(rtID)) 
			return getDefaultAS40Entries();
		if(AS_50.equals(rtID)) 
			return getDefaultAS50Entries();
		if(EAP_43.equals(rtID))
			return getDefaultEAP43Entries();
		if(AS_51.equals(rtID)) 
			return getDefaultAS50Entries();
		if(AS_60.equals(rtID)) 
			return getDefaultAS60Entries();
		if(EAP_50.equals(rtID))
			return getDefaultAS50Entries();

		if(AS_70.equals(type.getId()))
			return getDefaultAS70Entries();
		if(AS_71.equals(type.getId()))
			return getDefaultAS71Entries();
		if(EAP_60.equals(type.getId()))
			return getDefaultAS71Entries();
		
		// This will change if jboss-as ever gets us an api
		if(EAP_61.equals(type.getId()))
			return getDefaultEAP61Entries();
		if(WILDFLY_80.equals(type.getId()))
			return getDefaultEAP61Entries();
		
		// NEW_SERVER_ADAPTER add logic for new adapter here
		return new IRuntimePathProvider[]{};
	}
	
	public IPath[] getDefaultPaths(IRuntime rt) {
		return getAllEntries(rt, getDefaultPathProviders(rt.getRuntimeType()));
	}


	public static IPath[] getAllEntries(IRuntime runtime, IRuntimePathProvider[] sets) {
		return getAllEntries(new RuntimeVariableResolver(runtime), sets);
	}

	public static IPath[] getAllEntries(Map<String, String> map, IRuntimePathProvider[] sets) {
		return getAllEntries(new ExpressionResolver.MapVariableResolver(map), sets);
	}
	
	public static IPath[] getAllEntries(IVariableResolver resolver, IRuntimePathProvider[] sets) {
		ArrayList<IPath> retval = new ArrayList<IPath>();
		for( int i = 0; i < sets.length; i++ ) {
			sets[i].setVariableResolver(resolver);
			IPath[] absolute = sets[i].getAbsolutePaths();
			for( int j = 0; j < absolute.length; j++ ) {
				if( !retval.contains(absolute[j]))
					retval.add(absolute[j]);
			}
		}
		return (IPath[]) retval.toArray(new IPath[retval.size()]);
	}
	
	public IRuntimePathProvider[] getDefaultAS3Entries() {
		ArrayList<RuntimePathProviderFileset> sets = new ArrayList<RuntimePathProviderFileset>();
		sets.add(new RuntimePathProviderFileset(LIB));
		sets.add(new RuntimePathProviderFileset(CONFIG_DIR + SEP + LIB));
		sets.add(new RuntimePathProviderFileset(CLIENT));
		return sets.toArray(new RuntimePathProviderFileset[sets.size()]);
	}
	
	public IRuntimePathProvider[] getDefaultAS40Entries() {
		ArrayList<RuntimePathProviderFileset> sets = new ArrayList<RuntimePathProviderFileset>();
		String deployPath = CONFIG_DIR + SEP + DEPLOY;
		sets.add(new RuntimePathProviderFileset(LIB));
		sets.add(new RuntimePathProviderFileset(CONFIG_DIR + SEP + LIB));
		sets.add(new RuntimePathProviderFileset(deployPath + SEP + JBOSS_WEB_DEPLOYER + SEP + JSF_LIB));
		sets.add(new RuntimePathProviderFileset(deployPath + SEP + AOP_JDK5_DEPLOYER));
		sets.add(new RuntimePathProviderFileset(deployPath + SEP + EJB3_DEPLOYER));
		sets.add(new RuntimePathProviderFileset(CLIENT));
		return sets.toArray(new RuntimePathProviderFileset[sets.size()]);
	}

	public IRuntimePathProvider[] get42() {
		return getDefaultAS40Entries();
	}

	public IRuntimePathProvider[] getDefaultEAP43Entries() {
		return getDefaultAS40Entries();
	}
	
	public IRuntimePathProvider[] getDefaultAS50Entries() {
		ArrayList<RuntimePathProviderFileset> sets = new ArrayList<RuntimePathProviderFileset>();
		String deployerPath = CONFIG_DIR + SEP + DEPLOYERS;
		String deployPath = CONFIG_DIR + SEP + DEPLOY;
		sets.add(new RuntimePathProviderFileset(COMMON + SEP + LIB));
		sets.add(new RuntimePathProviderFileset(LIB));
		sets.add(new RuntimePathProviderFileset(CONFIG_DIR + SEP + LIB));
		
		sets.add(new RuntimePathProviderFileset(deployPath + SEP + JBOSSWEB_SAR + SEP + JSF_LIB));
		sets.add(new RuntimePathProviderFileset(EMPTY, deployPath + SEP + JBOSSWEB_SAR, JBOSS_WEB_SERVICE_JAR, EMPTY));
		sets.add(new RuntimePathProviderFileset(EMPTY, deployPath + SEP + JBOSSWEB_SAR, JSTL_JAR, EMPTY));
		sets.add(new RuntimePathProviderFileset(deployerPath + SEP + AS5_AOP_DEPLOYER));
		sets.add(new RuntimePathProviderFileset(deployerPath + SEP + EJB3_DEPLOYER));
		sets.add(new RuntimePathProviderFileset(EMPTY, deployerPath + SEP + WEBBEANS_DEPLOYER,JSR299_API_JAR, EMPTY));
		sets.add(new RuntimePathProviderFileset(CLIENT));
		return sets.toArray(new RuntimePathProviderFileset[sets.size()]);
	}
	
	public IRuntimePathProvider[] getDefaultAS60Entries() {
		ArrayList<IRuntimePathProvider> sets = new ArrayList<IRuntimePathProvider>();
		sets.addAll(Arrays.asList(getDefaultAS50Entries()));
		sets.add(new RuntimePathProviderFileset(CONFIG_DIR + SEP + DEPLOYERS + SEP + REST_EASY_DEPLOYER));
		sets.add(new RuntimePathProviderFileset(CONFIG_DIR + SEP + DEPLOYERS + SEP + JSF_DEPLOYER + SEP + MOJARRA_20 + SEP + JSF_LIB));
		return sets.toArray(new RuntimePathProviderFileset[sets.size()]);
	}
	
	/*
	 * Temporary method where we pass in which folder as a prefix. 
	 * This does not support layering and will need to change once
	 * a proper api is provided.
	 */
	public IRuntimePathProvider[] getDefaultAS7xEntries(String modulesPrefix) {
		
		ArrayList<IRuntimePathProvider> sets = new ArrayList<IRuntimePathProvider>();
		sets.add(new RuntimePathProviderFileset(EMPTY, modulesPrefix + "/javax", "**/*.jar", "**/jsf-api-1.2*.jar")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		sets.add(new RuntimePathProviderFileset(modulesPrefix + "/org/hibernate/validator")); //$NON-NLS-1$
		sets.add(new RuntimePathProviderFileset(modulesPrefix + "/org/resteasy"));//$NON-NLS-1$
		sets.add(new RuntimePathProviderFileset(modulesPrefix + "/org/picketbox"));//$NON-NLS-1$
		sets.add(new RuntimePathProviderFileset(modulesPrefix + "/org/jboss/as/controller-client/main/"));//$NON-NLS-1$
		sets.add(new RuntimePathProviderFileset(modulesPrefix + "/org/jboss/dmr/main/"));//$NON-NLS-1$
		sets.add(new RuntimePathProviderFileset(modulesPrefix + "/org/jboss/logging/main"));//$NON-NLS-1$
		sets.add(new RuntimePathProviderFileset(modulesPrefix + "/org/jboss/resteasy/resteasy-jaxb-provider/main"));//$NON-NLS-1$
		sets.add(new RuntimePathProviderFileset(modulesPrefix + "/org/jboss/resteasy/resteasy-jaxrs/main"));//$NON-NLS-1$
		sets.add(new RuntimePathProviderFileset(modulesPrefix + "/org/jboss/resteasy/resteasy-multipart-provider/main"));//$NON-NLS-1$
		sets.add(new RuntimePathProviderFileset(modulesPrefix + "/org/jboss/ejb3/main"));//$NON-NLS-1$
		return (IRuntimePathProvider[]) sets.toArray(new IRuntimePathProvider[sets.size()]);
	}
	
	/* 
	 * TODO have use the extended properties to get this path. Should not be hard-coded 
	 * Unfortunately currently no api exists to access this. 
	 */
	public IRuntimePathProvider[] getDefaultAS70Entries() {
		return getDefaultAS7xEntries("modules");//$NON-NLS-1$
	}
	public IRuntimePathProvider[] getDefaultEAP61Entries() {
		return getDefaultAS7xEntries("modules/system/layers/base");//$NON-NLS-1$
	}
	
	public IRuntimePathProvider[] getDefaultAS71Entries() {
		return getDefaultAS70Entries();
	}

}
