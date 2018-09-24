/******************************************************************************* 
 * Copyright (c) 2018 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.jmx.reddeer.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.reddeer.common.condition.AbstractWaitCondition;
import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.core.matcher.TreeItemTextMatcher;
import org.eclipse.reddeer.jface.handler.TreeViewerHandler;
import org.eclipse.reddeer.swt.api.TreeItem;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.condition.TreeContainsItem;
import org.eclipse.reddeer.swt.impl.button.OkButton;
import org.eclipse.reddeer.swt.impl.menu.ContextMenuItem;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.tree.DefaultTreeItem;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.hamcrest.Matcher;
import org.jboss.tools.jmx.reddeer.core.exception.JMXException;

/**
 * 
 * @author odockal
 *
 */
public class JMXConnection extends AbstractJMXConnection {
	
	private static final String MBEANS = "MBeans";
	
	private static final Logger log = Logger.getLogger(JMXConnection.class);
	
	public JMXConnection(TreeItem item) {
		super(item);
	}
	
	public void disconnect() {
		select();
		new ContextMenuItem("Disconnect").select();
		// process shell
		ShellIsAvailable shell = new ShellIsAvailable("JMX Disconnection");
		new WaitUntil(shell, TimePeriod.MEDIUM, false);
		if (shell.getResult() != null) {
			new DefaultShell(shell.getResult());
			new OkButton().click();
			new WaitWhile(shell, TimePeriod.MEDIUM);
		}
		new WaitUntil(new ConnectionHasState(this, JMXConnectionState.DISCONNECTED));
	}
	
	public void connect() {
		select();
		new ContextMenuItem("Connect...").select();
		new WaitUntil(new ConnectionHasState(this, JMXConnectionState.CONNECTED), TimePeriod.MEDIUM);
		new WaitWhile(new JobIsRunning(), TimePeriod.MEDIUM);
	}
	
	public void deleteConnection() {
		select();
		new ContextMenuItem("Delete Connection");
		new WaitUntil(new ConnectionHasState(this, JMXConnectionState.DISCONNECTED), TimePeriod.MEDIUM);
	}
	
	public ConnectionLabel getLabel() {
		select();
		return new ConnectionLabel(item);
	}
	
	public boolean isConnected() {
		select();
		return getLabel().getState().equals(JMXConnectionState.CONNECTED);
	}
	
	public List<TreeItem> getMBeansObjects() {
		activate();
		TreeItem mbeans = getMBeansTreeItem();
		List<String> path = new ArrayList<>();
		path.addAll(Arrays.asList(getItem().getPath()));
		path.add(MBEANS);
		new WaitUntil(new TreeContainsItem(getItem().getParent(), path.toArray(new String[0])), TimePeriod.MEDIUM);
		
		List<TreeItem> items = new ArrayList<>();
		items.addAll(mbeans.getItems());
		return items;
	}
	
	public TreeItem getPackageTreeItem(String name) {
		return getMBeansTreeItem().getItem(name);
	}
	
	public void openMBeanObjectEditor(int index, String... path) {
		List<String> allPath = new LinkedList<>();
		allPath.addAll(Arrays.asList(getMBeansTreeItem().getPath()));
		allPath.addAll(Arrays.asList(path));
		@SuppressWarnings("unchecked")
		Matcher<org.eclipse.swt.widgets.TreeItem>[] matchers = new Matcher[allPath.size()];
		int i = 0;
		for (String item : allPath) {
			matchers[i++] = new TreeItemTextMatcher(item);
		}
		DefaultTreeItem treeItem = new DefaultTreeItem(index, matchers);
		treeItem.select();
		treeItem.doubleClick();
	}
	
	public void openMBeanObjectEditor(TreeItem item) {
		item.select();
		item.doubleClick();
	}
	
	public TreeItem getMBeansTreeItem() {
		if (isConnected()) {
			List<String> path = new LinkedList<>();
			path.addAll(Arrays.asList(getItem().getPath()));
			path.add(MBEANS);
			new WaitUntil(new TreeContainsItem(getItem().getParent(), path.toArray(new String[0])), TimePeriod.MEDIUM);
			return item.getItem(MBEANS);
		} else {
			throw new JMXException("Connection " + getLabel().getName() + " is not connected");
		}
	}
	
	public class ConnectionLabel {
		
		private TreeViewerHandler treeHandler = TreeViewerHandler.getInstance();
		private String name;
		private String pid;
		private JMXConnectionState state;
		
		public ConnectionLabel(TreeItem item) {
			parse(item);
		}
		
		public void parse(TreeItem item) {
			log.info("Parsing treeItem text: " + item.getText());
			// expects connection name and pid in brackets
			String nonStyledText = treeHandler.getNonStyledText(item);
			if (nonStyledText.contains("[")) {
				name = nonStyledText.substring(0, nonStyledText.indexOf('[') - 1).trim();
				pid = nonStyledText.substring(nonStyledText.indexOf('[') + 1, nonStyledText.lastIndexOf(']')).trim();
			} else {
				name = nonStyledText.trim();
			}
			String [] styledText = treeHandler.getStyledTexts(item);
			if (styledText != null) {
				String stateText = styledText[0].substring(styledText[0].indexOf('[') + 1, styledText[0].lastIndexOf(']')).trim();
				state = JMXConnectionState.get(stateText);
			}
		}
		
		public String getName() {
			return name;
		}

		public String getPid() {
			return pid;
		}

		public JMXConnectionState getState() {
			return state;
		}
	}
	
	private class ConnectionHasState extends AbstractWaitCondition {

		private JMXConnection connection;
		private JMXConnectionState state;
		
		public ConnectionHasState(JMXConnection conn, JMXConnectionState state) {
			this.connection = conn;
			this.state = state;
		}
		
		@Override
		public boolean test() {
			return connection.getLabel().getState().equals(state);
		}
	}
}
