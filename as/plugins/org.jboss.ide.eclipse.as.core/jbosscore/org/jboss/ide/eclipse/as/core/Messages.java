/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.ide.eclipse.as.core.Messages"; //$NON-NLS-1$
	
	/* Standard and re-usable */
	public static String jboss;
	public static String server;
	public static String runtime;
	public static String serverVersionName;
	public static String serverName;
	public static String serverCountName;
	public static String runModeNotSupported;
	
	public static String loadXMLDocumentFailed;
	public static String saveXMLDocumentFailed;
	public static String loadJMXClassesFailed;
	public static String securityException;
	public static String JMXPoller;
	public static String XPathLoadFailure;
	
	public static String STOP_FAILED_MESSAGE;
	public static String FORCE_TERMINATED;
	public static String TERMINATED;
	public static String FORCE_TERMINATE_FAILED;
	
	public static String ServerStarting;
	public static String NotSupported;
	public static String UndeploySingleFilesJob;
	public static String SingleFileUndeployFailed;
	public static String SingleFileUndeployFailed2;
	public static String ServerSaveFailed;
	public static String LaunchConfigJREError;
	public static String UnexpectedServerStopError;
	public static String CopyFileError;
	public static String DeleteFileError;
	public static String DeleteFolderError;
	public static String Ports;
	public static String AddXPathDetailsJob;
	public static String CouldNotPublishModule;
	public static String FullPublishFail;
	public static String IncrementalPublishFail;
	public static String CountModifiedMembers;
	public static String DeleteModuleFail;
	public static String DeleteModuleFail2;
	public static String ModuleDeleted;
	public static String ModulePublished;
	public static String NoPublisherFound;
	public static String PollingStartupSuccess;
	public static String PollingShutdownSuccess;
	public static String PollingStartupFailed;
	public static String PollingShutdownFailed;
	public static String PollingStarting;
	public static String PollingShuttingDown;
	public static String StartupPollerNotFound;
	public static String ShutdownPollerNotFound;
	public static String PollerFailure;
	public static String PollerAborted;
	public static String ServerPollerThreadName;
	public static String ServerTypeDiscovererFail;
	public static String TerminateTwiddleFailed;
	public static String CannotSetUpImproperServer;
	public static String CannotLocateServerHome;
	public static String ServerRuntimeNotFound;
	public static String ServerRuntimeConfigNotFound;
	public static String ServerNotFound;
	public static String CannotLoadServerPoller;
	public static String AddingJMXDeploymentFailed;
	public static String JMXPauseScannerError;
	public static String JMXResumeScannerError;
	public static String JMXScannerCanceled;
	public static String CannotSaveServersStartArgs;
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	private Messages() {
	}
}
