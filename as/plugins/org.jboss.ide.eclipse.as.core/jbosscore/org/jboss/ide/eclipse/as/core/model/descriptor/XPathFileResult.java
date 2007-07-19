package org.jboss.ide.eclipse.as.core.model.descriptor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.tree.DefaultAttribute;
import org.dom4j.tree.DefaultElement;


public class XPathFileResult {
	protected XPathQuery query;
	protected List nodeList;
	protected String fileLoc;
	
	protected XPathResultNode[] children;
	public XPathFileResult(XPathQuery query, String fileLoc, List nodeList) {
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
	
	public XPathResultNode[] getChildren() {
		if( children == null ) {
			ArrayList childList = new ArrayList();
			Iterator i = nodeList.iterator();
			int z = 0;
			while(i.hasNext()) {
				Node o = (Node)i.next();
				childList.add(new XPathResultNode(o, query.getAttribute(), z++));
			}
			children = (XPathResultNode[]) childList.toArray(new XPathResultNode[childList.size()]);
		}
		return children;
	}
	
	public class XPathResultNode {
		protected Node node;
		protected String attribute;
		protected boolean hasAttribute;
		protected int index;
		protected Object val;
		protected boolean dirty;
		public XPathResultNode(Node node, String attribute, int index) {
			this.node = node;
			this.attribute = attribute;
			this.index = index;
			this.hasAttribute = attribute == null ? false : true;
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
			return "";
		}
		
		public Document getDocument() {
			if( node instanceof DefaultElement ) {
				return ((DefaultElement)node).getDocument();
			}
			return null;
		}
		
		public String getText() {
			try {
			if( node instanceof DefaultElement ) {
				if( !hasAttribute()) {
					return ((DefaultElement)node).getText();
				} else {
					Attribute att = ((DefaultElement)node).attribute(attribute);
					return att.getValue();
				}
			}
			} catch( NullPointerException npe ) {
			}
			return "";
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
			List l = element.elements();
			DefaultElement child;
			ArrayList names = new ArrayList();
			for( Iterator i = l.iterator();i.hasNext();) {
				child = (DefaultElement)i.next();
				if( !names.contains(child.getName()))
					names.add(child.getName());
			}
			return (String[]) names.toArray(new String[names.size()]);
		}
		public String[] getElementAttributeNames() {
			DefaultElement element = ((DefaultElement)node);
			List l = element.attributes();
			DefaultAttribute child;
			ArrayList names = new ArrayList();
			for( Iterator i = l.iterator();i.hasNext();) {
				child = (DefaultAttribute)i.next();
				if( !names.contains(child.getName()))
					names.add(child.getName());
			}
			return (String[]) names.toArray(new String[names.size()]);
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
