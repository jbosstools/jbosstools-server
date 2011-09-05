package org.jboss.ide.eclipse.as.openshift.internal.core.request;

import org.jboss.ide.eclipse.as.openshift.core.internal.marshalling.IOpenshiftRequest;


public class UserInfoRequest implements IOpenshiftRequest {

	private String rhlogin;
	private boolean debug;

	public UserInfoRequest(String username) {
		this(username, false);
	}

	public UserInfoRequest(String username, boolean debug) {
		this.rhlogin = username;
		this.debug = debug;
	}

	public String getRhLogin() {
		return rhlogin;
	}

	public boolean isDebug() {
		return debug;
	}
	
}
