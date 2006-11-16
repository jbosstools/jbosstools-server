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
package org.jboss.ide.eclipse.as.ui.util;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.eclipse.wst.sse.core.internal.provisional.StructuredModelManager;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Stolen from hibernate tools. Potential candidate to be moved to core
 *
 */
public class BaseXMLHyperlinkUtil {
	protected IRegion getHyperlinkRegion(Node node) {
		IRegion hyperRegion = null;

		if (node != null) {
			short nodeType = node.getNodeType();
			if (nodeType == Node.DOCUMENT_TYPE_NODE) {
				// handle doc type node
				IDOMNode docNode = (IDOMNode) node;
				hyperRegion = new Region(docNode.getStartOffset(), docNode.getEndOffset() - docNode.getStartOffset() );
			}
			else if (nodeType == Node.ATTRIBUTE_NODE) {
				// handle attribute nodes
				IDOMAttr att = (IDOMAttr) node;
				// do not include quotes in attribute value region
				int regOffset = att.getValueRegionStartOffset();
				int regLength = att.getValueRegion().getTextLength();
				String attValue = att.getValueRegionText();
				if (org.eclipse.wst.sse.core.utils.StringUtils.isQuoted(attValue) ) {
					regOffset = ++regOffset;
					regLength = regLength - 2;
				}
				hyperRegion = new Region(regOffset, regLength);
			}
		}
		return hyperRegion;
	}
	
	/**
	 * API addition
	 * @param document
	 * @param offset
	 * @return
	 */
	protected Attr getCurrentAttrNode(IDocument document, int offset) {
		return getCurrentAttrNode(getCurrentNode(document, offset), offset);
	}
	
	protected Attr getCurrentAttrNode(Node node, int offset) {
		if( node == null ) return null;
		
		if ( (node instanceof IndexedRegion) && ( (IndexedRegion) node).contains(offset) && (node.hasAttributes() ) ) {
			NamedNodeMap attrs = node.getAttributes();
			// go through each attribute in node and if attribute contains
			// offset, return that attribute
			for (int i = 0; i < attrs.getLength(); ++i) {
				// assumption that if parent node is of type IndexedRegion,
				// then its attributes will also be of type IndexedRegion
				IndexedRegion attRegion = (IndexedRegion) attrs.item(i);
				if (attRegion.contains(offset) ) {
					return (Attr) attrs.item(i);
				}
			}
		}
		return null;
	}
	
		
	/**
	 * Returns the element the cursor is currently on in the document. 
	 * null if no node is selected
	 * 
	 * @param offset
	 * @return Node either element, doctype, text, or null
	 */
	protected Node getCurrentNode(IDocument document, int offset) {
		// get the current node at the offset (returns either: element,
		// doctype, text)
		IndexedRegion inode = null;
		IStructuredModel sModel = null;
		try {
			sModel = StructuredModelManager.getModelManager().getExistingModelForRead(document);
			inode = sModel.getIndexedRegion(offset);
			if (inode == null)
				inode = sModel.getIndexedRegion(offset - 1);
		}
		finally {
			if (sModel != null)
				sModel.releaseFromRead();
		}

		if (inode instanceof Node) {
			return (Node) inode;
		}
		return null;
	}
}
