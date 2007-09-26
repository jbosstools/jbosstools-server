package org.jboss.ide.eclipse.as.ui.views.server.providers.descriptors;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathCategory;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathQuery;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult.XPathResultNode;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.views.server.ExtensionTableViewer.ContentWrapper;
import org.jboss.ide.eclipse.as.ui.views.server.providers.DescriptorXPathViewProvider;

public class DescriptorXPathPropertySheetPage implements IPropertySheetPage {
	private TreeViewer xpathTreeViewer;
	private TreeColumn column, column2;//, column3;
	private Tree xpathTree;
	private XPathPropertyLabelProvider labelProvider;
	private DescriptorXPathViewProvider provider;
	private Composite loading, wrapper;
	private PageBook book;
	
	public DescriptorXPathPropertySheetPage(DescriptorXPathViewProvider provider) {
		this.provider = provider;
	}
	
	public TreeViewer getViewer() { return xpathTreeViewer; }

	public void createControl(Composite parent) {
		book = new PageBook(parent, SWT.NONE);

		createLoadingComposite(book);
		createXPathComposite(book);
		addViewerMenus();
	}

	public XPathPropertyLabelProvider getLabelProvider() {
		if( labelProvider == null ) {
			labelProvider = new XPathPropertyLabelProvider();
		}
		return labelProvider;
	}
	
