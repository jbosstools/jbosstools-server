package org.jboss.ide.eclipse.as.core.server.runtime;

import org.jboss.ide.eclipse.as.core.server.JBossServer;

public class JBoss32RuntimeDelegate extends AbstractServerRuntimeDelegate {
	public static final String VERSION_ID = "org.jboss.ide.eclipse.as.runtime.32";

	public JBoss32RuntimeDelegate(JBossServerRuntime runtime) {
		super(runtime);
	}


	public String getId() {
		return "3.2";
	}

}
