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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XMLDocumentRepository;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathCategory;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathModel;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathQuery;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult.XPathResultNode;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.ui.Messages;


/**
 * A class with some XPath-related dialogs
 * @author Rob Stryker
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

	public static class XPathDialog extends TitleAreaDialog {

		protected Text nameText, baseDirText, filesetText, 
						xpathText, attributeText;
		protected Label nameLabel, baseDirLabel, filesetLabel, 
		 			xpathLabel, attributeLabel;
		protected Button previewButton, rootDirBrowse;

		protected XPathProposalProvider proposalProvider;

		protected IServer server;
		protected String name, rootDir, filePattern, xpath, attribute;
		protected String originalName = null;
		protected XPathQuery original = null;
		protected XPathCategory category;
		protected int previewId = 48879;

		protected Tree previewTree;
		protected TreeColumn column, column2;
		protected TreeViewer previewTreeViewer;
		protected Composite main;
		protected XMLDocumentRepository repository;

		public XPathDialog(Shell parentShell, IServer server) {
			this(parentShell, server, null);
		}
		public XPathDialog(Shell parentShell, IServer server, XPathQuery original) {
			super(parentShell);
			setShellStyle(getShellStyle() | SWT.RESIZE);
			this.server = server;
			repository = new XMLDocumentRepository(XMLDocumentRepository.getDefault());
			if( original != null ) {
				this.original = original;
				this.originalName = this.name = original.getName();
				this.filePattern = original.getFilePattern();
				this.rootDir = original.getBaseDir();
				this.category = original.getCategory();
				this.xpath = original.getXpathPattern();
			} 
			if( this.xpath == null ) this.xpath = "//server/mbean"; //$NON-NLS-1$
			if( this.filePattern == null ) this.filePattern = "**/*.xml"; //$NON-NLS-1$
			if( this.rootDir == null ) this.rootDir = ""; //$NON-NLS-1$
		}

		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			String title = original == null ? Messages.XPathNewXpath : Messages.XPathEditXpath;
			shell.setText(title);
			shell.setBounds(shell.getLocation().x, shell.getLocation().y, 550, 500);
		}
	    protected int getShellStyle() {
	        int ret = super.getShellStyle();
	        return ret | SWT.RESIZE;
	    }

		protected void createButtonsForButtonBar(Composite parent) {
			// create OK and Cancel buttons by default
			super.createButtonsForButtonBar(parent);
			previewButton = createButton(parent, previewId, Messages.XPathDialogs_PreviewButton, true);
			if( name == null ) getButton(IDialogConstants.OK_ID).setEnabled(false);
			addListeners();
			checkErrors();
		}


		protected Control createDialogArea(Composite parent) {
			String title = original == null ? Messages.XPathNewXpath : Messages.XPathEditXpath;
			setTitle(title);
			Composite main = new Composite((Composite)super.createDialogArea(parent), SWT.NONE);
			main.setLayoutData(new GridData(GridData.FILL_BOTH));
			main.setLayout(new FormLayout());
			layoutWidgets(main);
			if( name != null ) nameText.setText(name);
			if( attribute != null ) attributeText.setText(attribute);
			if( xpath != null ) xpathText.setText(xpath);
			if( filePattern != null ) filesetText.setText(filePattern);
			if( rootDir != null ) baseDirText.setText(rootDir);
			
			proposalProvider = new XPathProposalProvider(repository);
			if( server != null )
				proposalProvider.setPath(getConfigFolder(server));
			ContentProposalAdapter adapter = new
			ContentProposalAdapter(xpathText, new TextContentAdapter(),
					proposalProvider, null, null);
			adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);

			XPathAttributeProposalProvider provider2 = new XPathAttributeProposalProvider(repository, xpathText);
			if( server != null )
				provider2.setPath(getConfigFolder(server));
			ContentProposalAdapter adapter2 = new
			ContentProposalAdapter(attributeText, new TextContentAdapter(),
					provider2, null, null);
			adapter2.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);

			return main;
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
			baseDirText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					rootDir = baseDirText.getText();
				}
			});
			filesetText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					filePattern = filesetText.getText();
				}
			});
			rootDirBrowse.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					browsePressed();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
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


		protected void checkErrors() {
			ArrayList<String> errorList = getErrors();
			if( errorList.size() == 0 ) {
				setError(null);
				getButton(IDialogConstants.OK_ID).setEnabled(true);
				return;
			}
			setError(errorList.get(0));
			if( getButton(IDialogConstants.OK_ID) != null )
				 getButton(IDialogConstants.OK_ID).setEnabled(false);
			return;
		}
		protected ArrayList<String> getErrors() {
			ArrayList<String> list = new ArrayList<String>();
			String serverError = getServerError(); if( serverError != null ) list.add(serverError);
			String nameError = getNameError(); if( nameError != null ) list.add(nameError);
			return list;
		}

		protected String getServerError() {
			if( server == null ) return Messages.XPathDialogs_SelectServer;
			return null;
		}

		protected void setError(String message) {
			if( message == null ) 
				setMessage(Messages.XPathDialogs_XPathDescriptionLabel);
			else
				setMessage(message, IMessageProvider.ERROR);
		}

		protected String getNameError() {
			if( nameText.getText().equals("")) { //$NON-NLS-1$
				return Messages.XPathNameEmpty;
			}
			if( category != null ) {
				XPathQuery[] queries = category.getQueries();
				for( int i = 0; i < queries.length; i++ ) {
					if(nameText.getText().equals( ((XPathQuery)queries[i]).getName())) {
						if( originalName == null || !nameText.getText().equals(originalName))
							return Messages.XPathNameInUse;
					}
				}
			}
			return null;
		}

		protected void browsePressed() {
			DirectoryDialog d = new DirectoryDialog(rootDirBrowse.getShell());
			String folder = rootDir;
			if( !new Path(rootDir).isAbsolute())
				folder = server.getRuntime().getLocation().append(rootDir).toString();
			d.setFilterPath(folder);
			String result = d.open();
			if( result != null ) {
				IPath result2 = new Path(result);
				IRuntime rt = server.getRuntime();
				if( result2.isAbsolute() && rt != null) {
					if(rt.getLocation().isPrefixOf(result2)) {
						int size = rt.getLocation().toOSString().length();
						result2 = new Path(result2.toOSString().substring(size)).makeRelative();
					}
				}
				rootDir = result2.toString();
				baseDirText.setText(rootDir);
			}
		}
		protected void previewPressed() {
			if( server == null ) {
				checkErrors();
				return;
			}

			final String xpText = xpathText.getText();
			final String attText = attributeText.getText();
			final String filePattern = filesetText.getText();
			String directory = baseDirText.getText();
			if( !new Path(directory).isAbsolute()) {
				directory = server.getRuntime().getLocation().append(directory).toString();
			}
			final String directory2 = directory;
			IRunnableWithProgress op = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					XPathQuery tmp = new XPathQuery("", directory2, filePattern, xpText, attText); //$NON-NLS-1$
					tmp.setRepository(repository);
					final ArrayList<XPathFileResult> list = new ArrayList<XPathFileResult>();
					list.addAll(Arrays.asList(tmp.getResults()));
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							previewTreeViewer.setInput(list);
							if( list.size() == 0 ) {
								setError(Messages.XPathDialogs_NoElementsMatched);
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
				new ProgressMonitorDialog(new Shell()).run(true, true, op);
			} catch (InvocationTargetException e) {
			} catch (InterruptedException e) {
			}
		}
		protected void layoutWidgets(Composite c) {
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


			FormData middleCompositeData = new FormData();
			middleCompositeData.left = new FormAttachment(0,5);
			middleCompositeData.right = new FormAttachment(100, -5);
			middleCompositeData.top = new FormAttachment(0, 5);
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
			nameText = new Text(gridComposite, SWT.BORDER);
			nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

			baseDirLabel = new Label(gridComposite, SWT.NONE);
			Composite baseDirTextAndButton = new Composite(gridComposite, SWT.NONE);
			baseDirTextAndButton.setLayout(new FormLayout());
			baseDirTextAndButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
			baseDirText = new Text(baseDirTextAndButton, SWT.BORDER);
			rootDirBrowse = new Button(baseDirTextAndButton, SWT.PUSH);
			rootDirBrowse.setText(Messages.browse);
			FormData d = new FormData();
			d.right = new FormAttachment(100,-5);
			rootDirBrowse.setLayoutData(d);
			d = new FormData();
			d.left = new FormAttachment(0,0);
			d.right = new FormAttachment(rootDirBrowse, -5);
			baseDirText.setLayoutData(d);
			
			filesetLabel = new Label(gridComposite, SWT.NONE);
			filesetText = new Text(gridComposite, SWT.BORDER);
			filesetText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
			
			xpathLabel = new Label(gridComposite, SWT.NONE);
			xpathText = new Text(gridComposite, SWT.BORDER);
			xpathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
			attributeLabel = new Label(gridComposite, SWT.NONE);
			attributeText = new Text(gridComposite, SWT.BORDER);
			attributeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));


			// set some text
			nameLabel.setText(Messages.XPathName);
			xpathLabel.setText(Messages.XPathPattern);
			attributeLabel.setText(Messages.XPathAttribute);
			filesetLabel.setText(Messages.XPathFilePattern);
			baseDirLabel.setText(Messages.XPathRootDir);
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
		public String getBaseDir() {
			return rootDir;
		}
		public String getFilePattern() {
			return filePattern;
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
			ArrayList<String> names = new ArrayList<String>();
			XPathResultNode[] items = getXPath(parentPath);
			String[] attributes;
			for( int i = 0; i < items.length; i++ ) {
				attributes = items[0].getElementAttributeNames();
				for( int j = 0; j < attributes.length; j++ ) {
					if( attributes[j].startsWith(remainder) && !names.contains(attributes[j]))
						names.add(attributes[j]);
				}
			}
			return names.toArray(new String[names.size()]);
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
		private HashMap<String, ArrayList<XPathResultNode>> xpathCache;
		protected XMLDocumentRepository repository;

		public XPathProposalProvider(XMLDocumentRepository repository) {
			xpathCache = new HashMap<String, ArrayList<XPathResultNode>>();
			this.repository = repository;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public IContentProposal[] getProposals(String contents, int position) {
			if( contents.equals("") || contents.equals("/") || contents.equals(" ")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return new IContentProposal[] { new XPathContentProposal("/server/", "/server/".length(), null, null)}; //$NON-NLS-1$ //$NON-NLS-2$
			}

			int type = getType(contents);
			if( type == NEW_ELEMENT ) return getElementProposals(contents, ""); //$NON-NLS-1$
			if( type == IN_ELEMENT ) return getElementProposals(contents);
			if( type == NEW_ATTRIBUTE ) return getAttributeNameProposals(contents.substring(0, contents.length()-1), ""); //$NON-NLS-1$
			if( type == IN_ATTRIBUTE ) return getAttributeNameProposals(contents);
			if( type == NEW_ATTRIBUTE_VALUE ) return getAttributeValueProposals(contents, ""); //$NON-NLS-1$
			if( type == IN_ATTRIBUTE_VALUE ) return getAttributeValueProposals(contents);
			return new IContentProposal[]{};
		}

		protected XPathResultNode[] getXPath(String xpath) {
			if( path == null )
				return new XPathResultNode[0];

			if( xpathCache.containsKey(xpath)) {
				ArrayList list = xpathCache.get(xpath);
				return (XPathResultNode[]) list.toArray(new XPathResultNode[list.size()]);
			}
			XPathQuery tmp = new XPathQuery("", path, "**/*.xml", xpath, null); //$NON-NLS-1$ //$NON-NLS-2$
			tmp.setRepository(repository);
			ArrayList<XPathResultNode> list = new ArrayList<XPathResultNode>();
			XPathFileResult[] items = tmp.getResults();
			for( int i = 0; i < items.length; i++ ) {
				XPathResultNode[] children = items[i].getChildren();
				for( int j = 0; j < children.length; j++ ) {
					XPathResultNode i2 = (XPathResultNode)children[j];
					list.add(i2);
				}
			}
			xpathCache.put(xpath, list);
			return list.toArray(new XPathResultNode[list.size()]);
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
			TreeSet<String> set = new TreeSet<String>();
			XPathResultNode[] items = getXPath(parentPath + "*"); //$NON-NLS-1$
			for( int i = 0; i < items.length; i++ ) {
				if( items[i].getElementName().startsWith(elementPrefix)) {
					if( items[i].getElementName().equals(elementPrefix)) {
						set.addAll(Arrays.asList(getAttributeNameProposalStrings(parentPath + elementPrefix, ""))); //$NON-NLS-1$
					} else {
						set.add(parentPath + items[i].getElementName());
					}
				}
			}
			return set.toArray(new String[set.size()]);
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
			ArrayList<String> names = new ArrayList<String>();
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
				results[i] = parentPath + "[@" + names.get(i) + "="; //$NON-NLS-1$ //$NON-NLS-2$
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

			if( remainder.startsWith("'")) remainder = remainder.substring(1); //$NON-NLS-1$
			ArrayList<String> values = new ArrayList<String>();
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
			String prefix = parentElementPath + "[@" + attName + "='"; //$NON-NLS-1$ //$NON-NLS-2$
			for( int i = 0; i < results.length; i++ ) {
				results[i] = prefix + values.get(i) + "']/"; //$NON-NLS-1$
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
			ArrayList<XPathContentProposal> list = new ArrayList<XPathContentProposal>();
			for( int i = 0; i < strings.length; i++ ) {
				list.add(new XPathContentProposal(strings[i], strings[i].length(), null, null));
			}
			return list.toArray(new IContentProposal[list.size()]);
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
	
	public static class XPathPropertyLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			if( element instanceof XPathQuery) {
				if( columnIndex == 0 ) return ((XPathQuery)element).getName();
				if( columnIndex == 1 ) {
					XPathResultNode[] nodes = getResultNodes(((XPathQuery)element));
					if( nodes.length == 1 )
					return nodes[0].getText();
				}
			}

			if( element instanceof XPathFileResult ) {
				XPathFileResult result = (XPathFileResult)element;
				if( columnIndex == 0 ) {
					return result.getFileLocation().substring(result.getQuery().getBaseDir().length());
				}
				if( result.getChildren().length == 1 ) {
					element = result.getChildren()[0];
				}
			}
			
			if( element instanceof XPathResultNode ) {
				XPathResultNode element2 = (XPathResultNode)element;
				if( columnIndex == 0 ) return Messages.DescriptorXPathMatch + element2.getIndex();
				if( columnIndex == 1 ) return element2.getText().trim();
			}
			
			return null; 
		}

		public XPathResultNode[] getResultNodes(XPathQuery query) {
			ArrayList<XPathResultNode> l = new ArrayList<XPathResultNode>();
			XPathFileResult[] files = query.getResults();
			for( int i = 0; i < files.length; i++ ) {
				l.addAll(Arrays.asList(files[i].getChildren()));
			}
			return l.toArray(new XPathResultNode[l.size()]);
		}

	}

}
