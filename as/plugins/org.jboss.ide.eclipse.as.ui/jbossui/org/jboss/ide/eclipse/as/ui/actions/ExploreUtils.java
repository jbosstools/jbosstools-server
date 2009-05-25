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
import org.jboss.ide.eclipse.as.ui.Messages;

/**
 * @author snjeza
 * 
 */

public class ExploreUtils {

	public final static String PATH = "{%}"; //$NON-NLS-1$
	public final static String EXPLORE = Messages.ExploreUtils_Action_Text;
	public final static String EXPLORE_DESCRIPTION = Messages.ExploreUtils_Description;
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

	@SuppressWarnings("nls")
	private static void setExploreCommands() {
		if (Platform.OS_MACOSX.equals(Platform.getOS())) {
			exploreFolderCommand = "/usr/bin/open -a /System/Library/CoreServices/Finder.app \"" //$NON-NLS-1$
					+ PATH + "\""; //$NON-NLS-1$
			exploreFileCommand = exploreFolderCommand;
		} else if (Platform.OS_WIN32.equals(Platform.getOS())) {
			exploreFolderCommand = "cmd /C start explorer /root,/e,\"" //$NON-NLS-1$
					+ PATH + "\""; //$NON-NLS-1$
			exploreFileCommand = "cmd /C start explorer /select,/e,\"" //$NON-NLS-1$
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
		if (server.getRuntime() != null && deployableServer != null) {
			return deployableServer.getDeployFolder();
		}
		IServerWorkingCopy swc = server.createWorkingCopy();
		ServerAttributeHelper helper = new ServerAttributeHelper(swc
				.getOriginal(), swc);
		String deployDirectory = helper.getAttribute(
				IDeployableServer.DEPLOY_DIRECTORY, ""); //$NON-NLS-1$
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
	
	@SuppressWarnings("nls")
	public static IPath getDeployPath(IDeployableServer server,IModule[] moduleTree) {
		IPath root = new Path( server.getDeployFolder() );
		String type, name;
		for( int i = 0; i < moduleTree.length; i++ ) {
			type = moduleTree[i].getModuleType().getId();
			name = moduleTree[i].getName();
			if( new Path(name).segmentCount() > 1 )
				// we strongly suspect this is a binary object and not a project
				return root.append(new Path(name).lastSegment());
			if( "jst.ear".equals(type))  //$NON-NLS-1$
				root = root.append(name + ".ear"); //$NON-NLS-1$
			else if( "jst.web".equals(type))  //$NON-NLS-1$
				root = root.append(name + ".war"); //$NON-NLS-1$
			else if( "jst.utility".equals(type) && i >= 1 && "jst.web".equals(moduleTree[i-1].getModuleType().getId()))  //$NON-NLS-1$ //$NON-NLS-2$
				root = root.append("WEB-INF").append("lib").append(name + ".jar");			 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			else if( "jst.connector".equals(type)) { //$NON-NLS-1$
				root = root.append(name + ".rar"); //$NON-NLS-1$
			} else if( "jst.jboss.esb".equals(type)){ //$NON-NLS-1$
				root = root.append(name + ".esb"); //$NON-NLS-1$
			}else
				root = root.append(name + ".jar"); //$NON-NLS-1$
		}
		return root;
	}
	
}
