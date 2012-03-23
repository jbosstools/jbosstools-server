/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.jmx.integration;

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.tools.jmx.core.ExtensionManager;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXException;

public class JMXSafeRunner {
	private String user, pass;
	private IServer server;
	
	public JMXSafeRunner(IServer s) {
		this.server = s;
		user = ServerConverter.getJBossServer(s).getUsername();
		pass = ServerConverter.getJBossServer(s).getPassword();
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	
	public void run(IJMXRunnable r) throws CoreException {
		run(server,r,user,pass);
	}
	
	public static void run(IServer s, IJMXRunnable r) throws JMXException {
		String user = ServerConverter.getJBossServer(s).getUsername();
		String pass = ServerConverter.getJBossServer(s).getPassword();
		run(s,r,user,pass);
	}
	
	public static void run(IServer s, IJMXRunnable r, String user, String pass) throws JMXException {
		ExtensionManager.getProviders(); // todo clean up, this is here to ensure it's initialized 
		IConnectionWrapper c = JBossJMXConnectionProviderModel.getDefault().getConnection(s);
		if( c != null ) {
			HashMap<String, String> prefs = new HashMap<String, String>();
			prefs.put("force", "true");
			prefs.put("user", user);
			prefs.put("pass", pass);
			c.run(r, prefs);
		}
	}

}
