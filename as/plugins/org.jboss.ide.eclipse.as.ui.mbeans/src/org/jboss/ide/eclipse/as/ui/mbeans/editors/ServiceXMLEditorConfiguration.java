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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredPartitionTypes;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.text.IXMLPartitions;
import org.eclipse.wst.xml.ui.StructuredTextViewerConfigurationXML;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.NoRegionContentAssistProcessor;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentAssistProcessor;
import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImageHelper;
import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImages;
import org.jboss.ide.eclipse.as.ui.util.BaseXMLHyperlinkUtil;
import org.jboss.ide.eclipse.as.ui.util.PackageTypeSearcher;
import org.jboss.ide.eclipse.as.ui.util.ServiceXMLEditorUtil;
import org.jboss.ide.eclipse.as.ui.util.PackageTypeSearcher.ResultFilter;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class ServiceXMLEditorConfiguration extends
		StructuredTextViewerConfigurationXML {

	public ServiceXMLEditorConfiguration() {
		super();
	}

	protected IContentAssistProcessor[] getContentAssistProcessors(ISourceViewer sourceViewer, String partitionType) {
		IContentAssistProcessor[] processors = null;
		
		if ((partitionType == IStructuredPartitionTypes.DEFAULT_PARTITION) || (partitionType == IXMLPartitions.XML_DEFAULT)) {
			processors = new IContentAssistProcessor[] { new ServiceXMLContentAssistProcessor() };
			//processors = new IContentAssistProcessor[]{new CFGXMLContentAssistProcessor()};
		}
		else if (partitionType == IStructuredPartitionTypes.UNKNOWN_PARTITION) {
			processors = new IContentAssistProcessor[]{new NoRegionContentAssistProcessor()};
		}
		
		return processors;
	}	

	
	public class ServiceXMLContentAssistProcessor extends XMLContentAssistProcessor {
		private HashMap<String, ArrayList<ChildOccurances>> children;
		private HashMap<String, List> attributes;
		
		public ServiceXMLContentAssistProcessor() {
			super();
			children = new HashMap<String, ArrayList<ChildOccurances>>();
			attributes = new HashMap<String, List>();
			fillChildren();
			fillAttributes();
		}
		
		private class ChildOccurances {
			public static final String ZERO_OR_ONE = "_ZERO_OR_ONE_";
			public static final String ONE = "_ONE_";
			public static final String ZERO_TO_INFINITY = "_ZERO_TO_INFINITY_";
			public static final String ONE_TO_INFINITY = "_ONE_TO_INFINITY_";
			
			public String name;
			public String numOccurances;
			public ChildOccurances(String name, String numOcc) {
				this.name = name;
				this.numOccurances = numOcc;
			}
		}
		
		private class DTDAttributes {
			public static final int REQUIRED = 1;
			public static final int IMPLIED  = 2;
			public static final int FIXED  = 3;
			
			public static final int CDATA_TYPE = 1;
			public static final int ENUM_TYPE = 2;
			
			public String name;
			public int mandatory;
			public int dataType;
			public String[] enumOptions;
			public String defaultValue;
			
			public DTDAttributes(String name, int dataType, int mandatory ) {
				this(name, mandatory, dataType, null);
			}
			
			public DTDAttributes(String name, int dataType, int mandatory, String defaultValue) {
				this(name, mandatory, dataType, defaultValue, new String[0]);
			}
			
			public DTDAttributes(String name, int dataType, int mandatory, String defaultValue, String[] enumOptions) {
				this.name = name;
				this.enumOptions = enumOptions;
				this.mandatory = mandatory;
				this.dataType = dataType;
				this.defaultValue = defaultValue == null ? "" : defaultValue;
			}
			
		}
		
		private void fillChildren() {
			ArrayList<ChildOccurances> list = new ArrayList<ChildOccurances>();
			list.add(new ChildOccurances("loader-repository", ChildOccurances.ZERO_OR_ONE));
			list.add(new ChildOccurances("local-directory", ChildOccurances.ZERO_TO_INFINITY));
			list.add(new ChildOccurances("classpath", ChildOccurances.ZERO_TO_INFINITY));
			list.add(new ChildOccurances("mbean", ChildOccurances.ZERO_TO_INFINITY));
			children.put("server", list);
			
			list = new ArrayList<ChildOccurances>();
			list.add(new ChildOccurances("loader-repository-config", ChildOccurances.ZERO_TO_INFINITY));
			children.put("loader-repository", list);
			
			children.put("loader-repository-config", new ArrayList<ChildOccurances>());
			children.put("local-directory", new ArrayList<ChildOccurances>());
			children.put("classpath", new ArrayList<ChildOccurances>());
			
			list = new ArrayList<ChildOccurances>();
			list.add(new ChildOccurances("constructor", ChildOccurances.ZERO_OR_ONE));
			list.add(new ChildOccurances("xmbean", ChildOccurances.ZERO_OR_ONE));
			list.add(new ChildOccurances("attribute", ChildOccurances.ZERO_TO_INFINITY));
			list.add(new ChildOccurances("depends", ChildOccurances.ZERO_TO_INFINITY));
			list.add(new ChildOccurances("depends-list", ChildOccurances.ZERO_TO_INFINITY));
			children.put("mbean", list);
			
			children.put("xmbean", new ArrayList<ChildOccurances>());
			
			list = new ArrayList<ChildOccurances>();
			list.add(new ChildOccurances("arg", ChildOccurances.ZERO_TO_INFINITY));
			children.put("constructor", list);
			
			children.put("arg", new ArrayList<ChildOccurances>());
			children.put("attribute", new ArrayList<ChildOccurances>());
			children.put("property", new ArrayList<ChildOccurances>());
			
			list = new ArrayList<ChildOccurances>();
			list.add(new ChildOccurances("mbean", ChildOccurances.ZERO_TO_INFINITY));
			children.put("depends", list);
			
			list = new ArrayList<ChildOccurances>();
			list.add(new ChildOccurances("depends-list-element", ChildOccurances.ONE_TO_INFINITY));
			children.put("depends-list", list);
			
			list = new ArrayList<ChildOccurances>();
			list.add(new ChildOccurances("mbean", ChildOccurances.ZERO_TO_INFINITY));
			children.put("depends-list-element", list);
			
			
		}
		private void fillAttributes() {
			attributes.put("server", new ArrayList());
			
			/*
			 * <!ELEMENT loader-repository (#PCDATA | loader-repository-config)*>
			 * <!ATTLIST loader-repository loaderRepositoryClass CDATA  #IMPLIED>
			 */
			attributes.put("loader-repository", Arrays.asList(new DTDAttributes[] { 
					new DTDAttributes("loaderRepositoryClass", DTDAttributes.CDATA_TYPE, DTDAttributes.IMPLIED)
			}));
			
			/*
			 * <!ELEMENT loader-repository-config (#PCDATA)>
			 * <!ATTLIST loader-repository-config configParserClass CDATA  #IMPLIED>
			 */
			attributes.put("loader-repository-config", Arrays.asList(new DTDAttributes[] { 
					new DTDAttributes("configParserClass", DTDAttributes.CDATA_TYPE, DTDAttributes.IMPLIED)
			}));
			
			
			/*
			 * <!ELEMENT local-directory EMPTY>
			 *	<!ATTLIST local-directory path CDATA  #IMPLIED>
			 */
			attributes.put("local-directory", Arrays.asList(new DTDAttributes[] { 
					new DTDAttributes("path", DTDAttributes.CDATA_TYPE, DTDAttributes.IMPLIED)
			}));
			
			/*
			    <!ELEMENT classpath EMPTY>
				<!ATTLIST classpath codebase CDATA  #REQUIRED>
				<!ATTLIST classpath archives CDATA  #IMPLIED>
			 */
			
			attributes.put("classpath", Arrays.asList(new DTDAttributes[] { 
					new DTDAttributes("codebase", DTDAttributes.CDATA_TYPE, DTDAttributes.REQUIRED), 
					new DTDAttributes("archives", DTDAttributes.CDATA_TYPE, DTDAttributes.IMPLIED)
			}));
			

			/*
				<!ELEMENT mbean (constructor? , xmbean? , attribute* , depends* , depends-list*)>
				<!ATTLIST mbean code      CDATA  #REQUIRED>
				<!ATTLIST mbean name      CDATA  #REQUIRED>
				<!ATTLIST mbean interface CDATA  #IMPLIED>
				<!ATTLIST mbean xmbean-dd CDATA  #IMPLIED>
				<!ATTLIST mbean xmbean-code CDATA  #IMPLIED>
			 */
			attributes.put("mbean", Arrays.asList(new DTDAttributes[] { 
					new DTDAttributes("code", DTDAttributes.CDATA_TYPE, DTDAttributes.REQUIRED), 
					new DTDAttributes("name", DTDAttributes.CDATA_TYPE, DTDAttributes.REQUIRED), 
					
					new DTDAttributes("interface", DTDAttributes.CDATA_TYPE, DTDAttributes.IMPLIED), 
					new DTDAttributes("xmbean-dd", DTDAttributes.CDATA_TYPE, DTDAttributes.IMPLIED), 
					new DTDAttributes("xmbean-code", DTDAttributes.CDATA_TYPE, DTDAttributes.IMPLIED)
			}));



			/*
				<!ELEMENT arg EMPTY>
				<!ATTLIST arg type  CDATA  #IMPLIED>
				<!ATTLIST arg value CDATA  #REQUIRED>
			 */
			attributes.put("arg", Arrays.asList(new DTDAttributes[] { 
					new DTDAttributes("type", DTDAttributes.CDATA_TYPE, DTDAttributes.IMPLIED),
					new DTDAttributes("value", DTDAttributes.CDATA_TYPE, DTDAttributes.REQUIRED)
			}));

		
			
			/*
				<!ELEMENT attribute ANY>
				<!ATTLIST attribute name CDATA  #REQUIRED>
				<!ATTLIST attribute replace (true | false) 'true'>
				<!ATTLIST attribute trim (true | false) 'true'>
				<!ATTLIST attribute attributeClass CDATA  #IMPLIED>
				<!ATTLIST attribute serialDataType (text | javaBean | jbxb) 'text'>
			 */
			attributes.put("attribute", Arrays.asList(new DTDAttributes[] { 
					new DTDAttributes("name", DTDAttributes.CDATA_TYPE, DTDAttributes.REQUIRED),

					new DTDAttributes("replace", DTDAttributes.ENUM_TYPE, 
							DTDAttributes.IMPLIED, "true", new String[] { "true", "false" }),
					
					new DTDAttributes("trim", DTDAttributes.ENUM_TYPE, 
							DTDAttributes.IMPLIED, "true", new String[] { "true", "false" }),

					new DTDAttributes("attributeClass", DTDAttributes.CDATA_TYPE, DTDAttributes.IMPLIED),

					new DTDAttributes("serialDataType", DTDAttributes.ENUM_TYPE, 
							DTDAttributes.IMPLIED, "text", new String[] { "text", "javaBean", "jbxb"})
			}));

			
			/*
				<!ELEMENT property (#PCDATA)>
				<!ATTLIST property name CDATA #REQUIRED>
			*/
			attributes.put("property", Arrays.asList(new DTDAttributes[] { 
					new DTDAttributes("name", DTDAttributes.CDATA_TYPE, DTDAttributes.REQUIRED)
			}));
			
			/*

				<!ELEMENT depends (#PCDATA | mbean)*>
				<!ATTLIST depends optional-attribute-name CDATA  #IMPLIED>
				<!ATTLIST depends proxy-type CDATA  #IMPLIED>
			*/
			attributes.put("depends", Arrays.asList(new DTDAttributes[] { 
					new DTDAttributes("optional-attribute-name", DTDAttributes.CDATA_TYPE, DTDAttributes.IMPLIED),
					new DTDAttributes("proxy-type", DTDAttributes.CDATA_TYPE, DTDAttributes.IMPLIED)
				}));
			

			/*
				<!ELEMENT depends-list (depends-list-element)+>
				<!ATTLIST depends-list optional-attribute-name CDATA  #IMPLIED>
			*/
			attributes.put("depends-list", Arrays.asList(new DTDAttributes[] { 
					new DTDAttributes("type", DTDAttributes.CDATA_TYPE, DTDAttributes.IMPLIED)
				}));
			

			}
		

		protected void addEmptyDocumentProposals(ContentAssistRequest contentAssistRequest) {
			super.addEmptyDocumentProposals(contentAssistRequest);
			
			int beginPosition = contentAssistRequest.getReplacementBeginPosition();
			String text = "<server>";
			int cursorPos = text.length();
			contentAssistRequest.addProposal(new CompletionProposal(text,  beginPosition, 
					0, cursorPos, null, text, null, null));
			
			text += "\n\t\n</server>";
			contentAssistRequest.addProposal(new CompletionProposal(text,  beginPosition, 
					0, cursorPos+2, null, text.replaceAll("\n", "").replaceAll("\t", ""), null, null));
		}
		
		/**
		 * Seems to be unreachable for me.
		 * If <server> is not present, it seems to not even call the subclass... wierd
		 */
		protected void addStartDocumentProposals(ContentAssistRequest contentAssistRequest) {
			super.addStartDocumentProposals(contentAssistRequest);
		}

		
		protected void addTagNameProposals(ContentAssistRequest contentAssistRequest, int childPosition) {
			String parentElement = contentAssistRequest.getParent().getNodeName();
			String thisNode = contentAssistRequest.getNode().getNodeName();
			if( thisNode.equals("#text")) thisNode = "";
			
			ArrayList possibleNodes = children.get(parentElement);
			ChildOccurances occ;
			for( int i = 0; i < possibleNodes.size(); i++ ) {
				occ = (ChildOccurances)possibleNodes.get(i);
				if( occ.name.startsWith(thisNode)) {
					createAndAddTagCompletionProposals(contentAssistRequest.getNode(), occ, contentAssistRequest.getParent(), 
							contentAssistRequest.getReplacementBeginPosition(), contentAssistRequest, true);
				}
			}
		}

		protected void addTagInsertionProposals(ContentAssistRequest contentAssistRequest, int childPosition) {
			super.addTagInsertionProposals(contentAssistRequest, childPosition);
			List superProps = contentAssistRequest.getProposals();
			ICompletionProposal[] proposals = (ICompletionProposal[]) superProps.toArray(new ICompletionProposal[superProps.size()]);
			ArrayList<String> alreadyAddedStrings = new ArrayList<String>();
			for( int i = 0; i < proposals.length; i++ ) {
				alreadyAddedStrings.add(proposals[i].getDisplayString());
			}

			String parentElement = contentAssistRequest.getParent().getNodeName();
			ArrayList possibleNodes = children.get(parentElement);

			if( possibleNodes == null ) return;
	
			ChildOccurances occ;
			for( int i = 0; i < possibleNodes.size(); i++ ) {
				occ = (ChildOccurances)possibleNodes.get(i);
				createAndAddTagCompletionProposals(contentAssistRequest.getNode(), occ, contentAssistRequest.getParent(), 
						contentAssistRequest.getReplacementBeginPosition(), contentAssistRequest, false);
			}
		}
		
		private void createAndAddTagCompletionProposals(org.w3c.dom.Node thisNode, ChildOccurances occ, 
				org.w3c.dom.Node parentNode, int beginPosition, ContentAssistRequest contentAssistRequest, boolean isTagOpened ) {

			if( !confirmsOccuranceRequirements(thisNode, parentNode, occ)) return;

			
			Image propImage = XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_TAG_GENERIC);
			
			/*
			 * Our tag suggestions should be:
			 *     <element></element>
			 *     <element requiredAttribute="defaultValue" otherRequiredAtt="otherDefault" thirdRequiredNoDefault=""></element>
			 */
			
			// <element></element>
			String nName = (thisNode.getNodeName().equals("#text") ? "" : thisNode.getNodeName());

			
			String emptyCompletionText = occ.name + ">";
			int cursorLoc = emptyCompletionText.length();
			emptyCompletionText += "</" + occ.name + ">";
			String descriptionText = "";

			if( !isTagOpened ) {
				emptyCompletionText = "<" + emptyCompletionText;
				cursorLoc++;
			}
			descriptionText = occ.name;
			
			
			contentAssistRequest.addProposal(new CompletionProposal(emptyCompletionText,  beginPosition, 
					nName.length(), cursorLoc, propImage, descriptionText, null, null));
			
			
			if( attributes.containsKey(occ.name) ) {
				List l = attributes.get(occ.name);
				Iterator i = l.iterator();
				cursorLoc = -1;
				String attributes = "";
				while( i.hasNext()) {
					DTDAttributes attribute = (DTDAttributes)i.next();
					if( attribute.mandatory == DTDAttributes.REQUIRED) {
						attributes += " " + attribute.name + "=\"" + attribute.defaultValue + "\"";
						if( cursorLoc == -1 && attribute.defaultValue.equals("")) {
							cursorLoc = occ.name.length() + attributes.length() - 1;
						}
					}
				}
				
				String requiredCompletionText = occ.name + attributes + "></" + occ.name + ">";

				if( !isTagOpened ) {
					requiredCompletionText = "<" + requiredCompletionText;
					cursorLoc++;
				}
				descriptionText = occ.name + "  (with attributes)";

				if( !attributes.equals("")) {
					contentAssistRequest.addProposal(new CompletionProposal(requiredCompletionText,  
							beginPosition, nName.length(), cursorLoc != -1 ? cursorLoc : requiredCompletionText.length(), 
							propImage, descriptionText, null, null));
				}
			}
		}
		
		private boolean confirmsOccuranceRequirements(org.w3c.dom.Node thisNode, org.w3c.dom.Node parentNode,ChildOccurances occ) {
			// if only allowed once, prevent second usage?
			return true;
		}
		
		protected void addAttributeNameProposals(ContentAssistRequest contentAssistRequest) {
			super.addAttributeNameProposals(contentAssistRequest);
			List superProps = contentAssistRequest.getProposals();
			ICompletionProposal[] proposals = (ICompletionProposal[]) superProps.toArray(new ICompletionProposal[superProps.size()]);
			ArrayList<String> alreadyAddedStrings = new ArrayList<String>();
			for( int i = 0; i < proposals.length; i++ ) {
				alreadyAddedStrings.add(proposals[i].getDisplayString());
			}
			
			
			Image attImage = XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_ATTRIBUTE);

			ArrayList<String> activeAttributes = new ArrayList<String>();
			NamedNodeMap nnl = contentAssistRequest.getNode().getAttributes();
			for( int i = 0; i < nnl.getLength(); i++ ) {
				activeAttributes.add(nnl.item(i).getNodeName());
			}
			
			
			String elementName = contentAssistRequest.getNode().getNodeName();
			String match = contentAssistRequest.getMatchString();
			List list = attributes.get(elementName);
			Iterator i = list.iterator();
			while(i.hasNext()) {
				DTDAttributes att = (DTDAttributes)i.next();
				if( att.name.startsWith(match) && !activeAttributes.contains(att.name) && !alreadyAddedStrings.contains(att.name)) {
					String txt = att.name + "=\"" + att.defaultValue + "\"";
					contentAssistRequest.addProposal(new CompletionProposal(txt,  contentAssistRequest.getReplacementBeginPosition(), 
							match.length(), txt.length()-1, attImage, att.name, null, null));
				}
			}
		}
		
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
				if( elementName.equals("mbean") && attribute.getName().equals("code")) {
					handleCodeClassNameCompletion(contentAssistRequest);
				}
				if( elementName.equals("attribute") && attribute.getName().equals("name")) {
					handleAttributeNamesCompletion(contentAssistRequest);
				}
			}
		}
		
		protected void handleCodeClassNameCompletion(ContentAssistRequest contentAssistRequest) {
			String match = contentAssistRequest.getMatchString();
			String attributeCurrentValue;
			if( match.startsWith("\"")) attributeCurrentValue = match.substring(1);
			else attributeCurrentValue = match;
			
			ResultFilter filter = new ResultFilter() {
				public boolean accept(Object found) {
					try {
					if( found instanceof IPackageFragment ) {
						return ((IPackageFragment)found).containsJavaResources(); 
					} else if( found instanceof IType ) {
						// only show MBeans
						IType type = (IType)found;
						String[] interfaces = type.getSuperInterfaceNames();
						for( int i = 0; i < interfaces.length; i++ ) {
							if( interfaces[i].equals(type.getElementName() + "MBean")) {
								return true;
							}
						}
					}
					} catch( JavaModelException jme ) {
						// do nothing  
					}
					return false;
				}
			};
			PackageTypeSearcher searcher = new PackageTypeSearcher(attributeCurrentValue, filter);
			ICompletionProposal[] props = searcher.generateProposals(contentAssistRequest.getReplacementBeginPosition()+1);
			for( int i = 0; i < props.length; i++ ) 
				contentAssistRequest.addProposal(props[i]);

		}
		
		protected void handleAttributeNamesCompletion(ContentAssistRequest contentAssistRequest) {
			String match = contentAssistRequest.getMatchString();
			String attributeCurrentValue;
			if( match.startsWith("\"")) attributeCurrentValue = match.substring(1);
			else attributeCurrentValue = match;

			
			Node node = contentAssistRequest.getNode();
			Node mbeanNode = node.getParentNode();
			NamedNodeMap mbeanAttributes = mbeanNode.getAttributes();
			Node att = mbeanAttributes.getNamedItem("code");
			String codeClass = att.getNodeValue();
			IType type = ServiceXMLEditorUtil.findType(codeClass);
			if( type != null ) {
				IMethod[] methods = ServiceXMLEditorUtil.getAllMethods(type);
				String[] attributeNames = ServiceXMLEditorUtil.findAttributesFromMethods(methods, attributeCurrentValue);
				
				int beginReplacement = contentAssistRequest.getReplacementBeginPosition()+1;
				// Now turn them into proposals
				for( int i = 0; i < attributeNames.length; i++ ) {
					CompletionProposal cp = new CompletionProposal(attributeNames[i], beginReplacement, 
							attributeCurrentValue.length(), beginReplacement + attributeNames[i].length());
					contentAssistRequest.addProposal(cp);
				}
			}
				
		}
		
		/**
		 * Gets all methods that this type, or its super-types, have.
		 * @param type
		 * @return
		 */

	}
	
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		if (sourceViewer == null || hyperLinksEnabled() ) {
			return null;
		}
		
		IHyperlinkDetector[] baseDetectors =  super.getHyperlinkDetectors(sourceViewer);
		ServiceXMLHyperLinkDetector hyperlinkDetector = new ServiceXMLHyperLinkDetector();
		if(baseDetectors==null || baseDetectors.length==0) {
			return new IHyperlinkDetector[] { hyperlinkDetector };
		} else {
			IHyperlinkDetector[] result = new IHyperlinkDetector[baseDetectors.length+1];
			result[0] = hyperlinkDetector;
			for (int i = 0; i < baseDetectors.length; i++) {
				result[i+1] = baseDetectors[i]; 
			}
			return result;
		}
	}

	private boolean hyperLinksEnabled() {
		return !fPreferenceStore.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINKS_ENABLED);
	}

	public class ServiceXMLHyperLinkDetector extends BaseXMLHyperlinkUtil implements IHyperlinkDetector {

		public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
			if (region == null || textViewer == null) {
				return null;
			}
			IDocument document = textViewer.getDocument();
			Node currentNode = getCurrentNode(document, region.getOffset() );
			Attr attr = getCurrentAttrNode(currentNode, region.getOffset());
			
			if( currentNode.getNodeName().equals("mbean") && attr != null && attr.getName().equals("code")) {

				IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
				SearchPattern codePattern = SearchPattern.createPattern(attr.getValue(), 
						IJavaSearchConstants.CLASS,
						IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
				SearchEngine searchEngine = new SearchEngine();
				LocalSearchRequestor requestor = new LocalSearchRequestor();
				try {
				searchEngine.search(codePattern, new SearchParticipant[]
				           {SearchEngine.getDefaultSearchParticipant()}, scope, requestor, new NullProgressMonitor());
				Object[] results = requestor.getResults();
				if( results.length == 1 ) {
					if( results[0] instanceof IJavaElement ) 
						return new IHyperlink[] { 
							new ServiceXMLHyperlink((IJavaElement)results[0], 
									getHyperlinkRegion(attr)) };
				}
				} catch( Exception ce ) {
					// do nothing
				}
			}
			return null;
		}
		protected class ServiceXMLHyperlink implements IHyperlink {

			private IJavaElement element;
			private IRegion region;
			public ServiceXMLHyperlink(IJavaElement element, IRegion region ) {
				this.element = element;
				this.region = region;
			}
			public IRegion getHyperlinkRegion() {
				return region;
			}

			public String getHyperlinkText() {
				return null;
			}

			public String getTypeLabel() {
				return null;
			}

			public void open() {
				try {
					IEditorPart part = EditorUtility.openInEditor(element, true);
					if(part!=null) {
						EditorUtility.revealInEditor(part, element);
					}
				} catch (JavaModelException e) {
					// ignore...TODO?	
				} catch (PartInitException e) {
				}
			}
			
		}
		protected class LocalSearchRequestor extends SearchRequestor {
			private ArrayList<Object> list;
			public LocalSearchRequestor() {
				list = new ArrayList<Object>();
			}
			public void acceptSearchMatch(SearchMatch match) throws CoreException {
				list.add(match.getElement());
			}
			public Object[] getResults() {
				return list.toArray();
			}

		}
	}
	
	
		
}
