/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.actions.TextActionHandler;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathModel;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathQuery;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult.XPathResultNode;
import org.jboss.ide.eclipse.as.ui.Messages;
/**
 * Action to rename a server.
 */
public class XPathChangeValueAction extends SelectionProviderAction {
	protected CustomTreeEditor treeEditor;
	protected Tree tree;
	protected TreeViewer viewer;
	protected Text textEditor;
	protected Composite textEditorParent;
	private TextActionHandler textActionHandler;
	protected Shell shell;
	protected int width = -1;
	protected Rectangle newParentBounds;

	protected boolean saving = false;

	public XPathChangeValueAction(Shell shell, TreeViewer viewer, ISelectionProvider selectionProvider) {
		super(selectionProvider, Messages.XPathChangeValueAction_ActionText);
		this.shell = shell;
		this.viewer = viewer;
		this.tree = viewer.getTree();
		this.treeEditor = new CustomTreeEditor(tree);
	}

	public boolean shouldRun() {
		if( getStructuredSelection().toArray().length > 1)
			return false;
		
		Object o = getStructuredSelection().getFirstElement();
		if( XPathDecorator.getDecoration(o) != null )
			return true;
		return false;
	}
	
	public void run() {
		queryNewValueInline(getStructuredSelection().getFirstElement());
	}

	/**
	 * On Mac the text widget already provides a border when it has focus, so
	 * there is no need to draw another one. The value of returned by this
	 * method is usd to control the inset we apply to the text field bound's in
	 * order to get space for drawing a border. A value of 1 means a one-pixel
	 * wide border around the text field. A negative value supresses the border.
	 * However, in M9 the system property
	 * "org.eclipse.swt.internal.carbon.noFocusRing" has been introduced as a
	 * temporary workaround for bug #28842. The existence of the property turns
	 * the native focus ring off if the widget is contained in a main window
	 * (not dialog). The check for the property should be removed after a final
	 * fix for #28842 has been provided.
	 */
	private static int getCellEditorInset(Control c) {
		// special case for MacOS X
		if ("carbon".equals(SWT.getPlatform())) { //$NON-NLS-1$
			if (System
					.getProperty("org.eclipse.swt.internal.carbon.noFocusRing") == null || c.getShell().getParent() != null) { //$NON-NLS-1$
				return -2; // native border
			}
		}
		return 1; // one pixel wide black border
	}

	/**
	 * Get the Tree being edited.
	 * 
	 * @returnTree
	 */
	private Tree getTree() {
		return tree;
	}

	private Composite createParent() {
		Tree tree2 = getTree();
		Composite result = new Composite(tree2, SWT.NONE);
		TreeItem[] selectedItems = tree2.getSelection();
		treeEditor.horizontalAlignment = SWT.LEFT;
		treeEditor.grabHorizontal = true;
		treeEditor.setEditor(result, selectedItems[0]);
		return result;
	}

	/**
	 * Return the new name to be given to the target resource or
	 * <code>null<code>
	 * if the query was canceled. Rename the currently selected server using the table editor. 
	 * Continue the action when the user is done.
	 *
	 * @param server the server to rename
	 */
	private void queryNewValueInline(final Object node) {
		
		// Make sure text editor is created only once. Simply reset text
		// editor when action is executed more than once. Fixes bug 22269
		if (textEditorParent == null) {
			createTextEditor(node);
		}
		textEditor.setText(XPathModel.getResultNode(node).getText());
		// Open text editor with initial size
		Point textSize = textEditor.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		textSize.x += textSize.y; // Add extra space for new characters
		Point parentSize = textEditorParent.getSize();
		int inset = getCellEditorInset(textEditorParent);
		textEditor.setBounds(2, inset, Math.min(textSize.x, parentSize.x - 4),
				parentSize.y - 2 * inset);
		textEditor.setText(new XPathTreeLabelProvider().getText(node));
		width = textEditor.getSize().x - 10;
		textEditor.setText(XPathModel.getResultNode(node).getText());
		treeEditor.layout();
		textEditorParent.setVisible(true);
		textEditor.setVisible(true);
		textEditorParent.redraw();
		textEditor.selectAll();
		textEditor.setFocus();
	}

