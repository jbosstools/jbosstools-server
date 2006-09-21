/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.core.util;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.ServerType;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.runtime.JBossServerRuntime;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class ServerCloneUtil {

	public static void directoriesClone(File[] files, String config, JBossServer server, IProgressMonitor monitor) {
		SubProgressMonitor subMonitor;
		String relativeLoc;
		File newFile;
		
    	String oldConfigPath = server.getAttributeHelper().getConfigurationPath();
    	String newConfigPath = server.getAttributeHelper().getServerHome() 
    		+ Path.SEPARATOR + "server" + Path.SEPARATOR + config;
    	
    	monitor.beginTask("Copying configuration", files.length + 1);
    	
    	
    	subMonitor = new SubProgressMonitor(monitor, 1);
    	subMonitor.beginTask("Creating configuration directory: " + newConfigPath, 1);
    	new File(newConfigPath).mkdir();
    	subMonitor.worked(1);
		subMonitor.done();

		
		for( int i = 0; i < files.length; i++ ) {
			relativeLoc = files[i].getAbsolutePath().substring(oldConfigPath.length());
			newFile = new File(newConfigPath + relativeLoc);
			if( files[i].isDirectory() ) {
		    	subMonitor = new SubProgressMonitor(monitor, 1);
		    	subMonitor.beginTask("Creating directory: " + newFile.getAbsolutePath(), 1);

		    	boolean res = newFile.mkdir();

		    	subMonitor.worked(1);
				subMonitor.done();
			} else {
		    	subMonitor = new SubProgressMonitor(monitor, 1);
		    	subMonitor.beginTask("Copying file: " + newFile.getAbsolutePath(), 1);

				FileUtil.copyFile(files[i], newFile);

		    	subMonitor.worked(1);
				subMonitor.done();
			}
		}
		
		monitor.done();
	}
	
	public static void wstServerClone(JBossServer server, String newName, String config, IProgressMonitor monitor) {
		
		IServerType serverType = server.getServer().getServerType();
		IRuntimeType runtimeType = server.getServer().getRuntime().getRuntimeType();
		JBossServerRuntime oldJBRuntime = (JBossServerRuntime)server.getServer().getRuntime().loadAdapter(JBossServerRuntime.class, null);
		try {
			IServerWorkingCopy newServerWC = serverType.createServer(null, null, null, null);
			IRuntimeWorkingCopy newRuntimeWC = runtimeType.createRuntime("", null);
			
			
			
			newRuntimeWC.setName(newName);
			newRuntimeWC.setLocation(new Path(server.getAttributeHelper().getServerHome()));
			IRuntime runtime = newRuntimeWC.save(true, null);
			JBossServerRuntime newJBRuntime = (JBossServerRuntime)newRuntimeWC.loadAdapter(JBossServerRuntime.class, null);
			newJBRuntime.setVMInstall(oldJBRuntime.getVM());
			newJBRuntime.setConfigName(config);
			//newJBRuntime.setLocation(server.getAttributeHelper().getServerHome());
			newServerWC.setRuntime(runtime);
			
			IFolder configFolder = ServerType.getServerProject().getFolder(newName);
			if( !configFolder.exists() ) {
				configFolder.create(true, true, null);
			}
			
			newServerWC.setServerConfiguration(configFolder);
			newServerWC.setName(newName);
			IServer finalServer = newServerWC.save(true, null);
			
			// now clone the launch configuration?
			ILaunchConfiguration base = ((Server)server.getServer()).getLaunchConfiguration(false, new NullProgressMonitor());
			if( base != null ) {
				ILaunchConfigurationWorkingCopy baseWC = base.getWorkingCopy();
				ILaunchConfiguration finalConfig = ((Server)finalServer).getLaunchConfiguration(true, new NullProgressMonitor());
				ILaunchConfigurationWorkingCopy finalConfigWC = finalConfig.getWorkingCopy();
				
				Map baseMap = baseWC.getAttributes();
				Map finalMap = new HashMap();
				Iterator baseMapIterator = baseMap.keySet().iterator();
				while(baseMapIterator.hasNext()) {
					Object key = baseMapIterator.next();
					Object val = baseMap.get(key);
					finalMap.put(key, val);
				}
				finalMap.put("server-id", finalServer.getId());
				finalConfigWC.setAttributes(finalMap);
				finalConfigWC.doSave();
			}
		} catch( CoreException ce) {}
		
		monitor.beginTask("Cloning Server Elements", 50);
		monitor.worked(50);
		monitor.done();
	}
}
