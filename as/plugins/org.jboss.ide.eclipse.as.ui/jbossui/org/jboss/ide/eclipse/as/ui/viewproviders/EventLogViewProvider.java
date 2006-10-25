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

package org.jboss.ide.eclipse.as.ui.viewproviders;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.ui.internal.provisional.UIDecoratorManager;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.client.TwiddleLauncher.TwiddleLogEvent;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.ExceptionLogEvent;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.ProcessLogEvent;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.ProcessLogEventRoot;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ConsoleLogEvent;
import org.jboss.ide.eclipse.as.core.server.IServerLogListener;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.JBossServerBehavior.PublishLogEvent;
import org.jboss.ide.eclipse.as.core.server.ServerStateChecker.StateCheckerLogEvent;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin.ServerViewProvider;
import org.jboss.ide.eclipse.as.ui.viewproviders.PropertySheetFactory.PropertiesTextSashPropertiesPage;
import org.jboss.ide.eclipse.as.ui.views.JBossServerView;
import org.jboss.ide.eclipse.as.ui.views.JBossServerTableViewer.ContentWrapper;

public class EventLogViewProvider extends JBossServerViewExtension implements IServerLogListener {

	protected EventLogLabelProvider categoryLabelProvider;
	protected EventLogContentProvider categoryContentProvider;
	protected PropertiesTextSashPropertiesPage propertiesSheet;
	
	public EventLogViewProvider() {
		categoryLabelProvider = new EventLogLabelProvider();
		categoryContentProvider = new EventLogContentProvider();
	}
	
	
	public void enable() {
		ServerProcessModel.getDefault().addLogListener(this);
	}
	
	public void disable() {
		ServerProcessModel.getDefault().removeLogListener(this);
	}
	
	public void fillContextMenu(Shell shell, IMenuManager menu, Object selection) {
	}

	public ITreeContentProvider getContentProvider() {
		return categoryContentProvider;
	}

	public LabelProvider getLabelProvider() {
		return categoryLabelProvider;
	}

	protected class EventLogLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			
			if( obj instanceof StateCheckerLogEvent) {
				StateCheckerLogEvent event = (StateCheckerLogEvent)obj;

				if( event.getEventType() == StateCheckerLogEvent.BEFORE) {
					boolean expected = event.getExpectedState();
					return (expected == true ? Messages.EventLogStartingServer : Messages.EventLogStoppingServer);
				}
				
				if( event.getEventType() == StateCheckerLogEvent.AFTER ) {
					boolean current = (event.getCurrentState() == StateCheckerLogEvent.SERVER_UP);
					return current ? Messages.EventLogServerUp : Messages.EventLogServerDown;
				}
				
				if( event.getEventType() == StateCheckerLogEvent.DURING) {
					String ret = Messages.EventLogTwiddleLaunchServerStatePrefix;
					if( event.getCurrentState() == StateCheckerLogEvent.SERVER_STARTING ) ret += Messages.EventLogStillStarting;
					if( event.getCurrentState() == StateCheckerLogEvent.SERVER_STOPPING ) ret += Messages.EventLogStillStopping;
					if( event.getCurrentState() == StateCheckerLogEvent.SERVER_UP ) ret += Messages.EventLogUp;
					if( event.getCurrentState() == StateCheckerLogEvent.SERVER_DOWN ) ret += Messages.EventLogDown;
					return ret;
				}
				if( event.getEventType() == StateCheckerLogEvent.SERVER_STATE_CHANGE_CANCELED) {
					String ret = Messages.EventLogServerActionCanceled;
					return ret;
				}
				if( event.getEventType() == StateCheckerLogEvent.SERVER_STATE_CHANGE_TIMEOUT) {
					String ret = Messages.EventLogTimeoutReached;
					return ret;
				}
			}
			
			if( obj instanceof ConsoleLogEvent) {
				return Messages.EventLogConsoleOutput;
			}
			
