package org.jboss.ide.eclipse.as.reddeer.server.requirement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoteServerConfiguration {
	
	public String serverHome;
	public String serverHost;
	public String username;
	public String password;
	public String systemType;
	
	public String getSystemType() {
		return systemType;
	}
	public void setSystemType(String systemType) {
		this.systemType = systemType;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getServerHome() {
		return serverHome;
	}
	public void setServerHome(String serverHome) {
		this.serverHome = serverHome;
	}
	public String getServerHost() {
		return serverHost;
	}
	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}

}
