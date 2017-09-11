package org.jboss.ide.eclipse.as.reddeer.server.requirement;

import org.eclipse.reddeer.junit.requirement.configuration.RequirementConfiguration;
import org.jboss.ide.eclipse.as.reddeer.server.family.*;

/**
 * 
 * @author psrna, Radoslav Rabara
 *
 */
public class ServerRequirementConfig implements RequirementConfiguration {
	
	private String runtime;
	private String version;
	private JBossFamily family;
	private RemoteServerConfiguration remote;
	
	
	@Override
	public String getId() {
		return family.getLabel()+"-"+version;
	}


	public String getRuntime() {
		return runtime;
	}


	public void setRuntime(String runtime) {
		this.runtime = runtime;
	}


	public String getVersion() {
		return version;
	}


	public void setVersion(String version) {
		this.version = version;
	}


	public JBossFamily getFamily() {
		return family;
	}


	public void setFamily(JBossFamily family) {
		this.family = family;
	}


	public RemoteServerConfiguration getRemote() {
		return remote;
	}


	public void setRemote(RemoteServerConfiguration remote) {
		this.remote = remote;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((family == null) ? 0 : family.hashCode());
		result = prime * result + ((remote == null) ? 0 : remote.hashCode());
		result = prime * result + ((runtime == null) ? 0 : runtime.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServerRequirementConfig other = (ServerRequirementConfig) obj;
		if (family != other.family)
			return false;
		if (remote == null) {
			if (other.remote != null)
				return false;
		} else if (!remote.equals(other.remote))
			return false;
		if (runtime == null) {
			if (other.runtime != null)
				return false;
		} else if (!runtime.equals(other.runtime))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
	
	
	
	
}