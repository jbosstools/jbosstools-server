package org.jboss.ide.eclipse.as.ui.actions;

import org.eclipse.osgi.util.NLS;

public class ServerActionMessages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.ide.eclipse.as.ui.actions.messages"; //$NON-NLS-1$
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ServerActionMessages.class);		
	}
	public static String START_IN_DEBUG_MODE;
	public static String RESTART_IN_DEBUG_MODE;
	public static String START_IN_RUN_MODE;
	public static String RESTART_IN_RUN_MODE;
	public static String SELECT_A_SERVER;
	public static String SelectServerActionDelegate_NewServerMenuItem;
	public static String STOP_SERVER;
	public static String CHANGE_TIME_STAMP;
	public static String CHANGE_TIME_STAMP_DEFAULT;
	public static String ChangeTimeStampActionDelegate_cannot_unregister;
	public static String ChangeTimeStampActionDelegate_could_not_register_project;
	public static String DeployActionMessageBoxTitle;
	public static String DeployActionMessageBoxMsg;
}
