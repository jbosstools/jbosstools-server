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
package org.jboss.ide.eclipse.as.ui.views.server.providers;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.model.descriptor.XPathCategory;
import org.jboss.ide.eclipse.as.core.model.descriptor.XPathFileResult;
import org.jboss.ide.eclipse.as.core.model.descriptor.XPathModel;
import org.jboss.ide.eclipse.as.core.model.descriptor.XPathQuery;
import org.jboss.ide.eclipse.as.core.model.descriptor.XPathFileResult.XPathResultNode;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.dialogs.XPathDialogs;
import org.jboss.ide.eclipse.as.ui.dialogs.XPathDialogs.XPathCategoryDialog;
import org.jboss.ide.eclipse.as.ui.dialogs.XPathDialogs.XPathDialog;
import org.jboss.ide.eclipse.as.ui.views.server.JBossServerView;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.JBossServerViewExtension;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.ServerViewProvider;
import org.jboss.ide.eclipse.as.ui.views.server.providers.descriptors.DescriptorXPathPropertySheetPage;

public class DescriptorXPathViewProvider extends JBossServerViewExtension {

	private XPathTreeContentProvider contentProvider;
	private XPathTreeLabelProvider labelProvider;
	private DescriptorXPathPropertySheetPage propertyPage;

	public Action newXPathCategoryAction, deleteXPathCategoryAction, newXPathAction, 
					editXPathAction, deleteXPathAction, editFileAction;

	private IServer server;
	private XPathCategory activeCategory;
	private Object selectedPropertyObject;
	

	public DescriptorXPathViewProvider() {
		contentProvider = new XPathTreeContentProvider();
		labelProvider = new XPathTreeLabelProvider();
		createActions();
		addListeners();
	}

