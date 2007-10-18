/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ClasspathUIPlugin extends AbstractUIPlugin {
	
	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.ide.eclipse.as.classpath.ui";
	
	// The shared instance
	private static ClasspathUIPlugin plugin;
	
	/**
	 * The constructor
	 */
	public ClasspathUIPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ClasspathUIPlugin getDefault() {
		return plugin;
	}

	public static void alert(String string) {
		MessageDialog dialog = new MessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				"EJB3 Tools - Alert", Display.getDefault().getSystemImage(SWT.ICON_INFORMATION), string,
				MessageDialog.INFORMATION, new String[]
	            {"OK",}, 0);

		dialog.setBlockOnOpen(true);
		dialog.open();
	}

	public static void error(String string) {
		MessageDialog dialog = new MessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				"EJB3 Tools - Error", Display.getDefault().getSystemImage(SWT.ICON_ERROR), string, MessageDialog.ERROR,
	            new String[]
	            {"OK",}, 0);

		dialog.setBlockOnOpen(true);
		dialog.open();
	}

	public static void warn(String string) {
		MessageDialog dialog = new MessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				"EJB3 Tools - Warning", Display.getDefault().getSystemImage(SWT.ICON_WARNING), string,
	            MessageDialog.WARNING, new String[]
	            {"OK",}, 0);

	      dialog.setBlockOnOpen(true);
	      dialog.open();
	}

}
