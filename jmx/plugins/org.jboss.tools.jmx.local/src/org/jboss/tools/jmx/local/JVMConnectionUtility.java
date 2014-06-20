/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.jmx.local;

import org.jboss.tools.jmx.core.ExtensionManager;
import org.jboss.tools.jmx.core.IConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.jvmmonitor.core.IActiveJvm;
import org.jboss.tools.jmx.local.internal.JVMConnectionProvider;

/**
 * An API class for utility methods related to the discovered
 * process connection wrappers and jvmmonitor api integration
 */
public class JVMConnectionUtility {
	public static IConnectionWrapper findConnectionForJvm(IActiveJvm jvm) {
		String id = JVMConnectionProvider.PROVIDER_ID;
		IConnectionProvider provider = ExtensionManager.getProvider(id);
		if( provider instanceof JVMConnectionProvider) {
			return ((JVMConnectionProvider)provider).findConnection(jvm);
		}
		return null;
	}
}
