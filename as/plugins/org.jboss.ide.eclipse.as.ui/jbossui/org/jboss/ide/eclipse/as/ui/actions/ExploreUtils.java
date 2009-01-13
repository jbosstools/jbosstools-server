/*************************************************************************************
 * Copyright (c) 2008 JBoss, a division of Red Hat and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss, a division of Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.ide.eclipse.as.ui.actions;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;

/**
 * @author snjeza
 * 
 */

public class ExploreUtils {

	public final static String PATH = "{%}";
	public final static String EXPLORE = "Explore";
	public final static String EXPLORE_DESCRIPTION = "Explore deploy directory";
	private static String exploreFolderCommand;
	private static String[] exploreFolderCommandArray;
	private static String exploreFileCommand;
	
	public static String getExploreCommand() {
		if (exploreFolderCommand == null) {
			setExploreCommands();
		}
		return exploreFolderCommand;
	}
	
	public static String getExploreFileCommand() {
		if (exploreFileCommand == null) {
			setExploreCommands();
		}
		return exploreFileCommand;
	}

	private static void setExploreCommands() {
		if (Platform.OS_MACOSX.equals(Platform.getOS())) {
			exploreFolderCommand = "/usr/bin/open -a /System/Library/CoreServices/Finder.app \""
					+ PATH + "\"";
			exploreFileCommand = exploreFolderCommand;
		} else if (Platform.OS_WIN32.equals(Platform.getOS())) {
			exploreFolderCommand = "cmd /C start explorer /root,/e,\""
					+ PATH + "\"";
			exploreFileCommand = "cmd /C start explorer /select,/e,\""
					+ PATH + "\"";
		} else if (Platform.OS_LINUX.equals(Platform.getOS())) {
			
			if (new File("/usr/bin/nautilus").exists()) {
				exploreFolderCommandArray = new String[3];
				exploreFolderCommandArray[0]="/usr/bin/nautilus";
				exploreFolderCommandArray[1]="--no-desktop";
				exploreFolderCommand = "";
			} else if (new File("/usr/bin/konqueror").exists()) {
				exploreFolderCommandArray = new String[2];
				exploreFolderCommandArray[0]="/usr/bin/konqueror";
				exploreFolderCommand = "";
			}
			exploreFileCommand = exploreFolderCommand;
		}
	}
	
	public static String getDeployDirectory(IServer server) {
		IDeployableServer deployableServer = ServerConverter.getDeployableServer(server);
		if (server.getRuntime() != null && deployableServer != null) {
			return deployableServer.getDeployFolder();
		}
		IServerWorkingCopy swc = server.createWorkingCopy();
		ServerAttributeHelper helper = new ServerAttributeHelper(swc
				.getOriginal(), swc);
		String deployDirectory = helper.getAttribute(
				IDeployableServer.DEPLOY_DIRECTORY, "");
		return deployDirectory.trim();
	}
	
	public static boolean canExplore(IServer server) {
		String deployDirectory = ExploreUtils.getDeployDirectory(server);
		if (deployDirectory == null || deployDirectory.length() <= 0) {
			return false;
		}
		if (ExploreUtils.getExploreCommand() == null) {
			return false;
		}
		return true;
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
				if (Platform.OS_LINUX.equals(Platform.getOS())) {
					int len = exploreFolderCommandArray.length;
					exploreFolderCommandArray[len-1] = name;
					Runtime.getRuntime().exec(exploreFolderCommandArray);
				} else {
					command = command.replace(ExploreUtils.PATH, name);
					Runtime.getRuntime().exec(command);
				}
			} catch (IOException e) {
				JBossServerUIPlugin.log(e.getMessage(),e);
			}
		}
	}
	
	public static IPath getDeployPath(IDeployableServer server,IModule[] moduleTree) {
		IPath root = new Path( server.getDeployFolder() );
		String type, name;
		for( int i = 0; i < moduleTree.length; i++ ) {
			type = moduleTree[i].getModuleType().getId();
			name = moduleTree[i].getName();
			if( new Path(name).segmentCount() > 1 )
				// we strongly suspect this is a binary object and not a project
				return root.append(new Path(name).lastSegment());
			if( "jst.ear".equals(type)) 
				root = root.append(name + ".ear");
			else if( "jst.web".equals(type)) 
				root = root.append(name + ".war");
			else if( "jst.utility".equals(type) && i >= 1 && "jst.web".equals(moduleTree[i-1].getModuleType().getId())) 
				root = root.append("WEB-INF").append("lib").append(name + ".jar");			
			else if( "jst.connector".equals(type)) {
				root = root.append(name + ".rar");
			} else if( "jst.jboss.esb".equals(type)){
				root = root.append(name + ".esb");
			}else
				root = root.append(name + ".jar");
		}
		return root;
	}
	
}
