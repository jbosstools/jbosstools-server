package org.jboss.ide.eclipse.as.core.server.internal.extendedproperties;

import org.eclipse.wst.server.core.IServer;

/**
 *
 */
public class JBossAS7ExtendedProperties extends JBossExtendedProperties {
	public JBossAS7ExtendedProperties(IServer server) {
		super(server);
	}

	public String getNewFilesetDefaultRootFolder() {
		return "standalone/configuration"; //$NON-NLS-1$
	}
}