	private void addViewerMenus() {
		MenuManager menuManager = new MenuManager("#PopupMenu"); 
		menuManager.setRemoveAllWhenShown(true);
		final Shell shell = xpathTree.getShell();
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				xpathTreeMenuAboutToShow(shell, mgr);
			}
		});
		Menu menu = menuManager.createContextMenu(xpathTree);
		xpathTree.setMenu(menu);
	}
	
	private void xpathTreeMenuAboutToShow(Shell shell, IMenuManager menu) {
		Object selected = provider.getPropertySelection();
		menu.add(provider.newXPathAction);
		if( selected != null && selected instanceof XPathQuery) {
			menu.add(provider.editXPathAction);
			menu.add(provider.deleteXPathAction);
		}
		if( selected != null && (selected instanceof XPathResultNode || selected instanceof XPathFileResult ) || 
			(selected instanceof XPathQuery && ((XPathQuery)selected).getResults().length == 1)) {
			menu.add(provider.editFileAction);
		}
	}

	
	public void dispose() {
	}

	public Control getControl() {
		return book;
	}

	public void setActionBars(IActionBars actionBars) {
	}

	public void setFocus() {
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		//input = null;
		Object element = ((IStructuredSelection)selection).getFirstElement();
		if( element instanceof ContentWrapper ) {
			element = ((ContentWrapper)element).getElement();
		}
		if( element != null //)&& element != provider.getActiveCategory()
				&& element instanceof XPathCategory) {
			// show loading
			Display.getDefault().asyncExec(new Runnable() { 
				public void run() {
					book.showPage(loading);
				}
			});

			final XPathCategory cat = (XPathCategory)element;
			new Thread() {
				public void run() {
					// force load everything
					forceLoad(cat);
					Display.getDefault().asyncExec(new Runnable() { 
						public void run() {
							// do this stuff async
							book.showPage(wrapper);
							provider.setActiveCategory((XPathCategory)cat);
							xpathTreeViewer.setInput(cat);
							xpathTreeViewer.expandToLevel(2);					
						}
					});
				}
			}.start();
		}
	}
	
	protected void forceLoad(XPathCategory cat) {
		XPathQuery[] queries = cat.getQueries();
		XPathFileResult[] results;
		for( int i = 0; i < queries.length; i++ ) {
			results = queries[i].getResults();
			for( int j = 0; j < results.length; j++ ) {
				results[j].getChildren();
			}
		}
	}
	
	protected class XPathTreeSelectionListener implements Listener {
		
		private final Color black;
		private final TreeItem[] lastItem;
		private final TreeEditor editor;
		private int mouseX;
		
		public XPathTreeSelectionListener() {
			black = Display.getCurrent().getSystemColor (SWT.COLOR_BLACK);
			lastItem = new TreeItem [1];
			editor = new TreeEditor (xpathTree);
			mouseX = 0;
		}
		

		public int countResultNodes(XPathQuery query) {
			int count = 0;
			XPathFileResult[] files = query.getResults();
			for( int i = 0; i < files.length; i++ ) {
				count += files[i].getChildren().length;
			}
			return count;
		}
		
		// can not modify name or location
		private boolean canEditLocationColumn(TreeItem item) {
			return false;
		}
		
		// can only modify value under certain circumstances
		private boolean canEditValueColumn(TreeItem item) {
			Object o = item.getData();
			if( o instanceof XPathQuery && countResultNodes((XPathQuery)o) == 1 ) 
				return true;
			if( o instanceof XPathFileResult && ((XPathFileResult)o).getChildren().length == 1 )
				return true;
			if( o instanceof XPathResultNode ) return true;
			return false;
		}

		private int getColumnToEdit(TreeItem item) {
			boolean locColumn = canEditLocationColumn(item);
			boolean valueColumn = canEditValueColumn(item);
			int total = 0;
			if( locColumn ) total++;
			if(valueColumn) total++;
			
			if( total == 0 ) return -1;
			if( total == 1 ) {
				if( locColumn ) return 0;
				if( valueColumn) return 1;
			}
			
			// we CAN edit more than one. Where's the mouse?
			if( mouseX < column.getWidth() && locColumn ) return 0;
			if( mouseX < (column.getWidth() + column2.getWidth()) && valueColumn ) return 1;
			return 2;
		}
		
		
		public void handleEvent (Event event) {
			TreeItem[] selectedItems = xpathTree.getSelection();
			if( selectedItems.length != 1 ) return;
			
			final TreeItem item = selectedItems[0];
			
			final int column = getColumnToEdit(item);

			if( column == -1 ) {
				lastItem[0] = item;
				return;
			}
			
			boolean isCarbon = SWT.getPlatform ().equals ("carbon");
			final Composite composite = new Composite (xpathTree, SWT.NONE);
			if (!isCarbon) composite.setBackground (black);
			final Text text = new Text (composite, SWT.NONE);
			final int inset = isCarbon ? 0 : 1;
			composite.addListener (SWT.Resize, new Listener () {
				public void handleEvent (Event e) {
					Rectangle rect = composite.getClientArea ();
					text.setBounds (rect.x + inset, rect.y + inset, rect.width - inset * 2, rect.height - inset * 2);
				}
			});
			Listener textListener = new Listener () {
				public void handleEvent (final Event e) {
					switch (e.type) {
						case SWT.FocusOut:
							treeviewerEditorTextChanged(item, text, column);
							composite.dispose ();
							break;
						case SWT.Verify:
							String newText = text.getText();
							String leftText = newText.substring (0, e.start);
							String rightText = newText.substring (e.end, newText.length ());
							GC gc = new GC (text);
							Point size = gc.textExtent (leftText + e.text + rightText);
							gc.dispose ();
							size = text.computeSize (size.x, SWT.DEFAULT);
							editor.horizontalAlignment = SWT.LEFT;
							Rectangle itemRect = item.getBounds (), rect = xpathTree.getClientArea ();
							editor.minimumWidth = Math.max (size.x, itemRect.width) + inset * 2;
							int left = itemRect.x, right = rect.x + rect.width;
							editor.minimumWidth = Math.min (editor.minimumWidth, right - left);
							editor.minimumHeight = size.y + inset * 2;
							editor.setColumn(column);
							editor.layout ();
							break;
						case SWT.Traverse:
							switch (e.detail) {
								case SWT.TRAVERSE_RETURN:
									treeviewerEditorTextChanged(item, text, column);
									//FALL THROUGH
								case SWT.TRAVERSE_ESCAPE:
									composite.dispose ();
									e.doit = false;
							}
							break;
					}
				}
			};
			text.addListener (SWT.FocusOut, textListener);
			text.addListener (SWT.Traverse, textListener);
			text.addListener (SWT.Verify, textListener);
			editor.setEditor (composite, item);
			text.setText (item.getText(column));
			text.selectAll ();
			text.setFocus ();

			lastItem [0] = item;
		}

		public int getMouseX() {
			return mouseX;
		}

		public void setMouseX(int mouseX) {
			this.mouseX = mouseX;
		}
	}

	protected void createLoadingComposite(Composite book) {
		loading = new Composite(book, SWT.NONE);
		loading.setLayout(new FillLayout());
		Label load = new Label(loading, SWT.NONE);
		load.setText("Loading");
	}
	
	protected void createXPathComposite(Composite book) {
		wrapper = new Composite(book, SWT.NONE);
		wrapper.setLayout(new FillLayout());
		int groupWidth = 500;
		final Tree xpathTree = new Tree(wrapper, SWT.BORDER | SWT.FULL_SELECTION);
		this.xpathTree = xpathTree;

		column = new TreeColumn(xpathTree, SWT.NONE);
		column2 = new TreeColumn(xpathTree, SWT.NONE);
		
		column.setText(Messages.DescriptorXPathNameLocation);
		column2.setText(Messages.DescriptorXPathAttributeValue);

		column.setWidth(groupWidth * 10 / 20);
		column2.setWidth(groupWidth * 10 / 20);

		FormData treeData = new FormData();
		treeData.left = new FormAttachment(0,5);
		treeData.right = new FormAttachment(100, -5);
		treeData.top = new FormAttachment(0,5);
		treeData.bottom = new FormAttachment(100, -5);
		xpathTree.setLayoutData(treeData);
		
		xpathTree.setHeaderVisible(true);
		xpathTree.setLinesVisible(true);
		
		
		xpathTreeViewer = new TreeViewer(xpathTree);
		xpathTreeViewer.setContentProvider(new XPathPropertyContentProvider());
		labelProvider = getLabelProvider();
		xpathTreeViewer.setLabelProvider(labelProvider);
		
		final XPathTreeSelectionListener selListener = new XPathTreeSelectionListener();
		xpathTree.addListener (SWT.MouseDoubleClick, selListener);
		xpathTree.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				if( xpathTree.getSelection().length == 1 ) {
					Object sel = xpathTree.getSelection()[0].getData();
					provider.setPropertySelection(sel);
				}
			} 
		});
		
		// alerts the top listener as to the mouse position, to know which column is serverViewerSelection
		xpathTree.addListener(SWT.MouseMove, new Listener() {
			public void handleEvent(Event event) {
				selListener.setMouseX(event.x);
			} 
		});
	}
	
	private void treeviewerEditorTextChanged(TreeItem item, Text text, int column) {
		if( column == 1 ) {
			if( text.isDisposed() || item.isDisposed()) return;
			
			// we're changing the value in the actual descriptor.
			XPathResultNode itemToChange = getChangedNode(item.getData());
			if( itemToChange != null ) {
				
				// if its unchanged do nothing
				if( itemToChange.getText().equals(text.getText())) {
					return;
				}
				
				// set the text and add the document to the list of dirty ones
				itemToChange.setText(text.getText());
				if( itemToChange.getDocument() != null ) {
					itemToChange.saveDescriptor();
				}
				xpathTreeViewer.refresh(item.getData());
			} 		
		} 
	}
	
	private XPathResultNode getChangedNode(Object data) {
		// if we are the node to change, change me
		if( data instanceof XPathResultNode ) {
			return (XPathResultNode)data;
		}
		
		// if we're a node which represents a file, but only have one matched node, thats the node.
		if( data instanceof XPathFileResult && ((XPathFileResult)data).getChildren().length == 1 ) {
			return (XPathResultNode) (((XPathFileResult)data).getChildren()[0]);
		}
		
		// if we're a top level tree item (JNDI), with one file child and one mbean grandchild, the grandchild is the node
		if( data instanceof XPathQuery && ((XPathQuery)data).getResults().length == 1 ) {
			XPathFileResult item = ((XPathFileResult) ((XPathQuery)data).getResults()[0]);
			if( item.getChildren().length == 1 ) 
				return (XPathResultNode)item.getChildren()[0];
		}
		return null;
	}

}
