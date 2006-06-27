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
	public static String EventLogCategory;
	public static String ModulesCategory;
	public static String createWizardTitle;
	public static String createWizardDescription;
	public static String wizardFragmentNameLabel;
	public static String wizardFragmentHomeDirLabel;
	public static String wizardFragmentJRELabel;
	public static String installedJREs;
	public static String wizardFragmentConfigLabel;
	public static String serverNameInUse;
	public static String invalidDirectory;
	public static String nameTextBlank;
	public static String homeDirBlank;
	public static String browse;

	public static String ServerDialogHeading;
	
	/* Module extension properties */
	public static String ModulePropertyType;
	public static String ModulePropertyProject;
	public static String ModulePropertyModuleFactory;
	
	public static String RefreshViewerAction;
	public static String DisableCategoryAction;
	
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
	
	
	public static String HidePropertiesAction;
	public static String HideTextAction;
	public static String HideLowerFrameAction;
	
	static {
		NLS.initializeMessages(JBossServerUIPlugin.PLUGIN_ID + ".Messages", Messages.class);
	}
}
