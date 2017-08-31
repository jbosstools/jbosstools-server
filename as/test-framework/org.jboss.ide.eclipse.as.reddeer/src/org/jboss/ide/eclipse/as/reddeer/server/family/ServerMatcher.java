/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.reddeer.server.family;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jboss.ide.eclipse.as.reddeer.server.family.JBossFamily;

public class ServerMatcher extends BaseMatcher<Object>{
	
	JBossFamily family;
	
	private ServerMatcher(JBossFamily family) {
		this.family = family;
	}

	public static ServerMatcher EAP() {
		return new ServerMatcher(JBossFamily.EAP);
	}
	
	public static ServerMatcher WildFly() {
		return new ServerMatcher(JBossFamily.WILDFLY);
	}
	
	public static ServerMatcher AS() {
		return new ServerMatcher(JBossFamily.AS);
	}
	
	public boolean matches(Object arg0) {
		return arg0.equals(family);
	}
	
	public void describeTo(Description arg0) {
		// TODO Auto-generated method stub
	}
}