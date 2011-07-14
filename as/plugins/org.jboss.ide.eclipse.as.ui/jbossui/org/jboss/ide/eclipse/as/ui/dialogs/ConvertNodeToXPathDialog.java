/******************************************************************************* 
* Copyright (c) 2009 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.dialogs;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.ui.dialogs.XPathDialogs.XPathDialog;



/**
 *
 * @author rob.stryker@jboss.com
 */

// TODO This class is very broken and needs to be removed for now
public class ConvertNodeToXPathDialog extends XPathDialog {

	public ConvertNodeToXPathDialog(Shell parentShell, IServer server) {
		super(parentShell, server);
	}
//	private Node node;
//	private ArrayList keyRows;
//	
//	private static final String NO_ATTRIBUTE = "NONE"; //$NON-NLS-1$
//	
//	protected ConvertNodeToXPathDialog(Shell parentShell, Node node, String attributeName) {
//		super(parentShell);
//		this.node = node;
//		setAttribute(attributeName == null ? "" : attributeName); //$NON-NLS-1$
//	}
//	
//	protected void configureShell(Shell shell) {
//		super.configureShell(shell);
//		setShellStyle(getShellStyle() | SWT.RESIZE);
//		shell.setText(Messages.XPathNewXpath);
//		shell.setBounds(shell.getLocation().x, shell.getLocation().y, 550, 550);
//	}
//
//	protected Control createDialogArea(Composite parent) {
//		Control c = super.createDialogArea(parent);
//		refreshXPath();
//		return c;
//	}
//	
//	protected Composite createMiddleComposite(Composite c) {
//		Composite middleComposite = new Composite(c, SWT.NONE);
//		middleComposite.setLayout(new FormLayout());
//		
//		Composite gridComposite = super.createMiddleComposite(middleComposite);
//		FormData gridData = new FormData();
//		gridData.left = new FormAttachment(0,5);
//		gridData.right = new FormAttachment(100, -5);
//		gridData.top = new FormAttachment(0,5);
//		gridComposite.setLayoutData(gridData);
//		
//		// now add my stuff?
//		Composite keyComposite = createKeyComposite(middleComposite);
//		FormData keyData = new FormData();
//		keyData.left = new FormAttachment(0,5);
//		keyData.right = new FormAttachment(100,-5);
//		keyData.top = new FormAttachment(gridComposite, 5);
//		keyComposite.setLayoutData(keyData);
//		
//		return middleComposite;
//	}
//	
//	protected Composite createKeyComposite(Composite parent) {
//		Group main = new Group(parent, SWT.NONE);
//		main.setLayout(new GridLayout(2, false));
//		keyRows = new ArrayList();
//		Node localNode = node;
//		String pathText = ""; //$NON-NLS-1$
//		while( localNode != null && !(localNode instanceof IDOMDocument)) {
//			if( !localNode.getNodeName().equals("#text")) { //$NON-NLS-1$
//				keyRows.add(0, new NameComboPair(main, localNode));
//			}
//			localNode = localNode.getParentNode();
//		}
//
//		for( int i = 0; i < keyRows.size(); i++ ) {
//			((NameComboPair)keyRows.get(i)).create();
//		}
//		
//		main.setText(Messages.ConvertNodeToXPathDialog_KeyedAttributes);
//		
//		
//		// The very last row should not key off the same attribute name that they wish to change
//		final NameComboPair lastRow = (NameComboPair)keyRows.get(keyRows.size()-1);
//		lastRow.combo.addSelectionListener(new SelectionListener() {
//			public void widgetDefaultSelected(SelectionEvent e) {
//			}
//			public void widgetSelected(SelectionEvent e) {
//				checkErrors();
//			} 
//		});
//		
//		return main;
//	}
//	protected ArrayList getErrors() {
//		ArrayList errors = super.getErrors();
//		NameComboPair pair = (NameComboPair)keyRows.get(keyRows.size()-1);
//		String val = pair.combo.getItem(pair.combo.getSelectionIndex());
//		if(attribute != null && val.startsWith(attribute + "=")) { //$NON-NLS-1$
//			errors.add(Messages.ConvertNodeToXPathDialog_ErrorMessage);
//		}
//		return errors;
//	}
//
//	
//	public void refreshXPath() {
//		String finalString = ""; //$NON-NLS-1$
//		for( int i = 0; i < keyRows.size(); i++ ) {
//			finalString += ((NameComboPair)keyRows.get(i)).toString();
//		}
//		setXpath(finalString);
//		previewPressed();
//	}
//	
//	protected class NameComboPair {
//		private Node node;
//		private Composite parent;
//		private Label label;
//		private Combo combo;
//		public NameComboPair(Composite parent, Node node) {
//			this.node = node;
//			this.parent = parent;
//		}
//		public void create() {
//			label = new Label(parent, SWT.NONE);
//			label.setText(node.getNodeName());
//			combo = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
//			label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
//			combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
//			
//			// fill the combo
//			NamedNodeMap map = node.getAttributes();
//			ArrayList list = new ArrayList();
//			list.add(NO_ATTRIBUTE);
//			int selectedIndex = 0;
//			for( int i = 0; i < map.getLength(); i++ ) {
//				Node attr = map.item(i);
//				if( attr instanceof AttrImpl ) {
//					AttrImpl impl = ((AttrImpl)attr);
//					list.add(impl.getName() + "='" + impl.getValue() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
//					if( impl.getName().equals("name") && !attribute.equals("name")) //$NON-NLS-1$ //$NON-NLS-2$
//						selectedIndex = i+1;
//				}
//			}
//			
//			combo.setItems((String[]) list.toArray(new String[list.size()]));
//			combo.select(selectedIndex);
//			
//			
//			// selection listener
//			combo.addSelectionListener(new SelectionListener() {
//				public void widgetDefaultSelected(SelectionEvent e) {
//				}
//				public void widgetSelected(SelectionEvent e) {
//					refreshXPath();
//				} 
//			});
//		}
//		
//		public String toString() {
//			String ret = "/" + node.getNodeName(); //$NON-NLS-1$
//			String comboSelection = combo.getItem(combo.getSelectionIndex());
//			if( !comboSelection.equals(NO_ATTRIBUTE))
//				ret += "[@" + comboSelection + "]"; //$NON-NLS-1$ //$NON-NLS-2$
//			return ret;
//		}
//	}
//
//	
//	
//	public static class OpenXPathDialogProposal implements ICompletionProposal, ICompletionProposalExtension2 {
//		private Node node;
//		private String attributeName;
//		public OpenXPathDialogProposal(Node node, String attributeName ) {
//			this.node = node;
//			this.attributeName = attributeName;
//		}
//		public void apply(IDocument document) {
//		}
//
//		public String getAdditionalProposalInfo() {
//			return null;
//		}
//
//		public IContextInformation getContextInformation() {
//			return null;
//		}
//
//		public String getDisplayString() {
//			return Messages.ConvertNodeToXPathDialog_DisplayString;
//		}
//
//		public Image getImage() {
//			return null;
//		}
//
//		public Point getSelection(IDocument document) {
//			return null;
//		}
//
//		public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
//			new ConvertNodeRunnable(node, attributeName).run();
//		}
//
//		public void selected(ITextViewer viewer, boolean smartToggle) {
//		}
//
//		public void unselected(ITextViewer viewer) {
//		}
//
//		public boolean validate(IDocument document, int offset, DocumentEvent event) {
//			return false;
//		}
//		
//	}
//	
//	public static class ConvertNodeRunnable implements Runnable {
//
//		private String attributeName;
//		private Node node;
//		public ConvertNodeRunnable(Node n, String att) {
//			attributeName = att;
//			node = n;
//		}
//		
//		public void run() {
//			ConvertNodeToXPathDialog d = new ConvertNodeToXPathDialog(new Shell(), node, attributeName);
//			int result = -1;
//			try {
//				result = d.open();
//			} catch(Exception e) {e.printStackTrace(); }
//			if( result == Window.OK) {
//				IServer s = d.getServer();
//				String category = d.getCategory();
//				XPathCategory cat = XPathModel.getDefault().getCategory(s, category);
//				if( cat == null ) {
//					cat = XPathModel.getDefault().addCategory(s, category);
//				}
//				XPathQuery q = new XPathQuery(d.getName(), XPathDialogs.getConfigFolder(s), null, d.getXpath(), d.getAttribute());
//				cat.addQuery(q);
//				cat.save();
//			}
//		}
//	}
}
