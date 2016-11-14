package org.jboss.ide.eclipse.as.rse.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.ide.eclipse.as.rse.core.Messages"; //$NON-NLS-1$

	/* Standard and re-usable */
	public static String configErrorNonStandardDeploy;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	private Messages() {
	}
}