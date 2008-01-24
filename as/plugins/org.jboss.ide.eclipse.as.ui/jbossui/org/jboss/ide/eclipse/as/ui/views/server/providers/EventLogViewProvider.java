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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.EventLogRoot;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.IEventLogListener;
import org.jboss.ide.eclipse.as.core.util.SimpleTreeItem;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.dialogs.ShowStackTraceDialog;
import org.jboss.ide.eclipse.as.ui.preferencepages.ViewProviderPreferenceComposite;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.IEventLogLabelProvider;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.JBossServerViewExtension;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.PropertySheetFactory;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.ServerViewProvider;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.PropertySheetFactory.ISimplePropertiesHolder;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class EventLogViewProvider extends JBossServerViewExtension implements IEventLogListener, ISimplePropertiesHolder {
	
	public static final String SHOW_TIMESTAMP = "org.jboss.ide.eclipse.as.ui.views.server.providers.EventLogViewProvider.showTimestamp";
	public static final String GROUP_BY_CATEGORY = "org.jboss.ide.eclipse.as.ui.views.server.providers.EventLogViewProvider.groupByCategory";
	public static final String EVENT_ON_TOP = "org.jboss.ide.eclipse.as.ui.views.server.providers.EventLogViewProvider.eventOnTop";
	public static final int NEWEST_ON_TOP = 1;
	public static final int OLDEST_ON_TOP = 2;
	public static final int _TRUE_ = 1;
	public static final int _FALSE_ = 2;
	
	private ITreeContentProvider contentProvider;
	private LabelProvider labelProvider;
	private IEventLogLabelProvider[] labelProviderDelegates = null;
	private IPropertySheetPage propertyPage = null;
	private Object[] selection;

	private IServer input;
	private Action clearLogAction;
	private Action showStackTraceAction;
	
	private static HashMap<String, String> majorTypeToName = new HashMap<String, String>();
	static {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(JBossServerUIPlugin.PLUGIN_ID, "EventLogMajorType");
		for( int i = 0; i < cf.length; i++ ) {
			String type = cf[i].getAttribute("typeId");
			String name = cf[i].getAttribute("name");
			majorTypeToName.put(type, name);
		}
	}
	
	
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
		
		showStackTraceAction = new Action() {
			public void run() {
				EventLogTreeItem item = (EventLogTreeItem)selection[0];
				ShowStackTraceDialog dialog = new ShowStackTraceDialog(new Shell(), item);
				dialog.open();
			}
		};
		showStackTraceAction.setText("See Exception Details");
	}
	
	public class EventLogContentProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			if( parentElement instanceof ServerViewProvider && input != null ) {
				boolean categorize = getCategorize();  
				if( categorize ) 
					return getRootCategories();
				Object[] ret = EventLogModel.getModel(input).getRoot().getChildren();
				if( getSortOrder()) {
					List<Object> l = Arrays.asList(ret); 
					Collections.reverse(l);
					return l.toArray();
				}
				return ret;
			}
			
			if( parentElement instanceof String ) {
				// get children only of this type
				SimpleTreeItem[] children = EventLogModel.getModel(input).getRoot().getChildren();
				ArrayList<SimpleTreeItem> items = new ArrayList<SimpleTreeItem>();
				for( int i = 0; i < children.length; i++ ) {
					if( children[i] instanceof EventLogTreeItem ) {
						String type = ((EventLogTreeItem)children[i]).getEventClass();
						if( type != null && type.equals(parentElement))
							items.add(children[i]);
					}
				}
				
				if( getSortOrder() ) Collections.reverse(items);
				
				return items.toArray(new Object[items.size()]);
			}
			
			// just return the object's kids
			if( parentElement instanceof EventLogTreeItem ) {
				return ((EventLogTreeItem)parentElement).getChildren();
			}
			return new Object[0];
		}

		protected Object[] getRootCategories() {
			EventLogRoot root = EventLogModel.getModel(input).getRoot();
			ArrayList<String> majorTypes = new ArrayList<String>();
			SimpleTreeItem[] children = root.getChildren();
			for( int i = 0; i < children.length; i++ ) {
				if( children[i] instanceof EventLogTreeItem ) {
					String type = ((EventLogTreeItem)children[i]).getEventClass();
					if( !majorTypes.contains(type))
						majorTypes.add(type);
				}
			}
			return majorTypes.toArray(new String[majorTypes.size()]);
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
	    	if( element == null ) return "NULL, ERROR";
	    	String suffix = getShowTimestamp() ? createTimestamp(element) : "";
	    	if( labelProviderDelegates == null )
	    		loadLabelProviderDelegates();

	    	if( element instanceof ServerViewProvider ) {
	    		return ((ServerViewProvider)element).getName();
	    	}

	    	if( !(element instanceof EventLogTreeItem)) {
		    	if( element instanceof String ) {
		    		String val = majorTypeToName.get(element);
		    		if( val != null ) return val;
		    	}
	    		return element.toString();
	    	}
	    	EventLogTreeItem item = (EventLogTreeItem)element;
	    	
	    	for( int i = 0; i < labelProviderDelegates.length; i++ ) {
	    		if( labelProviderDelegates[i] != null 
	    				&& labelProviderDelegates[i].supports(item.getSpecificType())) {
	    			String text = labelProviderDelegates[i].getText((EventLogTreeItem)element);
	    			if( text != null ) return text + suffix;
	    		}
	    	}

	        return element == null ? "" : element.toString() + suffix;
	    }
	    
	    public void loadLabelProviderDelegates() {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IConfigurationElement[] elements = registry.getConfigurationElementsFor(JBossServerUIPlugin.PLUGIN_ID, "EventLogLabelProvider");
			labelProviderDelegates = new IEventLogLabelProvider[elements.length];
			for( int i = 0; i < elements.length; i++ ) {
				try {
					labelProviderDelegates[i] = (IEventLogLabelProvider)elements[i].createExecutableExtension("class");
				} catch( CoreException ce ) {
					JBossServerUIPlugin.log("Error loading Event Log Label Provider Delegate", ce);
				}
			}
	    }
	}
	
	public void fillContextMenu(Shell shell, IMenuManager menu, Object selection[]) {
		this.selection = selection;
		if( selection.length == 1 && selection[0] == this.provider) {
			menu.add(clearLogAction);
		}
		if( selection.length == 1 && selection[0] instanceof EventLogTreeItem && 
				(((EventLogTreeItem)selection[0]).getEventClass().equals(EventLogModel.EVENT_TYPE_EXCEPTION) ||
				((EventLogTreeItem)selection[0]).getSpecificType().equals(EventLogModel.EVENT_TYPE_EXCEPTION))) {
			menu.add(showStackTraceAction);
		}
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

	public ViewProviderPreferenceComposite createPreferenceComposite(Composite parent) {
		return new EventLogPreferenceComposite(parent);
	}
	
	protected class EventLogPreferenceComposite extends ViewProviderPreferenceComposite {
		private Button newestFirst, oldestFirst, showTime, sort;
		private Group firstGroup;
		private Label newestFirstLabel, oldestFirstLabel, showTimeLabel, sortLabel;
		public EventLogPreferenceComposite(Composite parent) {
			super(parent, SWT.NONE);
			setLayout(new FormLayout());
			
			createWidgets();
			fillWidgetsWithValues();
		}
		protected void fillWidgetsWithValues() {
			Preferences store = JBossServerUIPlugin.getDefault().getPluginPreferences();
			boolean showTimestamp = getShowTimestamp();
			boolean categorize = getCategorize();
			boolean onTop = getSortOrder();
			
			sort.setSelection(categorize);
			showTime.setSelection(showTimestamp);
			newestFirst.setSelection(onTop);
			oldestFirst.setSelection(!onTop);
		}
		
		
		protected void createWidgets() {
			firstGroup = new Group(this, SWT.NONE);
			firstGroup.setText("Which elements should be at the top?");
			firstGroup.setLayout(new GridLayout(2, false));
			newestFirst = new Button(firstGroup, SWT.RADIO);
			oldestFirst = new Button(firstGroup, SWT.RADIO);
			
			newestFirst.setText("Newest");
			oldestFirst.setText("Oldest");
			
			FormData firstGroupData = new FormData();
			firstGroupData.left = new FormAttachment(0,5);
			firstGroupData.top = new FormAttachment(0,5);
			firstGroup.setLayoutData(firstGroupData);
			
			showTime = new Button(this, SWT.CHECK);
			showTime.setText("Show timestamp? (ex: x minutes ago)");
			
			FormData d = new FormData();
			d.left = new FormAttachment(0, 5);
			d.top = new FormAttachment(firstGroup, 5);
			showTime.setLayoutData(d);
			
			sort = new Button(this, SWT.CHECK);
			sort.setText("Sort by event category?");
			
			d = new FormData();
			d.left = new FormAttachment(0, 5);
			d.top = new FormAttachment(showTime, 5);
			sort.setLayoutData(d);
			
//			d = new FormData();
//			d.left = new FormAttachment(sort, 5);
//			d.top = new FormAttachment(showTime, 5);
//			sortLabel.setLayoutData(d);
		}
		public boolean isValid() {
			return true;
		}
		public boolean performCancel() {
			return false;
		}
		public boolean performOk() {
			Preferences store = JBossServerUIPlugin.getDefault().getPluginPreferences();
			store.setValue(SHOW_TIMESTAMP, showTime.getSelection() ? _TRUE_ : _FALSE_);
			store.setValue(GROUP_BY_CATEGORY, sort.getSelection() ? _TRUE_ : _FALSE_);
			store.setValue(EVENT_ON_TOP, newestFirst.getSelection() ? NEWEST_ON_TOP : OLDEST_ON_TOP);
			return true;
		}
	}


	
	
	protected boolean getShowTimestamp() {
		Preferences store = JBossServerUIPlugin.getDefault().getPluginPreferences();
		int showTimestamp = store.getInt(SHOW_TIMESTAMP);
		if( showTimestamp == _TRUE_ ) return true;
		if( showTimestamp == _FALSE_) return false;
		return false; // default
	}
	protected boolean getSortOrder() {
		Preferences store = JBossServerUIPlugin.getDefault().getPluginPreferences();
		int showTimestamp = store.getInt(EVENT_ON_TOP);
		if( showTimestamp == OLDEST_ON_TOP) return false;
		return true;
	}
	protected boolean getCategorize() {
		Preferences store = JBossServerUIPlugin.getDefault().getPluginPreferences();
		int showTimestamp = store.getInt(GROUP_BY_CATEGORY);
		if( showTimestamp == _TRUE_ ) return true;
		if( showTimestamp == _FALSE_) return false;
		return false; // default
	}

	protected String createTimestamp(Object element) {
		if( element instanceof EventLogTreeItem ) {
			Long v = (Long)   ((EventLogTreeItem)element).getProperty(EventLogTreeItem.DATE);
			if( v == null ) return "";

			double date = v.doubleValue();
			double now = new Date().getTime();
			int seconds = (int) (( now - date) / 1000);
			int minutes = seconds / 60;
			int hours = minutes / 60;
			minutes -= (hours * 60);
			String minString = minutes + "m ago";
			if( hours == 0 )
				return "   (" + minString + ")";
			return "   (" + hours + "h " + minString + ")"; 
		}
		return "";
	}
	
	public void enable() {
		EventLogModel.enableLogging();
	}
	public void disable() {
		EventLogModel.disableLogging();
	}


}
