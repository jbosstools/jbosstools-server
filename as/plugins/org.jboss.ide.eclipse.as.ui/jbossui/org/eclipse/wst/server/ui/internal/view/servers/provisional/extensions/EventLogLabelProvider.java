package org.eclipse.wst.server.ui.internal.view.servers.provisional.extensions;

import java.util.Date;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.server.ui.internal.view.servers.provisional.extensions.EventLogContentProvider.MajorEventType;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.ServerEventModel;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.IEventLogLabelProvider;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.ServerViewProvider;

public class EventLogLabelProvider extends LabelProvider {
	
	public static final String SHOW_TIMESTAMP = "org.jboss.ide.eclipse.as.ui.views.server.providers.EventLogViewProvider.showTimestamp";
	public static final int _TRUE_ = 1;
	public static final int _FALSE_ = 2;
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

	private Image rootImage;
	public EventLogLabelProvider() {
		super();
		ImageDescriptor des = ImageDescriptor.createFromURL(JBossServerUIPlugin.getDefault().getBundle().getEntry("icons/info_obj.gif"));
		rootImage = des.createImage();
	}
	public void dispose() {
		super.dispose();
		rootImage.dispose();
	}
	
	private IEventLogLabelProvider[] labelProviderDelegates = null;
    public Image getImage(Object element) {
    	if( labelProviderDelegates == null )
    		loadLabelProviderDelegates();
    	
    	if( element instanceof ServerEventModel ) {
    		return rootImage;
    	}
    	
    	if( !(element instanceof EventLogTreeItem)) 
    		return null;
    	
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
    	if( element == null ) return "ERROR: Unknown String type";
    	
    	if( element instanceof ServerEventModel ) {
    		return "Event Log";
    	}

    	
    	String suffix = getShowTimestamp() ? createTimestamp(element) : "";
    	if( labelProviderDelegates == null )
    		loadLabelProviderDelegates();

    	if( element instanceof ServerViewProvider ) {
    		return ((ServerViewProvider)element).getName();
    	}

    	if( !(element instanceof EventLogTreeItem)) {
	    	if( element instanceof MajorEventType ) {
	    		String val = majorTypeToName.get(((MajorEventType)element).getId());
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
	protected boolean getShowTimestamp() {
		Preferences store = JBossServerUIPlugin.getDefault().getPluginPreferences();
		int showTimestamp = store.getInt(SHOW_TIMESTAMP);
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
}