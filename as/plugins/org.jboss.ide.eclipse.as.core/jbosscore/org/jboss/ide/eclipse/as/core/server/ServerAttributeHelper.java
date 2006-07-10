package org.jboss.ide.eclipse.as.core.server;

import java.io.File;

import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.runtime.AbstractServerRuntimeDelegate;

public class ServerAttributeHelper {
	
	public static final String PROP_START_ARGS = "PROP_START_ARGS";
	public static final String PROP_STOP_ARGS = "PROP_STOP_ARGS";
	public static final String PROP_PROG_ARGS = "PROP_PROG_ARGS";
	public static final String PROP_VM_ARGS = "PROP_VM_ARGS";
	public static final String PROP_CONFIG_PATH = "PROP_START_ARGS";
	public static final String PROP_CLASSPATH = "PROP_CLASSPATH";
	
	public static final String JBOSS_SERVER_HOME = "JBOSS_SERVER_HOME";
	public static final String JBOSS_CONFIG = "JBOSS_CONFIG";
	
	public static final String JBOSS_CONFIG_DEFAULT = "default";

	
	private ServerWorkingCopy server;
	private JBossServer jbServer;
	public ServerAttributeHelper(JBossServer jbServer, IServerWorkingCopy copy) {
		this.server = (ServerWorkingCopy)copy;
		this.jbServer = jbServer;
	}
	

	
	public String getServerHome() {
		return server.getAttribute(JBOSS_SERVER_HOME, (String)null);
	}
	public String getJbossConfiguration() {
		return server.getAttribute(JBOSS_CONFIG, JBOSS_CONFIG_DEFAULT);
	}
	
	public void setServerHome( String home ) {
		server.setAttribute(ServerAttributeHelper.JBOSS_SERVER_HOME, home);
	}
	
	public void setJbossConfiguration( String config ) {
		server.setAttribute(ServerAttributeHelper.JBOSS_CONFIG, config);
	}
	
	public void setProgramArgs(String args) {
		server.setAttribute(PROP_START_ARGS, args);
	}
	
	public void setVMArgs(String args) {
		server.setAttribute(PROP_VM_ARGS, args);
	}
	
	
	public void save() {
		try {
			server.save(true, null);
		} catch( Exception e) {
		}
	}
	
	
	/*
	 * These methods go back to the version delegate for defaults if 
	 * they are not set as attributes here.
	 */
	
	public String getStartArgs() {
		return server.getAttribute(PROP_START_ARGS, getVersionDelegate().getStartArgs(jbServer));
	}

	public String getStopArgs() {
		return server.getAttribute(PROP_STOP_ARGS, getVersionDelegate().getStopArgs(jbServer));
	}

	public String getVMArgs() {
		return server.getAttribute(PROP_VM_ARGS, getVersionDelegate().getVMArgs(jbServer));
	}
	
	public String getStartMainType() {
		return getVersionDelegate().getStartMainType();
	}
	
	
	public String getConfigurationPath() {
		return getServerHome() + File.separator + "server" + File.separator + getJbossConfiguration();
	}
	
	
	public String getDeployDirectory() {
		return getConfigurationPath() + File.separator + "deploy";
	}
	
	public AbstractServerRuntimeDelegate getVersionDelegate() {
		return jbServer.getJBossRuntime().getVersionDelegate();
	}


	
}
