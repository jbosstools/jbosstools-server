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

package org.jboss.tools.jmx.ui.internal.views.navigator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.jboss.tools.jmx.core.ExtensionManager;
import org.jboss.tools.jmx.core.HasName;
import org.jboss.tools.jmx.core.IConnectionCategory;
import org.jboss.tools.jmx.core.IConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.MBeanAttributeInfoWrapper;
import org.jboss.tools.jmx.core.MBeanInfoWrapper;
import org.jboss.tools.jmx.core.MBeanOperationInfoWrapper;
import org.jboss.tools.jmx.core.MBeanUtils;
import org.jboss.tools.jmx.core.tree.DomainNode;
import org.jboss.tools.jmx.core.tree.ErrorRoot;
import org.jboss.tools.jmx.core.tree.MBeansNode;
import org.jboss.tools.jmx.core.tree.ObjectNameNode;
import org.jboss.tools.jmx.core.tree.PropertyNode;
import org.jboss.tools.jmx.ui.ImageProvider;
import org.jboss.tools.jmx.ui.Messages;
import org.jboss.tools.jmx.ui.UIExtensionManager;
import org.jboss.tools.jmx.ui.UIExtensionManager.ConnectionCategoryUI;
import org.jboss.tools.jmx.ui.UIExtensionManager.ConnectionProviderUI;
import org.jboss.tools.jmx.ui.internal.JMXImages;
import org.jboss.tools.jmx.ui.internal.views.navigator.MBeanExplorerContentProvider.DelayProxy;
import org.jboss.tools.jmx.ui.internal.views.navigator.MBeanExplorerContentProvider.ProviderCategory;


/**
 * Label Provider for the view
 */
public class MBeanExplorerLabelProvider extends LabelProvider {
	private static ArrayList<MBeanExplorerLabelProvider> instances =
		new ArrayList<MBeanExplorerLabelProvider>();
	private static HashMap<String, Image> images =
		new HashMap<String, Image>();

	public MBeanExplorerLabelProvider() {
		super();
		instances.add(this);
	}

    public void dispose() {
		instances.remove(this);
		if( instances.isEmpty()) {
			Iterator<Image> i = images.values().iterator();
	    	while(i.hasNext()) {
	    		Image o = i.next();
	    		if( o != null )
	    			o.dispose();
	    	}
	    	
	    	// Dispose the contributed label providers
	    	IConnectionProvider[] providers = ExtensionManager.getProviders();
	    	for( int j = 0; j < providers.length; j++ ) {
	    		ConnectionProviderUI ui = UIExtensionManager.getConnectionProviderUI(providers[j].getId());
	    		if( ui != null ) {
	    			ui.dispose();
	    		}
	    	}
		}
	    super.dispose();
    }

	public String getText(Object obj) {
		if (obj instanceof HasName) {
			HasName hasName = (HasName) obj;
			return hasName.getName();
		}

		if( obj instanceof ProviderCategory ) {
			ConnectionCategoryUI ui = UIExtensionManager.getConnectionCategoryUI(((ProviderCategory)obj).getId());
			if( ui != null ) {
				return ui.getName();
			}
		}
		
		if( obj instanceof IConnectionCategory ) {
			return ((IConnectionCategory)obj).getCategoryId();
		}
		if( obj instanceof IConnectionWrapper ) {
			IConnectionProvider provider = ((IConnectionWrapper)obj).getProvider();
			return getLabelForConnection(provider, ((IConnectionWrapper)obj));
		}
		if( obj instanceof DelayProxy ) {
			return Messages.Loading;
		}
		if( obj instanceof ErrorRoot ) {
		    return Messages.ErrorLoading;
		}
		
		if (obj instanceof DomainNode) {
			DomainNode node = (DomainNode) obj;
			return node.getDomain();
		}
		if (obj instanceof ObjectNameNode) {
			ObjectNameNode node = (ObjectNameNode) obj;
			return node.getValue();
		}
		if (obj instanceof PropertyNode) {
			PropertyNode node = (PropertyNode) obj;
			return node.getValue();
		}
		if (obj instanceof MBeanInfoWrapper) {
			MBeanInfoWrapper wrapper = (MBeanInfoWrapper) obj;
			return wrapper.getObjectName().toString();
		}
		if (obj instanceof MBeanOperationInfoWrapper) {
			MBeanOperationInfoWrapper wrapper = (MBeanOperationInfoWrapper) obj;
			return MBeanUtils.prettySignature(wrapper.getMBeanOperationInfo());
		}
		if (obj instanceof MBeanAttributeInfoWrapper) {
			MBeanAttributeInfoWrapper wrapper = (MBeanAttributeInfoWrapper) obj;
			return wrapper.getMBeanAttributeInfo().getName();
		}
		return obj.toString();
	}


