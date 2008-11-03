/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    "Rob Stryker" <rob.stryker@redhat.com> - Initial implementation
 *******************************************************************************/
package org.jboss.tools.jmx.core.providers;

import org.jboss.tools.jmx.core.ConnectJob;
import org.jboss.tools.jmx.core.IConnectionProviderListener;
import org.jboss.tools.jmx.core.IConnectionWrapper;

/**
 * Ensures that DefaultConnectionWrapper type connections
 * automatically attempt to start
 */
public class AutomaticStarter implements IConnectionProviderListener {

	public void connectionAdded(IConnectionWrapper connection) {
		if( connection instanceof DefaultConnectionWrapper ) {
			new ConnectJob(new IConnectionWrapper[] { connection }).schedule();
		}
	}

	public void connectionChanged(IConnectionWrapper connection) {
	}
	public void connectionRemoved(IConnectionWrapper connection) {
	}

}