	protected void addListeners() {
		JBossServerView.getDefault().getExtensionFrame().getViewer().
			addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					activatePropertiesView();
				} 
			});		
	}
	
	protected void activatePropertiesView() {
		Object o = JBossServerView.getDefault().getExtensionFrame().getViewer().getSelectedElement();
		if( o instanceof XPathCategory ) {
			// show properties view
			String propsId = "org.eclipse.ui.views.PropertySheet";
			try {
				IWorkbench work = PlatformUI.getWorkbench();
				IWorkbenchWindow window = work.getActiveWorkbenchWindow(); 
				if( !isPropertiesOnTop()) {
					window.getActivePage().showView(propsId);
					if( propertyPage != null ) {
						propertyPage.selectionChanged(JBossServerView.getDefault().getViewSite().getPart(), JBossServerView.getDefault().getExtensionFrame().getViewer().getSelection());
					}
				}
			} catch( PartInitException pie ) {
			}
		}
	}
	
	protected boolean isPropertiesOnTop() {
		String propsId = "org.eclipse.ui.views.PropertySheet";
		IWorkbench work = PlatformUI.getWorkbench();
		IWorkbenchWindow window = work.getActiveWorkbenchWindow(); 
		IWorkbenchPage page = window.getActivePage();
		IViewReference ref = window.getActivePage().findViewReference(propsId);
		if( ref == null ) return false; 
		IWorkbenchPart part = ref.getPart(false);
		return ( part != null && page.isPartVisible(part));
	}
	public void setActiveCategory(XPathCategory o) {
		if( o != null && o != activeCategory) {
			activeCategory = o;
		}
	}

	public XPathCategory getActiveCategory() {
		return activeCategory;
	}

	public void setPropertySelection(Object o) {
		selectedPropertyObject = o;
	}
	
	public Object getPropertySelection() {
		return selectedPropertyObject;
	}
	
	protected class XPathTreeContentProvider implements ITreeContentProvider {
		
		public Object[] getChildren(Object parentElement) {
			if( parentElement instanceof ServerViewProvider ) {
				if( server == null ) return new Object[]{};
				return XPathModel.getDefault().getCategories(server);
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
			return new Object[0];
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if( oldInput != newInput ) {
				server = (IServer)newInput;
			}
		}
	}

	protected class XPathTreeLabelProvider extends LabelProvider {

		public Image getImage(Object element) {
			if( element instanceof XPathCategory) {
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
			}
			return null;
		}

		public String getText(Object element) {
			if( element instanceof XPathCategory) {
				return ((XPathCategory)element).getName();
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
	
	public void createActions() {
		newXPathCategoryAction = new Action() {
			public void run() {
				XPathCategoryDialog d = new XPathCategoryDialog(
							Display.getCurrent().getActiveShell(), server);
				if( d.open() == Window.OK ) {
					XPathModel.getDefault().addCategory(server, d.getText());
					XPathModel.getDefault().save(server);
					refreshViewer();
				}
			}
		};
		newXPathCategoryAction.setText("New Category");	
		
		deleteXPathCategoryAction= new Action() {
			public void run() {
				int style = SWT.APPLICATION_MODAL | SWT.YES | SWT.NO;
				MessageBox messageBox = new MessageBox (Display.getCurrent().getActiveShell(), style);
				messageBox.setText (Messages.DescriptorXPathRemoveCategory + "?");
				messageBox.setMessage (Messages.DescriptorXPathRemoveCategoryDesc);
				if( messageBox.open () == SWT.YES ) {
					XPathModel.getDefault().removeCategory(server, getActiveCategory().getName());
					XPathModel.getDefault().save(server);
					setActiveCategory(null);
					refreshViewer();
				}
			}
		};
		deleteXPathCategoryAction.setText(Messages.DescriptorXPathRemoveCategory);
		
		
		newXPathAction = new Action() {
			public void run() {
				XPathCategory category = getActiveCategory();
				if( category != null ) {
					String categoryName = category.getName();
					XPathDialog d = new XPathDialog(Display.getCurrent().getActiveShell(), server, categoryName);
					if( d.open() == Window.OK ) {
						XPathCategory[] categoryList = XPathModel.getDefault().getCategories(server);
						XPathCategory categoryItem = null;
						for( int i = 0; i < categoryList.length; i++ ) {
							if( categoryList[i].getName().equals(category.getName())) 
								categoryItem = categoryList[i];
						}
						if( categoryItem != null ) {
							XPathQuery query = new XPathQuery(d.getName(), XPathDialogs.getConfigFolder(server), null, d.getXpath(), d.getAttribute());
							categoryItem.addQuery(query);
							XPathModel.getDefault().save(server);
							refreshViewer();
						}
					}
				}
			}
		};
		newXPathAction.setText(Messages.DescriptorXPathNewXPath);

		editXPathAction = new Action() {
			public void run() {
				Object o = getPropertySelection();
				if( o != null && o instanceof XPathQuery) {
					XPathQuery original = (XPathQuery)o;
					XPathCategory category = original.getCategory();
					
					XPathDialog d = new XPathDialog(Display.getCurrent().getActiveShell(), 
							server, category.getName(), original.getName());
					d.setAttribute(original.getAttribute());
					d.setXpath(original.getXpathPattern());
					
					if( d.open() == Window.OK ) {
						original.setAttribute(d.getAttribute());
						original.setXpathPattern(d.getXpath());
						original.setName(d.getName());
						category.save();
						refreshViewer();
					}
				}
			}
		};
		editXPathAction.setText(Messages.DescriptorXPathEditXPath);

		deleteXPathAction = new Action() {
			public void run() {
				Object o = getPropertySelection();
				if( o instanceof XPathQuery ) {
					XPathCategory cat = ((XPathQuery)o).getCategory();
					cat.removeQuery((XPathQuery)o);
					cat.save();
					refreshViewer();
				}
			}
		};
		deleteXPathAction.setText(Messages.DescriptorXPathDeleteXPath);
		
		editFileAction = new Action() {
			public void run() {
				try {
					Object o = getPropertySelection();
					Path p = null;
					if( o instanceof XPathQuery && ((XPathQuery)o).getResults().length == 1 ) {
						o = (XPathFileResult)   ((XPathQuery)o).getResults()[0];
					}
					if( o instanceof XPathFileResult ) {
						p = new Path(((XPathFileResult)o).getFileLocation());
					} else if( o instanceof XPathResultNode ) {
						p = new Path(((XPathResultNode)o).getFileLocation());
					} 
					if( p != null ) {
						

						IFileStore fileStore =  EFS.getLocalFileSystem().getStore(p.removeLastSegments(1));
						fileStore =  fileStore.getChild(p.lastSegment());
						IWorkbench wb = PlatformUI.getWorkbench();
						IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
						IWorkbenchPage page = win.getActivePage();

						if (!fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists()) {
							try {
								IDE.openEditorOnFileStore(page, fileStore);
							} catch (PartInitException e) {}
						}
					}
				} catch( Exception exc ) {
				}
			}
		};
		editFileAction.setText("Edit File");
	}

	public void fillContextMenu(Shell shell, IMenuManager menu, Object selection) {
		if( selection instanceof ServerViewProvider ) {
			menu.add(this.newXPathCategoryAction);
			menu.add(new Separator());
			return;
		}
		
		if( selection instanceof XPathCategory ) {
			try {
				setActiveCategory((XPathCategory)selection);
				menu.add(this.newXPathAction);
				menu.add(this.deleteXPathCategoryAction);
				menu.add(new Separator());
			} catch( Exception e ) { e.printStackTrace(); }
		}
	}

	protected boolean supports(IServer server) {
		return true;
	}

	// Property Sheet Page
	public IPropertySheetPage getPropertySheetPage() {
		if( propertyPage == null ) {
			propertyPage = new DescriptorXPathPropertySheetPage(this);
		}
		return propertyPage;
	}
	
	protected void refreshViewer() {
		if( isEnabled() ) {
			try {
				super.refreshViewer();
				if( propertyPage != null ) {
					propertyPage.getViewer().refresh();
				}
			} catch(Exception e) {
			}
		}
	}

}