	/**
	 * Create the text editor widget.
	 * 
	 * @param server the server to rename
	 */
	private void createTextEditor(final Object node) {
		// Create text editor parent. This draws a nice bounding rect
		textEditorParent = createParent();
		textEditorParent.setVisible(false);
		final int inset = getCellEditorInset(textEditorParent);
		if (inset > 0) {
			textEditorParent.addListener(SWT.Paint, new Listener() {
				public void handleEvent(Event e) {
					Point textSize = textEditor.getSize();
					Point parentSize = textEditorParent.getSize();
					e.gc.drawRectangle(0, 0, Math.min(textSize.x + 4,
							parentSize.x - 1), parentSize.y - 1);
				}
			});
		}
		// Create inner text editor
		textEditor = new Text(textEditorParent, SWT.NONE);
		textEditor.setFont(tree.getFont());
		textEditorParent.setBackground(textEditor.getBackground());
		textEditor.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event e) {
				Point textSize = textEditor.computeSize(SWT.DEFAULT,
						SWT.DEFAULT);
				textSize.x += textSize.y; // Add extra space for new
				// characters.
				Point parentSize = textEditorParent.getSize();

				textEditor.setBounds(2, inset, Math.min(textSize.x,
						parentSize.x - 4), parentSize.y - 2 * inset);
				textEditorParent.redraw();
			}
		});
		textEditor.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event event) {

				// Workaround for Bug 20214 due to extra
				// traverse events
				switch (event.detail) {
				case SWT.TRAVERSE_ESCAPE:
					// Do nothing in this case
					disposeTextWidget();
					event.doit = true;
					event.detail = SWT.TRAVERSE_NONE;
					break;
				case SWT.TRAVERSE_RETURN:
					saveChangesAndDispose(node);
					event.doit = true;
					event.detail = SWT.TRAVERSE_NONE;
					break;
				}
			}
		});
		textEditor.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent fe) {
				saveChangesAndDispose(node);
			}
		});

		if (textActionHandler != null) {
			textActionHandler.addText(textEditor);
		}
		textEditor.setVisible(false);
	}

	/**
	 * Close the text widget and reset the editorText field.
	 */
	protected void disposeTextWidget() {
		width = -1;
		if (textActionHandler != null)
			textActionHandler.removeText(textEditor);

		if (textEditorParent != null) {
			textEditorParent.dispose();
			textEditorParent = null;
			textEditor = null;
			treeEditor.setEditor(null, null);
		}
	}

	/**
	 * Save the changes and dispose of the text widget.
	 * 
	 * @param server the server to rename
	 */
	protected void saveChangesAndDispose(Object node) {
		if (saving == true)
			return;
		saving = true;

		// Cache the resource to avoid selection loss since a selection of
		// another item can trigger this method
		final String newVal = textEditor.getText();
		XPathResultNode itemToChange = XPathModel.getResultNode(node);
		// if its unchanged do nothing
		if( !itemToChange.getText().equals(newVal)) {
			// set the text and add the document to the list of dirty ones
			itemToChange.setText(newVal);
			if( itemToChange.getDocument() != null ) {
				itemToChange.saveDescriptor();
			}
		}
		
		// Run this in an async to make sure that the operation that triggered
		// this action is completed. Otherwise this leads to problems when the
		// icon of the item being renamed is clicked (i.e., which causes the
		// rename text widget to lose focus and trigger this method)
		getTree().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					// Dispose the text widget regardless
					disposeTextWidget();
					// Ensure the Navigator tree has focus, which it may not if
					// the text widget previously had focus
					if (tree != null && !tree.isDisposed()) {
						tree.setFocus();
						viewer.refresh(getStructuredSelection().getFirstElement());
					}
				} finally {
					saving = false;
				}
			}
		});
	}

	
	protected class CustomTreeEditor extends TreeEditor {
		public CustomTreeEditor(Tree tree) {
			super(tree);
		}
		
		public void setItem (TreeItem item) {
			super.setItem(item);
		}
		
		private boolean hadFocus = false;
		public void layout () {
			if (tree == null || tree.isDisposed()) return;
			if (getItem() == null || getItem().isDisposed()) return;	
			int columnCount = tree.getColumnCount();
			if (columnCount == 0 && getColumn() != 0) return;
			if (columnCount > 0 && (getColumn() < 0 || getColumn() >= columnCount)) return;

			if (getEditor() == null || getEditor().isDisposed()) return;
			if (getEditor().getVisible ()) {
				hadFocus = getEditor().isFocusControl();
			} // this doesn't work because
			  // resizing the column takes the focus away
			  // before we get here
			getEditor().setBounds (customComputeBounds (superComputeBounds()));
			if (hadFocus) {
				if (getEditor() == null || getEditor().isDisposed()) return;
				getEditor().setFocus ();
			}
		}
		
		Rectangle customComputeBounds (Rectangle rect) {
			Rectangle r = new Rectangle(rect.x + (width == -1 ? 0 : width), rect.y, rect.width, rect.height);
			return r;
		}
		
		Rectangle superComputeBounds() {
			if (getItem() == null || getColumn() == -1 || getItem().isDisposed() ) return new Rectangle(0, 0, 0, 0);
			Rectangle cell = getItem().getBounds(getColumn());
			Rectangle rect = getItem().getImageBounds(getColumn());
			cell.x = rect.x + rect.width;
			cell.width -= rect.width;
			Rectangle area = tree.getClientArea();
			if (cell.x < area.x + area.width) {
				if (cell.x + cell.width > area.x + area.width) {
					cell.width = area.x + area.width - cell.x;
				}
			}
			Rectangle editorRect = new Rectangle(cell.x, cell.y, minimumWidth, minimumHeight);

			if (grabHorizontal) {
				if (tree.getColumnCount() == 0) {
					// Bounds of tree item only include the text area - stretch out to include 
					// entire client area
					cell.width = area.x + area.width - cell.x;
				}
				editorRect.width = Math.max(cell.width, minimumWidth);
			}
			
			if (grabVertical) {
				editorRect.height = Math.max(cell.height, minimumHeight);
			}
			
			if (horizontalAlignment == SWT.RIGHT) {
				editorRect.x += cell.width - editorRect.width;
			} else if (horizontalAlignment == SWT.LEFT) {
				// do nothing - cell.x is the right answer
			} else { // default is CENTER
				editorRect.x += (cell.width - editorRect.width)/2;
			}
			// don't let the editor overlap with the +/- of the tree
			editorRect.x = Math.max(cell.x, editorRect.x);
			
			if (verticalAlignment == SWT.BOTTOM) {
				editorRect.y += cell.height - editorRect.height;
			} else if (verticalAlignment == SWT.TOP) {
				// do nothing - cell.y is the right answer
			} else { // default is CENTER
				editorRect.y += (cell.height - editorRect.height)/2;
			}
			return editorRect;
		}

	}

}