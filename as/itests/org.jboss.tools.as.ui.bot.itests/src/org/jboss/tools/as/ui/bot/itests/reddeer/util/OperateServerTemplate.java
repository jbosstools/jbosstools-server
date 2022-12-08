/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.as.ui.bot.itests.reddeer.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.fail;

import org.eclipse.reddeer.common.condition.AbstractWaitCondition;
import org.eclipse.reddeer.common.exception.WaitTimeoutExpiredException;
import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.core.exception.CoreLayerException;
import org.eclipse.reddeer.eclipse.condition.ConsoleHasNoChange;
import org.eclipse.reddeer.eclipse.condition.ConsoleHasText;
import org.eclipse.reddeer.eclipse.exception.EclipseLayerException;
import org.eclipse.reddeer.eclipse.ui.console.ConsoleView;
import org.eclipse.reddeer.eclipse.wst.server.ui.RuntimePreferencePage;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.Server;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.ServersView2;
import org.eclipse.reddeer.swt.api.Shell;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.workbench.ui.dialogs.WorkbenchPreferenceDialog;
import org.jboss.ide.eclipse.as.reddeer.matcher.ServerConsoleContainsNoExceptionMatcher;

/**
 * Checks if the given server can be started, restarted, stopped and deleted
 * without error.
 * 
 * @author Lucia Jelinkova
 * @author Radoslav Rabara
 */
public class OperateServerTemplate {

	private final Logger LOGGER = Logger.getLogger(this.getClass());
	
	private ServersView2 serversView = new ServersView2();
	private ConsoleView consoleView = new ConsoleView();

	private String serverName;
	public OperateServerTemplate(String serverName) {
		this.serverName = serverName;
	}
	
	private String getServerName() {
		return serverName;
	}
	

	public void operateServer() {
		serverIsPresentInServersView();
		new WaitWhile(new JobIsRunning());
		new WaitUntil(new ServerHasState("Stopped"));
		LOGGER.step("Starting server");
		startServer();
		LOGGER.step("Restarting server");
		restartServer();
		LOGGER.step("Stopping server");
		stopServer();
		LOGGER.step("Deleting server");
		deleteServer();
	}
	
	public void startServerSafe() {
		serverIsPresentInServersView();
		new WaitWhile(new JobIsRunning());
		new WaitUntil(new ServerHasState("Stopped"));
		LOGGER.step("Starting server");
		startServer();
	}

	public void stopAndDeleteServer() {
		LOGGER.step("Stopping server");
		stopServer();
		LOGGER.step("Deleting server");
		deleteServer();
	}
	
	private void serverIsPresentInServersView() {
		ServersView2 sw = new ServersView2();
		sw.open();
		try {
			sw.getServer(getServerName());
		} catch(EclipseLayerException e) {
			String failMessage = "Server \"" + getServerName() + "\" not found in Servers View.";
			LOGGER.error(failMessage, e);
			fail(failMessage);
		}
	}

	public void setUp(){
		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
		serversView.open();
		for(Server server : serversView.getServers()){
			if(!server.getLabel().getName().equals(serversView.getServer(getServerName()).getLabel().getName())){
				server.delete();
			}
		}
		new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
		// Do not change JREs. We want defaults to "just work"
	}

	public void cleanServerAndConsoleView() {
		try{
			LOGGER.step("Trying to close shell \"Warning: server process not terminated\"");
			Shell warningShell = new DefaultShell("Warning: server process not terminated");
			new PushButton(warningShell, "Yes").click();
			new WaitWhile(new ShellIsAvailable(warningShell));
			LOGGER.step("Warning shell is closed.");
		}catch(Exception e){
			//nothing
		}
		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
		serversView.open();
		for (Server server : serversView.getServers()) {
			server.setServerStateChangeTimeout(TimePeriod.LONG);
			server.delete(true);
		}
		consoleView.open();
		try {
			consoleView.clearConsole();
		} catch(CoreLayerException ex) {
			//swallow - console is empty
		}
		
		removeAllRuntimes();
	}

	public void startServer() {
		serversView.getServer(getServerName()).start();
		final String state = "Started";
		new WaitUntil(new ConsoleHasNoChange(TimePeriod.getCustom(5)), TimePeriod.LONG);
		new WaitUntil(new ServerHasState(state));
		
		assertNoException("Starting server");
		assertServerState("Starting server", state);
	}

