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
package org.jboss.ide.eclipse.as.rse.core;

import org.eclipse.core.runtime.IStatus;
import org.jboss.ide.eclipse.as.core.server.IJBossBehaviourDelegate;

public class RSEWildfly8BehaviourDelegate extends RSEJBoss7BehaviourDelegate
		implements IJBossBehaviourDelegate {

	public RSEWildfly8BehaviourDelegate() {
	}
	
	protected IStatus gracefullStop() {
		return gracefullStopViaManagement();
	}

}
