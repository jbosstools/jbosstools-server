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
package org.jboss.ide.eclipse.as.ui.viewproviders;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.model.SimpleTreeItem;
import org.jboss.ide.eclipse.as.core.model.DescriptorModel.ServerDescriptorModel.XPathTreeItem;
import org.jboss.ide.eclipse.as.core.model.DescriptorModel.ServerDescriptorModel.XPathTreeItem2;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.server.ServerAttributeHelper.SimpleXPathPreferenceTreeItem;
import org.jboss.ide.eclipse.as.core.server.ServerAttributeHelper.XPathPreferenceTreeItem;
import org.jboss.ide.eclipse.as.core.util.ASDebug;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin.ServerViewProvider;
import org.jboss.ide.eclipse.as.ui.dialogs.XPathDialogs.XPathCategoryDialog;
import org.jboss.ide.eclipse.as.ui.dialogs.XPathDialogs.XPathDialog;
import org.jboss.ide.eclipse.as.ui.preferencepages.ViewProviderPreferenceComposite;
import org.jboss.ide.eclipse.as.ui.views.JBossServerView;
import org.jboss.ide.eclipse.as.ui.views.JBossServerTableViewer.ContentWrapper;

public class DescriptorXPathViewProvider extends JBossServerViewExtension {
	private XPathTreeContentProvider contentProvider;
	private XPathTreeLabelProvider labelProvider;
	private DescriptorXPathPropertySheetPage propertyPage;
	
	private static final String XPATH_PROPERTY = "_XPATH_PROPERTY_";
	
	private static String PREFERENCE_KEY = "_DESCRIPTOR_PREFERENCE_PAGE_DETAILS_SHOWN";
	
	private static boolean COMPLEX = true;
	private static boolean SIMPLE = false;
	


	private Action newXPathCategoryAction, deleteXPathCategoryAction, newXPathAction, editXPathAction, deleteXPathAction;

	private IServer server;
	private JBossServer jbServer;
	private SimpleXPathPreferenceTreeItem root;
	

	
	public DescriptorXPathViewProvider() {
		contentProvider = new XPathTreeContentProvider();
		labelProvider = new XPathTreeLabelProvider();
		getPropertySheetPage(); // prime it
		createActions();
		addListeners();
	}
	
	
	public boolean showSimple() {
		return JBossServerUIPlugin.getDefault().getPreferenceStore().getBoolean(PREFERENCE_KEY) == SIMPLE;
	}
	
