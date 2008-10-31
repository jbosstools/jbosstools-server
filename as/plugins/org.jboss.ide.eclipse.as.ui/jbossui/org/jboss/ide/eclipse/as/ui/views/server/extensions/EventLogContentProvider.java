package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.EventLogRoot;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.IEventLogListener;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.ServerEventModel;
import org.jboss.ide.eclipse.as.core.util.SimpleTreeItem;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;

public class EventLogContentProvider implements ITreeContentProvider {
	public static final String SHOW_TIMESTAMP = "org.jboss.ide.eclipse.as.ui.views.server.providers.EventLogViewProvider.showTimestamp";
	public static final String GROUP_BY_CATEGORY = "org.jboss.ide.eclipse.as.ui.views.server.providers.EventLogViewProvider.groupByCategory";
	public static final String EVENT_ON_TOP = "org.jboss.ide.eclipse.as.ui.views.server.providers.EventLogViewProvider.eventOnTop";
	public static final int NEWEST_ON_TOP = 1;
	public static final int OLDEST_ON_TOP = 2;
	public static final int _TRUE_ = 1;
	public static final int _FALSE_ = 2;
	
	private Viewer viewer;
	private IEventLogListener listener;
	
	public EventLogContentProvider() {
		EventLogModel.enableLogging();
		listener = new IEventLogListener() {
			public void eventModelChanged(String serverId,
					EventLogTreeItem changed) {
				if( viewer instanceof StructuredViewer ) {
					final Object o = changed.getEventRoot();
					if( o instanceof EventLogRoot) {
						Display.getDefault().asyncExec(new Runnable() { 
							public void run() {
								((StructuredViewer)viewer).refresh(((EventLogRoot)o).getModel());
							}
						});
					}
				}
			} 
		};
		EventLogModel.getDefault().addListener(listener);
	}
	
	public Object[] getChildren(Object parentElement) {
		if( parentElement instanceof IServer && parentElement != null ) {
			return new Object[] { EventLogModel.getModel((IServer)parentElement)};
		}
		if( parentElement instanceof ServerEventModel) {
			ServerEventModel s = ((ServerEventModel)parentElement);
			boolean categorize = getCategorize();  
			if( categorize ) 
				return getRootCategories(s);
			Object[] ret = s.getRoot().getChildren();
			if( getSortOrder()) {
				List<Object> l = Arrays.asList(ret); 
				Collections.reverse(l);
				return l.toArray();
			}
			return ret;
		}
		
		if( parentElement instanceof MajorEventType ) {
			// get children only of this type
			String serverId = ((MajorEventType)parentElement).getServer();
			SimpleTreeItem[] children = EventLogModel.getModel(serverId).getRoot().getChildren();
			ArrayList<SimpleTreeItem> items = new ArrayList<SimpleTreeItem>();
			for( int i = 0; i < children.length; i++ ) {
				if( children[i] instanceof EventLogTreeItem ) {
					String type = ((EventLogTreeItem)children[i]).getEventClass();
					if( type != null && type.equals(((MajorEventType)parentElement).getId()))
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

	public static class MajorEventType {
		private String id;
		private String serverId;
		public MajorEventType(String serverId, String id) {
			this.id = id;
			this.serverId = serverId;
		}
		public String getId() {
			return id;
		}
		public String getServer() { return serverId; }
		public String toString() { return id; }
		public boolean equals(Object o) {
			return o instanceof MajorEventType && 
				((MajorEventType)o).getId().equals(id) &&
				((MajorEventType)o).getServer().equals(serverId);
		}
	}
	
	protected Object[] getRootCategories(ServerEventModel model) {
		EventLogRoot root = model.getRoot();
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<MajorEventType> majorTypes = new ArrayList<MajorEventType>();
		SimpleTreeItem[] children = root.getChildren();
		for( int i = 0; i < children.length; i++ ) {
			if( children[i] instanceof EventLogTreeItem ) {
				String type = ((EventLogTreeItem)children[i]).getEventClass();
				if( !list.contains(type)) {
					list.add(type);
					majorTypes.add(new MajorEventType(model.getId(), type));
				}
			}
		}
		return majorTypes.toArray(new MajorEventType[majorTypes.size()]);
	}
	
	
	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0 ? true : false;
	}

	public Object[] getElements(Object inputElement) {
		return null;
	}

	public void dispose() {
		EventLogModel.disableLogging();
		EventLogModel.getDefault().removeListener(listener);
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
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
}