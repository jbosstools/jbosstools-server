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
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
import org.eclipse.wst.sse.ui.internal.correction.IQuickAssistProcessor;
import org.eclipse.wst.sse.ui.internal.correction.IQuickFixProcessor;
import org.eclipse.wst.xml.core.internal.document.DocumentImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.text.IXMLPartitions;
import org.eclipse.wst.xml.ui.internal.correction.CorrectionAssistantProviderXML;
import org.eclipse.wst.xml.ui.internal.correction.CorrectionProcessorXML;
import org.eclipse.wst.xml.ui.internal.correction.QuickAssistProcessorXML;
import org.eclipse.wst.xml.ui.internal.correction.QuickFixProcessorXML;
import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImageHelper;
import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImages;
import org.jboss.ide.eclipse.as.ui.mbeans.Activator;
import org.jboss.ide.eclipse.as.ui.mbeans.Messages;
import org.jboss.ide.eclipse.as.ui.mbeans.editors.proposals.IServiceXMLQuickFixProposalProvider;
import org.jboss.ide.eclipse.as.ui.util.ServiceXMLEditorUtil;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class ServiceXMLCorrectionAssistantProvider extends CorrectionAssistantProviderXML {
	public static IServiceXMLQuickFixProposalProvider[] providers;
	
	public static IServiceXMLQuickFixProposalProvider[] getProviders() {
		if( providers == null ) {
			ArrayList list = new ArrayList();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IConfigurationElement[] cf = registry.getConfigurationElementsFor(Activator.PLUGIN_ID, "ServiceXMLQuickFixProvider");
			for( int i = 0; i < cf.length; i++ ) {
				try {
					list.add((IServiceXMLQuickFixProposalProvider)cf[i].createExecutableExtension("class"));
				} catch( CoreException ce ) {
					// don't even log
				}
			}
			providers = (IServiceXMLQuickFixProposalProvider[])
				list.toArray(new IServiceXMLQuickFixProposalProvider[list.size()]);
		}
		return providers;
	}
	
	public ServiceXMLCorrectionAssistantProvider() {
		super();
	}

	public IContentAssistant getCorrectionAssistant(ISourceViewer sourceViewer) {
		IContentAssistant ca = null;

		if (sourceViewer != null) {
			ContentAssistant assistant = new ContentAssistant();

			if (sourceViewer != null) {
				IContentAssistProcessor correctionProcessor = new CorrectionProcessorServiceXML(sourceViewer);
				assistant.setContentAssistProcessor(correctionProcessor, IXMLPartitions.XML_DEFAULT);
				assistant.setContentAssistProcessor(correctionProcessor, IXMLPartitions.XML_CDATA);
				assistant.setContentAssistProcessor(correctionProcessor, IXMLPartitions.XML_COMMENT);
				assistant.setContentAssistProcessor(correctionProcessor, IXMLPartitions.XML_DECLARATION);
				assistant.setContentAssistProcessor(correctionProcessor, IXMLPartitions.XML_PI);
				assistant.setContentAssistProcessor(correctionProcessor, IXMLPartitions.DTD_SUBSET);
			}
			ca = assistant;
		}

		return ca;
	}

	public static class CorrectionProcessorServiceXML extends CorrectionProcessorXML {
		protected IQuickAssistProcessor fQuickAssistProcessor;
		protected IQuickFixProcessor fQuickFixProcessor;

		public CorrectionProcessorServiceXML(ISourceViewer sourceViewer) {
			super(sourceViewer);
		}

		protected IQuickAssistProcessor getQuickAssistProcessor() {
			if (fQuickAssistProcessor == null)
				fQuickAssistProcessor = new QuickAssistProcessorServiceXML();

			return fQuickAssistProcessor;
		}

		protected IQuickFixProcessor getQuickFixProcessor() {
			if (fQuickFixProcessor == null)
				fQuickFixProcessor = new QuickFixProcessorXML();

			return fQuickFixProcessor;
		}
	}
	
	public static class QuickAssistProcessorServiceXML extends QuickAssistProcessorXML {
		public boolean canAssist(StructuredTextViewer viewer, int offset) {
			if( super.canAssist(viewer, offset)) return true;

			IDOMNode node = (IDOMNode) ContentAssistUtils.getNodeAt(viewer, offset);
			if( mbeanHasAdditionalAttributes(node)) return true;

			return false;
		}
		

		private boolean mbeanHasAdditionalAttributes( IDOMNode node ) {
			int length = mbeanGetMissingAttributes(node).length;
			return length == 0 ? false : true;
		}
		
		private String[] mbeanGetMissingAttributes( IDOMNode node ) {
			Node parentNode = node.getParentNode();
			
			if( node.getNodeName().equals("mbean") || (node.getNodeName().equals("#text") && parentNode.getNodeName().equals("mbean"))) {
				Node mbeanNode = node.getNodeName().equals("mbean") ? node : parentNode;
				NamedNodeMap attributes = mbeanNode.getAttributes();
				for( int i = 0; i < attributes.getLength(); i++ ) {
					if( attributes.item(i).getNodeName().equals("code")) {
						// we found our code element, now lets get the IType. 
						String codeClass = attributes.item(i).getNodeValue();
						IType type = ServiceXMLEditorUtil.findType(codeClass);
						if( type != null ) {
							IMethod[] methods = ServiceXMLEditorUtil.getAllMethods(type);
							ArrayList attributeNames = new ArrayList(Arrays.asList(ServiceXMLEditorUtil.findAttributesFromMethods(methods, "")));
							NodeList mbeanChildren = mbeanNode.getChildNodes();
							
							// count children named 'attribute'
							for( int j = 0; j < mbeanChildren.getLength(); j++ ) {
								if( mbeanChildren.item(j).getNodeName().equals("attribute")) {
									try {
									Node t = mbeanChildren.item(j).getAttributes().getNamedItem("name");
									String attName = t.getNodeValue();
									attributeNames.remove(attName);
									} catch( Exception e ) {
										e.printStackTrace();
									}
								}
							}
							return (String[]) attributeNames.toArray(new String[attributeNames.size()]);
						}
					}
				}
			}
			return new String[] { };
		}
		
		public ICompletionProposal[] getProposals(StructuredTextViewer viewer, int offset) throws CoreException {
			ArrayList props = new ArrayList();
			if( super.canAssist(viewer, offset)) props.addAll(Arrays.asList(super.getProposals(viewer, offset)));

			IDOMNode node = (IDOMNode) ContentAssistUtils.getNodeAt(viewer, offset);
			addMissingAttributesProposal(node, props);
			addXPathProposals(viewer, offset, props);
			return (ICompletionProposal[]) props.toArray(new ICompletionProposal[props.size()]);
		}
		
		protected void addXPathProposals(StructuredTextViewer viewer, int offset, ArrayList proposals) {
			IServiceXMLQuickFixProposalProvider[] providers = getProviders();
			ICompletionProposal[] props;
			for( int i = 0; i < providers.length; i++ ) {
				props = providers[i].getProposals(viewer, offset);
				if( props != null )
					proposals.addAll(Arrays.asList(props));
			}
		}
		
		
		protected void addMissingAttributesProposal(IDOMNode node, ArrayList proposals) {
			String[] missing = mbeanGetMissingAttributes(node);
			if( missing.length == 0 ) return;

			Node parentNode = node.getParentNode();
			Node mbeanNode = node.getNodeName().equals("mbean") ? node : parentNode;

			
			// how many tabs?
			Node newParent = mbeanNode;
			StringBuffer attributeIndent = new StringBuffer();
			StringBuffer mbeanClosingIndent = new StringBuffer();
			boolean starting = true;
			attributeIndent.append("\n");
			mbeanClosingIndent.append("\n");
			while( newParent != null && !(newParent instanceof DocumentImpl)) {
				newParent = newParent.getParentNode();
				attributeIndent.append("\t");
				if( starting ) 
					starting = false;
				else 
					mbeanClosingIndent.append("\t");
			}
			
			
			StringBuffer buf = new StringBuffer();
			for( int i = 0; i < missing.length; i++ ) {
				buf.append(attributeIndent);
				buf.append("<attribute name=\"");
				buf.append(missing[i]);
				buf.append("\"></attribute>");
			}
			buf.append(mbeanClosingIndent);
			
			// where to put it
			IDOMNode lastChild = (IDOMNode)mbeanNode.getLastChild();
			int endOffset = lastChild.getEndOffset();
			
			
			Image elImage = XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_ELEMENT);

			proposals.add( new CompletionProposal(buf.toString().substring(2), endOffset, 0, 0,
					elImage, Messages.ServiceXMLAddAttributeTags, null, null) );
		}

	}
}
