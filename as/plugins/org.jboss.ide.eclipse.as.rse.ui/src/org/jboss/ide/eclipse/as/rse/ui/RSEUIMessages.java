package org.jboss.ide.eclipse.as.rse.ui;

import org.eclipse.osgi.util.NLS;

public class RSEUIMessages {
	private static final String BUNDLE_NAME = "org.jboss.ide.eclipse.as.rse.ui.RSEUIMessages"; //$NON-NLS-1$
	public static String UNSET_REMOTE_SERVER_HOME;
	public static String REMOTE_SERVER_CONFIG;
	public static String TEST;
	public static String REMOTE_SERVER_TEST;
	public static String REMOTE_SERVER_TEST_SUCCESS;
	public static String VALIDATING_REMOTE_CONFIG;
	public static String EMPTY_HOST;
	public static String FILE_SUBSYSTEM_NOT_FOUND;
	public static String REMOTE_FILESYSTEM_CONNECT_FAILED;
	public static String FILESERVICE_NOT_FOUND;
	public static String REMOTE_HOME_NOT_FOUND;
	public static String REMOTE_CONFIG_NOT_FOUND;
	public static String ERROR_CHECKING_REMOTE_SYSTEM;
	public static String BROWSE_REMOTE_SYSTEM;
	public static String CHANGE_RSE_HOST;
	public static String CHANGE_HOSTNAME;
	public static String CHANGE_REMOTE_SERVER_HOME;
	public static String CHANGE_REMOTE_SERVER_CONFIG;
	public static String RSE_REMOTE_LAUNCH;
	public static String RSE_START_COMMAND;
	public static String RSE_STOP_COMMAND;
	public static String RSE_AUTOMATICALLY_CALCULATE;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, RSEUIMessages.class);
	}
	private RSEUIMessages() {
	}

}
