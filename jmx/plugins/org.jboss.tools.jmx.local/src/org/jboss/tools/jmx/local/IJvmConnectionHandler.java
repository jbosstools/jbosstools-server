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

import org.jboss.tools.jmx.jvmmonitor.core.IJvm;

public interface IJvmConnectionHandler {
	/**
	 * Does this connection handler already 
	 * provide a connection that duplicates this Jvm?
	 * 
	 *  If yes, org.jboss.tools.jmx.local should ignore 
	 *  changes to this JVM and not provide a connection.
	 *  
	 * @param jvm
	 * @return True if already handled, false if jmx.local should handle
	 */
	public boolean handles(IJvm jvm);
}
