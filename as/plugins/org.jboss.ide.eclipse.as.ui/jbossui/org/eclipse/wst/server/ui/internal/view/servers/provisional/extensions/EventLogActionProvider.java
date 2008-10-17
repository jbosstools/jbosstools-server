package org.eclipse.wst.server.ui.internal.view.servers.provisional.extensions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.ServerEventModel;
import org.jboss.ide.eclipse.as.ui.dialogs.ShowStackTraceDialog;

public class EventLogActionProvider extends CommonActionProvider {
	private Action clearLogAction;
	private Action showStackTraceAction;
	private ICommonActionExtensionSite actionSite;
	public EventLogActionProvider() {
		super();
	}
	
	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		this.actionSite = aSite;
		createActions();
	}
	public void fillContextMenu(IMenuManager menu) {
		ICommonViewerSite site = actionSite.getViewSite();
		IStructuredSelection selection = null;
		if( site instanceof ICommonViewerWorkbenchSite ) {
			ICommonViewerWorkbenchSite wsSite = (ICommonViewerWorkbenchSite)site;
			selection = (IStructuredSelection) wsSite.getSelectionProvider().getSelection();
			Object first = selection.getFirstElement();
			if( first != null && first instanceof ServerEventModel) {
				menu.add(clearLogAction);
			}
			if( first != null && first instanceof EventLogTreeItem && 
					(((EventLogTreeItem)first).getEventClass().equals(EventLogModel.EVENT_TYPE_EXCEPTION) ||
					((EventLogTreeItem)first).getSpecificType().equals(EventLogModel.EVENT_TYPE_EXCEPTION))) {
				menu.add(showStackTraceAction);
			}
		}
	}
	
	
	protected void createActions() {
		clearLogAction = new Action() {
			public void run() {
				IStructuredSelection selection = (IStructuredSelection)actionSite.getStructuredViewer().getSelection();
				if(selection.getFirstElement() != null && selection.getFirstElement() instanceof ServerEventModel) {
					((ServerEventModel)selection.getFirstElement()).clearEvents();
					actionSite.getStructuredViewer().refresh(((ServerEventModel)selection.getFirstElement()));
				}
			}
		};
		clearLogAction.setText("Clear Event Log");
		
		showStackTraceAction = new Action() {
			public void run() {
				IStructuredSelection selection = (IStructuredSelection)actionSite.getStructuredViewer().getSelection();
				if( selection.getFirstElement() != null && selection.getFirstElement() instanceof EventLogTreeItem ) {
					EventLogTreeItem item = (EventLogTreeItem)selection.getFirstElement();
					ShowStackTraceDialog dialog = new ShowStackTraceDialog(new Shell(), item);
					dialog.open();
				}
			}
		};
		showStackTraceAction.setText("See Exception Details");
	}
}
