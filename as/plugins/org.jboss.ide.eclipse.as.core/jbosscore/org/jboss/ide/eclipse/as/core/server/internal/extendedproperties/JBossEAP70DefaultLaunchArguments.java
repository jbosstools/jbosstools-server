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
package org.jboss.ide.eclipse.as.core.server.internal.extendedproperties;

import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;

public class JBossEAP70DefaultLaunchArguments extends
		Wildfly100DefaultLaunchArguments {
	public JBossEAP70DefaultLaunchArguments(IServer s) {
		super(s);
	}
	public JBossEAP70DefaultLaunchArguments(IRuntime rt) {
		super(rt);
	}
}
