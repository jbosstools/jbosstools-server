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

public class Messages {
	/* Standard and re-usable */
	public static String browse;
	public static String serverName;

	/* Server and Runtime Wizard Fragments */
	public static String wf_BaseNameVersionReplacement;
	public static String wf_NameLabel;
	public static String wf_HomeDirLabel;
	public static String wf_JRELabel;
	public static String wf_ConfigLabel;
	public static String JBAS_version;
	public static String JBEAP_version;
	public static String rwf_Title1;
	public static String rwf_Title2;
	public static String rwf_Explanation;
	public static String rwf_BaseName;
	public static String rwf_NameInUse;
	public static String rwf_homeMissingFiles;
	public static String rwf_homeIncorrectVersion;
	public static String rwf_nameTextBlank;
	public static String rwf_homeDirBlank;
	public static String rwf_NoVMSelected;
	public static String rwf_jre6NotFound;
	public static String swf_Title;
	public static String swf_RuntimeInformation;
	public static String swf_AuthorizationDescription;
	public static String swf_Explanation;
	public static String swf_Explanation2;
	public static String swf_AuthenticationGroup;
	public static String swf_DeployGroup;
	public static String swf_Username;
	public static String swf_Password;
	public static String swf_BaseName;
	public static String swf_NameInUse;
	public static String swf_DeployEditorHeading;
	public static String swf_DeploymentDescription;
	public static String swf_DeployDirectory;
	public static String swf_TempDeployDirectory;
	public static String sswf_Title;
	public static String sswf_BaseName;

	public static String ServerDialogHeading;
	public static String credentials_warning;
	public static String credentials_save;


	/* Module extension properties */
	public static String ModulePropertyType;
	public static String ModulePropertyProject;
	public static String ModulePropertyModuleFactory;
	public static String ModulePropertyName;

	public static String RefreshViewerAction;
	public static String DisableCategoryAction;
	public static String EditLaunchConfigurationAction;
	public static String TwiddleServerAction;
	public static String CloneServerAction;

	/* Properties of view extenders (categories) */
	public static String ExtensionID;
	public static String ExtensionName;
	public static String ExtensionDescription;
	public static String ExtensionProviderClass;

	/* Properties of JBoss Servers in the view Properties */
	public static String ServerRuntimeVersion;
	public static String ServerHome;
	public static String ServerConfigurationName;
	public static String ServerDeployDir;

	public static String property;
	public static String value;

	public static String DeleteModuleText;
	public static String IncrementalPublishModuleText;
	public static String FullPublishModuleText;
	public static String DeleteModuleDescription;
	public static String PublishModuleDescription;

	public static String DeleteModuleConfirm;

	/* Action Delegate */

	public static String ActionDelegateStartServer;
	public static String ActionDelegateStopServer;
	public static String ActionDelegateDebugServer;
	public static String ActionDelegateNew;
	public static String ActionDelegateNewMBeanStubs;
	public static String ActionDelegateNewServer;
	public static String ActionDelegateMakeDeployable;
	public static String ActionDelegateMakeUndeployable;
	public static String ActionDelegateDeployableServersNotFoundTitle;
	public static String ActionDelegateDeployableServersNotFoundDesc;
	public static String ActionDelegateCannotPublish;
	public static String ActionDelegatePublishFailed;
	public static String ActionDelegateFileResourcesOnly;
	public static String ActionDelegateSelectServer;

	/* Console */
	public static String ConsoleResourceNotFound;
	public static String ConsoleCouldNotLocateInWorkspace;


	/* Twiddle Dialog */
	public static String TwiddleDialog;
	public static String TwiddleDialogExecute;
	public static String TwiddleDialogDone;
	public static String TwiddleDialogArguments;
	public static String TwiddleDialogTutorial;

	/* XPath Dialog */
	public static String XPathNewCategory;
	public static String XPathNewCategoryNameInUse;
	public static String XPathCategoryName;
	public static String XPathNewXpath;
	public static String XPathNameEmpty;
	public static String XPathNameInUse;
	public static String XPathColumnLocation;
	public static String XPathColumnAttributeVals;

	public static String XPathName;
	public static String XPathPattern;
	public static String XPathAttribute;

	/* Preference Page */
	public static String PreferencePageServerTimeouts;
	public static String PreferencePageStartTimeouts;
	public static String PreferencePageStopTimeouts;
	public static String PreferencePageUponTimeout;
	public static String PreferencePageUponTimeoutAbort;
	public static String PreferencePageUponTimeoutIgnore;

	/* View Preference Page */
	public static String ViewPreferencePageName;
	public static String ViewPreferencePageEnabled;
	public static String ViewPreferencePageWeight;
	public static String ViewPreferencePageDescription;
	public static String ViewExtensionEnablementDescription;
	public static String ViewExtensionPreferenceDescription;

	public static String DescriptorXPathRemoveCategory;
	public static String DescriptorXPathRemoveCategoryDesc;
	public static String DescriptorXPathNewXPath;
	public static String DescriptorXPathEditXPath;
	public static String DescriptorXPathDeleteXPath;
	public static String DescriptorXPathAttributeValue;
	public static String DescriptorXPathXPathXML;
	public static String DescriptorXPathAttributeKeyValue;
	public static String DescriptorXPathDescriptorValues;
	public static String DescriptorXPathNameLocation;
	public static String DescriptorXPathServerName;
	public static String DescriptorXPathPortCategory;
	public static String DescriptorXPathSimple;
	public static String DescriptorXPathComplex;
	public static String DescriptorXPathMatch;



	/* Editor Strings */
	public static String EditorUseWorkspaceMetadata;
	public static String EditorUseServersDeployFolder;
	public static String EditorUseCustomDeployFolder;
	public static String EditorSetDeployLabel;
	public static String EditorSetTempDeployLabel;
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
	public static String EditorAutomaticallyDetectPort;
	public static String EditorPortInvalid;
	public static String EditorChangeJNDICommandName;
	public static String EditorChangeWebCommandName;
	public static String EditorChangeUsernameCommandName;
	public static String EditorChangePasswordCommandName;
	public static String EditorCPD_Value;
	public static String EditorCPD_DefaultShellTitle;
	public static String EditorCPD_DefaultDescription;
	public static String EditorCPD_RestoreDefault;



	public static String Configure;

	// misc
	public static String ServerSaveFailed;

	// Launch Config
	public static String LaunchInvalidConfigChanged;
	public static String LaunchInvalidHostChanged;

	static {
		NLS.initializeMessages(JBossServerUIPlugin.PLUGIN_ID + ".Messages",
				Messages.class);
	}
}
