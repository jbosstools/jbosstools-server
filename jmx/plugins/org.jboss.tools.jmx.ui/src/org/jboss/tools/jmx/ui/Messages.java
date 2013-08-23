/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jboss.tools.jmx.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String NewConnectionAction;
	public static String EditConnectionAction;
	public static String EditConnectionWizardTitle;
	public static String NewConnectionWizard;
	public static String NewConnectionWizard_CreateNewConnection;
	public static String DefaultConnectionWizardPage_Title;
	public static String DefaultConnectionWizardPage_Description;
	public static String DefaultConnectionWizardPage_Simple;
	public static String DefaultConnectionWizardPage_Advanced;
	public static String DefaultConnectionWizardPage_Name;
	public static String DefaultConnectionWizardPage_Default_Name;
	public static String DefaultConnectionWizardPage_Host;
	public static String DefaultConnectionWizardPage_Port;
	public static String DefaultConnectionWizardPage_Username;
	public static String DefaultConnectionWizardPage_Password;
	public static String DefaultConnectionWizardPage_JMX_URL;
	public static String DefaultConnectionWizardPage_Blank_Invalid;
	public static String DefaultConnectionWizardPage_Invalid_Connection;
	public static String DefaultConnectionWizardPage_Name_In_Use;
	public static String DeleteConnection;

    public static String AttributeControlFactory_updateButtonTitle;
    public static String AttributeDetails_title;
    public static String AttributeDetailsSection_errorTitle;
    public static String AttributesPage_title;
    public static String AttributesSection_title;
    public static String className;
    public static String description;
    public static String domain;
    public static String general;
    public static String horizontal;
    public static String impact;
    public static String InfoPage_notificationsSectionTitle;
    public static String InfoPage_title;
    public static String InvocationResultDialog_title;
    public static String key;
    public static String MBeanAttributeValue_Warning;
    public static String MBeanServerConnectAction_text;
    public static String MBeanServerDisconnectAction_text;
    public static String MBeanServerDisconnectAction_dialogTitle;
    public static String MBeanServerDisconnectAction_dialogText;
    public static String name;
    public static String NotificationsPage_clearActionToolTip;
    public static String NotificationsPage_message;
    public static String NotificationsPage_sequenceNumber;
    public static String NotificationsPage_source;
    public static String NotificationsPage_subscribeActionToolTip;
    public static String NotificationsPage_timestamp;
    public static String NotificationsPage_title;
    public static String NotificationsPage_type;
    public static String NotificationsPage_userData;
    public static String objectName;
    public static String OpenMBeanAction_description;
    public static String OpenMBeanAction_dialogDescription;
    public static String OpenMBeanAction_dialogTitle;
    public static String OpenMBeanAction_text;
    public static String OpenMBeanAction_tooltip;
    public static String OperationDetails_invocationError;
    public static String OperationDetails_invocationResult;
    public static String OperationDetails_invocationSuccess;
    public static String OperationDetails_title;
    public static String OperationsPage_title;
    public static String OperationsSection_title;
    public static String parameters;
    public static String permission;
    public static String readable;
    public static String readOnly;
    public static String readWrite;
    public static String returnType;
    public static String type;
    public static String unavailable;
    public static String value;
    public static String vertical;
    public static String writable;
    public static String writeOnly;
	public static String TypeInAFilter;
	public static String LoadingJMXNodes;
	public static String Loading;
	public static String ErrorLoading;
	public static String UpdatingSelectionJob;
	
	public static String StateConnected;
	public static String StateDisconnected;

	public static String JMXUIImageDescriptorNotFound;
	
	static {
	    NLS.initializeMessages("org.jboss.tools.jmx.ui.Messages", //$NON-NLS-1$
	                    Messages.class);
	}
}