	public SimpleXPathPreferenceTreeItem getRoot() {
		if( root == null ) {
			root = jbServer.getAttributeHelper().getXPathPreferenceTree();
		}
		return root;
	}
	

	
	public void createActions() {
		newXPathCategoryAction = new Action() {
			public void run() {
				SimpleTreeItem item = getRoot();
				XPathCategoryDialog d = 
					new XPathCategoryDialog(Display.getCurrent().getActiveShell(), item);
				if( d.open() == Window.OK ) {
					String newCategory = d.getText();
					SimpleXPathPreferenceTreeItem child = new SimpleXPathPreferenceTreeItem(item, newCategory);
					ServerAttributeHelper helper = jbServer.getAttributeHelper();
					helper.saveXPathPreferenceTree(getRoot());
					helper.save();
					refreshViewer();
				}
			}
		};
		newXPathCategoryAction.setText("New Category");	
		
		deleteXPathCategoryAction= new Action() {
			public void run() {
				Object selected = getServerViewSelection();
				if( selected instanceof SimpleXPathPreferenceTreeItem && ((SimpleXPathPreferenceTreeItem)selected).getParent().equals(getRoot())) {
					int style = SWT.APPLICATION_MODAL | SWT.YES | SWT.NO;
					MessageBox messageBox = new MessageBox (Display.getCurrent().getActiveShell(), style);
					messageBox.setText (Messages.DescriptorXPathRemoveCategory + "?");
					messageBox.setMessage (Messages.DescriptorXPathRemoveCategoryDesc);
					if( messageBox.open () == SWT.YES ) {
						getRoot().deleteChild(((SimpleXPathPreferenceTreeItem)selected));
						ServerAttributeHelper helper = jbServer.getAttributeHelper();
						helper.saveXPathPreferenceTree(getRoot());
						helper.save();
						
						refreshViewer();
					}
				}
			}
		};
		deleteXPathCategoryAction.setText(Messages.DescriptorXPathRemoveCategory);
		
		
		newXPathAction = new Action() {
			public void run() {
				SimpleTreeItem tree = getRoot();
					Object o = getServerViewSelection();
					if( o != null && o instanceof SimpleXPathPreferenceTreeItem) {
						String category = (String)  ((SimpleXPathPreferenceTreeItem)o).getData();
						XPathDialog d = new XPathDialog(Display.getCurrent().getActiveShell(), tree, category, jbServer);
						if( d.open() == Window.OK ) {
							SimpleTreeItem[] categories = tree.getChildren2();
							SimpleTreeItem categoryItem = null;
							for( int i = 0; i < categories.length; i++ ) {
								if( categories[i].getData().equals(category)) 
									categoryItem = categories[i];
							}
							if( categoryItem != null ) {
								XPathPreferenceTreeItem dsfa = new XPathPreferenceTreeItem(categoryItem, d.getName(), d.getXpath(), d.getAttribute());
								ServerAttributeHelper helper = jbServer.getAttributeHelper();
								helper.saveXPathPreferenceTree(getRoot());
								helper.save();
								refreshViewer();
							}
						}
					}
				}
		};
		newXPathAction.setText(Messages.DescriptorXPathNewXPath);

		editXPathAction = new Action() {
			public void run() {
				SimpleTreeItem tree = getRoot();
				Object o = getXPathViewSelection();
					if( o != null && o instanceof XPathPreferenceTreeItem) {
						XPathPreferenceTreeItem original = (XPathPreferenceTreeItem)o;
						String category = (String)  original.getParent().getData();
						
						XPathDialog d = new XPathDialog(Display.getCurrent().getActiveShell(), tree, 
														category, jbServer, original.getName());
						d.setAttribute(original.getAttributeName());
						d.setXpath(original.getXPath());
						
						if( d.open() == Window.OK ) {
							original.setAttributeName(d.getAttribute());
							original.setXPath(d.getXpath());
							original.setName(d.getName());
							ServerAttributeHelper helper = jbServer.getAttributeHelper();
							helper.saveXPathPreferenceTree(getRoot());
							helper.save();
							refreshViewer();
						}
					}
				}
		};
		editXPathAction.setText(Messages.DescriptorXPathEditXPath);

		deleteXPathAction = new Action() {
			public void run() {
				SimpleTreeItem tree = getRoot();
				Object o = getXPathViewSelection();
				if( o instanceof XPathPreferenceTreeItem ) {
					((XPathPreferenceTreeItem)o).getParent().deleteChild((XPathPreferenceTreeItem)o);
					ServerAttributeHelper helper = jbServer.getAttributeHelper();
					helper.saveXPathPreferenceTree(getRoot());
					helper.save();
					refreshViewer();
				}
			}
		};
		deleteXPathAction.setText(Messages.DescriptorXPathDeleteXPath);
	}
	
	public void addListeners() {
		
	}
	
	public void fillContextMenu(Shell shell, IMenuManager menu, Object selection) {
		if( getServerViewSelection() instanceof ServerViewProvider ) {
			menu.add(this.newXPathCategoryAction);
			menu.add(new Separator());
			return;
		}
		
		if( getServerViewSelection() instanceof SimpleXPathPreferenceTreeItem ) {
			menu.add(this.newXPathAction);
			menu.add(this.deleteXPathCategoryAction);
			menu.add(new Separator());
		}
	}
	
