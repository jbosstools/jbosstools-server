/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal;

import org.eclipse.wst.server.core.model.RuntimeDelegate;

/**
 * This class is almost completely unused but cannot be removed. 
 * In order for a server type to be able to deploy any modules, 
 * it must have a Runtime type, even if it does not use it. 
 * 
 * A runtime type must have an implementation class, so this is it.
 */
public class DeployableServerRuntime extends RuntimeDelegate {

	public DeployableServerRuntime() {
		// Nothing to do
	}

}
