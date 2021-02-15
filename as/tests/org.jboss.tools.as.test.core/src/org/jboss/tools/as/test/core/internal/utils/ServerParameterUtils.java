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
package org.jboss.tools.as.test.core.internal.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class ServerParameterUtils {

	public static final String ZIPPED = "zipped";
	public static final String UNZIPPED = "unzipped";
	public static final String ZIP_OPTION_UNSET = "zipUnset";
	public static final String DEPLOY_META = "metadata";
	public static final String DEPLOY_SERVER = "server";
	public static final String DEPLOY_CUSTOM_REL = "customRelative";
	public static final String DEPLOY_CUSTOM_ABS = "customAbsolute";
	public static final String DEPLOY_CUSTOM_NULL = "customNull";
	public static final String DEPLOY_PERMOD_DEFAULT = "permod_default";
	public static final String DEPLOY_PERMOD_ABS = "permod_absolute";
	public static final String DEPLOY_PERMOD_REL = "permod_relative";
	
	protected static final List<String> TESTED_SERVERS = new ArrayList<String>();
	
	static {
		// AUTOGEN_SERVER_ADAPTER_CHUNK
		TESTED_SERVERS.add(IJBossToolingConstants.SERVER_WILDFLY_210);
		TESTED_SERVERS.add(IJBossToolingConstants.SERVER_WILDFLY_220);
		TESTED_SERVERS.add(IJBossToolingConstants.SERVER_WILDFLY_230);
		// AUTOGEN_SERVER_ADAPTER_CHUNK
		TESTED_SERVERS.add(IJBossToolingConstants.SERVER_EAP_71); 
		TESTED_SERVERS.add(IJBossToolingConstants.SERVER_EAP_72); 
		TESTED_SERVERS.add(IJBossToolingConstants.SERVER_EAP_73);
		// AUTOGEN_SERVER_ADAPTER_CHUNK
		// NEW_SERVER_ADAPTER Add the new runtime constant above this line
	}
	

	// Turn an array [item1, item2, item3] into a collection of 1-length items
	// ie new Collection<Object[]>() { new Object[]{item1}, new Object[]{item2}, new Object[]{item3}};
	
	public static Collection<Object[]> asCollection(Object[] items) {
		ArrayList<Object[]> ret = new ArrayList<Object[]>();
		for( int i = 0; i < items.length; i++ ) {
			ret.add(new Object[]{items[i]});
		}
		return ret;
	}
	
	public static Collection<Object[]> asCollection(String[] items) {
		ArrayList<Object[]> ret = new ArrayList<Object[]>();
		for( int i = 0; i < items.length; i++ ) {
			ret.add(new Object[]{items[i]});
		}
		return ret;
	}
	
	public static final String SKIP_PRIVATE_REQUIREMENTS = 
			"org.jboss.tools.tests.skipPrivateRequirements";
	public static boolean skipPrivateRequirements() {
        if( Boolean.getBoolean(SKIP_PRIVATE_REQUIREMENTS))
            return true;
        return false;
	}
	
	public static Object[] getJBossServerTypeParametersPlusAdditionalMocks() {
		ArrayList<Object> l = new ArrayList<Object>(Arrays.asList(getJBossServerTypeParameters()));
		l.addAll(Arrays.asList(ServerCreationTestUtils.TEST_SERVER_TYPES_TO_MOCK));
		return (String[]) l.toArray(new String[l.size()]);
	}

	public static String[] getJBossServerTypeParameters() {
		boolean skipReqs = skipPrivateRequirements();
		ArrayList<String> jbservers = new ArrayList<String>();
		for( int i = 0; i < IJBossToolingConstants.ALL_JBOSS_SERVERS.length; i++ ) {
			// we're not skipping reqs, or, we are skipping AND it doesn't start with eap, then add
			if( (!skipReqs || !IJBossToolingConstants.ALL_JBOSS_SERVERS[i].startsWith(IJBossToolingConstants.EAP_SERVER_PREFIX)) 
					&& TESTED_SERVERS.contains(IJBossToolingConstants.ALL_JBOSS_SERVERS[i])) {
					jbservers.add(IJBossToolingConstants.ALL_JBOSS_SERVERS[i]);
			}
		}
		return (String[]) jbservers.toArray(new String[jbservers.size()]);
	}
	
	@Deprecated
	public static String[] getAllJBossServerTypeParamterers() {
		return getAllJBossServerTypeParameters();
	}
	
	public static String[] getAllJBossServerTypeParameters() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(IJBossToolingConstants.DEPLOY_ONLY_SERVER);
		list.addAll(Arrays.asList(getJBossServerTypeParameters()));
		return (String[]) list.toArray(new String[list.size()]);
	}
	
	/*
	 * Return the most common DIFFERENT server types where impl may matter
	 */
	public static String[] getPublishServerTypes() {
		return new String[] { 
				IJBossToolingConstants.DEPLOY_ONLY_SERVER,
				IJBossToolingConstants.SERVER_AS_60, IJBossToolingConstants.SERVER_AS_71
		};
	}
	
	public static String[] getServerZipOptions() {
		return new String[] { 
				ZIPPED, UNZIPPED
		};
	}
	
	public static String[] getDefaultDeployOptions() {
		return new String[] { 
				DEPLOY_META, DEPLOY_SERVER, DEPLOY_CUSTOM_NULL,  DEPLOY_CUSTOM_ABS, DEPLOY_CUSTOM_REL
		};
	}

	/* TODO add changing the deploy name (ex: from project.ear to project1.jar */
	public static String[] getPerModuleOverrideOptions() {
		return new String[] { 
				DEPLOY_PERMOD_DEFAULT, DEPLOY_PERMOD_ABS, DEPLOY_PERMOD_REL
		};
	}

	
}
