package org.jboss.ide.eclipse.as.ui.views.server.providers.jmx;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class TreeEditorSelectionListener implements Listener {
	
	public static interface SelectionCallbackHandler {
		public boolean canModify(TreeItem item);
		public void cannotModify(TreeItem item);
		public void handleChange(TreeItem item, Text text);
	}
	
	private Color black;
	private TreeEditor editor;
	private Tree tree;
	private int column;
	private SelectionCallbackHandler callback;

	public TreeEditorSelectionListener(Tree tree, int column, SelectionCallbackHandler handler) {
		black = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
		editor = new TreeEditor(tree);
		this.tree = tree;
		this.column = column;
		this.callback = handler;
	}

	public void handleEvent(Event event) {
		if( callback == null ) return;

		TreeItem[] selectedItems = tree.getSelection();
		if (selectedItems.length != 1)
			return;
		final TreeItem item = selectedItems[0];
		
		if( !callback.canModify(item)) {
			callback.cannotModify(item);
			return;
		}
		
		boolean isCarbon = SWT.getPlatform().equals("carbon");
		final Composite composite = new Composite(tree, SWT.NONE);
		if (!isCarbon)
			composite.setBackground(black);
		final Text text = new Text(composite, SWT.NONE);
		final int inset = isCarbon ? 0 : 1;
		composite.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				Rectangle rect = composite.getClientArea();
				text.setBounds(rect.x + inset, rect.y + inset, rect.width
						- inset * 2, rect.height - inset * 2);
			}
		});
		Listener textListener = new Listener() {
			public void handleEvent(final Event e) {
				switch (e.type) {
				case SWT.FocusOut:
					composite.dispose();
					break;
				case SWT.Verify:
					String newText = text.getText();
					String leftText = newText.substring(0, e.start);
					String rightText = newText.substring(e.end, newText
							.length());
					GC gc = new GC(text);
					Point size = gc.textExtent(leftText + e.text
							+ rightText);
					gc.dispose();
					size = text.computeSize(size.x, SWT.DEFAULT);
					editor.horizontalAlignment = SWT.LEFT;
					Rectangle itemRect = item.getBounds(),
					rect = tree.getClientArea();
					editor.minimumWidth = Math.max(size.x, itemRect.width)
							+ inset * 2;
					int left = itemRect.x,
					right = rect.x + rect.width;
					editor.minimumWidth = Math.min(editor.minimumWidth,
							right - left);
					editor.minimumHeight = size.y + inset * 2;
					editor.setColumn(column);
					editor.layout();
					break;
				case SWT.Traverse:
					switch (e.detail) {
					case SWT.TRAVERSE_RETURN:
						try {
							callback.handleChange(item, text);
						} catch( Exception except ) {}
						// FALL THROUGH
					case SWT.TRAVERSE_ESCAPE:
						composite.dispose();
						e.doit = false;
					}
					break;
				}
			}
		};
		text.addListener(SWT.FocusOut, textListener);
		text.addListener(SWT.Traverse, textListener);
		text.addListener(SWT.Verify, textListener);
		editor.setEditor(composite, item);
		text.setText(item.getText(column));
		text.selectAll();
		text.setFocus();

	}
}
