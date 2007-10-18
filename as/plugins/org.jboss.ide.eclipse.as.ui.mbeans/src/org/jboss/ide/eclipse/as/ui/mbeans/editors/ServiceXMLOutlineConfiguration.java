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
package org.jboss.ide.eclipse.as.ui.mbeans.editors;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.sse.ui.internal.IReleasable;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.eclipse.wst.xml.ui.internal.contentoutline.XMLNodeActionManager;
import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImageHelper;
import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImages;
import org.eclipse.wst.xml.ui.views.contentoutline.XMLContentOutlineConfiguration;
import org.jboss.ide.eclipse.as.ui.mbeans.Activator;
import org.jboss.ide.eclipse.as.ui.mbeans.editors.proposals.IServiceXMLOutlineActionProvider;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class ServiceXMLOutlineConfiguration extends
		XMLContentOutlineConfiguration {

	private ILabelProvider fLabelProvider = null;
	private IContentProvider fLocalContentProvider = null;
	
	public ServiceXMLOutlineConfiguration() {
		super();
	}


	public ILabelProvider getLabelProvider(TreeViewer viewer) {
		if (fLabelProvider == null) {
			fLabelProvider = new ServiceXMLLabelProvider(super.getLabelProvider(viewer));
		}
		return fLabelProvider;
	}

	private class ServiceXMLLabelProvider extends LabelProvider {
		private ILabelProvider delegate;
		private ServiceXMLLabelProvider(ILabelProvider delegate) {
			this.delegate = delegate;
		}
	    public Image getImage(Object element) {
	    	try {
	    		return delegate.getImage(element);
	    	} catch( Exception e ) {
	    		if( element instanceof Node && ((Node)element).getNodeType() == Node.ATTRIBUTE_NODE ) {
	    			return XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_ATTRIBUTE);
	    		}
	    		return null;
	    	}
	    }
	    
		private String getNodeName(Object object) {
			StringBuffer nodeName = new StringBuffer();
			Node node = (Node) object;
			nodeName.append(node.getNodeName());

			if (node.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
				nodeName.insert(0, "DOCTYPE:"); //$NON-NLS-1$
			}
			return nodeName.toString();
		}


	    public String getText(Object element) {
	    	try {
	    		return delegate.getText(element);
	    	} catch( Exception e ) {
	    		return null;
	    	}
	    }

	}
	
	public IContentProvider getContentProvider(TreeViewer viewer) {
		IContentProvider parentProvider = super.getContentProvider(viewer);
		if( fLocalContentProvider == null ) {
			fLocalContentProvider = new ServiceXMLContentProvider();
		}
		return fLocalContentProvider;
	}

	private class ServiceXMLContentProvider implements ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			Node node = (Node) parentElement;
			ArrayList v = new ArrayList();
			
			// First add the attributes 
			NamedNodeMap map = node.getAttributes();
			if( map != null ) {
				for( int i = 0; i < map.getLength(); i++ ) {
					Node current = map.item(i);
					v.add(current);
				}
			}
			
			
			// Now add element children
			for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
				Node n = child;
				if (n.getNodeType() != Node.TEXT_NODE)
					v.add(n);
			}
			return v.toArray();
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0 ? true : false;
		}

		public Object[] getElements(Object inputElement) {
			Object topNode = inputElement;
			if (inputElement instanceof IDOMModel)
				topNode = ((IDOMModel) inputElement).getDocument();
			
			return getChildren(topNode);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
	}
	
	
	
	
	
	private ActionManagerMenuListener fContextMenuFiller = null;
	
	/**
	 * Pilfered from superclass, where it's currently PRIVATE (BOOOO!)
	 */
	private class ActionManagerMenuListener implements IMenuListener, IReleasable {
		private XMLNodeActionManager fActionManager;
		private TreeViewer fTreeViewer;
		private OutlineMenuProvider[] menuProviders = null;
		
		public ActionManagerMenuListener(TreeViewer viewer) {
			fTreeViewer = viewer;
			
		}

		public OutlineMenuProvider[] getProviders() {
			if( menuProviders == null ) {
				ArrayList list = new ArrayList();
				IExtensionRegistry registry = Platform.getExtensionRegistry();
				IConfigurationElement[] cf = registry.getConfigurationElementsFor(Activator.PLUGIN_ID, "ServiceXMLOutlineMenuProvider");
				for( int i = 0; i < cf.length; i++ ) {
					try {
						list.add(new OutlineMenuProvider(cf[i]));
					} catch( CoreException ce ) {
					}
				}
				menuProviders = (OutlineMenuProvider[]) list.toArray(new OutlineMenuProvider[list.size()]);
			}
			return menuProviders;
		}

		public void menuAboutToShow(IMenuManager manager) {
			if (fActionManager == null) {
				fActionManager = createNodeActionManager(fTreeViewer);
			}
			fActionManager.fillContextMenu(manager, fTreeViewer.getSelection());
			for( int i = 0; i < getProviders().length; i++ ) {
				getProviders()[i].menuAboutToShow(manager, fTreeViewer.getSelection());
			}
		}

		public void release() {
			fTreeViewer = null;
			if (fActionManager != null) {
				fActionManager.setModel(null);
			}
			for( int i = 0; i < getProviders().length; i++ ) {
				getProviders()[i].release();
			}
		}
	}
	
	private static class OutlineMenuProvider {
		private IServiceXMLOutlineActionProvider listener;
		public OutlineMenuProvider(IConfigurationElement element) throws CoreException {
			listener = (IServiceXMLOutlineActionProvider)element.createExecutableExtension("class");
		}
		public void menuAboutToShow(IMenuManager manager, ISelection selection) {
			if( listener != null ) listener.menuAboutToShow(manager, selection);
		}
		public void release() {
			if( listener != null && listener instanceof IReleasable) {
				((IReleasable)listener).release();
			}
		}
	}
	
	public IMenuListener getMenuListener(TreeViewer viewer) {
		if (fContextMenuFiller == null) {
			fContextMenuFiller = new ActionManagerMenuListener(viewer);
		}
		return fContextMenuFiller;
	}

}
