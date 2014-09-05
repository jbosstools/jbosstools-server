/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    "Rob Stryker" <rob.stryker@redhat.com> - Initial implementation
 *******************************************************************************/
package org.jboss.tools.jmx.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.tools.jmx.core.IConnectionWrapper;

/**
 * Extension Manager for UI extensions
 */
public class UIExtensionManager {
	/* Wizard Pages */
	private static final String CONNECTION_UI_EXT_PT = "org.jboss.tools.jmx.ui.providerUI"; //$NON-NLS-1$
	private static final String ID = "id"; //$NON-NLS-1$
	private static final String NAME = "name"; //$NON-NLS-1$
	private static final String ICON = "icon"; //$NON-NLS-1$
	private static final String CLASS = "class";  //$NON-NLS-1$
	private static final String EDITABLE = "editable";  //$NON-NLS-1$
	
	/**
	 * The UI for specific connection providers
	 */
	public static class ConnectionCategoryUI {
		String id, name, icon;
		ImageDescriptor imageDescriptor;
		public ConnectionCategoryUI(IConfigurationElement element) {
			id = element.getAttribute(ID);
			name = element.getAttribute(NAME);
			icon = element.getAttribute(ICON);
			String pluginName = element.getDeclaringExtension().getContributor().getName();
			imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(pluginName, icon);
			if( imageDescriptor == null ) {
				IStatus s = new Status(IStatus.WARNING, JMXUIActivator.PLUGIN_ID, 
						NLS.bind(Messages.JMXUIImageDescriptorNotFound, icon, pluginName));
				JMXUIActivator.getDefault().log(s);
			}
		}
		public String getId() {
			return id;
		}
		public String getName() {
			return name;
		}
		public String getIcon() {
			return icon;
		}
		public ImageDescriptor getImageDescriptor() {
			return imageDescriptor;
		}
	}

	/**
	 * The UI for specific connection providers
	 */
	public static class ConnectionProviderUI extends ConnectionCategoryUI {
		String id, name, icon;
		boolean editable;
		IConfigurationElement labelProviderElement;
		ILabelProvider labelProvider;
		IConfigurationElement[] wizardPages;
		ImageDescriptor imageDescriptor;
		public ConnectionProviderUI(IConfigurationElement element) {
			super(element);
			editable = Boolean.parseBoolean(element.getAttribute(EDITABLE));
			IConfigurationElement[] children = element.getChildren();
			ArrayList<IConfigurationElement> wizardPageList = new ArrayList<IConfigurationElement>();
			labelProviderElement = null;
			for( int i = 0; i < children.length; i++ ) {
				if( children[i].getName().equals("wizardPage")) { //$NON-NLS-1$
					wizardPageList.add(children[i]);
				} else if( children[i].getName().equals("connectionLabelProvider")) { //$NON-NLS-1$
					labelProviderElement = children[i];
				}
			}
			wizardPages = (IConfigurationElement[]) wizardPageList.toArray(new IConfigurationElement[wizardPageList.size()]);
		}
		/**
		 * Return true if the image can change
		 * Return false if the image is static for this provider ui under all circumstances and can be cached
		 * @return
		 */
		public boolean hasLabelProvider() {
			return getLabelProvider() != null;
		}
		
		public Image getImageForConnection(IConnectionWrapper connection) {
			if( hasLabelProvider()) {
				return getLabelProvider().getImage(connection);
			}
			return null;
		}
		
		public String getTextForConnection(IConnectionWrapper connection) {
			if( hasLabelProvider()) {
				return getLabelProvider().getText(connection);
			}
			return null;
		}
		
		private ILabelProvider getLabelProvider() {
			if( labelProvider == null ) {
				if( labelProviderElement != null ) {
					try {
						labelProvider = (ILabelProvider) labelProviderElement.createExecutableExtension(CLASS);
					} catch(CoreException ce) {
						JMXUIActivator.log(ce.getStatus());
					}
				}
			}
			return labelProvider;
		}
		
		public boolean isEditable() {
			return editable;
		}
		public ConnectionWizardPage[] createPages() {
			ArrayList<ConnectionWizardPage> list = new ArrayList<ConnectionWizardPage>();
			for( int i = 0; i < wizardPages.length; i++ ) {
				try {
					ConnectionWizardPage wp = (ConnectionWizardPage)wizardPages[i].createExecutableExtension(CLASS);
					list.add(wp);
				} catch( CoreException ce ) {
					JMXUIActivator.log(ce.getStatus());
				}
			}
			return list.toArray(new ConnectionWizardPage[list.size()]);
		}
	}

	private static HashMap<String, ConnectionProviderUI> connectionUIElements;
	private static HashMap<String, ConnectionCategoryUI> connectionCategoryUIElements;
	
	private static void ensureLoaded() {
		if( connectionUIElements == null ) {
			loadConnectionUI();
		}
		if( connectionCategoryUIElements == null ) {
			loadConnectionCategoryUI();
		}
	}
	
	public static HashMap<String, ConnectionProviderUI> getConnectionUIElements() {
		ensureLoaded();
		return connectionUIElements;
	}

	public static ConnectionProviderUI getConnectionProviderUI(String id) {
		ensureLoaded();
		return connectionUIElements.get(id);
	}

	public static ConnectionCategoryUI getConnectionCategoryUI(String id) {
		ensureLoaded();
		return connectionCategoryUIElements.get(id);
	}
	
	private static void loadConnectionUI() {
		HashMap<String, ConnectionProviderUI> map = new HashMap<String, ConnectionProviderUI>();
		IExtension[] extensions = findExtension(CONNECTION_UI_EXT_PT);
		ConnectionProviderUI pUI;
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement elements[] = extensions[i].getConfigurationElements();
			for (int j = 0; j < elements.length; j++) {
				if( elements[j].getName().equals("providerUI")) { //$NON-NLS-1$
					try {
						pUI = new ConnectionProviderUI(elements[j]);
						map.put(pUI.getId(), pUI);
					} catch (InvalidRegistryObjectException e) {
						// TODO document
					}
				}
			}
		}
		connectionUIElements = map;
	}
	
	private static void loadConnectionCategoryUI() {
		HashMap<String, ConnectionCategoryUI> map = new HashMap<String, ConnectionCategoryUI>();
		IExtension[] extensions = findExtension(CONNECTION_UI_EXT_PT);
		ConnectionCategoryUI pUI;
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement elements[] = extensions[i].getConfigurationElements();
			for (int j = 0; j < elements.length; j++) {
				if( elements[j].getName().equals("providerCategoryUI")) { //$NON-NLS-1$
					try {
						pUI = new ConnectionCategoryUI(elements[j]);
						map.put(pUI.getId(), pUI);
					} catch (InvalidRegistryObjectException e) {
						// TODO document
					}
				}
			}
		}
		connectionCategoryUIElements = map;
	}

	public static IWizardPage[] getNewConnectionWizardPages(String typeName) {
		ConnectionProviderUI ui = connectionUIElements.get(typeName);
		if( ui != null ) {
			IWizardPage[] pages = ui.createPages();
			if( pages != null )
				return pages;
		}
		return new IWizardPage[]{};
	}

	private static IExtension[] findExtension(String extensionId) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry
				.getExtensionPoint(extensionId);
		return extensionPoint.getExtensions();
	}
}
