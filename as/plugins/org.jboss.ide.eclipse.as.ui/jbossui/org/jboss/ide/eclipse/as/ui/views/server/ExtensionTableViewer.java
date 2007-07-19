package org.jboss.ide.eclipse.as.ui.views.server;

import java.util.ArrayList;
import java.util.Properties;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.ui.ServerUICore;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.PropertySheetFactory;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.ServerViewProvider;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.PropertySheetFactory.ISimplePropertiesHolder;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.PropertySheetFactory.SimplePropertiesPropertySheetPage;

public class ExtensionTableViewer extends TreeViewer {

	protected TableViewerPropertySheet propertySheet;
	protected Action disableCategoryAction, refreshAction, refreshFullAction;
	protected boolean suppressingRefresh = false;
	public ExtensionTableViewer(Tree tree) {
		super(tree);
		setContentProvider(new ContentProviderDelegator());
		setLabelProvider(new LabelProviderDelegator());
		propertySheet = new TableViewerPropertySheet();
		createActions();
		ServerCore.addServerLifecycleListener(new IServerLifecycleListener() {

			public void serverAdded(IServer server) {
			}

			public void serverChanged(IServer server) {
			}

			public void serverRemoved(IServer server) {
				final IServer server2 = server;
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						Object o = getInput();
						if( server2.equals(o)) {
							setInput(null);
						}
					}
				});
			} 
		});
	}

	protected void createActions() {
		disableCategoryAction = new Action() {
			public void run() {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						Object selected = getSelectedElement();
						if( selected instanceof ServerViewProvider) {
							((ServerViewProvider)selected).setEnabled(false);
							refresh();
						}
					} 
				} );
			}
		};
		disableCategoryAction.setText(Messages.DisableCategoryAction);
		refreshAction = new Action() { 
			public void run() {
				Object el = getSelectedElement();
				if( el instanceof ServerViewProvider ) 
					refresh(el);
				else 
					refresh(((IStructuredSelection)getSelection()).getFirstElement());
			}
		};
		refreshAction.setText("Refresh Item");
		
		refreshFullAction = new Action() {
			public void run() {
				refresh(null);
			}
		};
		refreshFullAction.setText("Refresh Full Tree");
	}
	
	public static class ContentWrapper {
		private Object o;
		private ServerViewProvider provider;
		
		public ContentWrapper(Object o, ServerViewProvider provider) {
			this.o = o;
			this.provider = provider;
		}

		public Object getElement() {
			return o;
		}

		public ServerViewProvider getProvider() {
			return provider;
		}
		
		public boolean equals(Object other) {
			if( other == null ) return false;
			
			if( other instanceof ContentWrapper ) {
				if( ((ContentWrapper)other).getElement() == null )
					return o == null;
				return ((ContentWrapper)other).getElement().equals(o); 
			}	
			return false;
		}
		
		public int hashCode() {
			if( o == null ) return super.hashCode();
			return o.hashCode();
		}
	}
	
	protected class LabelProviderDelegator extends LabelProvider {
		public String getText(Object obj) {
			if( obj instanceof JBossServer) {
				JBossServer server = (JBossServer)obj;
				String ret = server.getServer().getName(); 
				return ret;
			}
			if( obj instanceof ServerViewProvider) {
				return ((ServerViewProvider)obj).getName();
			}
			
			if( obj instanceof ContentWrapper ) {
				ContentWrapper wrapper = (ContentWrapper)obj;
				return wrapper.getProvider().getDelegate().getLabelProvider().getText(wrapper.getElement());
			}
			return obj.toString();
		}
		public Image getImage(Object obj) {
			if( obj instanceof JBossServer ) {
				return ServerUICore.getLabelProvider().getImage(((JBossServer)obj).getServer());				
			}
			if( obj instanceof ServerViewProvider ) {
				return ((ServerViewProvider)obj).getImage();
			}
			
			if( obj instanceof ContentWrapper ) {
				Object object2 = ((ContentWrapper)obj).getElement();
				return ((ContentWrapper)obj).getProvider().getDelegate().getLabelProvider().getImage(object2);
			}
			
			return null;
		}
	}
	protected class ContentProviderDelegator implements ITreeContentProvider {
		
		public ContentProviderDelegator() {
		}

		public ContentWrapper[] wrap( Object[] elements, ServerViewProvider provider ) {
			if( elements == null ) return new ContentWrapper[0];
			
			ContentWrapper[] wrappers = new ContentWrapper[elements.length];
			for( int i = 0; i < wrappers.length; i++ ) {
				wrappers[i] = new ContentWrapper(elements[i], provider);
			}
			return wrappers;
		}
		
		public Object[] getElements(Object inputElement) {
			if( inputElement == null ) return new Object[0];
			IServer tmp = (IServer)getInput();
			if( tmp == null )
				return new Object[0];
			return JBossServerUIPlugin.getDefault().getEnabledViewProviders(tmp);
		}

		public Object[] getChildren(Object parentElement) {
			try {
				if( parentElement == null ) return new Object[0];

				if( parentElement instanceof ServerViewProvider) {
					Object[] ret = ((ServerViewProvider)parentElement).getDelegate().getContentProvider().getChildren(parentElement);
					return wrap(ret, ((ServerViewProvider)parentElement));
				}
				
				if( parentElement instanceof ContentWrapper ) {
					ContentWrapper parentWrapper = (ContentWrapper)parentElement;
					Object[] o = null;
					try {
						o = parentWrapper.getProvider().getDelegate().getContentProvider().getChildren(parentWrapper.getElement());
					} catch( Exception e) {
					}
					if( o == null ) 
						return new Object[0];
					return wrap(o, parentWrapper.getProvider());
				}
			} catch( Exception e ) { 
				//e.printStackTrace(); 
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0 ? true : false;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			ServerViewProvider[] providers = JBossServerUIPlugin.getDefault().
				getEnabledViewProviders(newInput instanceof IServer ? (IServer)newInput : null);
			for( int i = 0; i < providers.length; i++ ) {
				try {
					providers[i].getDelegate().getContentProvider().inputChanged(viewer, oldInput, newInput);
				} catch( Exception e) {
				}
			}
		}
		
	}

	public Object getRawElement(Object o) {
		if( o instanceof ContentWrapper )
			return ((ContentWrapper)o).getElement();
		return o;
	}
	
	public ServerViewProvider getParentViewProvider(Object o) {
		if( o instanceof ContentWrapper ) 
			return ((ContentWrapper)o).getProvider();
		return null;
	}
	
	public Object getSelectedElement() {
		ISelection sel = getSelection();
		if( sel instanceof IStructuredSelection ) {
			Object o = ((IStructuredSelection)sel).getFirstElement(); 
			if( o != null && o instanceof ContentWrapper ) {
				o = ((ContentWrapper)o).getElement();
			}
			return o;
		}
		return null;
	}

	protected void fillSelectedContextMenu(Shell shell, IMenuManager mgr) {
		ISelection sel = getSelection();
		if( sel instanceof IStructuredSelection ) {
			Object selected = ((IStructuredSelection)sel).getFirstElement();
			if( selected != null ) {
				ServerViewProvider provider;
				if( selected instanceof ServerViewProvider ) {
					provider = (ServerViewProvider)selected;
				} else {
					provider = getParentViewProvider(selected);
				}
				if( provider != null ) 
					provider.getDelegate().fillContextMenu(shell, mgr, getRawElement(selected));
			}
		}

	}

	
	protected void fillJBContextMenu(Shell shell, IMenuManager menu) {
		Object selected = getSelectedElement();
		menu.add(refreshAction);
		menu.add(refreshFullAction);
		if( selected instanceof ServerViewProvider ) {
			menu.add(disableCategoryAction);
		}
		
		//menu.add(new TempAction());
		menu.add(new Separator());
	}

	public IPropertySheetPage getPropertySheet() {
			return propertySheet;
	}
	
	public class TableViewerPropertySheet implements IPropertySheetPage {

		private PageBook book;
		private ArrayList addedControls = new ArrayList();
		private SimplePropertiesPropertySheetPage topLevelPropertiesPage;
		
		public void createControl(Composite parent) {
			topLevelPropertiesPage = PropertySheetFactory.createSimplePropertiesSheet(new TopLevelProperties());
			book = new PageBook(parent, SWT.NONE);
			addedControls.clear();
			topLevelPropertiesPage.createControl(book);
			book.showPage(topLevelPropertiesPage.getControl());
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

		public void selectionChanged(IWorkbenchPart part, ISelection sel) {
			Object selected = getSelectedObject(sel);
			if( selected != null ) {
				
				IPropertySheetPage page = null;
				if( selected instanceof ContentWrapper ) {
					page = getDelegatePage((ContentWrapper)selected);
				}
				
				if( page == null ) {
					page = topLevelPropertiesPage;
				}
				page.selectionChanged(part, sel);
				book.showPage(page.getControl());
			}
		}
		private IPropertySheetPage getDelegatePage(ContentWrapper wrapper) {
			ServerViewProvider provider = wrapper.getProvider();
			IPropertySheetPage returnSheet = null;
			returnSheet = provider.getDelegate().getPropertySheetPage();
			if( !addedControls.contains(provider) && returnSheet != null) {
				returnSheet.createControl(book);
				addedControls.add(provider);
			}
			return returnSheet;
		}
		
		public Object getSelectedObject(ISelection sel) {
			if( sel instanceof IStructuredSelection ) {
				IStructuredSelection selection = (IStructuredSelection)sel;
				Object selected = selection.getFirstElement();
				return selected;
			}
			return null;
		}
		
	}
	/**
	 * Properties for the top level elements 
	 *    (a server or a category / extension point 
	 * @author rstryker
	 *
	 */
	class TopLevelProperties implements ISimplePropertiesHolder {
		public Properties getProperties(Object selected) {
			Properties ret = new Properties();
			if( selected instanceof ServerViewProvider ) {
				ServerViewProvider provider = (ServerViewProvider)selected;
				ret.setProperty(Messages.ExtensionID, provider.getId());
				ret.setProperty(Messages.ExtensionName, provider.getName());
				ret.setProperty(Messages.ExtensionDescription, provider.getDescription());
				ret.setProperty(Messages.ExtensionProviderClass, provider.getDelegateName());
			}
			return ret;
		}


		public String[] getPropertyKeys(Object selected) {
			if( selected instanceof ServerViewProvider) {
				return new String[] { 
						Messages.ExtensionName, Messages.ExtensionDescription, 
						Messages.ExtensionID, Messages.ExtensionProviderClass
				};
				
			}
			if( selected instanceof JBossServer ) {
				return new String[] {
						Messages.ServerRuntimeVersion, Messages.ServerHome, 
						Messages.ServerConfigurationName, Messages.ServerDeployDir,
				};
			}
			return new String[] {};
		}
	}

	public void dispose() {
		// The Loner
		propertySheet.dispose();
		
		ServerViewProvider[] providers = JBossServerUIPlugin.getDefault().getAllServerViewProviders();
		for( int i = 0; i < providers.length; i++ ) {
			providers[i].dispose();
		}
	}

	
	public void suppressingRefresh(Runnable runnable) {
		boolean currentVal = suppressingRefresh;
		suppressingRefresh = true;
		runnable.run();
		suppressingRefresh = currentVal;
	}

	public void refresh() {
		refresh(null);
	}
	public void refresh(final Object element) {
		if(!suppressingRefresh) {
			try {
				super.refresh(element);
			} catch( Exception e ) {
				e.printStackTrace();
			}
		} 
	}
	

}
