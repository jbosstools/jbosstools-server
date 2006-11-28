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
package org.jboss.ide.eclipse.as.ui.views.server.providers;

import java.util.ArrayList;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.model.EventLogModel;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogRoot;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.IEventLogListener;
import org.jboss.ide.eclipse.as.core.util.SimpleTreeItem;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.IEventLogLabelProvider;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.JBossServerViewExtension;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.PropertySheetFactory;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.ServerViewProvider;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.PropertySheetFactory.ISimplePropertiesHolder;

public class EventLogViewProvider extends JBossServerViewExtension implements IEventLogListener, ISimplePropertiesHolder {
	
	private ITreeContentProvider contentProvider;
	private LabelProvider labelProvider;
	
	private IEventLogLabelProvider[] labelProviderDelegates = null;
	
	private IPropertySheetPage propertyPage = null;

	private IServer input;
	private Action clearLogAction;
	

	public EventLogViewProvider() {
		contentProvider = new EventLogContentProvider();
		labelProvider = new EventLogLabelProvider();
		EventLogModel.getDefault().addListener(this);
		createActions();
	}
	
	protected void createActions() {
		clearLogAction = new Action() {
			public void run() {
				try {
					EventLogModel.getModel(input).clearEvents();
					refreshViewer();
				} catch( Exception e) {}
			}
		};
		clearLogAction.setText("Clear Event Log");
		//clearLogAction.setImageDescriptor(newImage)
	}
	
	public class EventLogContentProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			if( parentElement instanceof ServerViewProvider && input != null ) {
				boolean categorize = true; // TODO: get from preferences 
				if( categorize ) 
					return getRootCategories();
				return EventLogModel.getModel(input).getRoot().getChildren();
			}
			
			if( parentElement instanceof String ) {
				// get children only of this type
				SimpleTreeItem[] children = EventLogModel.getModel(input).getRoot().getChildren();
				ArrayList items = new ArrayList();
				for( int i = 0; i < children.length; i++ ) {
					if( children[i] instanceof EventLogTreeItem ) {
						String type = ((EventLogTreeItem)children[i]).getEventClass();
						if( type.equals(parentElement))
							items.add(children[i]);
					}
				}
				return (Object[]) items.toArray(new Object[items.size()]);
			}
			
			// just return the object's kids
			if( parentElement instanceof EventLogTreeItem ) {
				return ((EventLogTreeItem)parentElement).getChildren();
			}
			return new Object[0];
		}

		protected Object[] getRootCategories() {
			EventLogRoot root = EventLogModel.getModel(input).getRoot();
			ArrayList majorTypes = new ArrayList();
			SimpleTreeItem[] children = root.getChildren();
			for( int i = 0; i < children.length; i++ ) {
				if( children[i] instanceof EventLogTreeItem ) {
					String type = ((EventLogTreeItem)children[i]).getEventClass();
					if( !majorTypes.contains(type))
						majorTypes.add(type);
				}
			}
			return (String[]) majorTypes.toArray(new String[majorTypes.size()]);
		}
		
		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0 ? true : false;
		}

		public Object[] getElements(Object inputElement) {
			// Unused
			return null;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			input = (IServer)newInput;
		}
		
	}
	
	public class EventLogLabelProvider extends LabelProvider {
	    public Image getImage(Object element) {
	    	if( labelProviderDelegates == null )
	    		loadLabelProviderDelegates();
	    	
	    	if( element instanceof ServerViewProvider ) {
	    		return ((ServerViewProvider)element).getImage();
	    	}
	    	
	    	if( !(element instanceof EventLogTreeItem)) return null;
	    	EventLogTreeItem item = (EventLogTreeItem)element;
	    	
	    	for( int i = 0; i < labelProviderDelegates.length; i++ ) {
	    		if( labelProviderDelegates[i] != null 
	    				&& labelProviderDelegates[i].supports(item.getSpecificType())) {
	    			Image image = labelProviderDelegates[i].getImage((EventLogTreeItem)element);
	    			if( image != null ) return image;
	    		}
	    	}
	    	
	        return null;
	    }

	    public String getText(Object element) {
	    	if( labelProviderDelegates == null )
	    		loadLabelProviderDelegates();

	    	if( element instanceof ServerViewProvider ) {
	    		return ((ServerViewProvider)element).getName();
	    	}

	    	if( !(element instanceof EventLogTreeItem)) return element.toString();
	    	EventLogTreeItem item = (EventLogTreeItem)element;
	    	
	    	for( int i = 0; i < labelProviderDelegates.length; i++ ) {
	    		if( labelProviderDelegates[i] != null 
	    				&& labelProviderDelegates[i].supports(item.getSpecificType())) {
	    			String text = labelProviderDelegates[i].getText((EventLogTreeItem)element);
	    			if( text != null ) return text;
	    		}
	    	}

	        return element == null ? "" : element.toString();
	    }
	    
	    public void loadLabelProviderDelegates() {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IConfigurationElement[] elements = registry.getConfigurationElementsFor(JBossServerUIPlugin.PLUGIN_ID, "EventLogLabelProvider");
			labelProviderDelegates = new IEventLogLabelProvider[elements.length];
			for( int i = 0; i < elements.length; i++ ) {
				try {
					labelProviderDelegates[i] = (IEventLogLabelProvider)elements[i].createExecutableExtension("class");
				} catch( CoreException ce ) {}
			}
	    }
	}
	
	public void fillContextMenu(Shell shell, IMenuManager menu, Object selection) {
		menu.add(clearLogAction);
	}

	public ITreeContentProvider getContentProvider() {
		return contentProvider;
	}

	public LabelProvider getLabelProvider() {
		return labelProvider;
	}

	public IPropertySheetPage getPropertySheetPage() {
		if( propertyPage == null )
			propertyPage = PropertySheetFactory.createSimplePropertiesSheet(this);
		return propertyPage;
	}

	
	public void eventModelChanged(String serverId, EventLogTreeItem changed) {
		if( input != null && serverId.equals(input.getId())) {
			if(changed.getSpecificType().equals(EventLogModel.JBOSS_EVENT_ROOT_TYPE))
				refreshViewer();
			else
				refreshViewer(changed);
		}
	}

	public Properties getProperties(Object selected) {
    	if( !(selected instanceof EventLogTreeItem)) return new Properties();
    	EventLogTreeItem item = (EventLogTreeItem)selected;
    	
    	for( int i = 0; i < labelProviderDelegates.length; i++ ) {
    		if( labelProviderDelegates[i] != null 
    				&& labelProviderDelegates[i].supports(item.getSpecificType())) {
    			Properties props = labelProviderDelegates[i].getProperties((EventLogTreeItem)selected);
    			if( props != null ) return props;
    		}
    	}
    	return new Properties();
	}

}
