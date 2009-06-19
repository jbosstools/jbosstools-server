/**
 * JBoss by Red Hat
 * Copyright 2006-2009, Red Hat Middleware, LLC, and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.classpath.ui;

import org.eclipse.osgi.util.NLS;

public class Messages  extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.ide.eclipse.as.classpath.ui.Messages"; //$NON-NLS-1$
	public static String ClasspathUIPlugin_ALERT;
	public static String ClasspathUIPlugin_ERROR;
	public static String ClasspathUIPlugin_OK;
	public static String ClasspathUIPlugin_WARNING;
	public static String DirectoryLibraryPage_Description;
	public static String DirectoryLibraryPage_LabelProject;
	public static String DirectoryLibraryPage_WebProjects;
	public static String DirectoryLibraryPage_Title;
	public static String jeeClasspathAdding;
	public static String jeeClasspathBody;
	public static String jeeClasspathDescription;
	public static String ejb3ClasspathPageTitle;
	public static String ejb3ClasspathPageDescription;
	public static String JBossEJB3LibrariesPage_ConfigurationDoesNotContainEJB3Libraries;
	public static String JBossSelectionPage_ButtonText;
	public static String JBossSelectionPage_Name;
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	private Messages() {
	}
}
