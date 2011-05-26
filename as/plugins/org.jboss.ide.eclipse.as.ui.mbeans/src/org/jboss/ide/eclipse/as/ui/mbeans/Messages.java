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
package org.jboss.ide.eclipse.as.ui.mbeans;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.jboss.ide.eclipse.as.ui.mbeans.Messages"; //$NON-NLS-1$

	public static String ConvertNodeToXPathDialogOutlineMenuItemProvider_AddToXPathsAction;

	public static String ConvertNodeToXPathDialogOutlineMenuItemProvider_AddToXPathsDescription;

	/* XML service editor */
	public static String ServiceXMLAddAttributeTags;

	public static String ServiceXMLCorrectionAssistantProvider_could_not_access_mbean;
	public static String ServiceXMLEditorConfiguration_OccurrenceNameWithAttributes;

	public static String ServiceXMLEditorConfiguration_UnableToDisplayInEditor;

	/* MBean Wizard */

	public static String NewMBeanInterface;
	public static String NewMBeanInterfaceDesc;
	public static String NewMBeanName;
	public static String NewMBeanInterfaceName;
	public static String NewMBeanClass;
	public static String MBeanClassDescription;
	public static String MBeanServiceXML;

	public static String NewSessionBeanWizardPage_could_not_create_type;
	public static String NewSessionBeanWizardPage_could_not_update_remoteinterface_value;
	public static String NewMBeanWizard_could_not_create_files;
	public static String NewSessionBeanWizard_WindowTitle;
	public static String NewSessionBeanWizardTitle;
	public static String NewSessionBeanWizardBeanTypeLabel;
	public static String NewSessionBeanWizardStatefulButtonLabel;
	public static String NewSessionBeanWizardStatelessButtonLabel;
	public static String NewSessionBeanWizardBeanPackageLabel;
	public static String NewSessionBeanWizardBeanNameLabel;
	public static String NewSessionBeanWizardBeanClassNameLabel;
	public static String NewSessionBeanWizardUseCustomInterfacePackageButtonLabel;
	public static String NewSessionBeanWizardRemoteInterfaceNameLabel;
	public static String NewSessionBeanWizardMessage;
	public static String NewSessionBeanWizardDescription;
	public static String NewMBeanWizard_CreatingTaskName;
	public static String NewMBeanWizard_WindowTitle;
	public static String NewMBeanWizard_XMLTaskName;
	public static String NewMessageBeanWizardMessage;
	public static String NewMessageBeanWizardDescription;

	public static String NewMessageDrivenBeanWizardPage_Name;
	
	public static String ServiceXMLEditorUtil_could_not_get_methods;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
