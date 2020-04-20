/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.ide.eclipse.as.reddeer.server.view;

import java.util.ArrayList;
import java.util.List;

import org.jboss.ide.eclipse.as.reddeer.server.editor.JBossServerEditor;
import org.jboss.ide.eclipse.as.reddeer.server.editor.WelcomeToServerEditor;
import org.eclipse.reddeer.common.condition.AbstractWaitCondition;
import org.eclipse.reddeer.common.exception.WaitTimeoutExpiredException;
import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.common.util.Display;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.eclipse.exception.EclipseLayerException;
import org.eclipse.reddeer.eclipse.ui.console.ConsoleView;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.AbstractServer;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.ServerModule;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.reddeer.eclipse.wst.server.ui.editor.ServerEditor;
import org.eclipse.reddeer.swt.api.Shell;
import org.eclipse.reddeer.swt.api.TreeItem;
import org.eclipse.reddeer.swt.exception.SWTLayerException;
import org.eclipse.reddeer.swt.impl.menu.ContextMenuItem;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.wst.server.ui.IServerModule;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;

/**
 * Represents a JBoss server and contains state and operations specific to this kind of server. 
 * Note, however, that it is upon the user of this class to check if the server is really JBoss. 
 * 
 * @author Lucia Jelinkova
 *
 */
public class JBossServer extends AbstractServer {

	public static final String XML_LABEL_DECORATION_SEPARATOR = "   ";
	
	private static final Logger log = Logger.getLogger(JBossServer.class);

	public JBossServer(TreeItem treeItem) {
		super(treeItem);
	}

	@Override
	public JBossServerEditor open() {
		String editorName = getLabel().getName();
		super.open();

		return new JBossServerEditor(editorName);
	}

	@Override
	public JBossServerModule getModule(String name) {		
		return getModule(new IsEqual<String>(name));
	}
	
	@Override
	public JBossServerModule getModule(Matcher<String> stringMatcher) {
		for (JBossServerModule module : getJBossModules()) {
			if (stringMatcher.matches(module.getLabel().getName())) {
				return module;
			}
		}
		throw new EclipseLayerException("There is no module with name matching matcher " + stringMatcher.toString()
				+ " on server " + getLabel().getName());
	}
	
	public List<JBossServerModule> getJBossModules() {
		activate();
		final List<JBossServerModule> modules = new ArrayList<JBossServerModule>();

		for (final TreeItem item : treeItem.getItems()) {
			Display.syncExec(new Runnable() {

				@Override
				public void run() {
					org.eclipse.swt.widgets.TreeItem swtItem = item.getSWTWidget();
					Object data = swtItem.getData();
					if (data instanceof IModule || data instanceof IServerModule) {
						modules.add(createServerModule(item));
					}
				}
			});
		}

		return modules;
	}

	public WelcomeToServerEditor openWebPage(){
		activate();
		new WaitUntil(new ContextMenuIsEnabled("Show In", "Web Browser"));
		new ContextMenuItem("Show In", "Web Browser").select();
		return new WelcomeToServerEditor();
	}

	@Override
	public void start() {
		checkServerAlreadyRunningDialog();
		try {
			super.start();
		} catch (WaitTimeoutExpiredException e){
			log.error("JBoss server failed to start");
			checkServerAlreadyRunningDialog();
			log.error("JBoss server's console dump:");
			ConsoleView view = new ConsoleView();
			view.open();
			log.error("\t" + view.getConsoleText());
			throw e;
		}
	}
	
	@Override
	public void restart() {
		try {
			super.restart();
		} catch (WaitTimeoutExpiredException e){
			log.error("JBoss server failed to restart");
			checkServerAlreadyRunningDialog();
			log.error("JBoss server's console dump:");
			ConsoleView view = new ConsoleView();
			view.open();
			log.error("\t" + view.getConsoleText());
			throw e;
		}
	}

	/**
	 * Retrieves the XML configuration items listed under the specified category. 
	 * 
	 * @param categoryName
	 * @return
	 */
	public List<XMLConfiguration> getXMLConfiguration(String categoryName){
		activate();
		TreeItem categoryItem = treeItem.getItem("XML Configuration").getItem(categoryName);
		List<TreeItem> configurationItems = categoryItem.getItems();
		
		// does not work on AS 4.0
		new WaitUntil(new TreeItemLabelDecorated(configurationItems.get(0)), TimePeriod.DEFAULT, false);
		
		// does not work on AS 3.2
		new WaitUntil(new TreeItemLabelDecorated(configurationItems.get(configurationItems.size() - 1)), TimePeriod.NONE, false);

		List<XMLConfiguration> configurations = new ArrayList<XMLConfiguration>();
		for (final TreeItem item : configurationItems){
			String[] columns = item.getText().split(XML_LABEL_DECORATION_SEPARATOR);
			if (columns.length < 2){
				// it is nested node, we should process it recursively in the future
				// but for now not crucial, let's skip it
				continue;
			}
			configurations.add(new XMLConfiguration(columns[0].trim(), columns[1].trim()));
		}
		return configurations;
	}

	protected ServerEditor createServerEditor(String title) {
		return new JBossServerEditor(title);
	}

	protected JBossServerModule createServerModule(TreeItem item) {
		return new JBossServerModule(item, view);
	}

	private void checkServerAlreadyRunningDialog() {
		try {
			Shell shell = new DefaultShell("Server already running on localhost");
			shell.close();
			throw new ServerAlreadyStartedException();
		} catch (SWTLayerException e){
			// do nothing
		}
	}
	
	/**
	 * Checks if the tree item label is decorated. In case of server, the separator is "  ".
	 * 
	 * @author Lucia Jelinkova
	 *
	 */
	private static class TreeItemLabelDecorated extends AbstractWaitCondition {

		private TreeItem item;

		private TreeItemLabelDecorated(TreeItem item) {
			super();
			this.item = item;
		}

		@Override
		public boolean test() {
			return item.getText().contains(XML_LABEL_DECORATION_SEPARATOR);
		}

		@Override
		public String description() {
			return "Expected the tree item to be decorated with separator '" + XML_LABEL_DECORATION_SEPARATOR + "'";
		}
	}
	
	private static class ContextMenuIsEnabled extends AbstractWaitCondition {

		private String[] path;
		
		public ContextMenuIsEnabled(String... path) {
			this.path = path;
		}

		@Override
		public boolean test() {
			return new ContextMenuItem(path).isEnabled();
		}

		@Override
		public String description() {
			return "context menu item is enabled";
		}
		
	}

	class ServerAlreadyStartedException extends RuntimeException {

		private static final long serialVersionUID = 1L;
		
		public ServerAlreadyStartedException() {
			super("Server already running on localhost");
		}
	}
}