	public Object getServerViewSelection() {
		return JBossServerView.getDefault().getJbViewer().getSelectedElement();
	}
	public Object getXPathViewSelection() {
		if( propertyPage != null && propertyPage.xpathTree != null ) {
			TreeItem[] items = propertyPage.xpathTree.getSelection();
			if( items.length == 1 ) {
				return items[0].getData();
			}
		}
		return null;
	}

	protected class XPathTreeContentProvider implements ITreeContentProvider {
		
		public Object[] getChildren(Object parentElement) {
			if( parentElement instanceof ServerViewProvider ) 
				return getRoot().getChildren2();
			return new Object[0];
		}
		public Object getParent(Object element) {
			return null;
		}
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0 ? true : false;
		}
		public Object[] getElements(Object inputElement) {
			return new Object[0];
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			server = (IServer)newInput;
			jbServer = server == null ? null : JBossServerCore.getServer(server);
			root = null;
		}

	}
	protected class XPathTreeLabelProvider extends LabelProvider {
		public Image getImage(Object element) {
			return null;
		}
		public String getText(Object element) {
			if( element instanceof SimpleXPathPreferenceTreeItem) {
				return ((SimpleXPathPreferenceTreeItem)element).getData().toString();
			}
			return "";
		}
	}

	
	public ITreeContentProvider getContentProvider() {
		return contentProvider;
	}

	public LabelProvider getLabelProvider() {
		return labelProvider;
	}

	
	public IPropertySheetPage getPropertySheetPage() {
		if( propertyPage == null ) {
			propertyPage = new DescriptorXPathPropertySheetPage();
		}
		return propertyPage;
	}
	
	
	
	public class DescriptorXPathPropertySheetPage implements IPropertySheetPage {
		private Object input;
		
		
		private TreeViewer xpathTreeViewer;
		private TreeColumn column, column2, column3;
		private Tree xpathTree;
		private XPathPropertyLabelProvider labelProvider;

		private Group xPathGroup;
		
		public void createControl(Composite parent) {
			createXPathGroup(parent);
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
			menu.add(newXPathAction);
			if( getXPathViewSelection() != null && getXPathViewSelection() instanceof XPathPreferenceTreeItem) {
				menu.add(editXPathAction);
				menu.add(deleteXPathAction);
			}
		}

		public void setSimple(boolean simple) {
			boolean showSimple = showSimple();

			if( showSimple != simple ) {
				JBossServerUIPlugin.getDefault().getPreferenceStore().setValue(PREFERENCE_KEY, simple == SIMPLE);
				JBossServerUIPlugin.getDefault().savePluginPreferences();

				// if the properties page isn't opened, it'll throw nulls
				try {
					if( simple ) {
						getLabelProvider().setSimple(true);
						column3.setText("");
						column2.setText(Messages.DescriptorXPathAttributeValue);
						xpathTreeViewer.refresh();
					} else {
						getLabelProvider().setSimple(false);
						column3.setText(Messages.DescriptorXPathXPathXML);
						column2.setText(Messages.DescriptorXPathAttributeKeyValue);
						xpathTreeViewer.refresh();
					}
				} catch ( Exception e ) {
				}
			}
		}
		
		public void dispose() {
		}

		public Control getControl() {
			return xPathGroup;
		}

		public void setActionBars(IActionBars actionBars) {
		}

		public void setFocus() {
		}

		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			//input = null;
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if( element instanceof ContentWrapper ) {

				if( input == ((ContentWrapper)element).getElement() ) return;
				
				input = ((ContentWrapper)element).getElement();
				IRunnableWithProgress op = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						final IProgressMonitor monitor2 = monitor;
						jbServer.getDescriptorModel().refreshDescriptors(monitor2);
						Display.getDefault().asyncExec(new Runnable() { 
							public void run() {
								xpathTreeViewer.setInput(input);
								xpathTreeViewer.expandToLevel(2);
								jbServer.getAttributeHelper().setServerPorts(root);
							}
						});
					}
				};
				try {
				new ProgressMonitorDialog(new Shell()).run(false, true, op);
					//JBossServerCore.getServer(contentProvider.server).getDescriptorModel().refreshDescriptors(new NullProgressMonitor());

				} catch( Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		

		protected class XPathTreeLabelProvider implements ITableLabelProvider {
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
			public String getColumnText(Object element, int columnIndex) {
				if( element instanceof XPathTreeItem ) {
					Object data = ((XPathTreeItem)element).getData();
					if( data instanceof File) {
						if( columnIndex == 0 ) {
							return ((File)data).getName();
						}
						if( columnIndex == 1 && ((XPathTreeItem)element).getChildren2().length == 1 ) {
							element = ((XPathTreeItem)element).getChildren2()[0];
						}
					}
				}
				
				if( element instanceof XPathPreferenceTreeItem) {
					if( columnIndex == 0 ) return ((XPathPreferenceTreeItem)element).getName().toString();
					if( columnIndex == 1 ) {
						Object o = ((XPathPreferenceTreeItem)element).getProperty(XPATH_PROPERTY);
						return o == null ? "" : o.toString();
					}
				}
				if( element instanceof SimpleXPathPreferenceTreeItem ) {
					if( columnIndex == 0 ) 
						return ((SimpleXPathPreferenceTreeItem)element).getData().toString();
				}
				return "";
			}
			public void addListener(ILabelProviderListener listener) {
			}
			public void dispose() {
			}
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			public void removeListener(ILabelProviderListener listener) {
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
			
			
			private boolean canEditLocationColumn(TreeItem item) {
				if( item == null || item != lastItem[0] ) return false;
				if( showSimple() ) return false;

				// if we're "JNDI" (aka an XPath item) then yes. All others no.
				if( item.getData() instanceof XPathPreferenceTreeItem ) return true;
				return false;
			}
			
			private boolean canEditAttributeColumn(TreeItem item) {
				// if we're the "JNDI" line then we're editing the xpath's attribute name (code, name, etc)
				if( item.getData() instanceof XPathPreferenceTreeItem ) {
					if( showSimple() ) return false;
					return true;
				}

				// if we're a file entry under "JNDI", and have multiple matches, no editing on this line
				if( item.getData() instanceof XPathTreeItem && ((XPathTreeItem)item.getData()).getChildren2().length > 1 ) return false;

				return true;
			}

			private boolean canEditValueColumn(TreeItem item) {
				if( showSimple() ) return false;

				// if we're the "JNDI" line 
				if( item.getData() instanceof XPathPreferenceTreeItem ) {
					return true;
				}
				return false;
			}

			private int getColumnToEdit(TreeItem item) {
				boolean locColumn = canEditLocationColumn(item);
				boolean attributeColumn = canEditAttributeColumn(item);
				boolean valueColumn = canEditValueColumn(item);
				int total = 0;
				if( locColumn ) total++;
				if(attributeColumn) total++;
				if(valueColumn) total++;
				
				if( total == 0 ) return -1;
				if( total == 1 ) {
					if( locColumn ) return 0;
					if(attributeColumn) return 1;
					if( valueColumn) return 2;
				}
				
				// we CAN edit more than one. Where's the mouse?
				if( mouseX < column.getWidth() && locColumn ) return 0;
				if( mouseX < (column.getWidth() + column2.getWidth()) && valueColumn ) return 1;
				return 2;
			}
			
			
			public void handleEvent (Event event) {
//				final TreeItem item = (TreeItem) event.item;
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

		protected void createXPathGroup(Composite main) {
			xPathGroup = new Group(main, SWT.NONE);
			
			xPathGroup.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					input = null;
				} 
			} );
			
			int groupWidth = 500;
			
			xPathGroup.setText(Messages.DescriptorXPathDescriptorValues);
			FormData groupData = new FormData();
			groupData.right = new FormAttachment(100, -5);
			groupData.left = new FormAttachment(0, 5);
			groupData.top = new FormAttachment(0,5);
			groupData.bottom = new FormAttachment(100, -5);
			xPathGroup.setLayoutData(groupData);
			
			xPathGroup.setLayout(new FormLayout());
			
			
			final Tree xpathTree = new Tree(xPathGroup, SWT.BORDER | SWT.FULL_SELECTION);
			this.xpathTree = xpathTree;

			column = new TreeColumn(xpathTree, SWT.NONE);
			column2 = new TreeColumn(xpathTree, SWT.NONE);
			column3 = new TreeColumn(xpathTree, SWT.NONE);
			
			column.setText(Messages.DescriptorXPathNameLocation);
			column2.setText(Messages.DescriptorXPathAttributeKeyValue);
			column3.setText(Messages.DescriptorXPathXPathXML);

			column.setWidth(groupWidth * 7 / 20);
			column2.setWidth(groupWidth * 6 / 20);
			column3.setWidth(groupWidth * 7 / 20);
			

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
			labelProvider.setSimple(showSimple());
			setSimple(showSimple());
			xpathTreeViewer.setLabelProvider(labelProvider);
			
			final XPathTreeSelectionListener selListener = new XPathTreeSelectionListener();
			xpathTree.addListener (SWT.MouseDoubleClick, selListener);
			
			// alerts the top listener as to the mouse position, to know which column is serverViewerSelection
			xpathTree.addListener(SWT.MouseMove, new Listener() {
				public void handleEvent(Event event) {
					selListener.setMouseX(event.x);
				} 
			});
		}
		
		private void treeviewerEditorTextChanged(TreeItem item, Text text, int column) {
			if( column == 0 ) { 
				xpathTextKeyChanged(item, text);
			} else if( column == 1 ) {
				xpathTextValueChanged(item, text);
			} else if( column == 2 ) {
				// TODO add to own method
				if( item.getData() instanceof XPathPreferenceTreeItem ) {
					String xp = ((XPathPreferenceTreeItem)item.getData()).getXPath();
					if( !xp.equals(text.getText())) {
						((XPathPreferenceTreeItem)item.getData()).setXPath(text.getText());
						xpathTreeViewer.refresh();
					}
				}

			}
		}
		
		private void xpathTextValueChanged(TreeItem item, Text text) {
			if( text.isDisposed() || item.isDisposed()) return;
			
			// xpath changed entirely
			if( item.getData() instanceof XPathPreferenceTreeItem ) {
				String att = ((XPathPreferenceTreeItem)item.getData()).getAttributeName();
				if( att != null && !att.equals(text.getText())) {
					((XPathPreferenceTreeItem)item.getData()).setAttributeName(text.getText());
					xpathTreeViewer.refresh();
				}
				return;
			}

			
			// we're changing the value in the actual descriptor.
			XPathTreeItem2 itemToChange = getChangedNode(item.getData());
			if( itemToChange != null && itemToChange instanceof XPathTreeItem2 ) {
				
				// if its unchanged do nothing
				if( ((XPathTreeItem2)itemToChange).getText().equals(text.getText())) {
					return;
				}
				
				// set the text and add the document to the list of dirty ones
				((XPathTreeItem2)itemToChange).setText(text.getText());
				if( itemToChange.getDocument() != null ) {
					itemToChange.saveDescriptor();
				}
				xpathTreeViewer.refresh(item.getData());
			} 
		}
		
		private void xpathTextKeyChanged(TreeItem item, Text text) {
			// "JNDI" 
			if( text.isDisposed() || item.isDisposed()) return;
			
			if( item.getData() instanceof XPathPreferenceTreeItem ) {
				
				SimpleXPathPreferenceTreeItem parentItem = (SimpleXPathPreferenceTreeItem)((SimpleXPathPreferenceTreeItem)item.getData()).getParent();
				SimpleTreeItem[] kids = parentItem.getChildren2();
				
				boolean valid = true;
				String textString = text.getText();
				for( int i = 0; i < kids.length; i++ ) {
					if( textString.equals(((XPathPreferenceTreeItem)kids[i]).getName())) 
						valid = false;
				}

				if( valid ) {
					((XPathPreferenceTreeItem)item.getData()).setName(text.getText());
					ServerAttributeHelper helper = jbServer.getAttributeHelper(); 
					helper.saveXPathPreferenceTree(parentItem.getParent());
					helper.save();
					xpathTreeViewer.refresh();
				}
				return;
			}
		}
		
		private XPathTreeItem2 getChangedNode(Object data) {
			// if we are the node to change, change me
			if( data instanceof XPathTreeItem2 ) {
				return (XPathTreeItem2)data;
			}
			
			// if we're a node which represents a file, but only have one matched node, thats the node.
			if( data instanceof XPathTreeItem && ((XPathTreeItem)data).getChildren2().length == 1 ) {
				return (XPathTreeItem2) (((XPathTreeItem)data).getChildren2()[0]);
			}
			
			// if we're a top level tree item (JNDI), with one file child and one mbean grandchild, the grandchild is the node
			if( data instanceof XPathPreferenceTreeItem && ((XPathPreferenceTreeItem)data).getChildren2().length == 1 ) {
				XPathTreeItem item = ((XPathTreeItem) ((XPathPreferenceTreeItem)data).getChildren2()[0]);
				if( item.getChildren2().length == 1 ) 
					return (XPathTreeItem2)item.getChildren2()[0];
			}
			return null;
		}
	}
	
	protected class XPathPropertyContentProvider implements ITreeContentProvider {
		
		private SimpleXPathPreferenceTreeItem category;
		
		public Object[] getChildren(Object parentElement) {
			// we're a leaf
			if( parentElement instanceof XPathTreeItem2 ) 
				return new Object[0];
			
			// we're a file node (blah.xml) 
			if( parentElement instanceof XPathTreeItem ) {
				if( ((XPathTreeItem)parentElement).getChildren2().length > 1 ) 
					return ((XPathTreeItem)parentElement).getChildren2();
				return new Object[0];
			}
			
			// we're the named element (JNDI)
			if( parentElement instanceof XPathPreferenceTreeItem ) {
				SimpleTreeItem[] kids = ((XPathPreferenceTreeItem)parentElement).getChildren2();
				return kids;
			}

			// re-creates it from scratch... hrmm
			if( parentElement instanceof ServerViewProvider ) 
				return jbServer.getAttributeHelper().getXPathPreferenceTree().getChildren2();
			return new Object[0];
		}
		public Object getParent(Object element) {
			return null;
		}
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0 ? true : false;
		}
		public Object[] getElements(Object inputElement) {
			if( inputElement instanceof SimpleXPathPreferenceTreeItem ) {
				SimpleTreeItem[] items =  ((SimpleXPathPreferenceTreeItem)inputElement).getChildren2();
				for( int i = 0; i < items.length; i++ )
					ensureLoaded((SimpleXPathPreferenceTreeItem)items[i]);
				return items;
			}
			return new Object[0];
		}
		public void ensureLoaded(SimpleXPathPreferenceTreeItem item) {
			if( item instanceof XPathPreferenceTreeItem ) {
				((XPathPreferenceTreeItem)item).ensureLoaded(jbServer);
			}
		}

		
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			category = (SimpleXPathPreferenceTreeItem) newInput;
		}

	}
	
	public ViewProviderPreferenceComposite createPreferenceComposite(Composite parent) {
		return new DescriptorPreferencePage(parent);
	}

	public class DescriptorPreferencePage extends ViewProviderPreferenceComposite {

		private Button simple, complex;
		private Combo portsCategoryCombo;
		private Combo serversCombo;
		
		private JBossServer[] servers;
		
		private HashMap serverToCategoryList;
		private HashMap serverToDefaultPortCat;
		private HashMap serverToAttributeHelper;
		
		public DescriptorPreferencePage(Composite parent) {
			super(parent, SWT.NONE);
			setLayout(new FormLayout());
			
			serverToCategoryList = new HashMap();
			serverToDefaultPortCat = new HashMap();
			serverToAttributeHelper = new HashMap();
			
			
			Composite viewDetailsComp = new Composite(this, SWT.NONE);
			fillViewDetails(viewDetailsComp);
			
			Composite defaultPortComp = new Composite(this, SWT.BORDER);
			FormData portData = new FormData();
			portData.left = new FormAttachment(0,5);
			portData.top = new FormAttachment(viewDetailsComp,5);
			portData.right = new FormAttachment(50,-5);
			defaultPortComp.setLayoutData(portData);
			fillPortPreferences(defaultPortComp);
		}
		
		private ServerAttributeHelper getAttributeHelper(JBossServer server) {
			Object helper = serverToAttributeHelper.get(server);
			if( helper == null ) {
				helper = server.getAttributeHelper();
				serverToAttributeHelper.put(server, helper);
			}
			return (ServerAttributeHelper)helper;
		}
		
		protected void fillPortPreferences(Composite portComp) {
			portComp.setLayout(new RowLayout(SWT.VERTICAL));
			
			servers = JBossServerCore.getAllJBossServers();
			String[] serverNames = new String[servers.length];
			for( int i = 0; i < servers.length; i++ )
				serverNames[i] = servers[i].getServer().getName();
			
			portComp.setLayout(new RowLayout(SWT.VERTICAL));
			
			Label nameLabel = new Label(portComp, SWT.NONE);
			nameLabel.setText(Messages.DescriptorXPathServerName);
			
			serversCombo = new Combo(portComp, SWT.READ_ONLY);
			serversCombo.setItems(serverNames);

			Label categoryLabel = new Label(portComp, SWT.NONE);
			categoryLabel.setText(Messages.DescriptorXPathPortCategory);
			
			portsCategoryCombo = new Combo(portComp, SWT.READ_ONLY);
			
			
			serversCombo.addSelectionListener(new SelectionListener() {

				public void widgetDefaultSelected(SelectionEvent e) {
				}

				public void widgetSelected(SelectionEvent e) {
					serverComboChanged();
				} 
			});
			
			portsCategoryCombo.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}
				public void widgetSelected(SelectionEvent e) {
					JBossServer server = getSelectedServer();
					if( server != null ) {
						int index = portsCategoryCombo.getSelectionIndex();
						String selectedCategory = portsCategoryCombo.getItem(index);
						serverToDefaultPortCat.put(server, selectedCategory);
					}
				} 
				
			} );
		}
		private void serverComboChanged() {
			JBossServer s = getSelectedServer();
			if( s != null ) {
				Object o = serverToCategoryList.get(s);
				ArrayList list;
				if( o == null ) {
					ServerAttributeHelper helper = getAttributeHelper(s);
					SimpleXPathPreferenceTreeItem tree = helper.getXPathPreferenceTree();
					SimpleTreeItem[] children = tree.getChildren2();
					String[] categoryNames = new String[children.length];
					for( int i = 0; i < children.length; i++ ) {
						categoryNames[i] = (String)children[i].getData();
					}
					list = new ArrayList();
					list.addAll(Arrays.asList(categoryNames));
					String portDir = helper.getDefaultPortCategoryName();
					serverToDefaultPortCat.put(s, portDir);
					serverToCategoryList.put(s, list);
				} else {
					list = (ArrayList)o;
				}
				portsCategoryCombo.setItems((String[]) list
						.toArray(new String[list.size()]));
				
				String portDir = (String)serverToDefaultPortCat.get(s);
				if( portDir != null ) {
					int index2 = portsCategoryCombo.indexOf(portDir);
					portsCategoryCombo.select(index2);
				}
			}
		}
		private JBossServer getSelectedServer() {
			int index = serversCombo.getSelectionIndex();
			String selected = serversCombo.getItem(index);
			if( selected == null ) return null;
			
			for( int i = 0; i < servers.length; i++ ) {
				if( servers[i].getServer().getName().equals(selected)) {
					return servers[i];
				}
			}
			return null;
		}
		
		protected void fillViewDetails(Composite c) {
			FormData vdcData = new FormData();
			vdcData.left = new FormAttachment(0,5);
			vdcData.top = new FormAttachment(0,5);
			c.setLayoutData(vdcData);

			c.setLayout(new RowLayout(SWT.VERTICAL));

			simple = new Button(c, SWT.RADIO);
			complex = new Button(c, SWT.RADIO);
			
			simple.setText(Messages.DescriptorXPathSimple);
			complex.setText(Messages.DescriptorXPathComplex);
			
			boolean prefVal = JBossServerUIPlugin.getDefault().getPreferenceStore().getBoolean(PREFERENCE_KEY);
			simple.setSelection(prefVal == SIMPLE);
			complex.setSelection(prefVal == COMPLEX);
		}
		
		public boolean isValid() {
			return true;
		}
		public boolean performCancel() {
			return true;
		}
		public boolean performOk() {
			boolean simp = simple.getSelection() == true ? SIMPLE : COMPLEX;
			propertyPage.setSimple(simp == SIMPLE);
			
			// now save default port categories
			ServerAttributeHelper helper;
			for( int i = 0; i < servers.length; i++ ) {
				helper = (ServerAttributeHelper)serverToAttributeHelper.get(servers[i]);
				String category = (String)serverToDefaultPortCat.get(servers[i]);
				if( category != null ) {
					helper.setDefaultPortCategoryName(category);
					helper.save();
				}
			}
			
			
			return true;
		}
		public void dispose() {
			super.dispose();
		}
	}
	
	public static class XPathPropertyLabelProvider extends LabelProvider implements ITableLabelProvider {

		private boolean simple = false;
		public void setSimple(boolean val) {
			simple = val;
		}
		
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			if( element instanceof XPathTreeItem ) {
				Object data = ((XPathTreeItem)element).getData();
				if( data instanceof File) {
					if( columnIndex == 0 ) {
						return ((File)data).getName();
					}
					if( ((XPathTreeItem)element).getChildren2().length == 1 ) {
						element = ((XPathTreeItem)element).getChildren2()[0];
					}
				}
			}
			
			if( element instanceof XPathTreeItem2) {
				XPathTreeItem2 element2 = (XPathTreeItem2)element;
				if( columnIndex == 0 ) return Messages.DescriptorXPathMatch + element2.getIndex();
				if( columnIndex == 1 ) return element2.getText();
				if( columnIndex == 2 && !simple) return element2.elementAsXML();
			}
			
			if( element instanceof XPathPreferenceTreeItem) {
				if( columnIndex == 0 ) return ((XPathPreferenceTreeItem)element).getName().toString();
				if( columnIndex == 1 && !simple) return ((XPathPreferenceTreeItem)element).getAttributeName();
				if( columnIndex == 2 && !simple) return ((XPathPreferenceTreeItem)element).getXPath();
			}
			if( element instanceof SimpleXPathPreferenceTreeItem ) {
				if( columnIndex == 0 ) 
					return ((SimpleXPathPreferenceTreeItem)element).getData().toString();
			}
			return "";
		}
	}

	protected void refreshViewer() {
		if( isEnabled() ) {
			try {
				JBossServerView.getDefault().refreshJBTree(provider);
				if( propertyPage != null ) {
					propertyPage.xpathTreeViewer.refresh();
				}
			} catch(Exception e) {
			}
		}
	}

}
