package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.osgi.framework.Bundle;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class ServerViewProvider {
	public static final String EXTENSION_ENABLED = "EXTENSION_ENABLED_";
	public static final String EXTENSION_WEIGHT = "EXTENSION_WEIGHT_";
	
	public static final String ID_LABEL = "id";
	public static final String NAME_LABEL = "name";
	public static final String DESCRIPTION_LABEL = "description";
	public static final String PROVIDER_LABEL = "providerClass";
	public static final String ICON_LABEL = "icon";
	
	
	private IConfigurationElement element;
	private JBossServerViewExtension extension;
	
	private ImageDescriptor iconDescriptor;
	private Image icon;
	
	private boolean enabled;
	private int weight;
	
	public ServerViewProvider(IConfigurationElement element) {
		this.element = element;
		
		// Am I enabled?
		Preferences prefs = JBossServerUIPlugin.getDefault().getPluginPreferences();

		
		String enabledKey = EXTENSION_ENABLED + getId();
		setEnabled( prefs.contains(enabledKey) ? prefs.getBoolean(enabledKey) : true );

		String weightKey = EXTENSION_WEIGHT + getId();
		setWeight( prefs.contains(weightKey) ? prefs.getInt(weightKey) : 0 );
		
		Bundle pluginBundle = JBossServerUIPlugin.getDefault().getBundle();
		String iconLoc = getIconLocation();
		if( iconLoc != null ) {
			iconDescriptor = 
				ImageDescriptor.createFromURL(pluginBundle.getEntry(iconLoc));
		}
	}
	
	public String getId() {
		return element.getAttribute(ID_LABEL);
	}
	
	public String getName() {
		return element.getAttribute(NAME_LABEL);
	}
	
	public String getDescription() {
		return element.getAttribute(DESCRIPTION_LABEL);
	}
	
	public String getIconLocation() {
		return element.getAttribute(ICON_LABEL);
	}
	
	public Image getImage() {
		if( icon == null && iconDescriptor != null ) {
			icon = iconDescriptor.createImage();
		} else if( icon == null && iconDescriptor == null ){
			icon = getDelegate().createIcon();
		} else if( icon != null && icon.isDisposed()) {
			icon = iconDescriptor == null ? getDelegate().createIcon() : iconDescriptor.createImage();
		}
		return icon;
	}
	
	public JBossServerViewExtension getDelegate() {
		try {
			if( extension == null ) {
				extension = (JBossServerViewExtension)element.createExecutableExtension(PROVIDER_LABEL);
				extension.setViewProvider(this);
			}
		} catch( CoreException ce ) {
			ce.printStackTrace();
		}
		return extension;
	}
	
	public String getDelegateName() {
		return element.getAttribute(PROVIDER_LABEL);
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enable) {
		if( enable && !enabled ) {
			enabled = true;
			getDelegate().enable();
		} else if( !enable && enabled ) {
			enabled = false;
			getDelegate().disable();
		}
	}
	
	public boolean supports(IServer server) {
		return getDelegate().supports(server);
	}
	
	public int getWeight() {
		return weight;
	}
	
	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	public void dispose() {
		getDelegate().dispose();
		if( icon != null && iconDescriptor != null ) 
			icon.dispose();
		
		Preferences prefs = JBossServerUIPlugin.getDefault().getPluginPreferences();

		prefs.setValue(EXTENSION_ENABLED + getId(), enabled);
		prefs.setValue(EXTENSION_WEIGHT + getId(), weight);
		JBossServerUIPlugin.getDefault().savePluginPreferences();
	}
}
