package org.jboss.ide.eclipse.as.core.server.runtime;

import org.jboss.ide.eclipse.as.core.server.JBossServer;

public class JBoss40RuntimeDelegate extends AbstractServerRuntimeDelegate {
	public static final String VERSION_ID = "org.jboss.ide.eclipse.as.runtime.40";
	
	public JBoss40RuntimeDelegate(JBossServerRuntime runtime) {
		super(runtime);
	}
	
	public String getId() {
		return "4.0";
	}

}
