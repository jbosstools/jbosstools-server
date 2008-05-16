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
package org.jboss.ide.eclipse.as.ui.views.server.util;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.jboss.tools.wst.server.ui.views.server.JBossServerView;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class ViewUtilityMethods {


	public static void activatePropertiesView(final IPropertySheetPage propertyPage) {
		// show properties view
		Runnable run = new Runnable() { public void run() {
			String propsId = "org.eclipse.ui.views.PropertySheet";
			try {
				IWorkbench work = PlatformUI.getWorkbench();
				IWorkbenchWindow window = work.getActiveWorkbenchWindow(); 
				if( !isPropertiesOnTop()) {
					window.getActivePage().showView(propsId);
					if( propertyPage != null ) {
						propertyPage.selectionChanged(JBossServerView.getDefault().getViewSite().getPart(), JBossServerView.getDefault().getExtensionFrame().getViewer().getSelection());
					}
				}
			} catch( PartInitException pie ) {
			}
		}};
		Display.getDefault().asyncExec(run);
	}
	
	protected static boolean isPropertiesOnTop() {
		String propsId = "org.eclipse.ui.views.PropertySheet";
		IWorkbench work = PlatformUI.getWorkbench();
		IWorkbenchWindow window = work.getActiveWorkbenchWindow(); 
		IWorkbenchPage page = window.getActivePage();
		IViewReference ref = window.getActivePage().findViewReference(propsId);
		if( ref == null ) return false; 
		IWorkbenchPart part = ref.getPart(false);
		return ( part != null && page.isPartVisible(part));
	}
}
