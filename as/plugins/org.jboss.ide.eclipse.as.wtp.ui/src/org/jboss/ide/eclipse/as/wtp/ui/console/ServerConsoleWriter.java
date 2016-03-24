/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.wtp.ui.console;

import java.io.IOException;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.jboss.ide.eclipse.as.core.server.IServerConsoleWriter;

/**
 * Creates or locates a console for a given server through which
 * arbitrary output can be displayed to the user
 */
public class ServerConsoleWriter implements IServerConsoleWriter {
	
	@Override
	public void writeToShell(final String serverId, final String[] lines) {
		writeToShell(serverId, lines, true);
	}

	@Override
	public void writeToShell(final String serverId, final String[] lines, final boolean activateOnWrite) {
		final MessageConsole myConsole = findConsole(serverId);
		try (MessageConsoleStream out = myConsole.newMessageStream()) {
			out.setActivateOnWrite(activateOnWrite);
			for (int i = 0; i < lines.length; i++)
				out.println(lines[i]);
		} catch (IOException e) {
			org.jboss.ide.eclipse.as.wtp.ui.WTPOveridePlugin.logError(e);
		}
	}

	/**
	 * Finds or creates the console with the given {@code name}. If the console
	 * is created, the console view is shown, too.
	 * 
	 * @param name
	 *            the name of the console to find or created
	 * @return the console
	 */
	private static synchronized MessageConsole findConsole(final String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		// show the console when it is created, only.
		conMan.showConsoleView(myConsole);
		return myConsole;
	}

}