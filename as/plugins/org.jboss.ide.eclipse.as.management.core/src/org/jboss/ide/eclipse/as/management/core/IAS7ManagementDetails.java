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
package org.jboss.ide.eclipse.as.management.core;

import org.eclipse.wst.server.core.IServer;

public interface IAS7ManagementDetails {
	public String getHost();
	public int getManagementPort();	
	public String getManagementUsername();
	public String getManagementPassword();	
	public String[] handleCallbacks(String[] prompts) throws UnsupportedOperationException;	
	public IServer getServer();
}
