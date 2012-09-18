/*************************************************************************************
 * Copyright (c) 2008-2011 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.ide.eclipse.as.ui.actions;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.Messages;

/**
 * 
 * The utilities in this class are only useful for
 * exploring LOCALLY. 
 * 
 * @author snjeza
 * 
 */

public class ExploreUtils {

	public final static String PATH = "{%}"; //$NON-NLS-1$
	public final static String EXPLORE = Messages.ExploreUtils_Action_Text;
	public final static String EXPLORE_DESCRIPTION = Messages.ExploreUtils_Description;
	private static String exploreFolderCommand;
	private static String[] exploreFolderCommandArray;
	private static String[] exploreFileCommandArray;
	private static String exploreFileCommand;
	
	public static String getExploreCommand() {
		if (exploreFolderCommand == null) {
			setExploreCommands();
		}
		return exploreFolderCommand;
	}
	
	public static String getExploreFileCommand() {
		exploreFileCommand = null;
		exploreFolderCommandArray = null;
		exploreFolderCommand = null;
		if (exploreFileCommand == null) {
			setExploreCommands();
		}
		return exploreFileCommand;
	}

	private static void setExploreCommands() {
		if (Platform.OS_MACOSX.equals(Platform.getOS())) {
			exploreFolderCommandArray = new String[] {"/usr/bin/open", "-a", "/System/Library/CoreServices/Finder.app", ""};   //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
			exploreFolderCommand = ""; //$NON-NLS-1$
		} else if (Platform.OS_WIN32.equals(Platform.getOS())) {
			exploreFolderCommandArray = new String[5];
			exploreFolderCommandArray[0]= "cmd"; //$NON-NLS-1$
			exploreFolderCommandArray[1]= "/C"; //$NON-NLS-1$
			exploreFolderCommandArray[2]= "start"; //$NON-NLS-1$
			exploreFolderCommandArray[3]= "explorer"; //$NON-NLS-1$
			
			exploreFileCommandArray = new String[6];
			exploreFileCommandArray[0]= "cmd"; //$NON-NLS-1$
			exploreFileCommandArray[1]= "/C"; //$NON-NLS-1$
			exploreFileCommandArray[2]= "start"; //$NON-NLS-1$
			exploreFileCommandArray[3]= "explorer"; //$NON-NLS-1$
			exploreFileCommandArray[4]= "/select,"; //$NON-NLS-1$
			
			exploreFolderCommand = "cmd /C start explorer \"" //$NON-NLS-1$
					+ PATH + "\""; //$NON-NLS-1$
			exploreFileCommand = "cmd /C start explorer /select,\"" //$NON-NLS-1$
					+ PATH + "\""; //$NON-NLS-1$
		} else if (Platform.OS_LINUX.equals(Platform.getOS())) {
			
			if (new File("/usr/bin/nautilus").exists()) { //$NON-NLS-1$
				exploreFolderCommandArray = new String[3];
				exploreFolderCommandArray[0]="/usr/bin/nautilus"; //$NON-NLS-1$
				exploreFolderCommandArray[1]="--no-desktop"; //$NON-NLS-1$
				exploreFolderCommand = ""; //$NON-NLS-1$
			} else if (new File("/usr/bin/konqueror").exists()) { //$NON-NLS-1$
				exploreFolderCommandArray = new String[2];
				exploreFolderCommandArray[0]="/usr/bin/konqueror"; //$NON-NLS-1$
				exploreFolderCommand = ""; //$NON-NLS-1$
			}
			exploreFileCommand = exploreFolderCommand;
		}
	}
	
	public static String getDeployDirectory(IServer server) {
		IDeployableServer deployableServer = ServerConverter.getDeployableServer(server);
		if (server != null && deployableServer != null) {
			return deployableServer.getDeployFolder();
		}
		String ret = server.getAttribute(IDeployableServer.DEPLOY_DIRECTORY,(String) null); //$NON-NLS-1$
		if( ret != null )
			return ret.trim();
		
		// Other runtimes like tomcat / default behavior (?)
		IRuntime rt = server.getRuntime();
		if( rt != null ) {
			return rt.getLocation().toString();
		}
		
		return null; // No idea
	}
	
	public static boolean canExplore(IServer server) {
		String deployDirectory = ExploreUtils.getDeployDirectory(server);
		if (deployDirectory == null || deployDirectory.length() <= 0 && new File(deployDirectory).exists()) {
			return false;
		}
		if (ExploreUtils.getExploreCommand() == null) {
			return false;
		}
		return true;
	}
	public static boolean canExplore(IServer server, IModule[] modules) {
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		if( ds != null ) {
			IPath p = ds.getDeploymentLocation(modules, false); 
			if (p == null || !p.toFile().exists() || ExploreUtils.getExploreCommand() == null)
				return false;
			return true;
		}
		return false;
	}
	
	public static void explore(String name) {
		File file = new File(name);
		String command = null;
		if (file.isFile()) {
			command = getExploreFileCommand();
		} else {
			command = getExploreCommand();
		}
		if (command != null) {
			if (Platform.getOS().equals(Platform.OS_WIN32)) {
				name = name.replace('/', '\\');
			}
			try {
				if (Platform.OS_LINUX.equals(Platform.getOS()) || Platform.OS_MACOSX.equals(Platform.getOS())) {
					int len = exploreFolderCommandArray.length;
					String name2 = file.isFile() ? new Path(name).removeLastSegments(1).toString() : name;
					exploreFolderCommandArray[len-1] = name2;
					Runtime.getRuntime().exec(exploreFolderCommandArray);
				} else if (Platform.getOS().equals(Platform.OS_WIN32)) {
					if (file.isDirectory()) {
						int len = exploreFolderCommandArray.length;
						exploreFolderCommandArray[len-1] = "\"" + name + "\""; //$NON-NLS-1$ //$NON-NLS-2$
						Runtime.getRuntime().exec(exploreFolderCommandArray);
					} else {
						int len = exploreFileCommandArray.length;
						exploreFileCommandArray[len-1] = "\"" + name + "\""; //$NON-NLS-1$ //$NON-NLS-2$
						Runtime.getRuntime().exec(exploreFileCommandArray);
					}
				} else {
					command = command.replace(ExploreUtils.PATH, name);
					if (JBossServerUIPlugin.getDefault().isDebugging()) {
						IStatus status = new Status(IStatus.WARNING, JBossServerUIPlugin.PLUGIN_ID, "command=" + command, null); //$NON-NLS-1$
						JBossServerUIPlugin.getDefault().getLog().log(status);
					}
					Runtime.getRuntime().exec(command);
				}
			} catch (IOException e) {
				JBossServerUIPlugin.log(e.getMessage(),e);
			}
		}
	}
}
