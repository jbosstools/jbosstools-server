package org.jboss.ide.eclipse.as.openshift.core;

import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.IOpenshiftRequest;

public class UserInfo implements IOpenshiftRequest {

	private String rhlogin;
	private String uuId;
	private String sshKey;
	private String rhcDomain;
	private String namespace;

	UserInfo(String rhlogin, String uuid, String sshKey, String rhcDomain, String namespace) {
		this.rhlogin = rhlogin;
		this.uuId = uuid;
		this.sshKey = sshKey;
		this.rhcDomain = rhcDomain;
		this.namespace = namespace;
	}

	public String getRhlogin() {
		return rhlogin;
	}

	public String getUuId() {
		return uuId;
	}

	public String getRhcDomain() {
		return rhcDomain;
	}

	public String getSSHKey() {
		return sshKey;
	}

	public String getNamespace() {
		return namespace;
	}
}
