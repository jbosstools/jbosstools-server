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
package org.jboss.ide.eclipse.as.core.server;



public interface IUserPrompter {
	/**
	 * An event key expecting a return of one of the 3 following values
	 */
	public static final int EVENT_CODE_SERVER_ALREADY_STARTED = 101;
	public static final int RETURN_CODE_SAS_CONTINUE_STARTUP = 1;
	public static final int RETURN_CODE_SAS_ONLY_CONNECT = 2;
	public static final int RETURN_CODE_SAS_CANCEL = 3;

	/**
	 * An event key expecting a boolean result, whether
	 * to force-terminate a zombie process. 
	 */
	public static final int EVENT_CODE_PROCESS_UNTERMINATED = 102;

	
	public void addPromptHandler(int code, IPromptHandler prompt);
	public void removePromptHandler(int code, IPromptHandler prompt);
	public Object promptUser(int code, Object... data);
}
