package org.jboss.ide.eclipse.as.openshift.core;

import org.jboss.ide.eclipse.as.openshift.core.internal.User;

public class UserFactory {

	public static IUser create(String rhlogin, String password) {
		return new User(rhlogin, password);
	}
	
}
