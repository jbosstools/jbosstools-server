/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.core.server.behavior;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServerWorkingCopy;

/**
 * Used so that a server started in a given profile can be initialized properly
 * by the various participants
 */
public interface IServerProfileInitializer {

	
	/**
	 * Initialize this server working copy with settings
	 * 
	 * @param wc
	 * @throws CoreException
	 */
	public void initialize(IServerWorkingCopy wc) throws CoreException;
}
