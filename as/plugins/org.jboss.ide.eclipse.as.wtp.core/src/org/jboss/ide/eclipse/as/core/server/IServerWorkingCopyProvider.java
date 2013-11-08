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
package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.wst.server.core.IServerWorkingCopy;

/**
 * A simple interface for an object which can provide a relevant IServerWorkingCopy
 * @since 3.0
 */
public interface IServerWorkingCopyProvider {
	public IServerWorkingCopy getServer();
}
