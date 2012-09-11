/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.ide.eclipse.as.ui.Messages"; //$NON-NLS-1$
	/* Standard and re-usable */
	public static String browse;
	public static String serverName;
	public static String copy;
	public static String delete;
	public static String directory;
	
	/* Server and Runtime Wizard Fragments */
	public static String wf_BaseNameVersionReplacement;
	public static String wf_NameLabel;
	public static String wf_HomeDirLabel;
	public static String wf_JRELabel;
	public static String wf_ConfigLabel;
	public static String wf_ExecEnvironmentLabel;
	
	public static String J2EEModuleExportOperation_could_not_export_module;
	public static String J2EEModuleExportOperation_ErrorExportingArchive;
	public static String JBAS_version;
	public static String JBEAP_version;
	public static String JBoss7ServerWizardFragment_could_not_create_ui;
	public static String JBossRuntimeWizardFragment_AllFieldsRequired;
	public static String JBossRuntimeWizardFragment_CopyAConfigShellText;
	public static String JBossRuntimeWizardFragment_DeleteConfigConfirmation;
	public static String JBossRuntimeWizardFragment_DeleteConfigTitle;
	public static String JBossRuntimeWizardFragment_MustSelectValidConfig;
	public static String JBossRuntimeWizardFragment_OutputFolderExists;
	public static String rwf_DownloadRuntime;
	public static String rwf_InstallingRuntimeJob;
	public static String rwf_DownloadAndInstallRuntimeWizard;
	public static String rwf_DownloadingMayTakeLongTime;
	public static String rwf_DownloadServerFileChooser;
	public static String rwf_InstallingASTitle;
	public static String rwf_CopyConfigLabel;
	public static String rwf_DestinationLabel;
	public static String rwf_JBossRuntime;
	public static String rwf_Explanation;
	public static String rwf_NameInUse;
	public static String rwf_homeMissingFiles;
	public static String rwf_jboss7homeNotValid;
	public static String rwf_homeIncorrectVersion;
	public static String rwf_homeIncorrectVersionError;
	public static String rwf_nameTextBlank;
	public static String rwf_homeDirBlank;
	public static String rwf_NoVMSelected;
	public static String rwf_jre6NotFound;
	public static String rwf_noValidJRE;
	public static String rwf_DefaultJREForExecEnv;
	public static String rwf7_ConfigFileError;
	public static String swf_Title;
	public static String swf_RuntimeInformation;
	public static String swf_AuthorizationDescription;
	public static String swf_Explanation;
	public static String swf_Explanation2;
	public static String swf_ConfigurationLocation;
	public static String swf_AuthenticationGroup;
	public static String swf_Username;
	public static String swf_Password;
	public static String swf_BaseName;
	public static String swf_NameInUse;
	public static String swf_DeployEditorHeading;
	public static String swf_DeploymentDescription;
	public static String swf_DeploymentDescriptionLabel;
	public static String swf_DeployDirectory;
	public static String swf_TempDeployDirectory;
	public static String swf_CloneConfiguration;
	public static String sswf_Title;
	public static String sswf_BaseName;

	public static String ServerActionProvider_CouldNotOpenServerError;
	public static String ServerContentProvider_ErrorInServersViewAnimation;
	public static String ServerDialogHeading;
	public static String ServerLogView_ImportingLogTaskName;
	public static String ServerLogView_NoLogSelected;
	public static String credentials_warning;
	public static String credentials_save;


	public static String property;
	public static String value;

	public static String DeleteModuleText;
	public static String IncrementalPublishModuleText;
	public static String FullPublishModuleText;
	public static String DeleteModuleDescription;
	public static String PollerSection_ServerPollingSection;
	public static String PublishModuleDescription;

	public static String DeleteModuleConfirm;

	/* Action Delegate */

	public static String ActionDelegateMakeDeployable;
	public static String ActionDelegateMakeUndeployable;
	public static String ActionDelegateDeployableServersNotFoundTitle;
	public static String ActionDelegateDeployableServersNotFoundDesc;
	public static String ActionDelegateCannotPublish;
	public static String ActionDelegatePublishFailed;
	public static String ActionDelegateFileResourcesOnly;
	public static String ActionDelegateSelectServer;

	public static String ChangePortDialog_could_not_change_port_interrupted;
	public static String ChangePortDialog_could_not_determine_port;
	public static String ChangePortDialog_LoadingTaskName;
	/* Console */
	public static String ConsoleResourceNotFound;
	public static String ConsoleCouldNotLocateInWorkspace;


	/* Twiddle Dialog */
	public static String TwiddleDialog;
	public static String TwiddleDialog_UnexpectedError;
	public static String TwiddleDialogExecute;
	public static String TwiddleDialogDone;
	public static String TwiddleDialogArguments;

	/* XPath Dialog */
	public static String XPathNewCategory;
	public static String XPathNewCategoryNameInUse;
	public static String XPathCategoryName;
	public static String XPathChangeValueAction_ActionText;
	public static String XPathNewXpath;
	public static String XPathEditXpath;
	public static String XPathNameEmpty;
	public static String XPathNameInUse;
	public static String XPathColumnLocation;
	public static String XPathColumnAttributeVals;

	public static String XPathName;
	public static String XPathPattern;
	public static String XPathFilePattern;
	public static String XPathRootDir;
	public static String XPathActionProvider_EditFileAction;
	public static String XPathActionProvider_ErrorRunningAction;
	public static String XPathActionProvider_NewCategoryAction;
	public static String XPathActionProvider_RemoveCategoryQuestion;
	public static String XPathAttribute;


	public static String XPathDialogs_BlankCategoryError;
	public static String XPathDialogs_CategoryLabel;
	public static String XPathDialogs_NoElementsMatched;
	public static String XPathDialogs_PreviewButton;
	public static String XPathDialogs_SelectServer;
	public static String XPathDialogs_ServerLabel;
	public static String XPathDialogs_XPathDescriptionLabel;
	public static String XPathTreeContentProvider_JobName;
	public static String XPathTreeLabelProvider_LoadingLabel;
	public static String XPathTreeLabelProvider_MatchIndexLabel;
	public static String XPathTreeLabelProvider_XMLConfigLabel;
	public static String DescriptorXPathRemoveCategory;
	public static String DescriptorXPathRemoveCategoryDesc;
	public static String DescriptorXPathNewXPath;
	public static String DescriptorXPathEditXPath;
	public static String DescriptorXPathDeleteXPath;
	public static String DescriptorXPathMatch;



	/* Editor Strings */
	public static String EditorDeployment;
	public static String EditorUseWorkspaceMetadata;
	public static String EditorUseServersDeployFolder;
	public static String EditorUseCustomDeployFolder;
	public static String EditorSetDeployLabel;
	public static String EditorSetTempDeployLabel;
	public static String EditorEditDeployLocCommand;
	public static String EditorModule;
	public static String EditorDoNotLaunch;
	public static String EditorDoNotLaunchCommand;
	public static String EditorListenOnAllHosts;
	public static String EditorListenOnAllHostsCommand;
	public static String EditorExposeManagement;
	public static String EditorExposeManagementCommand;
	public static String EditorChangeServerMode;
	public static String EditorZipDeployments;
	public static String EditorSetRadioClicked;
	public static String EditorDeployDNE;
	public static String EditorTempDeployDNE;
	public static String EditorStartupPollerLabel;
	public static String EditorShutdownPollerLabel;
	public static String EditorChangeStartPollerCommandName;
	public static String EditorChangeStopPollerCommandName;
	public static String EditorServerPorts;
	public static String EditorServerPortsDescription;
	public static String EditorJNDIPort;
	public static String EditorWebPort;
	public static String EditorJMXRMIPort;
	public static String EditorAS7ManagementPort;
	public static String EditorAutomaticallyDetectPort;
	public static String EditorPortInvalid;
	public static String EditorChangeJNDICommandName;
	public static String EditorChangeWebCommandName;
	public static String EditorChangeJMXRMICommandName;
	public static String EditorChangeAS7ManagementCommandName;
	public static String EditorChangeUsernameCommandName;
	public static String EditorChangePasswordCommandName;
	public static String EditorCPD_Value;
	public static String EditorCPD_DefaultShellTitle;
	public static String EditorCPD_DefaultDescription;
	public static String EditorCPD_RestoreDefault;
	public static String EditorNoRuntimeSelected;
	public static String EditorLocalDeployment;
	public static String EditorRefreshViewer;
	public static String EditorDeploymentPageWarning;
	
	
	public static String ExploreUtils_Action_Text;
	public static String ExploreUtils_Description;

	public static String ShowInJMXConsole_Action_Text;
	public static String ShowInWebManagementConsole_Action_Text;
	public static String ShowInWelcomePage_Action_Text;
	public static String ShowInAction_Error;
	public static String ShowInAction_Error_Title;
	
	public static String Configure;
	public static String ConfigureRuntimeMarkerResolution_Description;

	public static String ConvertNodeToXPathDialog_DisplayString;
	public static String ConvertNodeToXPathDialog_ErrorMessage;
	public static String ConvertNodeToXPathDialog_KeyedAttributes;
	// misc
	public static String ServerSaveFailed;

	public static String StrippedServerWizardFragment_DeployFolderDoesNotExistStatusMessage;
	public static String StrippedServerWizardFragment_NameInUseStatusMessage;
	public static String StrippedServerWizardFragment_TemporaryDeployFolderDoesNotExistStatusMessage;
	// Launch Config
	public static String LaunchInvalidConfigChanged;
	public static String LaunchInvalidHostChanged;

	public static String LogLabelProvider_HoursMinutesAgo;
	public static String LogLabelProvider_MinutesSecondsAgo;
	public static String LogLabelProvider_PublishingEventType;
	public static String LogLabelProvider_SecondsAgo;
	public static String LogLabelProvider_StartupShutdownEventType;
	public static String LogLabelProvider_UnknownEventType;
	public static String LogAction_AlsoLogErrorsToErrorLog;
	public static String LogAction_DoNotLogErrorsToErrorLog;
	
	public static String RequiredCredentialsDialog_IgnoreButton;
	public static String RequiredCredentialsDialog_ShellText;
	
	public static String ChangeTimestampServerListDialog_Title;
	public static String ChangeTimestampServerListDialog_Message;

	public static String ServerAlreadyStartedDialog_Message;
	public static String ServerAlreadyStartedDialog_Title;
	public static String ServerAlreadyStartedDialog_Desc;
	public static String ServerAlreadyStartedDialog_Connect;
	public static String ServerAlreadyStartedDialog_Launch;

	public static String ServerJavaArgsSyncText;
	public static String ServerJavaArgsSyncDesc;
	
	public static String ServerContent_Type_root;
	public static String ServerContent_Type_attributes;
	public static String ServerContent_Type_core_service;
	public static String ServerContent_Type_deployment;
	public static String ServerContent_Type_extension;
	public static String ServerContent_Type_interface;
	public static String ServerContent_Type_path;
	public static String ServerContent_Type_socket_binding_group;
	public static String ServerContent_Type_subsystem;
	public static String ServerContent_Type_system_property;
	public static String ServerContent_Value_undefined;
	public static String ServerContent_Label_Attribute_Value;
    public static String ServerContent_Label_Loading;
    public static String ServerContent_Label_Not_Connected;
    public static String ServerContent_Label_Retrieval_Error;
    public static String ServerContent_Job_Title;

	static {
		NLS.initializeMessages(BUNDLE_NAME,
				Messages.class);
	}
	
	private Messages() {
	}
}
