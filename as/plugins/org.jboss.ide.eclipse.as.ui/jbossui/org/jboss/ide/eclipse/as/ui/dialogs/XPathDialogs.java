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
package org.jboss.ide.eclipse.as.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.jboss.ide.eclipse.as.core.model.DescriptorModel;
import org.jboss.ide.eclipse.as.core.model.DescriptorModel.ServerDescriptorModel;
import org.jboss.ide.eclipse.as.core.model.DescriptorModel.ServerDescriptorModel.XPathTreeItem;
import org.jboss.ide.eclipse.as.core.model.DescriptorModel.ServerDescriptorModel.XPathTreeItem2;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.ServerAttributeHelper.XPathPreferenceTreeItem;
import org.jboss.ide.eclipse.as.core.util.SimpleTreeItem;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.views.server.providers.DescriptorXPathViewProvider.XPathPropertyLabelProvider;

public class XPathDialogs {

	public static class XPathCategoryDialog extends Dialog {

		private String textValue;
		private Label errorLabel;
		private SimpleTreeItem tree;
		
		public XPathCategoryDialog(Shell parentShell, SimpleTreeItem tree) {
			super(parentShell);
			this.tree = tree;
		}
		public XPathCategoryDialog(Shell parentShell, SimpleTreeItem tree, String startText) {
			super(parentShell);
			this.tree = tree;
			this.textValue = startText;
		}
		
		
		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			shell.setText(Messages.XPathNewCategory);
		}
		
