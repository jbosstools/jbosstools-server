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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XMLDocumentRepository;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathCategory;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathModel;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathQuery;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult.XPathResultNode;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.views.server.providers.descriptors.XPathPropertyLabelProvider;


/**
 * A class with some XPath-related dialogs
 */
public class XPathDialogs {
	public static class XPathCategoryDialog extends Dialog {

		private String initialName;
		private String currentText;
		private IServer server;
		private Label errorLabel;
		
		public XPathCategoryDialog(Shell parentShell, IServer server) {
			super(parentShell);
			this.server = server;
		}
		public XPathCategoryDialog(Shell parentShell, IServer server, String initialName) {
			this(parentShell, server);
			this.initialName = initialName;
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

			if( currentText != null ) {
				t.setText(currentText);
			}
			
			t.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					verifyText(t.getText());
				} 
			});
			
			return c;
		}
		
		private void verifyText(String text) {
			boolean valid = !XPathModel.getDefault().containsCategory(server, text) || (initialName != null && initialName.equals(text));
			if( valid ) {
				errorLabel.setVisible(false);
				currentText = text;
				getButton(IDialogConstants.OK_ID).setEnabled(true);
			} else {
				errorLabel.setVisible(true);
				getButton(IDialogConstants.OK_ID).setEnabled(false);
			}
		}
		
		public String getText() {
			return currentText;
		}
	}

	public static class XPathDialog extends Dialog {

		protected Label errorImage, errorLabel, descriptionLabel;
		protected Text nameText, xpathText, attributeText;
		protected Combo categoryCombo, serverCombo;
		protected Label nameLabel, serverLabel, categoryLabel, xpathLabel, attributeLabel;
		protected Button previewButton;
		
		protected XPathProposalProvider proposalProvider;
		
		protected IServer server;
		protected String name, xpath, attribute, category;
		protected String originalName = null;
		protected XPathQuery original = null;
		protected int previewId = 48879;
		
		protected Tree previewTree;
		protected TreeColumn column, column2;
		protected TreeViewer previewTreeViewer;
		protected Composite main;
		protected XMLDocumentRepository repository;
		
		public XPathDialog(Shell parentShell) {
			this(parentShell, null);
		}
		public XPathDialog(Shell parentShell, IServer server) {
			this(parentShell, server, null);
		}
		public XPathDialog(Shell parentShell, IServer server, String categoryName) {
			this(parentShell, server, categoryName, null);
		}

		public XPathDialog(Shell parentShell, IServer server, String categoryName, String originalName) {
			super(parentShell);
			setShellStyle(getShellStyle() | SWT.RESIZE);
			this.category = categoryName;
			this.server = server;
			this.originalName = this.name = originalName;
			repository = new XMLDocumentRepository(XMLDocumentRepository.getDefault());
		}

		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			shell.setText(Messages.XPathNewXpath);
			shell.setBounds(shell.getLocation().x, shell.getLocation().y, 550, 400);
		}
	    protected int getShellStyle() {
	        int ret = super.getShellStyle();
	        return ret | SWT.RESIZE;
	    }
		
		protected void createButtonsForButtonBar(Composite parent) {
			// create OK and Cancel buttons by default
			super.createButtonsForButtonBar(parent);
			previewButton = createButton(parent, previewId, "Preview", true);
			if( name == null ) getButton(IDialogConstants.OK_ID).setEnabled(false);
			addListeners();
			try {
				checkErrors();
			} catch( Exception e ) {
				e.printStackTrace();
			}
		}

		
		protected Control createDialogArea(Composite parent) {
			main = (Composite)super.createDialogArea(parent);
			main.setLayout(new FormLayout());
			layoutWidgets(main);
			fillCombos();
			if( name != null ) nameText.setText(name);
			if( attribute != null ) attributeText.setText(attribute);
			if( xpath != null ) xpathText.setText(xpath);
			
			proposalProvider = new XPathProposalProvider(repository);
			proposalProvider.setPath(getConfigFolder(server));
			ContentProposalAdapter adapter = new
			ContentProposalAdapter(xpathText, new TextContentAdapter(),
					proposalProvider, null, null);
			adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
			
			XPathAttributeProposalProvider provider2 = new XPathAttributeProposalProvider(repository, xpathText);
			provider2.setPath(getConfigFolder(server));
			ContentProposalAdapter adapter2 = new
			ContentProposalAdapter(attributeText, new TextContentAdapter(),
					provider2, null, null);
			adapter2.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
			
			return main;
		} 
		
		protected void fillCombos() {
			if( serverCombo != null ) {
				IServer servers[] = ServerConverter.getJBossServersAsIServers();
				String[] names = new String[servers.length];
				for( int i = 0; i < servers.length; i++ ) {
					names[i] = servers[i].getName();
				}
				serverCombo.setItems(names);
			}
			
			if( categoryCombo != null ) { 
				refreshCategoryCombo();
			}
		}
		
		protected void addListeners() {
			nameText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					checkErrors();
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
			
			
			if( serverCombo != null ) {
				serverCombo.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent e) {
					}
					public void widgetSelected(SelectionEvent e) {
						int index = serverCombo.getSelectionIndex();
						String val = serverCombo.getItem(index);
						IServer[] list = ServerCore.getServers();
						for( int i = 0; i < list.length; i++ ) {
							if( list[i].getName().equals(val)) {
								setServer(list[i]);
								return;
							}
						}
					} 
				});
			}
			
			if( categoryCombo != null ) {
				categoryCombo.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						category = categoryCombo.getText();
						checkErrors();
					} 
				});
				categoryCombo.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent e) {
					}
					public void widgetSelected(SelectionEvent e) {
						category = categoryCombo.getText();
						checkErrors();
					}
				});
			}
		}
		
		
		protected void checkErrors() {
			ArrayList errorList = getErrors();
			if( errorList.size() == 0 ) { 
				setError(null); 
				getButton(IDialogConstants.OK_ID).setEnabled(true);
				return; 
			}
			setError((String)errorList.get(0));
			if( getButton(IDialogConstants.OK_ID) != null ) 
				 getButton(IDialogConstants.OK_ID).setEnabled(false);
			return;
		}
		protected ArrayList getErrors() {
			ArrayList list = new ArrayList();
			String serverError = getServerError(); if( serverError != null ) list.add(serverError);
			String nameError = getNameError(); if( nameError != null ) list.add(nameError);
			String categoryError = getCategoryError(); if( categoryError != null ) list.add(categoryError);
			return list;
		}
		
		protected String getServerError() {
			if( server == null ) return "Please Select a Server";
			return null;
		}
		
		protected String getCategoryError() {
			if( "".equals(category)) {
				return "Category must not be blank";
			}
			return null;
		}
		
		protected void setError(String message) {
			if( message == null ) {
				errorImage.setVisible(false);
				errorLabel.setVisible(false);
				errorLabel.setText("");
			} else {
				errorImage.setVisible(true);
				errorLabel.setVisible(true);
				errorLabel.setText(message);
			}
		}
		
		protected String getNameError() {
			if( nameText.getText().equals("")) {
				return Messages.XPathNameEmpty;
			}
			if( server == null ) return null;
			XPathCategory[] categories = XPathModel.getDefault().getCategories(server);
			XPathCategory category = null;
			for( int i = 0; i < categories.length; i++ ) {
				if( categories[i].getName().equals(this.category)) 
					category = categories[i];
			}
			if( category != null ) {
				XPathQuery[] queries = category.getQueries();
				boolean found = false;
				for( int i = 0; i < queries.length; i++ ) {
					if(nameText.getText().equals( ((XPathQuery)queries[i]).getName())) {
						
						if( originalName == null || !nameText.getText().equals(originalName)) 
							return Messages.XPathNameInUse;
					}
				}
			}
			return null;
		}

		
		protected void setServer(IServer s) {
			server = s;
			proposalProvider.setPath(getConfigFolder(s));
			refreshCategoryCombo();
			checkErrors();
		}
		
		protected void refreshCategoryCombo() {
			if( server != null ) {
				XPathCategory[] categories = XPathModel.getDefault().getCategories(server);
				String[] categoryNames = new String[categories.length];
				for( int i = 0; i < categories.length; i++ ) {
					categoryNames[i] = (String)categories[i].getName();
				}
				categoryCombo.setItems(categoryNames);
			}
		}
		
		protected void previewPressed() {
			if( server == null ) {
				checkErrors();
				return;
			}
			
			IRunnableWithProgress op = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					final IProgressMonitor monitor2 = monitor;
					XPathQuery tmp = new XPathQuery("", getConfigFolder(server), null, xpathText.getText(), attributeText.getText());
					tmp.setRepository(repository);
					final ArrayList list = new ArrayList();
					list.addAll(Arrays.asList(tmp.getResults()));
					Display.getDefault().asyncExec(new Runnable() { 
						public void run() {
							previewTreeViewer.setInput(list);
							if( list.size() == 0 ) {
								errorImage.setVisible(true);
								errorLabel.setText("No XML elements matched your search.");
								errorLabel.setVisible(true);
								previewTreeViewer.getTree().setEnabled(false);
							} else {
								previewTreeViewer.getTree().setEnabled(true);
								checkErrors();
							}
							main.layout();
						}
					});
				}
			};
			try {
				new ProgressMonitorDialog(new Shell()).run(false, true, op);
			} catch( Exception e) {
				e.printStackTrace();
			}
		}
		protected void layoutWidgets(Composite c) {
			// create widgets
			descriptionLabel = new Label(c, SWT.WRAP);
			descriptionLabel.setText("An XPath is a way to find a specific XML element inside an xml file. \n" + 
					"This dialog will help you create one. These XPaths' values can then be modified\n"
					+ "by using the JBoss Servers View with the Properties View.");
			descriptionLabel.setVisible(true);
			errorLabel = new Label(c, SWT.NONE);
			errorImage = new Label(c, SWT.NONE);
			errorImage.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
			
			
			Composite middleComposite = createMiddleComposite(c);
			
			
			// Now do the tree and viewer
			previewTree = new Tree(c, SWT.BORDER);
			previewTree.setHeaderVisible(true);
			previewTree.setLinesVisible(true);
			column = new TreeColumn(previewTree, SWT.NONE);
			column2 = new TreeColumn(previewTree, SWT.NONE);
			
			column.setText(Messages.XPathColumnLocation);
			column2.setText(Messages.XPathColumnAttributeVals);

			column.setWidth(150);
			column2.setWidth(150);

			previewTreeViewer = new TreeViewer(previewTree);

			c.layout();
			int pixel = Math.max(Math.max(nameLabel.getSize().x, xpathLabel.getSize().x), attributeLabel.getSize().x);
			pixel += 5;
			
			// Lay them out
			FormData descriptionData = new FormData();
			descriptionData.left = new FormAttachment(0, 5);
			descriptionData.right = new FormAttachment(100, -5);
			descriptionData.top = new FormAttachment(0,5);
			descriptionLabel.setLayoutData(descriptionData);
			
			FormData errorData = new FormData();
			errorData.left = new FormAttachment(errorImage,5);
			errorData.top = new FormAttachment(descriptionLabel,5);
			errorData.right = new FormAttachment(0,300);
			errorLabel.setLayoutData(errorData);
			errorLabel.setVisible(false);
			
			FormData errorImageData = new FormData();
			errorImageData.left = new FormAttachment(0,5);
			errorImageData.top = new FormAttachment(descriptionLabel,5);
			errorImage.setLayoutData(errorImageData);
			errorImage.setVisible(false);

			
			
			FormData middleCompositeData = new FormData();
			middleCompositeData.left = new FormAttachment(0,5);
			middleCompositeData.right = new FormAttachment(100, -5);
			middleCompositeData.top = new FormAttachment(errorLabel, 5);
			middleComposite.setLayoutData(middleCompositeData);
			
			// Tree layout data
			FormData previewTreeData = new FormData();
			previewTreeData.left = new FormAttachment(0,5);
			previewTreeData.right = new FormAttachment(100,-5);
			previewTreeData.top = new FormAttachment(middleComposite,5);
			previewTreeData.bottom = new FormAttachment(100,-5);
			previewTree.setLayoutData(previewTreeData);
			
			previewTreeViewer.setContentProvider(new ITreeContentProvider() {
				public Object[] getChildren(Object parentElement) {
					// we're a leaf
					if( parentElement instanceof XPathResultNode ) 
						return new Object[0];
					
					// we're a file node (blah.xml) 
					if( parentElement instanceof XPathFileResult ) {
						if( ((XPathFileResult)parentElement).getChildren().length > 1 ) 
							return ((XPathFileResult)parentElement).getChildren();
						return new Object[0];
					}
					
					// we're the named element (JNDI)
					if( parentElement instanceof XPathQuery ) {
						XPathFileResult[] kids = ((XPathQuery)parentElement).getResults();
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

		protected Composite createMiddleComposite(Composite c) {
			Composite gridComposite = new Composite(c, SWT.NONE);
			gridComposite.setLayout(new GridLayout(2, false));
			
			nameLabel = new Label(gridComposite, SWT.NONE);
			nameText= new Text(gridComposite, SWT.BORDER);
			nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
			
			if( server == null ) {
				serverLabel = new Label(gridComposite, SWT.NONE);
				serverCombo = new Combo(gridComposite, SWT.BORDER | SWT.READ_ONLY);
				serverCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
			}
			
			if( category == null ) {
				categoryLabel = new Label(gridComposite, SWT.NONE);
				categoryCombo = new Combo(gridComposite, SWT.BORDER);
				categoryCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
			}
			
			xpathLabel = new Label(gridComposite, SWT.NONE);
			xpathText = new Text(gridComposite, SWT.BORDER);
			xpathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
			attributeLabel = new Label(gridComposite, SWT.NONE);
			attributeText = new Text(gridComposite, SWT.BORDER);
			attributeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));


			// set some text
			nameLabel.setText(Messages.XPathName);
			if( serverLabel != null ) serverLabel.setText("Server: ");
			if( categoryLabel != null ) categoryLabel.setText("Category: ");
			xpathLabel.setText(Messages.XPathPattern);
			attributeLabel.setText(Messages.XPathAttribute);
			return gridComposite;
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
		
		public String getCategory() {
			return category;
		}
		public IServer getServer() {
			return server;
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
	
	public static class XPathAttributeProposalProvider extends XPathProposalProvider {
		private Text elementText;
		public XPathAttributeProposalProvider(XMLDocumentRepository repo, Text elementText) {
			super(repo);
			this.elementText = elementText;
		}
		public IContentProposal[] getProposals(String contents, int position) {
			String[] strings = getAttributeNameProposalStrings(elementText.getText(), contents.trim());
			return convertProposals(strings);
		}
		
		public String[] getAttributeNameProposalStrings(String parentPath, String remainder) {
			ArrayList names = new ArrayList();
			XPathResultNode[] items = getXPath(parentPath);
			String[] attributes;
			for( int i = 0; i < items.length; i++ ) {
				attributes = items[0].getElementAttributeNames();
				for( int j = 0; j < attributes.length; j++ ) {
					if( attributes[j].startsWith(remainder) && !names.contains(attributes[j])) 
						names.add(attributes[j]);
				}
			}
			return (String[]) names.toArray(new String[names.size()]);
		}
	}
	
	public static class XPathProposalProvider implements IContentProposalProvider {

		protected static final int NEW_ELEMENT = 1;
		protected static final int NEW_ATTRIBUTE = 2;
		protected static final int NEW_ATTRIBUTE_VALUE = 3;
		protected static final int IN_ELEMENT = 4;
		protected static final int IN_ATTRIBUTE = 5;
		protected static final int IN_ATTRIBUTE_VALUE = 6;
		protected static final int CLOSE_ATTRIBUTE = 7;
		
		private String path;
		private HashMap xpathCache;
		protected XMLDocumentRepository repository;
		
		public XPathProposalProvider(XMLDocumentRepository repository) {
			xpathCache = new HashMap();
			this.repository = repository;
		}
		
		public void setPath(String path) {
			this.path = path;
		}
		
		public IContentProposal[] getProposals(String contents, int position) {
			if( contents.equals("") || contents.equals("/") || contents.equals(" ")) {
				return new IContentProposal[] { new XPathContentProposal("/server/", "/server/".length(), null, null)};
			}
			
			try {
				int type = getType(contents);
				if( type == NEW_ELEMENT ) return getElementProposals(contents, "");
				if( type == IN_ELEMENT ) return getElementProposals(contents);
				if( type == NEW_ATTRIBUTE ) return getAttributeNameProposals(contents.substring(0, contents.length()-1), "");
				if( type == IN_ATTRIBUTE ) return getAttributeNameProposals(contents);
				if( type == NEW_ATTRIBUTE_VALUE ) return getAttributeValueProposals(contents, "");
				if( type == IN_ATTRIBUTE_VALUE ) return getAttributeValueProposals(contents);
			} catch( Exception e) {e.printStackTrace();}
			return new IContentProposal[]{};
		}
		
		protected XPathResultNode[] getXPath(String xpath) {
			if( path == null ) 
				return new XPathResultNode[0];
			
			if( xpathCache.containsKey(xpath)) {
				ArrayList list = (ArrayList)xpathCache.get(xpath);
				return (XPathResultNode[]) list.toArray(new XPathResultNode[list.size()]);
			}
			XPathQuery tmp = new XPathQuery("", path, "**/*.xml", xpath, null);
			tmp.setRepository(repository);
			ArrayList list = new ArrayList();
			XPathFileResult[] items = tmp.getResults();
			for( int i = 0; i < items.length; i++ ) {
				XPathResultNode[] children = items[i].getChildren();
				for( int j = 0; j < children.length; j++ ) {
					XPathResultNode i2 = (XPathResultNode)children[j];
					list.add(i2);
				}
			}
			xpathCache.put(xpath, list);
			return (XPathResultNode[]) list.toArray(new XPathResultNode[list.size()]);
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
			TreeSet set = new TreeSet();
			XPathResultNode[] items = getXPath(parentPath + "*");
			for( int i = 0; i < items.length; i++ ) {
				if( items[i].getElementName().startsWith(elementPrefix)) {
					if( items[i].getElementName().equals(elementPrefix)) {
						set.addAll(Arrays.asList(getAttributeNameProposalStrings(parentPath + elementPrefix, "")));
					} else {
						set.add(parentPath + items[i].getElementName());
					}
				}
			}
			return (String[]) set.toArray(new String[set.size()]);
		}
		
		public IContentProposal[] getAttributeNameProposals(String path) {
			String parent = path.substring(0, path.lastIndexOf('['));
			int attName = path.lastIndexOf('[') > path.lastIndexOf('@') ? path.lastIndexOf('[') : path.lastIndexOf('@');
			String[] props = getAttributeNameProposalStrings(parent, path.substring(attName+1));
			return convertProposals(props);
		}
		
		public IContentProposal[] getAttributeNameProposals(String parentPath, String remainder) {
			return convertProposals(getAttributeNameProposalStrings(parentPath, remainder));
		}

		public String[] getAttributeNameProposalStrings(String parentPath, String remainder) {
			ArrayList names = new ArrayList();
			XPathResultNode[] items = getXPath(parentPath);
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
			return results;
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
			XPathResultNode[] items = getXPath(parentElementPath);
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
	}
	
	public static String getConfigFolder(IServer server) {
		JBossServer jbs = (JBossServer)server.loadAdapter(JBossServer.class, new NullProgressMonitor());
		if( jbs != null ) {
			return jbs.getConfigDirectory();
		}
		return server.getRuntime().getLocation().toOSString();
		//return null;
	}
}
