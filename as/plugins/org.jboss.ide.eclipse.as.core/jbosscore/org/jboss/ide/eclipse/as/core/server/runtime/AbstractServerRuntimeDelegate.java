/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.core.server.runtime;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.ServerType;
import org.jboss.ide.eclipse.as.core.server.JBossServer;


/**
 * Much of this class simply sets useful defaults for any information
 * that might not already be in the JBossRuntimeConfiguration, which
 * stores arguements and other data in a properties file.
 * 
 * Also, things like the main type for start, stop, twiddle, 
 * the jars and directories files are in, can change with new verseions. For 
 * that reason these are located here where a new JBoss Runtime (JBoss 4.x, 4.y, etc)
 * can overwrite these fields in a new RuntimeDelegate instance. 
 * 
 * @author rstryker
 *
 */
public abstract class AbstractServerRuntimeDelegate {
	
	protected static String startMainType = "org.jboss.Main";
	protected static String stopMainType = "org.jboss.Shutdown";
	protected static String twiddleMainType = "org.jboss.console.twiddle.Twiddle";
	
	protected static String defaultShutdownArgs = "-S";
	
	protected static String runJar = "bin" + File.separator + "run.jar";
	protected static String shutdownJar = "bin" + File.separator + "shutdown.jar";
	protected static String twiddleJar = "bin" + File.separator + "twiddle.jar";
	
	
	private JBossServerRuntime runtime;
	
	
	
	
	public AbstractServerRuntimeDelegate(JBossServerRuntime runtime) {
		this.runtime = runtime;
	}

	public abstract String getId();
	public abstract String[] getMinimalRequiredPaths();
	
	public String getStartArgs(JBossServer server) {
		return "--configuration=" + server.getAttributeHelper().getJbossConfiguration() + 
				" --host=" + server.getServer().getHost();
	}
	public String getStopArgs(JBossServer server) {
		return defaultShutdownArgs;
	}


	public String getVMArgs(JBossServer server) {
		return "";
	}
	
	
	
	public String getStartMainType() {
		return startMainType;
	}
	public String getStopMainType() {
		return stopMainType;
	}
	public String getTwiddleMainType() {
		return twiddleMainType;
	}

	
	public String getStartJar(JBossServer server) {
		return server.getAttributeHelper().getServerHome() + File.separator + runJar;
	}
	public String getShutdownJar(JBossServer server) {
		return server.getAttributeHelper().getServerHome() + File.separator + shutdownJar;
	}
	public String getTwiddleJar(JBossServer server) {
		return server.getAttributeHelper().getServerHome() + File.separator + twiddleJar;		
	}
	
	public List getRuntimeClasspath(JBossServer server) {
		return getRuntimeClasspath( server, IJBossServerRuntimeDelegate.ACTION_START);
	}
	
	
	/**
	 * The runtime classpath for starting or stopping a jboss server
	 * is simply the bin directory. 
	 * 
	 * Twiddle executions require more of the libraries, and so 
	 * I instead add most if not all of the libraries to ensure
	 * a proper execution. 
	 * 
	 * @param server
	 * @param action
	 * @return
	 */
	
	public List getRuntimeClasspath(JBossServer server, int action) {
		String serverHome = server.getAttributeHelper().getServerHome();
		ArrayList classpath = new ArrayList();
		
		if( action == IJBossServerRuntimeDelegate.ACTION_START) {
			classpath.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(getStartJar(server))));
		} else if( action == IJBossServerRuntimeDelegate.ACTION_SHUTDOWN ) {
			classpath.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(getShutdownJar(server))));
		} else if( action == IJBossServerRuntimeDelegate.ACTION_TWIDDLE ) {
			
			// Twiddle requires more classes and I'm too lazy to actually figure OUT which ones it needs.
			classpath.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(getTwiddleJar(server))));
			addDirectory (serverHome, classpath, "lib");
			addDirectory (serverHome, classpath, "lib" + File.separator + "endorsed");
			addDirectory (serverHome, classpath, "client");
			addDirectory (server.getAttributeHelper().getConfigurationPath(), classpath, "lib"); 
		}
				
		ArrayList runtimeClassPaths = convertClasspath(classpath, runtime.getVM());
		return runtimeClassPaths;
	}
	
	private ArrayList convertClasspath(ArrayList cp, IVMInstall vmInstall) {
		if (vmInstall != null) {
			try {
				cp.add(JavaRuntime.newRuntimeContainerClasspathEntry(
					new Path(JavaRuntime.JRE_CONTAINER)
						.append("org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType")
						.append(vmInstall.getName()), IRuntimeClasspathEntry.BOOTSTRAP_CLASSES));
			} catch (Exception e) {
				// ignore
			}			
			
			IPath jrePath = new Path(vmInstall.getInstallLocation().getAbsolutePath());
			if (jrePath != null) {
				IPath toolsPath = jrePath.append("lib").append("tools.jar");
				if (toolsPath.toFile().exists()) {
					cp.add(JavaRuntime.newArchiveRuntimeClasspathEntry(toolsPath));
				}
			}
		}
		
		Iterator cpi = cp.iterator();
		ArrayList list = new ArrayList();
		while (cpi.hasNext()) {
			IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry) cpi.next();
			try {
				list.add(entry.getMemento());
			} catch (Exception e) {
				//Trace.trace(Trace.SEVERE, "Could not resolve classpath entry: " + entry, e);
			}
		}

		return list;
	}
	
	private void addDirectory (String serverHome, ArrayList classpath, String dirName) {
		String libPath = serverHome + File.separator + dirName;
		File libDir = new File(libPath);
		File libs[] = libDir.listFiles(new FilenameFilter(){
			public boolean accept(File dir, String name)
			{
				return (name != null && name.endsWith("jar"));
			}
		});
		
		for (int i = 0; i < libs.length; i++)
		{
			classpath.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(libPath + File.separator + libs[i].getName())));
		}
	} // end method


}
	

