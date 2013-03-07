/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui;

import org.eclipse.wst.server.core.IServerAttributes;

/**
 * A previously internal interface, this is in charge
 * of IN-WORKBENCH browsing of a server. 
 * 
 * @author rob
 *
 */
public interface IBrowseBehavior {
	/**
	 * Open a browse dialog of some sort inside the workspace
	 * to display the contents of the given path on the given server.
	 * 
	 * @param server
	 * @param original
	 * @return
	 */
	public String openBrowseDialog(IServerAttributes server, String original);
}