		protected Control createDialogArea(Composite parent) {
			Composite c = (Composite)super.createDialogArea(parent);
			c.setLayout(new FormLayout());
			
			errorLabel = new Label(c, SWT.NONE);
			errorLabel.setText(Messages.XPathNewCategoryNameInUse);
			FormData errorData = new FormData();
			errorData.left = new FormAttachment(0,5);
			errorData.top = new FormAttachment(0,5);
			errorLabel.setLayoutData(errorData);
			errorLabel.setVisible(false);
			
			Label l = new Label(c, SWT.NONE);
			l.setText(Messages.XPathCategoryName);
			FormData labelData = new FormData();
			labelData.left = new FormAttachment(0,5);
			labelData.top = new FormAttachment(errorLabel,5);
			l.setLayoutData(labelData);

			
			final Text t = new Text(c, SWT.BORDER); 
			FormData tData = new FormData();
			tData.left = new FormAttachment(l,5);
			tData.top = new FormAttachment(errorLabel,5);
			tData.right = new FormAttachment(100, -5);
			t.setLayoutData(tData);

			if( textValue != null ) {
				t.setText(textValue);
			}
			
			t.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					verifyText(t.getText());
				} 
			});
			
			return c;
		}
		
		private void verifyText(String text) {
			boolean valid = true;
			SimpleTreeItem[] kids = tree.getChildren();
			for( int i = 0; i < kids.length; i++ ) {
				if( text.equals(kids[i].getData())) 
					valid = false;
			}
			
			if( valid ) {
				errorLabel.setVisible(false);
				textValue = text;
				getButton(IDialogConstants.OK_ID).setEnabled(true);
			} else {
				errorLabel.setVisible(true);
				getButton(IDialogConstants.OK_ID).setEnabled(false);
			}
			
		}
		
		public String getText() {
			return textValue;
		}
	}

	
	public static class XPathDialog extends Dialog {

		private Label errorImage, errorLabel;
		private Text nameText, xpathText, attributeText;
		private Label nameLabel, xpathLabel, attributeLabel;
		private Button previewButton;
		
		private SimpleTreeItem tree;
		private String category;
		private JBossServer server;
		private String name, xpath, attribute;
		private String originalName = null;
		private XPathPreferenceTreeItem original = null;
		int previewId = 48879;
		
		private Tree previewTree;
		private TreeColumn column, column2, column3;
		private TreeViewer previewTreeViewer;
		private Composite main;
		
		public XPathDialog(Shell parentShell, SimpleTreeItem tree, String categoryName, JBossServer server) {
			super(parentShell);
			this.tree = tree;
			this.category = categoryName;
			this.server = server;
		}

		public XPathDialog(Shell parentShell, SimpleTreeItem tree, String categoryName, JBossServer server, String originalName) {
			super(parentShell);
			this.tree = tree;
			this.category = categoryName;
			this.server = server;
			this.originalName = this.name = originalName;
		}

		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			setShellStyle(getShellStyle() | SWT.RESIZE);
			shell.setText(Messages.XPathNewXpath);
			shell.setBounds(shell.getLocation().x, shell.getLocation().y, 550, 400);
		}
		
		protected void createButtonsForButtonBar(Composite parent) {
			// create OK and Cancel buttons by default
			super.createButtonsForButtonBar(parent);
			previewButton = createButton(parent, previewId, "Preview", true);
			if( name == null ) getButton(IDialogConstants.OK_ID).setEnabled(false);

			addListeners();
		}

		
		protected Control createDialogArea(Composite parent) {
			main = (Composite)super.createDialogArea(parent);
			main.setLayout(new FormLayout());
			layoutWidgets(main);
			
			if( name != null ) nameText.setText(name);
			if( attribute != null ) attributeText.setText(attribute);
			if( xpath != null ) xpathText.setText(xpath);
			
			 ContentProposalAdapter adapter = new
			 ContentProposalAdapter(xpathText, new TextContentAdapter(),
			                                 new XPathProposalProvider(server), null, null);
			                
			 adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);

			return main;
		} 
		
		protected void addListeners() {
			nameText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					verifyName();
					name = nameText.getText();
				} 
			});
			attributeText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					attribute = attributeText.getText();
				} 
			});
			xpathText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					xpath = xpathText.getText();
				} 
			});
			
			previewButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}
				public void widgetSelected(SelectionEvent e) {
					previewPressed();
				} 
			});
		}
		protected void verifyName() {
			
			if( nameText.getText().equals("")) {
				errorLabel.setText(Messages.XPathNameEmpty);
				errorLabel.setVisible(true);
				errorImage.setVisible(true);
				getButton(IDialogConstants.OK_ID).setEnabled(false);
				return;
			}
			
			SimpleTreeItem[] categories = tree.getChildren();
			SimpleTreeItem categoryItem = null;
			for( int i = 0; i < categories.length; i++ ) {
				if( categories[i].getData().equals(category)) 
					categoryItem = categories[i];
			}
			if( categoryItem != null ) {
				SimpleTreeItem[] xpathNames = categoryItem.getChildren();
				boolean found = false;
				for( int i = 0; i < xpathNames.length; i++ ) {
					if(nameText.getText().equals( ((XPathPreferenceTreeItem)xpathNames[i]).getName())) {
						
						if( originalName == null || !nameText.getText().equals(originalName)) 
							found = true;
					}
				}
				if( found ) {
					// error, name in use
					errorLabel.setText(Messages.XPathNameInUse);
					errorLabel.setVisible(true);
					errorImage.setVisible(true);
					getButton(IDialogConstants.OK_ID).setEnabled(false);
				} else {
					errorLabel.setVisible(false);
					errorImage.setVisible(false);
					getButton(IDialogConstants.OK_ID).setEnabled(true);
				}
			}
		}
		protected void previewPressed() {
			XPathTreeItem[] item = server.getDescriptorModel().getXPath(xpathText.getText(), attributeText.getText());
			ArrayList list = new ArrayList();
			list.addAll(Arrays.asList(item));
			previewTreeViewer.setInput(list);
			
			if( list.size() == 0 ) {
				errorImage.setVisible(true);
				errorLabel.setText("No XML elements matched your search.");
				errorLabel.setVisible(true);
			}
			
			main.layout();
		}
		protected void layoutWidgets(Composite c) {
			// create widgets
			errorLabel = new Label(c, SWT.NONE);
			errorImage = new Label(c, SWT.NONE);
			errorImage.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
			
			nameLabel = new Label(c, SWT.NONE);
			xpathLabel = new Label(c, SWT.NONE);
			attributeLabel = new Label(c, SWT.NONE);
			nameText= new Text(c, SWT.BORDER);
			xpathText = new Text(c, SWT.BORDER);
			attributeText = new Text(c, SWT.BORDER);

			// Now do the tree and viewer
			previewTree = new Tree(c, SWT.BORDER);
			previewTree.setHeaderVisible(true);
			previewTree.setLinesVisible(true);
			column = new TreeColumn(previewTree, SWT.NONE);
			column2 = new TreeColumn(previewTree, SWT.NONE);
			column3 = new TreeColumn(previewTree, SWT.NONE);
			
			column.setText(Messages.XPathColumnLocation);
			column2.setText(Messages.XPathColumnAttributeVals);
			column3.setText(Messages.XPathColumnRawXML);

			column.setWidth(100);
			column2.setWidth(100);
			column3.setWidth(100);

			previewTreeViewer = new TreeViewer(previewTree);

			
			// set some text
			nameLabel.setText(Messages.XPathName);
			xpathLabel.setText(Messages.XPathPattern);
			attributeLabel.setText(Messages.XPathAttribute);

			c.layout();
			int pixel = Math.max(Math.max(nameLabel.getSize().x, xpathLabel.getSize().x), attributeLabel.getSize().x);
			pixel += 5;
			
			// Lay them out
			//errorLabel.setText("Category Name In Use.");
			FormData errorData = new FormData();
			errorData.left = new FormAttachment(errorImage,5);
			errorData.top = new FormAttachment(0,5);
			errorData.right = new FormAttachment(0,300);
			errorLabel.setLayoutData(errorData);
			errorLabel.setVisible(false);
			
			FormData errorImageData = new FormData();
			errorImageData.left = new FormAttachment(0,5);
			errorImageData.top = new FormAttachment(0,5);
			errorImage.setLayoutData(errorImageData);
			errorImage.setVisible(false);

			
			/* Name */
			FormData nameLabelData = new FormData();
			nameLabelData.left = new FormAttachment(0,5);
			nameLabelData.top = new FormAttachment(errorLabel,5);
			nameLabel.setLayoutData(nameLabelData);

			FormData nameTextData = new FormData();
			nameTextData.left = new FormAttachment(0,pixel);
			nameTextData.right = new FormAttachment(100,-5);
			nameTextData.top = new FormAttachment(errorLabel,4);
			nameText.setLayoutData(nameTextData);


			/* XPath */
			FormData xpathLabelData = new FormData();
			xpathLabelData.left = new FormAttachment(0,5);
			xpathLabelData.top = new FormAttachment(nameText,5);
			xpathLabel.setLayoutData(xpathLabelData);

			FormData xpathTextData = new FormData();
			xpathTextData.left = new FormAttachment(0,pixel);
			xpathTextData.right = new FormAttachment(100,-5);
			xpathTextData.top = new FormAttachment(nameText,4);
			xpathText.setLayoutData(xpathTextData);


			/* Attribute */
			FormData attributeLabelData = new FormData();
			attributeLabelData.left = new FormAttachment(0,5);
			attributeLabelData.top = new FormAttachment(xpathText,5);
			attributeLabel.setLayoutData(attributeLabelData);

			FormData attributeTextData = new FormData();
			attributeTextData.left = new FormAttachment(0, pixel);
			attributeTextData.right = new FormAttachment(100,-5);
			attributeTextData.top = new FormAttachment(xpathText,4);
			
			attributeText.setLayoutData(attributeTextData);
			
			
			// Tree layout data
			FormData previewTreeData = new FormData();
			previewTreeData.left = new FormAttachment(0,5);
			previewTreeData.right = new FormAttachment(100,-5);
			previewTreeData.top = new FormAttachment(attributeText,5);
			previewTreeData.bottom = new FormAttachment(100,-5);
			previewTree.setLayoutData(previewTreeData);
			
			previewTreeViewer.setContentProvider(new ITreeContentProvider() {
				public Object[] getChildren(Object parentElement) {
					// we're a leaf
					if( parentElement instanceof XPathTreeItem2 ) 
						return new Object[0];
					
					// we're a file node (blah.xml) 
					if( parentElement instanceof XPathTreeItem ) {
						if( ((XPathTreeItem)parentElement).getChildren().length > 1 ) 
							return ((XPathTreeItem)parentElement).getChildren();
						return new Object[0];
					}
					
					// we're the named element (JNDI)
					if( parentElement instanceof XPathPreferenceTreeItem ) {
						SimpleTreeItem[] kids = ((XPathPreferenceTreeItem)parentElement).getChildren();
						return kids;
					}

					return new Object[0];
				}

				public Object getParent(Object element) {
					return null;
				}

				public boolean hasChildren(Object element) {
					return getChildren(element).length > 0 ? true : false;
				}

				public Object[] getElements(Object inputElement) {
					if( inputElement instanceof ArrayList ) {
						return ((ArrayList)inputElement).toArray();
					}
					return new Object[0];
				}

				public void dispose() {
				}

				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				} 
				
			});
			
			previewTreeViewer.setLabelProvider(new XPathPropertyLabelProvider());
		}

		public String getAttribute() {
			return attribute;
		}

		public String getName() {
			return name;
		}

		public String getXpath() {
			return xpath;
		}

		public void setAttribute(String attribute) {
			this.attribute = attribute;
			if( attributeText != null && !attributeText.isDisposed())
				attributeText.setText(this.attribute);
		}

		public void setName(String name) {
			this.name = name;
			if( nameText != null && !nameText.isDisposed())
				nameText.setText(this.name);
		}

		public void setXpath(String xpath) {
			this.xpath = xpath;
			if( xpathText != null && !xpathText.isDisposed())
				xpathText.setText(this.xpath);
		}
	}
	
	
	
	public static class XPathProposalProvider implements IContentProposalProvider {
		
		private static final int NEW_ELEMENT = 1;
		private static final int NEW_ATTRIBUTE = 2;
		private static final int NEW_ATTRIBUTE_VALUE = 3;
		private static final int IN_ELEMENT = 4;
		private static final int IN_ATTRIBUTE = 5;
		private static final int IN_ATTRIBUTE_VALUE = 6;
		private static final int CLOSE_ATTRIBUTE = 7;
		
		
		private JBossServer server;
		private ServerDescriptorModel model;
		public XPathProposalProvider(JBossServer server) {
			this.server = server;
			String serverConfDir = server.getConfigDirectory(false);
			model = DescriptorModel.getDefault().getServerModel(new Path(serverConfDir));
		}
		public IContentProposal[] getProposals(String contents, int position) {
			if( contents.equals("") || contents.equals("/") || contents.equals(" ")) {
				return new IContentProposal[] { new XPathContentProposal("/server/", "/server/".length(), null, null)};
			}
			
			int type = getType(contents);
			if( type == NEW_ELEMENT ) return getElementProposals(contents, "");
			if( type == IN_ELEMENT ) return getElementProposals(contents);
			if( type == NEW_ATTRIBUTE ) return getAttributeNameProposals(contents.substring(0, contents.length()-1), "");
			if( type == IN_ATTRIBUTE ) return getAttributeNameProposals(contents);
			if( type == NEW_ATTRIBUTE_VALUE ) return getAttributeValueProposals(contents, "");
			if( type == IN_ATTRIBUTE_VALUE ) return getAttributeValueProposals(contents);
			return null;
		}
		
		protected XPathTreeItem2[] getXPath(String xpath) {
			ArrayList list = new ArrayList();
			XPathTreeItem[] items = model.getXPath(xpath);
			for( int i = 0; i < items.length; i++ ) {
				SimpleTreeItem[] children = items[i].getChildren();
				for( int j = 0; j < children.length; j++ ) {
					XPathTreeItem2 i2 = (XPathTreeItem2)children[j];
					list.add(i2);
				}
			}
			return (XPathTreeItem2[]) list.toArray(new XPathTreeItem2[list.size()]);
		}
		
		public IContentProposal[] getElementProposals(String path) {
			String parentPath = path.substring(0, path.lastIndexOf('/') + 1);
			String prefix = path.substring(path.lastIndexOf('/') + 1);
			return getElementProposals(parentPath, prefix);
		}
		
		public IContentProposal[] getElementProposals(String parentPath, String elementPrefix ) {
			String[] strings = getElementProposalStrings(parentPath, elementPrefix );
			return convertProposals(strings);
		}
		
		public String[] getElementProposalStrings(String parentPath, String elementPrefix) {
			ArrayList list = new ArrayList();
			XPathTreeItem2[] items = getXPath(parentPath + "*");
			for( int i = 0; i < items.length; i++ ) {
				if( items[i].getElementName().startsWith(elementPrefix) && !list.contains(parentPath + items[i].getElementName()))
					list.add(parentPath + items[i].getElementName());
			}
			return (String[]) list.toArray(new String[list.size()]);
		}
		
		public IContentProposal[] getAttributeNameProposals(String path) {
			String parent = path.substring(0, path.lastIndexOf('['));
			int attName = path.lastIndexOf('[') > path.lastIndexOf('@') ? path.lastIndexOf('[') : path.lastIndexOf('@');
			return getAttributeNameProposals(parent, path.substring(attName+1));
		}
		public IContentProposal[] getAttributeNameProposals(String parentPath, String remainder) {
			ArrayList names = new ArrayList();
			XPathTreeItem2[] items = getXPath(parentPath);
			String[] attributes;
			for( int i = 0; i < items.length; i++ ) {
				attributes = items[0].getElementAttributeNames();
				for( int j = 0; j < attributes.length; j++ ) {
					if( attributes[j].startsWith(remainder) && !names.contains(attributes[j])) 
						names.add(attributes[j]);
				}
			}
			
			String[] results = new String[names.size()];
			for( int i = 0; i < results.length; i++ ) {
				results[i] = parentPath + "[@" + names.get(i) + "=";
			}
			return convertProposals(results);
		}
		
		public IContentProposal[] getAttributeValueProposals(String path) {
			return getAttributeValueProposals(path.substring(0, path.lastIndexOf('=')), path.substring(path.lastIndexOf('=')+1));
		}
		public IContentProposal[] getAttributeValueProposals(String parentPath, String remainder) {
			String parentElementPath = parentPath.substring(0, parentPath.lastIndexOf('['));
			int brackIndex = parentPath.lastIndexOf('[');
			int eqIndex = parentPath.lastIndexOf('=') == -1 ? parentPath.length() : parentPath.lastIndexOf('=');
			if( eqIndex < brackIndex ) eqIndex = parentPath.length();
			String attName = parentPath.substring(brackIndex+2, eqIndex);

			if( remainder.startsWith("'")) remainder = remainder.substring(1);
			ArrayList values = new ArrayList();
			XPathTreeItem2[] items = getXPath(parentElementPath);
			String[] attributes;
			for( int i = 0; i < items.length; i++ ) {
				attributes = items[i].getElementAttributeValues(attName);
				for( int j = 0; j < attributes.length; j++ ) {
					if( attributes[j].startsWith(remainder) && !values.contains(attributes[j])) 
						values.add(attributes[j]);
				}
			}
			
			String[] results = new String[values.size()];
			String prefix = parentElementPath + "[@" + attName + "='";
			for( int i = 0; i < results.length; i++ ) {
				results[i] = prefix + values.get(i) + "']/";
			}
			Arrays.sort(results);
			return convertProposals(results);
		}
		
		public int getType(String contents) {
			switch(contents.charAt(contents.length()-1)) {
				case '/':
					return NEW_ELEMENT;
				case '[':
					return NEW_ATTRIBUTE;
				case ']':
					return CLOSE_ATTRIBUTE;
				case '=':
					return NEW_ATTRIBUTE_VALUE;
				default:
					int max = -1;
					int lastSlash = contents.lastIndexOf('/'); max = (lastSlash > max ? lastSlash : max);
					int lastOpenBracket = contents.lastIndexOf('['); max = (lastOpenBracket > max ? lastOpenBracket : max);
					int lastCloseBracket = contents.lastIndexOf(']'); max = (lastCloseBracket > max ? lastCloseBracket : max);
					int lastEquals = contents.lastIndexOf('='); max = (lastEquals > max ? lastEquals : max);
					
					if( max == lastSlash ) return IN_ELEMENT;
					if( max == lastOpenBracket ) return IN_ATTRIBUTE;
					if( max == lastCloseBracket ) return CLOSE_ATTRIBUTE;
					if( max == lastEquals ) return IN_ATTRIBUTE_VALUE;
					break;
			}
			return -1;
		}
		
		public IContentProposal[] convertProposals(String[] strings) {
			ArrayList list = new ArrayList();
			for( int i = 0; i < strings.length; i++ ) {
				list.add(new XPathContentProposal(strings[i], strings[i].length(), null, null));
			}
			return (IContentProposal[]) list.toArray(new IContentProposal[list.size()]);
		}
		
		public class XPathContentProposal implements IContentProposal {
			private String content,description,label;
			private int position;
			public XPathContentProposal(String content, int position, String description, String label) {
				this.content = content;
				this.description = description;
				this.label = label;
				this.position = position;
			}
			public String getContent() {
				return content;
			}

			public int getCursorPosition() {
				return position;
			}

			public String getDescription() {
				return description;
			}

			public String getLabel() {
				return label;
			}
		}
		
		public String[] getAllSuggegstions() {
			HashMap possibilities = new HashMap();
			XPathTreeItem[] items = model.getXPath("/server/mbean");
			for( int i = 0; i < items.length; i++ ) {
				SimpleTreeItem[] children = items[i].getChildren();
				for( int j = 0; j < children.length; j++ ) {
					XPathTreeItem2 i2 = (XPathTreeItem2)children[j];
					possibilities.put(i2.getElementName(), i2.getElementName());
				}
			}
			return (String[]) possibilities.keySet().toArray(new String[possibilities.size()]);
		}
		
	}
}
