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
import java.util.HashMap;
import java.util.List;

import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.LatestServerUtility;
import org.jboss.tools.as.test.core.TestConstants;

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
	
	protected static final List<String> TESTED_SERVER_TYPES = new ArrayList<String>();	
	static {
		// AUTOGEN_SERVER_ADAPTER_CHUNK
		TESTED_SERVER_TYPES.add(IJBossToolingConstants.SERVER_WILDFLY_240);
		TESTED_SERVER_TYPES.add(IJBossToolingConstants.SERVER_WILDFLY_270);
		// AUTOGEN_SERVER_ADAPTER_CHUNK
		TESTED_SERVER_TYPES.add(IJBossToolingConstants.SERVER_EAP_74);
		TESTED_SERVER_TYPES.add(IJBossToolingConstants.SERVER_EAP_80);
		// AUTOGEN_SERVER_ADAPTER_CHUNK
		// NEW_SERVER_ADAPTER Add the new runtime constant above this line
	}

	
	protected static final List<String> TESTED_SERVER_HOMES = new ArrayList<String>();	
	static {
		// AUTOGEN_SERVER_ADAPTER_CHUNK
		TESTED_SERVER_HOMES.add(TestConstants.JBOSS_WF_260_HOME);
		TESTED_SERVER_HOMES.add(TestConstants.JBOSS_WF_270_HOME);
		// AUTOGEN_SERVER_ADAPTER_CHUNK
		TESTED_SERVER_HOMES.add(TestConstants.JBOSS_EAP_74_HOME);
		TESTED_SERVER_HOMES.add(TestConstants.JBOSS_EAP_80_HOME);
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
		String[] toTest = (String[]) TESTED_SERVER_TYPES.toArray(new String[TESTED_SERVER_TYPES.size()]);
		for( int i = 0; i < toTest.length; i++ ) {
			// we're not skipping reqs, or, we are skipping AND it doesn't start with eap, then add
			boolean dontSkip = (!skipReqs || !toTest[i].startsWith(IJBossToolingConstants.EAP_SERVER_PREFIX));
			if( dontSkip ) 
				jbservers.add(toTest[i]);
		}
		return (String[]) jbservers.toArray(new String[jbservers.size()]);
	}
	
	public static String[] getJBossServerHomeParameters() {
		boolean skipReqs = skipPrivateRequirements();
		ArrayList<String> ret = new ArrayList<String>();
		String[] toTest = (String[]) TESTED_SERVER_HOMES.toArray(new String[TESTED_SERVER_HOMES.size()]);
		HashMap<String, String> map = TestConstants.serverHomeDirToServerType();
		for( int i = 0; i < toTest.length; i++ ) {
			String home = toTest[i];
			String serverType = map.get(home);
			// we're not skipping reqs, or, we are skipping AND it doesn't start with eap, then add
			boolean dontSkip = (!skipReqs || !serverType.startsWith(IJBossToolingConstants.EAP_SERVER_PREFIX));
			if( dontSkip ) 
				ret.add(toTest[i]);
		}
		return (String[]) ret.toArray(new String[ret.size()]);
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
//		return new String[] { 
//				IJBossToolingConstants.DEPLOY_ONLY_SERVER,
//				IJBossToolingConstants.SERVER_AS_60, IJBossToolingConstants.SERVER_AS_71
//		};
		return new String[] { LatestServerUtility.findLatestWildflyServerTypeId()};
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
