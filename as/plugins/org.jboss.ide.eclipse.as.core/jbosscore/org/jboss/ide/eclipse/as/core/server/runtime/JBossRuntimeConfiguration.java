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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.util.ASDebug;
import org.jboss.ide.eclipse.as.core.util.RuntimeConfigUtil;

/**
 * This class represents runtime configuration options, such as 
 * command line arguments, classpath, etc. 
 * 
 * This does *NOT* model a jboss configuration (default, minimal, etc) 
 * and what that configuration would currently support.
 * 
 * @author rstryker
 *
 */
public class JBossRuntimeConfiguration {
	
	public static final String PROP_START_ARGS = "PROP_START_ARGS";
	public static final String PROP_STOP_ARGS = "PROP_STOP_ARGS";
	public static final String PROP_PROG_ARGS = "PROP_PROG_ARGS";
	public static final String PROP_VM_ARGS = "PROP_VM_ARGS";
	public static final String PROP_CONFIG_PATH = "PROP_START_ARGS";
	public static final String PROP_CLASSPATH = "PROP_CLASSPATH";
	
	public static final String JBOSS_SERVER_HOST = "JBOSS_SERVER_HOST";
	public static final String JBOSS_SERVER_HOME = "JBOSS_SERVER_HOME";
	public static final String JBOSS_CONFIG = "JBOSS_CONFIG";
	
	public static final String JBOSS_SERVER_HOST_DEFAULT = "localhost";
	public static final String JBOSS_CONFIG_DEFAULT = "default";
	
	
	public static final String CONFIG_FILE = "jboss.config";
	
	private JBossServer server;
	private Properties properties;
	private boolean dirty = false;
	
	public JBossRuntimeConfiguration(JBossServer server) {
		this.server = server;

		load();

	}
	
	
	
	public String getAttribute(String key, String defaultVal) {
		if( properties == null ) load();
		String ret = properties.getProperty(key);
		if( ret == null ) return defaultVal;
		return ret;
	}
	
	private void setAttribute(String key, String val ) {
		if( properties == null ) load();
		ASDebug.p("", this);
		if( getAttribute(key, null) != null && !getAttribute(key, null).equals(val)) {
			properties.put(key, val);
			dirty = true;
		} else if( getAttribute(key, null) == null ) {
			properties.put(key, val);
			dirty = true;
		}
	}
	
	
	
	
	
	public void setHost(String val) {
		setAttribute(JBOSS_SERVER_HOST, val);
	}
	
	public void setServerHome(String val) {
		setAttribute(JBOSS_SERVER_HOME, val);
	}
	
	public void setJbossConfiguration(String val) {
		setAttribute(JBOSS_CONFIG, val);
	}
	
	public String getHost() {
		return getAttribute(JBOSS_SERVER_HOST, JBOSS_SERVER_HOST_DEFAULT);
	}
	public String getServerHome() {
		return getAttribute(JBOSS_SERVER_HOME, (String)null);
	}
	public String getJbossConfiguration() {
		return getAttribute(JBOSS_CONFIG, JBOSS_CONFIG_DEFAULT);
	}
	
	
	
	
	/*
	 * These methods go back to the version delegate for defaults if 
	 * they are not set as attributes here.
	 */
	
	public String getStartArgs() {
		return getAttribute(PROP_START_ARGS, getVersionDelegate().getStartArgs(server));
	}

	public String getStopArgs() {
		return getAttribute(PROP_STOP_ARGS, getVersionDelegate().getStopArgs(server));
	}

	public String getVMArgs() {
		return getAttribute(PROP_VM_ARGS, getVersionDelegate().getVMArgs(server));
	}
	
	public String getStartMainType() {
		return getVersionDelegate().getStartMainType(server);
	}
	
	
	public String getConfigurationPath() {
		return getServerHome() + File.separator + "server" + File.separator + getJbossConfiguration();
	}
	
	
	public String getDeployDirectory() {
		return getConfigurationPath() + File.separator + "deploy";
	}
	
	public AbstractServerRuntimeDelegate getVersionDelegate() {
		return server.getJBossRuntime().getVersionDelegate();
	}
	
	public void updateConfiguration(ILaunchConfiguration configuration) {
		try {
			String progArgs = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "");
			String vmArgs = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");
			
			setAttribute(PROP_START_ARGS, progArgs);
			setAttribute(PROP_VM_ARGS, vmArgs);
			String config = RuntimeConfigUtil.getCommandArgument(progArgs, "-c", "--configuration");
			String host = RuntimeConfigUtil.getCommandArgument(progArgs, "-b", "--host");
			if( config != null && config != "") 
				setAttribute(JBOSS_CONFIG, config);

			if( host != null && host != "") 
				setAttribute(JBOSS_SERVER_HOST, host);
			save();
		} catch( CoreException ce ) {
			
		}
	}
	
	
	public void save() {
		if( properties == null ) {
			load();
			return;
		}
		
		if( !dirty ) return;
		
		IFolder folder = server.getServer().getServerConfiguration();
		if( folder == null ) return;
		if( !folder.exists() ) return;
		
		IFile ifile = folder.getFile(JBossRuntimeConfiguration.CONFIG_FILE);
		if( ifile == null ) return;
		
		File file = ifile.getLocation().toFile();
		//ASDebug.p("Output file: " + file.getAbsolutePath(), this);
		try {	
			OutputStream out = new FileOutputStream(file);
			properties.store(out, "JBoss Server Config");
		} catch( Exception e ) {
			e.printStackTrace();
		}
		
	}
	
	public void load() {
		if( server.getServer() == null ) return;

		IFolder folder = server.getServer().getServerConfiguration();
		if( folder == null ) return;
		if( !folder.exists() ) return;
		
		IFile ifile = folder.getFile(JBossRuntimeConfiguration.CONFIG_FILE);
		if( ifile == null ) return;
		
		File file = ifile.getLocation().toFile();
		ASDebug.p("Output file: " + file.getAbsolutePath(), this);
		
		this.properties = new Properties();

		if( file == null ) return;
		
		if( file.canRead()) {
			try {
				FileInputStream in = new FileInputStream(file);
				properties.load(in);
			} catch( Exception e ) {
				e.printStackTrace();
			}
		} else {
			System.out.println("cannot read the file: " + file.getAbsolutePath());
		}

	}
}
