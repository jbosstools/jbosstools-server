/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.util;

/**
 * This is a dumb class that assumes the latest / newest are at the end of the array
 * I feel no shame about this. 
 * @author rob
 *
 */
public class LatestServerUtility {

	public static String findLatestWildflyServerTypeId() {
		String prefix = IJBossToolingConstants.WF_SERVER_PREFIX;
		return findLatestServerTypeId(prefix);
	}

	public static String findLatestWildflyRuntimeTypeId() {
		String prefix = IJBossToolingConstants.WF_RUNTIME_PREFIX;
		return findLatestRuntimeTypeId(prefix);
	}
	
	public static String findLatestEAPServerTypeId() {
		String prefix = IJBossToolingConstants.EAP_SERVER_PREFIX;
		return findLatestServerTypeId(prefix);
	}

	public static String findLatestEAPRuntimeTypeId() {
		String prefix = IJBossToolingConstants.EAP_RUNTIME_PREFIX;
		return findLatestRuntimeTypeId(prefix);
	}
	
	private static String findLatestServerTypeId(String prefix) {
		String[] arr = IJBossToolingConstants.ALL_JBOSS_SERVERS;
		for( int i = arr.length-1; i >= 0; i-- ) {
			if( arr[i].startsWith(prefix)) {
				return arr[i];
			}
		}
		return null;
	}

	private static String findLatestRuntimeTypeId(String prefix) {
		String[] arr = IJBossToolingConstants.ALL_JBOSS_RUNTIMES;
		for( int i = arr.length-1; i >= 0; i-- ) {
			if( arr[i].startsWith(prefix)) {
				return arr[i];
			}
		}
		return null;
	}
}
