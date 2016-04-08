/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server;

/**
 * 
 */
public interface IServerConsoleWriter extends IJBASHostShellListener {
	
	/**
	 * Writes the given {@code lines} into the {@link MessageConsole} associated with the given {@code serverId}.
	 * If the {@link MessageConsole} did not exist yet, it is created and shown. Calling this method will activate
	 * the console while the lines are written.
	 * 
	 * @param serverId the id of the server associated with this console.
	 * @param lines the lines to write in the console.
	 */
	public void writeToShell(String serverId, String[] lines);
	
	/**
	 * Writes the given {@code lines} into the {@link MessageConsole} associated with the given {@code serverId}.
	 * If the {@link MessageConsole} did not exist yet, it is created and shown.
	 * You should prefer the other method as it is now possible for the user to toggle automatic activation
	 * or not of the  console. Using this method will go against user choice.
	 *  
	 * @param serverId the id of the server associated with this console.
	 * @param lines the lines to write in the console.
	 * @param activateOnWrite if the view should be activated when the line are written in the console.
	 */
	public void writeToShell(final String serverId, final String[] lines, final boolean activateOnWrite);
	
}
