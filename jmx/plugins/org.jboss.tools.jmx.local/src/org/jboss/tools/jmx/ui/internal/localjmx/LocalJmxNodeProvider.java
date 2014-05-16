/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.jmx.ui.internal.localjmx;

import java.util.List;

import org.jboss.tools.jmx.commons.tree.PartialRefreshableNode;
import org.jboss.tools.jmx.commons.tree.RefreshableUI;
import org.jboss.tools.jmx.ui.RootJmxNodeProvider;



public class LocalJmxNodeProvider implements RootJmxNodeProvider{

	@SuppressWarnings("unchecked")
	public void provideRootJmxNodes(RefreshableUI contentProvider, List list) {
		PartialRefreshableNode connections = new JvmConnectionsNode(null, contentProvider);
		list.add(connections);
	}

}
