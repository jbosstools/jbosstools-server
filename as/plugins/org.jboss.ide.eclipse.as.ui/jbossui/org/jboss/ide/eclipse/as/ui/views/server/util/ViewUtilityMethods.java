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
import org.jboss.ide.eclipse.as.ui.views.server.JBossServerView;

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
