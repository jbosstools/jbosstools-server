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

import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.server.core.IRuntimeType;
import org.jboss.ide.eclipse.as.classpath.core.runtime.IRuntimePathProvider;
import org.jboss.ide.eclipse.as.classpath.core.runtime.RuntimeJarUtility;
import org.jboss.ide.eclipse.as.classpath.core.runtime.path.internal.RuntimePathProviderFileset;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

/**
 * Find ALL jars for a relevant configuration of a server
 */
public class SourceJarsLocator implements IJBossToolingConstants, IJBossRuntimeResourceConstants {
	private static final String SEP = "/"; //$NON-NLS-1$
	private static final String CONFIG_DIR = RuntimeJarUtility.CONFIG_DIR_VAR_PATTERN;

	public IRuntimePathProvider[] getDefaultPathProviders(IRuntimeType rt) {
		ServerExtendedProperties props = (ServerExtendedProperties) Platform.getAdapterManager().getAdapter(
				rt, ServerExtendedProperties.class);
		IRuntimePathProvider[] providers = new IRuntimePathProvider[]{};
		if( props != null ) {
			int structure = props.getFileStructure();
			if( structure == ServerExtendedProperties.FILE_STRUCTURE_SERVER_CONFIG_DEPLOY) {
				providers = serverConfigDeployJars(rt);
			} else if( structure == ServerExtendedProperties.FILE_STRUCTURE_CONFIG_DEPLOYMENTS ) { 
				providers = configDeploymentsJars(rt);
			}
		}
		// NEW_SERVER_ADAPTER add logic for new adapter here
		return providers;
	}
	
	/* Essentially, jars for app servers based on AS-6.x or lower */
	private IRuntimePathProvider[] serverConfigDeployJars(IRuntimeType rt) {
		ArrayList<RuntimePathProviderFileset> sets = new ArrayList<RuntimePathProviderFileset>();
		sets.add(new RuntimePathProviderFileset(COMMON));
		sets.add(new RuntimePathProviderFileset(LIB));
		sets.add(new RuntimePathProviderFileset(CONFIG_DIR + SEP + LIB));
		sets.add(new RuntimePathProviderFileset(CONFIG_DIR + SEP + DEPLOY + SEP + LIB));
		sets.add(new RuntimePathProviderFileset(CONFIG_DIR + SEP + DEPLOY + SEP + "jbossweb.sar")); //$NON-NLS-1$
		sets.add(new RuntimePathProviderFileset(CONFIG_DIR + SEP + DEPLOYERS));
		sets.add(new RuntimePathProviderFileset(CLIENT));
		return sets.toArray(new RuntimePathProviderFileset[sets.size()]);
	}

	/* Essentially, jars for app servers based on AS-7 or higher */
	private IRuntimePathProvider[] configDeploymentsJars(IRuntimeType rt) {
		ArrayList<RuntimePathProviderFileset> sets = new ArrayList<RuntimePathProviderFileset>();
		sets.add(new RuntimePathProviderFileset("jboss-modules.jar"));
		sets.add(new RuntimePathProviderFileset("modules"));
		sets.add(new RuntimePathProviderFileset("bundles"));
		return sets.toArray(new RuntimePathProviderFileset[sets.size()]);
	}
}