			if( obj instanceof PublishLogEvent ) {
				PublishLogEvent publishEvent = ((PublishLogEvent)obj);
				if( publishEvent.getEventType() == PublishLogEvent.ROOT) {
					return Messages.EventLogPublishEvent;
				}
				if( publishEvent.getEventType() == PublishLogEvent.PUBLISH) {
					return Messages.EventLogPublishingToServer + publishEvent.getModuleName();
				}
				if( publishEvent.getEventType() == PublishLogEvent.UNPUBLISH) {
					return Messages.EventLogPublishRemoveFromServer + publishEvent.getModuleName();
				}
				return Messages.EventLogPublishUnknownEvent;
			}
			if( obj instanceof ExceptionLogEvent ) {
				ExceptionLogEvent event = ((ExceptionLogEvent)obj);
				return event.getException().getLocalizedMessage();
			}
			if( obj instanceof ProcessLogEvent ) {
				ProcessLogEvent event = ((ProcessLogEvent)obj);
				SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss.S");
				String eventToString;
				if( event.getEventType() == ProcessLogEvent.UNKNOWN ) {
					eventToString = Messages.EventLogUnknownEvent;
				} else {
					eventToString = event.toString();
				}
				return eventToString + "   " + format.format(new Date(event.getDate()));
			}
			return obj.toString();
		}
		public Image getImage(Object obj) {
			try {
			ProcessLogEvent event = (ProcessLogEvent)obj;
			IServer server = event.getRoot().getServer();
			IServerType serverType = server.getServerType();

			if( obj instanceof StateCheckerLogEvent  ) {
				StateCheckerLogEvent scle = (StateCheckerLogEvent)obj;
				if( scle.getEventType() == StateCheckerLogEvent.BEFORE) {
					boolean expected = scle.getExpectedState();
					if( expected ) return getStateImage(serverType, IServer.STATE_STARTED, server.getMode());
					return getStateImage(serverType, IServer.STATE_STOPPED, server.getMode());
				}

				if( scle.getCurrentState() == StateCheckerLogEvent.SERVER_STARTING) {
					return getStateImage(serverType, IServer.STATE_STARTING, server.getMode());
				}
				if( scle.getCurrentState() == StateCheckerLogEvent.SERVER_STOPPING) {
					return getStateImage(serverType, IServer.STATE_STOPPING, server.getMode());
				}
				if( scle.getCurrentState() == StateCheckerLogEvent.SERVER_UP) {
					return getStateImage(serverType, IServer.STATE_STARTED, server.getMode());					
				}
				if( scle.getCurrentState() == StateCheckerLogEvent.SERVER_DOWN) {
					return getStateImage(serverType, IServer.STATE_STOPPED, server.getMode());					
				}
				if( scle.getCurrentState() == StateCheckerLogEvent.SERVER_STATE_CHANGE_CANCELED) {
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);					
				}
				if( event.getEventType() == StateCheckerLogEvent.SERVER_STATE_CHANGE_TIMEOUT) {
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);					
				}
			}
			if( obj instanceof ConsoleLogEvent ) {
				return JBossServerUISharedImages.getImage(JBossServerUISharedImages.CONSOLE_IMAGE);
			}
			if( obj instanceof PublishLogEvent) {
				if( event.getEventType() == PublishLogEvent.ROOT) {
					return JBossServerUISharedImages.getImage(JBossServerUISharedImages.PUBLISH_IMAGE);
				}
				if( event.getEventType() == PublishLogEvent.PUBLISH) {
					return JBossServerUISharedImages.getImage(JBossServerUISharedImages.PUBLISH_IMAGE);
				}
				if( event.getEventType() == PublishLogEvent.UNPUBLISH) {
					return JBossServerUISharedImages.getImage(JBossServerUISharedImages.UNPUBLISH_IMAGE);
				}
			}
			if( obj instanceof ExceptionLogEvent ) {
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
			}
			if( obj instanceof TwiddleLogEvent ) {
				return JBossServerUISharedImages.getImage(JBossServerUISharedImages.TWIDDLE_IMAGE);
			}

			if( event.getEventType() == ProcessLogEvent.UNKNOWN ) {
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
			}

			
			} catch( Exception e ) {
				e.printStackTrace();
				ProcessLogEvent event = (ProcessLogEvent)obj;
				event.getRoot();

			}

			
			return null;
		}
		
		protected Image getStateImage(IServerType serverType, int state, String mode) {
			return UIDecoratorManager.getUIDecorator(serverType).getStateImage(state, mode, 0);
		}

	}
	
	protected class EventLogContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		protected ProcessLogEventRoot input;
		public Object[] getElements(Object inputElement) {
			return new Object[0];
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if( newInput instanceof IServer && JBossServerCore.getServer((IServer)newInput) != null ) {
				JBossServer s = JBossServerCore.getServer((IServer)newInput);
				input = s.getProcessModel().getEventLog();
			}
		}

		public Object[] getChildren(Object parentElement) {
			if( parentElement instanceof ServerViewProvider ) {
				return input.getChildren();
			}
			if( parentElement instanceof ProcessLogEvent ) {
				return ((ProcessLogEvent)parentElement).getChildren();
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			if( element instanceof ProcessLogEventRoot ) {
				return provider;
			}
			if( element instanceof ProcessLogEvent ) {
				return ((ProcessLogEvent)element).getParent();
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0 ? true : false;
		}
	}


	public void logChanged(ProcessLogEvent event) {
		try {
			IServer s = JBossServerView.getDefault().getSelectedServer();
			if( event.getRoot().getServer().equals(s))
				refreshViewer();
		} catch( Exception e ) {
		}
	}


	public IPropertySheetPage getPropertySheetPage() {
		if( propertiesSheet == null ) {
			createPropertiesSheet();
		}
		return propertiesSheet;
	}
	
	protected void createPropertiesSheet() {
		try {
			propertiesSheet = new EventLogPropertiesSheetPage();
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	protected class EventLogPropertiesProvider extends LabelProvider 
		implements ITableLabelProvider, ITreeContentProvider {
	
		public static final int SHOW_TEXT = 1;
		public static final int SHOW_TREE = 2;
		
		public final static String STATECHECKER_PROPERTIES = "_STATECHECKER_PROPERTIES_";
		
		
		protected Object newInput;
		
		public int getContentType(Object selected) {
			if( selected instanceof ConsoleLogEvent ) return SHOW_TEXT;
			if( selected instanceof TwiddleLogEvent ) return SHOW_TREE;
			if( selected instanceof StateCheckerLogEvent ) return SHOW_TREE;
			return SHOW_TREE ;
		}
		
		public String getTextContent(Object selected) {
			if( selected instanceof ConsoleLogEvent ) return ((ConsoleLogEvent)selected).toString();
			if( selected instanceof StateCheckerLogEvent) return ((StateCheckerLogEvent)selected).getTwiddleLogEvent().getOut();
			return "";
		}
				
		public Object[] getChildren(Object parentElement) {
			// top level elements for the input
			if( newInput instanceof StateCheckerLogEvent ) 
				return getChildren_((StateCheckerLogEvent)newInput, parentElement);
			
			if( newInput instanceof PublishLogEvent ) {
				if( newInput == parentElement ) {
					Set s = ((PublishLogEvent)parentElement).getProperties().keySet();
					return (Object[]) s.toArray(new Object[s.size()]);
				}
					
					
				PublishLogEvent pubEvent = (PublishLogEvent)newInput;
			}
			
			return new Object[0];
		}

		protected Object[] getChildren_(StateCheckerLogEvent input, Object parent) {
			if( input == parent ) return input.getAvailableProperties();
			
			return new Object[] { };
		}
		
		protected String getColumnText_(StateCheckerLogEvent input, Object element, int columnIndex) {
			try {
				if( columnIndex == 0 ) return element.toString();
				if( columnIndex == 1 ) return input.getProperty(element).toString();
			} catch( Exception e) {}
			return "";
		}
		
		
		public String getColumnText(Object element, int columnIndex) {
			if( newInput instanceof StateCheckerLogEvent) return getColumnText_((StateCheckerLogEvent)newInput, element, columnIndex);
			
			if( newInput instanceof PublishLogEvent ) {
				
				try {
					if( columnIndex == 0 ) return element.toString();
					if( columnIndex == 1 ) return ((ProcessLogEvent)newInput).getProperty(element).toString();
				} catch( Exception e) {}
				return "";
			}
			
			return element.toString();
		}

		
		
		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0 ? true : false;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.newInput = newInput;
		}
		

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}


		
	}
	
	public class Pair {
		public Object fFirst = null;
		public Object fSecond = null;
		public Pair(Object first, Object second) {
			fFirst = first;
			fSecond = second;
		}
	}
	
	protected class EventLogPropertiesSheetPage extends PropertiesTextSashPropertiesPage {

		
		public void createControl(Composite parent) {
			super.createControl(parent);
			EventLogPropertiesProvider p = new EventLogPropertiesProvider();
			setContentProvider(p);
			setLabelProvider(p);
		}

		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if( selection instanceof IStructuredSelection ) {
				Object selectedElement = ((IStructuredSelection)selection).getFirstElement();
				if( selectedElement instanceof ContentWrapper) {
					selectedElement = ((ContentWrapper)selectedElement).getElement();
				}
				
				int type = getContentProvider().getContentType(selectedElement);
				
				// weight
				if( type == (EventLogPropertiesProvider.SHOW_TEXT | 
						EventLogPropertiesProvider.SHOW_TREE )) {

					setSashWeights(new int[] {50, 50});
					getText().setText(getContentProvider().getTextContent(selectedElement));
					this.propertiesViewer.setInput(selectedElement);
					
				} else if( type == EventLogPropertiesProvider.SHOW_TEXT) {
					showTextOnly();
					getText().setText(getContentProvider().getTextContent(selectedElement));
					
				} else if( type == EventLogPropertiesProvider.SHOW_TREE ) {
					showPropertiesOnly();
					this.propertiesViewer.setInput(selectedElement);
				}
				
			}
		}
		
		public EventLogPropertiesProvider getContentProvider() {
			return ((EventLogPropertiesProvider)propertiesViewer.getContentProvider());
		}
	}

}
