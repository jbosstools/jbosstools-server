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
	public static String createWizardTitle;
	public static String runtimeInformation;
	public static String createRuntimeWizardTitle;
	public static String createWizardDescription;
	public static String wizardFragmentNameLabel;
	public static String wizardFragmentHomeDirLabel;
	public static String wizardFragmentJRELabel;
	public static String installedJREs;
	public static String wizardFragmentConfigLabel;
	public static String runtimeWizardFragmentExplanation;
	public static String serverWizardFragmentExplanation;
	public static String serverWizardFragmentExplanation2;
	public static String serverNameInUse;
	public static String runtimeNameInUse;
	public static String invalidDirectory;
	public static String nameTextBlank;
	public static String homeDirBlank;
	public static String browse;
	public static String deployDirectory;
	public static String ServerDialogHeading;
	
	/* Module extension properties */
	public static String ModulePropertyType;
	public static String ModulePropertyProject;
	public static String ModulePropertyModuleFactory;
	
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
	public static String PublishModuleText;
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
	
	/* Launch Group */
	public static String LaunchGroupStartArgs;
	public static String LaunchGroupStopArgs;
	public static String LaunchGroupTwiddleArgs;
	public static String LaunchGroupConfigChanged;
	
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
	public static String XPathColumnRawXML;

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
	
	/* Upgrade Wizard to 2.0.0 */
	public static String UpgradeWizardTitle;
	public static String UpgradeWizardDescription;
	public static String UpgradeWizardNoConvertableConfigs;
	public static String UpgradeWizardDetails;
	public static String UpgradeWizardLaunchConfigName;
	public static String UpgradeWizardLaunchHomeDir;
	public static String UpgradeWizardLaunchJBossConfig;
	public static String UpgradeWizardLaunchStartArgs;
	public static String UpgradeWizardLaunchShutdownArgs;
	public static String UpgradeWizardLaunchJRE;
	public static String UpgradeWizardLaunchConvert;
	public static String UpgradeWizardLaunchConfigName2;
	public static String UpgradeWizardLaunchRuntimeName;
	public static String UpgradeWizardLaunchServerName;
	public static String UpgradeWizardLaunchMonitorMainTask;
	public static String UpgradeWizardLaunchMonitorConverting;
	public static String UpgradeWizardLaunchRuntimeNamesInUse;
	public static String UpgradeWizardLaunchServerNamesInUse;
	
	
	/* Inactive Categories */
	public static String InactiveCategoriesReEnable;
	
	
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
	
	
	static {
		NLS.initializeMessages(JBossServerUIPlugin.PLUGIN_ID + ".Messages", Messages.class);
	}
}
