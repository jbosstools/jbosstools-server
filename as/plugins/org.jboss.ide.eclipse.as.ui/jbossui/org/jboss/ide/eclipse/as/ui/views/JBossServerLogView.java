package org.jboss.ide.eclipse.as.ui.views;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.ui.internal.ImageResource;
import org.eclipse.wst.server.ui.internal.provisional.UIDecoratorManager;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.ProcessLogEvent;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.ProcessLogEventRoot;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ServerProcessModelEntity;
import org.jboss.ide.eclipse.as.core.server.IServerLogListener;
import org.jboss.ide.eclipse.as.core.util.ASDebug;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;


public class JBossServerLogView extends ViewPart implements IServerLogListener {
	private TreeViewer viewer;
	private Action clearAllLogsAction, removeFromLog;
	private Action doubleClickAction;

	class ViewContentProvider implements IStructuredContentProvider, 
										   ITreeContentProvider {

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			if (parent.equals(ServerProcessModel.getDefault())) {
				// return the servers
				try {
				} catch( Exception e ) {
					e.printStackTrace();
				}
				return ((ServerProcessModel)parent).getModels();
			}
			return getChildren(parent);
		}
		public Object getParent(Object child) {
			if (child instanceof ProcessLogEventRoot) {
				return ((ProcessLogEventRoot)child).getProcessModel();
			}
			
			if( child instanceof ProcessLogEvent ) {
				return ((ProcessLogEvent)child).getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			if (parent instanceof ServerProcessModelEntity) {
				return ((ServerProcessModelEntity)parent).getEventLog().getChildren();
			}
			if( parent instanceof ProcessLogEvent ) {
				return ((ProcessLogEvent)parent).getChildren();
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			return getChildren(parent).length == 0 ? false : true;
		}
	}

	
	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			if( obj instanceof ProcessLogEvent ) {
				ProcessLogEvent event = ((ProcessLogEvent)obj);
				SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss.S");
				return event.getText() + "   " + format.format(new Date(event.getDate()));
			}
			if( obj instanceof ServerProcessModelEntity) {
				String id = ((ServerProcessModelEntity)obj).getServerID(); 
				IServer server = ServerCore.findServer(id);
				Image image = ImageResource.getImage(server.getServerType().getId());
				return server == null ? "Server: " + id : server.getName();
			}
			return obj.toString();
		}
		public Image getImage(Object obj) {
			if( obj instanceof ServerProcessModelEntity) {
				String id = ((ServerProcessModelEntity)obj).getServerID(); 
				IServer server = ServerCore.findServer(id);
				Image image = ImageResource.getImage(server.getServerType().getId());
				return image;
			}

			ProcessLogEvent event = (ProcessLogEvent)obj;
			IServer server = event.getRoot().getServer();
			IServerType serverType = server.getServerType();

			if( obj instanceof ProcessLogEvent ) {
				if( event.getEventType() == ProcessLogEvent.SERVER_STARTING) {
					return getStateImage(serverType, IServer.STATE_STARTING, server.getMode());
				}
				if( event.getEventType() == ProcessLogEvent.SERVER_STOPPING) {
					return getStateImage(serverType, IServer.STATE_STOPPING, server.getMode());
				}
				if( event.getEventType() == ProcessLogEvent.SERVER_UP) {
					return getStateImage(serverType, IServer.STATE_STARTED, server.getMode());					
				}
				if( event.getEventType() == ProcessLogEvent.SERVER_DOWN) {
					return getStateImage(serverType, IServer.STATE_STOPPED, server.getMode());					
				}
				if( event.getEventType() == ProcessLogEvent.SERVER_CONSOLE) {
					return JBossServerUISharedImages.getImage(JBossServerUISharedImages.CONSOLE_IMAGE);
				}
				if( event.getEventType() == ProcessLogEvent.TWIDDLE) {
					return JBossServerUISharedImages.getImage(JBossServerUISharedImages.TWIDDLE_IMAGE);
				}
				if( event.getEventType() == ProcessLogEvent.SERVER_PUBLISH) {
					return JBossServerUISharedImages.getImage(JBossServerUISharedImages.PUBLISH_IMAGE);
				}
				if( event.getEventType() == ProcessLogEvent.SERVER_UNPUBLISH) {
					return JBossServerUISharedImages.getImage(JBossServerUISharedImages.UNPUBLISH_IMAGE);
				}
				
				
			}
			
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
		
		protected Image getStateImage(IServerType serverType, int state, String mode) {
			return UIDecoratorManager.getUIDecorator(serverType).getStateImage(state, mode, 0);
		}

	}

	/**
	 * The constructor.
	 */
	public JBossServerLogView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(ServerProcessModel.getDefault());
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		ServerProcessModel.getDefault().addLogListener(this);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				JBossServerLogView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		//fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	
	/**
	 * Menu is about to show so we can customize the context 
	 * menu based on what is selected. I think.
	 * @param manager
	 */
	private void fillContextMenu(IMenuManager manager) {
		ISelection sel = viewer.getSelection();
		if( sel instanceof IStructuredSelection ) {
			IStructuredSelection sel2 = (IStructuredSelection)sel;
			if( sel2.size() > 0 ) {
				manager.add(removeFromLog);
			}
		}
		manager.add(clearAllLogsAction);
		manager.add(new Separator());
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(clearAllLogsAction);
		manager.add(new Separator());
	}

	private void makeActions() {
		clearAllLogsAction = new Action() {
			public void run() {
				ServerProcessModelEntity[] ent = ServerProcessModel.getDefault().getModels();
				for( int i = 0; i < ent.length; i++ ) {
					ent[i].getEventLog().deleteChildren();
				}
				logChanged(null);
			}
		};
		clearAllLogsAction.setText("Clear All JBoss Server Logs");
		clearAllLogsAction.setToolTipText("Clear All Event Logs");
		clearAllLogsAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		
		
		removeFromLog = new Action() {
			public void run() {
				boolean changed = false;
				ISelection selection = viewer.getSelection();
				Iterator i = ((IStructuredSelection)selection).iterator();
				while(i.hasNext()) {
					Object o = i.next();
					if( o instanceof ServerProcessModelEntity) {
						((ServerProcessModelEntity)o).getEventLog().deleteChildren();
						changed = true;
					} else if( o instanceof ProcessLogEvent ) {
						((ProcessLogEvent)o).getParent().deleteChild((ProcessLogEvent)o);
						changed = true;
					}
					
					
					if( changed ) {
						logChanged(null);
					}
				}
			}
		};
		removeFromLog.setText("Remove From Log");
		removeFromLog.setText("Remove From Log");
		removeFromLog.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				if( obj instanceof ProcessLogEvent) {
					ProcessLogEvent e = (ProcessLogEvent)obj;
					showMessage("Object is " + e.getText() + " with type " + e.getEventType());
				} else {
					showMessage("Double-click detected on "+obj.toString());
				}
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Sample View",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void logChanged(ServerProcessModelEntity ent) {
		final ServerProcessModelEntity e2 = ent;
		getSite().getShell().getDisplay().asyncExec(new Runnable() {

			public void run() {
				Object[] expanded = viewer.getExpandedElements();
				ISelection selected = viewer.getSelection();
				//viewer.setInput(ServerProcessModel.getDefault());
				viewer.refresh();
				viewer.setExpandedElements(expanded);
				viewer.setSelection(selected);
			} 
			
			
		} );

	}
}