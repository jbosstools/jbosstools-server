/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server;

public interface IPollResultListener {

	/**
	 * Called if the poller did reach the expected state
	 * @param expectedState the expected state
	 * @param currentState  the actual state
	 */
	public void stateAsserted(boolean expectedState, boolean currentState);

	/**
	 * Called if the poller did NOT reach the expected state
	 * @param expectedState the expected state
	 * @param currentState  the actual state
	 */
	public void stateNotAsserted(boolean expectedState, boolean currentState);	
}