	private Image getImageForProvider(IConnectionProvider provider) {
		ConnectionProviderUI ui = UIExtensionManager.getConnectionProviderUI(provider.getId());
		if( ui != null ) {
			if(!images.containsKey(ui.getId()) || images.get(ui.getId()).isDisposed())
				images.put(ui.getId(),
						ui.getImageDescriptor().createImage());
			return images.get(ui.getId());
		}
		return null;
	}
	
	private Image getImageForConnection(IConnectionProvider provider, IConnectionWrapper connection) {
		ConnectionProviderUI ui = UIExtensionManager.getConnectionProviderUI(provider.getId());
		if( ui != null ) {
			if( ui.hasLabelProvider()) {
				Image i = ui.getImageForConnection(connection);
				if( i != null ) {
					return i;
				}
			}
		}
		// As a fallback, use the default image for the provider
		return getImageForProvider(provider);
	}

	private String getLabelForConnection(IConnectionProvider provider, IConnectionWrapper connection) {
		ConnectionProviderUI ui = UIExtensionManager.getConnectionProviderUI(provider.getId());
		if( ui != null ) {
			if( ui.hasLabelProvider()) {
				String s = ui.getTextForConnection(connection);
				if( s != null ) {
					return s;
				}
			}
		}
		// As a fallback, use the default image for the provider
		return provider.getName(connection);
	}
	

	private Image getImageForCategory(ProviderCategory provider) {
		ConnectionCategoryUI ui = UIExtensionManager.getConnectionCategoryUI(provider.getId());
		if( ui != null ) {
			if(!images.containsKey(ui.getId()) || images.get(ui.getId()).isDisposed())
				images.put(ui.getId(),
						ui.getImageDescriptor().createImage());
			return images.get(ui.getId());
		}
		return null;
	}
	
	@Override
	public Image getImage(Object obj) {
		if (obj instanceof ImageProvider) {
			ImageProvider provider = (ImageProvider) obj;
			Image answer = provider.getImage();
			if (answer != null) {
				return answer;
			} else {
			}
		}
		
		if( obj instanceof ProviderCategory ) {
			return getImageForCategory((ProviderCategory)obj);
		}
		
		if( obj instanceof IConnectionProvider ) {
			return getImageForProvider((IConnectionProvider)obj);
		}
		
		if( obj instanceof IConnectionWrapper ) {
			IConnectionProvider provider = ((IConnectionWrapper)obj).getProvider();
			return getImageForConnection(provider, ((IConnectionWrapper)obj));
		}
		if( obj instanceof DelayProxy ) {
			return null;
		}

		if( obj instanceof ErrorRoot ) {
		    return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
		}
		if( obj instanceof MBeansNode) {
			return JMXImages.get(JMXImages.IMG_MBEANS);
		}
		if (obj instanceof DomainNode) {
			return JMXImages.get(JMXImages.IMG_OBJS_LIBRARY);
		}
		if (obj instanceof ObjectNameNode) {
			return JMXImages.get(JMXImages.IMG_OBJS_METHOD);
		}
		if (obj instanceof PropertyNode) {
			return JMXImages.get(JMXImages.IMG_OBJS_PACKAGE);
		}
		if (obj instanceof MBeanInfoWrapper) {
			return JMXImages.get(JMXImages.IMG_OBJS_METHOD);
		}
		if (obj instanceof MBeanAttributeInfoWrapper) {
			return JMXImages.get(JMXImages.IMG_FIELD_PUBLIC);
		}
		if (obj instanceof MBeanOperationInfoWrapper) {
			return JMXImages.get(JMXImages.IMG_MISC_PUBLIC);
		}
		String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	}

}
