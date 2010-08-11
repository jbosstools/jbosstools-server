/**
 * JBoss by Red Hat
 * Copyright 2006-2009, Red Hat Middleware, LLC, and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.ui.mbeans.editors.proposals.internal;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentAssistProcessor;
import org.jboss.ide.eclipse.as.ui.mbeans.editors.proposals.IServiceXMLQuickFixProposalProvider;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * The functionality for this is being removed for now
 * until a cleaner solution can be come up with
 * @author rob.stryker@jboss.com
 */
public class ConvertNodeToXPathDialogProvider extends XMLContentAssistProcessor
		implements IServiceXMLQuickFixProposalProvider {

	public ICompletionProposal[] getProposals(ITextViewer viewer, int offset) {
		return computeCompletionProposals(viewer, offset);
	}
	
//	protected void addAttributeNameProposals(ContentAssistRequest contentAssistRequest) {
//		Node n = contentAssistRequest.getNode();
//		String name = n.getNodeName();
//		String attName = ((AttrImpl)n.getAttributes().getNamedItem(contentAssistRequest.getText())).getName();
//		contentAssistRequest.addProposal(createProposal(n, attName));
//	}
	
	protected void addAttributeValueProposals(ContentAssistRequest contentAssistRequest) {
		String elementName = contentAssistRequest.getNode().getNodeName();
		String match = contentAssistRequest.getMatchString();
		String text = contentAssistRequest.getText();
		int beginPos = contentAssistRequest.getReplacementBeginPosition();

		// find the attribute we're inside of, because the contentAssistRequester only returns the element (BOO!)
		NamedNodeMap map = contentAssistRequest.getNode().getAttributes();
		
		boolean found = false;
		AttrImpl attribute = null;
		for( int i = 0; i < map.getLength() && !found; i++ ) {
			Node tmp = map.item(i);
			if( tmp instanceof AttrImpl ) {
				int start = ((AttrImpl)tmp).getStartOffset();
				int end = ((AttrImpl)tmp).getEndOffset();
				if( beginPos > start && beginPos < end ) {
					found = true;
					attribute = (AttrImpl)tmp;
				}
			}
		}
		if( found ) {
			//contentAssistRequest.addProposal(createProposal(contentAssistRequest.getNode(), attribute.getName()));
		}
	}
	
	protected void addEmptyDocumentProposals(ContentAssistRequest contentAssistRequest) {
	}
	
//	protected void addTagInsertionProposals(ContentAssistRequest contentAssistRequest, int childPosition) {
//		contentAssistRequest.addProposal(createProposal(contentAssistRequest.getNode(), null));
//	}
	
	
//	protected ICompletionProposal createProposal(Node node, String attributeName) {
//		return new OpenXPathDialogProposal(node, attributeName);
//	}

}