	public void restartServer() {
	    consoleView.open();
        try {
            consoleView.clearConsole();
        } catch(CoreLayerException ex) {
            //swallow - console is empty
        }
        
		try {
			serversView.getServer(getServerName()).restart();
	        new WaitUntil(new ConsoleHasText("started in"), TimePeriod.getCustom(30));
		} catch (WaitTimeoutExpiredException ex){
			//try it once again
			serversView.getServer(getServerName()).restart();
            new WaitUntil(new ConsoleHasText("started in"), TimePeriod.getCustom(30));
		}
		tryServerProcessNotTerminated();
		final String state = "Started";
		new WaitUntil(new ConsoleHasNoChange(TimePeriod.getCustom(5)), TimePeriod.LONG);
		new WaitUntil(new ServerHasState(state));

		assertNoException("Restarting server");
		assertNoError("Restarting server");
		assertServerState("Restarting server", state);
	}

	public void stopServer() {
		new WaitUntil(new JobIsRunning(),TimePeriod.LONG, false);
		serversView.open();
		Server actualServer = serversView.getServer(getServerName());
		actualServer.setServerStateChangeTimeout(TimePeriod.getCustom(30));
		try {
            actualServer.stop();
		} catch (WaitTimeoutExpiredException ex) {
			//try to stop server once again
            serversView.open();
		    actualServer.stop();
		}
		tryServerProcessNotTerminated();
		final String state = "Stopped";
		new WaitUntil(new ConsoleHasNoChange(TimePeriod.getCustom(5)), TimePeriod.LONG);
		new WaitUntil(new ServerHasState(state));

		assertNoException("Stopping server");
		assertServerState("Stopping server", state);
	}

	/**
	 * Closes "Server process not terminated" shell in case it pops up.
	 */
	private void tryServerProcessNotTerminated() {
		try{
			LOGGER.step("Trying to close shell \"Warning: server process not terminated\"");
			DefaultShell warningShell = new DefaultShell("Warning: server process not terminated");
			new PushButton(warningShell, "Yes").click();
			new WaitWhile(new ShellIsAvailable(warningShell));
			LOGGER.step("Warning shell is closed.");
		}catch (CoreLayerException e) {
			//Shell did not pop up -> do nothing
		} 
	}

	public void deleteServer() {
		serversView.getServer(getServerName()).delete();
	}

	private void removeAllRuntimes() {
		WorkbenchPreferenceDialog preferenceDialog = new WorkbenchPreferenceDialog();
		preferenceDialog.open();
		RuntimePreferencePage runtimePage = new RuntimePreferencePage(preferenceDialog);
		preferenceDialog.select(runtimePage);
		runtimePage.removeAllRuntimes();
		preferenceDialog.ok();
	}

	protected void assertNoException(String message) {
		ConsoleView console = new ConsoleView();

		console.open();
		assertThat(message, console, new ServerConsoleContainsNoExceptionMatcher());
	}

	protected void assertNoError(String message) {
		ConsoleView console = new ConsoleView();

		console.open();
		String consoleText = console.getConsoleText();
		if (consoleText != null) {
			assertThat(message, consoleText, not(containsString("Error:")));
		} else {
			fail("Text from console could not be obtained.");
		}
	}
	
	protected void assertServerState(String message, String state) {
		// need to catch EclipseLayerException because:
		// serverView cannot find server with name XXX for the first time
		String textState;
		serversView.open();
		try {
			textState = serversView.getServer(getServerName()).getLabel()
					.getState().getText();
		} catch (EclipseLayerException ex) {
			new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
			textState = serversView.getServer(getServerName()).getLabel()
					.getState().getText();
		}

		assertThat(message, textState, is(state));
	}
	
	private class ServerHasState extends AbstractWaitCondition {

		private String expectedState;
		private String state;
		private ServerHasState(String expectedState) {
			this.expectedState = expectedState;
		}

		@Override
		public boolean test() {
			state = serversView.getServer(getServerName()).getLabel().getState().getText();
			return state.equals(state);
		}

		@Override
		public String description() {
			return "Server in server view is in given state."
					+ "Expected: \"" + expectedState + "\" but was \"" + state + "\"";
		}
	}
}
