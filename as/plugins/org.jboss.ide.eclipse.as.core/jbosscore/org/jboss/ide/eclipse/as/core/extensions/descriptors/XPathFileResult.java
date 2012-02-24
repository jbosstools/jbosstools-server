/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.extensions.descriptors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.tree.DefaultAttribute;
import org.dom4j.tree.DefaultElement;

/**
 * A class representing an xpath file result.
 * It's children may be individual element results
 * @author rob.stryker@redhat.com
 *
 */
public class XPathFileResult {
	protected XPathQuery query;
	protected List<Node> nodeList;
	protected String fileLoc;
	
	protected XPathResultNode[] children;
	public XPathFileResult(XPathQuery query, String fileLoc, List<Node> nodeList) {
		this.query = query;
		this.fileLoc = fileLoc;
		this.nodeList = nodeList;
	}
	
	public String getFileLocation() {
		return fileLoc;
	}
	
	public XPathQuery getQuery() {
		return query;
	}
	
	/* Lazily load the children */
	public XPathResultNode[] getChildren() {
		if( children == null ) {
			ArrayList<XPathResultNode> childList = new ArrayList<XPathResultNode>();
			Iterator<Node> i = nodeList.iterator();
			int z = 0;
			while(i.hasNext()) {
				childList.add(new XPathResultNode(i.next(), query.getAttribute(), z++, this));
			}
			children = childList.toArray(new XPathResultNode[childList.size()]);
		}
		return children;
	}
	
	public boolean isLoaded() {
		return children == null ? false : true;
	}
	
	/* A class representing an actual result node / element in the document */
	public class XPathResultNode {
		protected Node node;
		protected String attribute;
		protected boolean hasAttribute;
		protected int index;
		protected Object val;
		protected boolean dirty;
		protected XPathFileResult file;
		public XPathResultNode(Node node, String attribute, int index, XPathFileResult file) {
			this.node = node;
			this.attribute = attribute;
			this.index = index;
			this.hasAttribute = attribute == null || attribute.equals("") ? false : true;//$NON-NLS-1$
			this.file = file;
		}
		
		public XPathFileResult getFile() {
			return file;
		}
		
		public int getIndex() {
			return index;
		}
		
		public boolean hasAttribute() {
			return hasAttribute;
		}

		public String getAttribute() {
			return attribute;
		}

		public String getAttributeValue() {
			if( node instanceof DefaultElement ) {
				return ((DefaultElement)node).attributeValue(attribute);
			}
			return "";//$NON-NLS-1$
		}
		
		public Document getDocument() {
			if( node instanceof DefaultElement ) {
				return ((DefaultElement)node).getDocument();
			}
			return null;
		}
		
		public String getText() {
			if( node != null && node instanceof DefaultElement ) {
				if( !hasAttribute()) {
					return ((DefaultElement)node).getText();
				} else {
					Attribute att = ((DefaultElement)node).attribute(attribute);
					if( att != null )
						return att.getValue();
				}
			}
			return "";//$NON-NLS-1$
		}
		
		public void setText(String newValue) {
			if( node instanceof DefaultElement ) {
				if( !hasAttribute()) {
					((DefaultElement)node).setText(newValue);
				} else {
					((DefaultElement)node).attribute(attribute).setValue(newValue);
				}
				dirty = true;
			}
		}
		
		public String elementAsXML() {
			return ((DefaultElement)node).asXML();
		}
		
		public String getElementName() {
			return ((DefaultElement)node).getName();
		}
		
		public String[] getElementChildrenNames() {
			DefaultElement element = ((DefaultElement)node);
			List<DefaultElement> l = element.elements();
			DefaultElement child;
			ArrayList<String> names = new ArrayList<String>();
			for( Iterator<DefaultElement> i = l.iterator();i.hasNext();) {
				child = i.next();
				if( !names.contains(child.getName()))
					names.add(child.getName());
			}
			return names.toArray(new String[names.size()]);
		}
		public String[] getElementAttributeNames() {
			DefaultElement element = ((DefaultElement)node);
			List l = element.attributes();
			DefaultAttribute child;
			ArrayList<String> names = new ArrayList<String>();
			for( Iterator i = l.iterator();i.hasNext();) {
				child = (DefaultAttribute)i.next();
				if( !names.contains(child.getName()))
					names.add(child.getName());
			}
			return names.toArray(new String[names.size()]);
		}
		public String[] getElementAttributeValues(String attName) {
			DefaultElement element = ((DefaultElement)node);
			Attribute at = element.attribute(attName);
			return at == null ? new String[0] : new String[] {at.getValue()};
		}
		public boolean isDirty() {
			return dirty;
		}
		public String getFileLocation() {
			return fileLoc;
		}
		public void saveDescriptor() {
			XMLDocumentRepository.saveDocument(node.getDocument(), fileLoc);
			dirty = false;
		}
	}
}
