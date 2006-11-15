package org.jboss.ide.eclipse.as.ui.views.server.providers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.model.EventLogModel;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.IEventLogListener;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.IEventLogLabelProvider;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.JBossServerViewExtension;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.ServerViewProvider;

public class EventLogViewProvider extends JBossServerViewExtension implements IEventLogListener {
	
	private ITreeContentProvider contentProvider;
	private LabelProvider labelProvider;
	
	private IEventLogLabelProvider[] labelProviderDelegates = null;

	private IServer input;

	public EventLogViewProvider() {
		contentProvider = new EventLogContentProvider();
		labelProvider = new EventLogLabelProvider();
		EventLogModel.getDefault().addListener(this);
	}
	
	
	public class EventLogContentProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			if( parentElement instanceof ServerViewProvider && input != null ) {
				return EventLogModel.getModel(input).getRoot().getChildren();
			}
			if( parentElement instanceof EventLogTreeItem ) {
				return ((EventLogTreeItem)parentElement).getChildren();
			}
			return new Object[0];
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
	    				&& labelProviderDelegates[i].supports(item.getType())) {
	    			Image image = labelProviderDelegates[i].getImage(element);
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
	    				&& labelProviderDelegates[i].supports(item.getType())) {
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
	}

	public ITreeContentProvider getContentProvider() {
		return contentProvider;
	}

	public LabelProvider getLabelProvider() {
		return labelProvider;
	}

	public IPropertySheetPage getPropertySheetPage() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jboss.ide.eclipse.as.core.model.EventLogModel.IEventLogListener#eventModelChanged(java.lang.String, org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogTreeItem)
	 */
	public void eventModelChanged(String serverId, EventLogTreeItem changed) {
		if( input != null && serverId.equals(input.getId())) {
			if(changed.getType().equals("jboss.event.root"))
				refreshViewer();
			else
				refreshViewer(changed);
		}
	}

}
