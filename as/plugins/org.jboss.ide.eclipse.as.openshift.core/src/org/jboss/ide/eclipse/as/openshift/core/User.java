package org.jboss.ide.eclipse.as.openshift.core;

import org.jboss.ide.eclipse.as.openshift.core.internal.InternalUser;

public class User extends InternalUser {

	public User(String rhlogin, String password) {
		super(rhlogin, password);
	}
}
