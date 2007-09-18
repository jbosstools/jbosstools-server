package org.jboss.ide.eclipse.as.ui.views.server.providers;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXClassLoaderRepository;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXAttributesWrapper;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXBean;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXDomain;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXException;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.WrappedMBeanOperationInfo;
import org.jboss.ide.eclipse.as.ui.views.server.JBossServerView;
import org.jboss.ide.eclipse.as.ui.views.server.ExtensionTableViewer.ContentWrapper;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.JBossServerViewExtension;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.ServerViewProvider;
import org.jboss.ide.eclipse.as.ui.views.server.util.ViewUtilityMethods;

public class JMXViewProvider extends JBossServerViewExtension {
	protected static final Object LOADING = new Object();
	protected static final String[] LOADING_STRING_ARRAY = new String[] { "Loading..." };
	protected static final String ATTRIBUTES_STRING = "Attributes...";

	protected JMXPropertySheetPage propertyPage;
	protected JMXServerLifecycleListener lcListener;
	protected JMXServerListener serverListener;
	protected JMXTreeContentProvider contentProvider;
	protected JMXLabelProvider labelProvider;
	protected IServer server;
	protected JMXModel model;

	public JMXViewProvider() {
		model = new JMXModel();
		addListeners();
	}

	protected void addListeners() {
		JBossServerView.addExtensionFrameListener(
				new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						Object o = JBossServerView.getDefault()
								.getExtensionFrame().getViewer()
								.getSelectedElement();
						if (o instanceof JMXBean) {
							ViewUtilityMethods.activatePropertiesView(null);
						}
					}
				});

		// make sure we know about server events
		serverListener = new JMXServerListener();
		lcListener = new JMXServerLifecycleListener();
		ServerCore.addServerLifecycleListener(lcListener);
		IServer[] servers = ServerCore.getServers();
		for (int i = 0; i < servers.length; i++) {
			servers[i].addServerListener(serverListener);
		}

	}

	protected class JMXServerLifecycleListener implements
			IServerLifecycleListener {
		public void serverAdded(IServer server) {
			server.addServerListener(serverListener);
		}

		public void serverChanged(IServer server) {
		}

		public void serverRemoved(IServer server) {
			server.removeServerListener(serverListener);
			JMXClassLoaderRepository.getDefault()
					.removeConcerned(server, model);
		}
	}

	protected class JMXServerListener implements IServerListener {
		public void serverChanged(ServerEvent event) {
			if ((event.getKind() & ServerEvent.SERVER_CHANGE) != 0) {
				if ((event.getKind() & ServerEvent.STATE_CHANGE) != 0) {
					if (event.getState() == IServer.STATE_STARTED) {
						JMXClassLoaderRepository.getDefault().addConcerned(
								event.getServer(), model);
					} else {
						JMXClassLoaderRepository.getDefault().removeConcerned(
								event.getServer(), model);
					}
				}
			}
		}
	}

	public IPropertySheetPage getPropertySheetPage() {
		if (propertyPage == null) {
			propertyPage = new JMXPropertySheetPage(this);
		}
		return propertyPage;
	}

	public ITreeContentProvider getContentProvider() {
		if (contentProvider == null)
			contentProvider = new JMXTreeContentProvider();
		return contentProvider;
	}

	public LabelProvider getLabelProvider() {
		if (labelProvider == null)
			labelProvider = new JMXLabelProvider();
		return labelProvider;
	}

	class JMXLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			if (obj instanceof JMXDomain)
				return ((JMXDomain) obj).getName();
			if (obj instanceof JMXBean) {
				return ((JMXBean) obj).getName().substring(
						((JMXBean) obj).getDomain().length() + 1);
			}
			if (obj instanceof WrappedMBeanOperationInfo) {
				return ((WrappedMBeanOperationInfo) obj).getInfo().getName();
			}

			if (obj instanceof JMXException) {
				String message = "";
				message += ((JMXException) obj).getException().getClass()
						.getName()
						+ ": ";
				message += ((JMXException) obj).getException().getMessage();
				return message;
			}
			if (obj instanceof JMXAttributesWrapper) {
				return "Attributes";
			}
			if (obj == LOADING)
				return "loading...";
			return "not sure yet: " + obj.getClass().getName();
		}

		public Image getImage(Object obj) {
			if (obj instanceof JMXException) {
				return PlatformUI.getWorkbench().getSharedImages().getImage(
						ISharedImages.IMG_OBJS_ERROR_TSK);
			}
			if (obj instanceof WrappedMBeanOperationInfo)
				return JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);
			if (obj instanceof JMXAttributesWrapper) {
				return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PUBLIC);
			}

			return null;
		}

	}

	public class JMXTreeContentProvider implements ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ServerViewProvider) {
				if (server == null)
					return new Object[] {};
				if (server.getServerState() != IServer.STATE_STARTED) {
					model.clearModel(server);
					return new Object[] {};
				}
				JMXDomain[] domains = model.getModel(server).getDomains();
				if (domains == null
						&& model.getModel(server).getException() == null) {
					loadChildrenRefreshViewer(parentElement);
					return new Object[] { LOADING };
				} else if (domains == null
						&& model.getModel(server).getException() != null) {
					return new Object[] { model.getModel(server).getException() };
				}
				return domains;
			}
			if (parentElement instanceof JMXDomain) {
				JMXBean[] beans = ((JMXDomain) parentElement).getBeans();
				if (beans == null
						&& ((JMXDomain) parentElement).getException() == null) {
					loadChildrenRefreshViewer(parentElement);
					return new Object[] { LOADING };
				} else if (beans == null
						&& ((JMXDomain) parentElement).getException() != null) {
					return new Object[] { ((JMXDomain) parentElement)
							.getException() };
				}
				return beans;
			}
			if (parentElement instanceof JMXBean) {
				WrappedMBeanOperationInfo[] operations = ((JMXBean) parentElement)
						.getOperations();
				if (operations == null
						&& ((JMXBean) parentElement).getException() == null) {
					loadChildrenRefreshViewer(parentElement);
					return new Object[] { LOADING };
				} else if (operations == null
						&& ((JMXBean) parentElement).getException() != null) {
					return new Object[] { ((JMXBean) parentElement)
							.getException() };
				}
				// add the Attributes element
				ArrayList<Object> list = new ArrayList<Object>();
				list.add(new JMXAttributesWrapper((JMXBean) parentElement));
				list.addAll(Arrays.asList(operations));
				return (Object[]) list.toArray(new Object[list.size()]);
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			return null; // unused
		}

		public boolean hasChildren(Object element) {
			if (element instanceof ServerViewProvider) {
				if (server == null
						|| server.getServerState() != IServer.STATE_STARTED) {
					return false;
				}
				return true;
			}
			if (element instanceof JMXException)
				return false;
			if (element instanceof WrappedMBeanOperationInfo)
				return false;
			if (element instanceof JMXAttributesWrapper)
				return false;
			return true; // always true?
		}

		public Object[] getElements(Object inputElement) {
			return null; // unused here
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (oldInput != newInput) {
				server = (IServer) newInput;
			}
		}
	}

	protected void loadChildrenRefreshViewer(final Object parent) {
		new Thread() {
			public void run() {
				loadChildren(parent);
				refreshViewerAsync(parent);
			}
		}.start();
	}

	protected void loadChildren(Object parent) {
		if (parent instanceof ServerViewProvider)
			model.getModel(server).loadDomains();
		else if (parent instanceof JMXDomain)
			((JMXDomain) parent).loadBeans();
		else if (parent instanceof JMXBean)
			((JMXBean) parent).loadInfo();
	}

	protected void refreshViewerAsync(final Object parent) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				refreshViewer(parent);
			}
		});
	}

	public void refreshModel(Object object) {
		if( object instanceof ContentWrapper ) 
			object = ((ContentWrapper)object).getElement();
		if (object instanceof ServerViewProvider) {
			model.clearModel(server);
		} else if (object instanceof JMXDomain) {
			((JMXDomain) object).resetChildren();
		} else if (object instanceof JMXBean) {
			((JMXBean) object).resetChildren();
		}
	}

	public class JMXPropertySheetPage implements IPropertySheetPage {

		// data
		protected JMXViewProvider provider;
		protected JMXBean bean;
		protected WrappedMBeanOperationInfo[] operations;

		// ui pieces
		protected Composite main;
		protected Combo pulldown;
		protected PageBook book;
		protected ErrorGroup errorGroup;
		protected OperationGroup operationGroup;
		protected AttributeGroup attributeGroup;
		
		public JMXPropertySheetPage(JMXViewProvider provider) {
			this.provider = provider;
		}

		public void createControl(Composite parent) {
			main = new Composite(parent, SWT.NONE);
			main.setLayout(new FormLayout());

			pulldown = new Combo(main, SWT.READ_ONLY);
			FormData pulldownData = new FormData();
			pulldownData.left = new FormAttachment(0, 10);
			// pulldownData.right = new FormAttachment(0,300);
			pulldownData.top = new FormAttachment(0, 10);
			pulldown.setLayoutData(pulldownData);

			
			Listener listener = new Listener() {
				public void handleEvent(Event event) {
					switch(event.type) {
					case SWT.Selection:
					case SWT.Modify:
						pulldownSelectionChanged();
						break;
					}
				}
			};
			pulldown.addListener(SWT.Selection, listener);
			pulldown.addListener(SWT.Modify, listener);
			
			book = new PageBook(main, SWT.NONE);
			FormData bookData = new FormData();
			bookData.top = new FormAttachment(pulldown, 10);
			bookData.left = new FormAttachment(0,5);
			bookData.right = new FormAttachment(100,-5);
			bookData.bottom = new FormAttachment(100, -5);
			book.setLayoutData(bookData);
			
			errorGroup = new ErrorGroup(book, SWT.NONE);
			operationGroup = new OperationGroup(book, SWT.NONE);
			attributeGroup = new AttributeGroup(book, SWT.NONE);
		}

		public void dispose() {
		}

		public Control getControl() {
			return main;
		}

		public void setActionBars(IActionBars actionBars) {
		}

		public void setFocus() {
		}

		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (selection instanceof IStructuredSelection) {
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				if (obj instanceof ContentWrapper)
					obj = ((ContentWrapper) obj).getElement();

				if (obj == null)
					return;
				setInputObject(obj);
			}
		}

		protected void setInputObject(Object obj) {
			if (obj instanceof JMXDomain) {
				showDomainComposite((JMXDomain) obj);
			} else if (obj instanceof JMXBean) {
				setBean((JMXBean) obj);
			} else if (obj instanceof JMXAttributesWrapper
					|| obj instanceof WrappedMBeanOperationInfo) {
				setBean(getBeanFromInput(obj));
				setComboSelectionFromInput(obj);
			}
		}

		protected void setBean(JMXBean bean) {
			if (bean != null ) {
				boolean requiresLoading = bean.getOperations() == null && bean.getException() == null;
				boolean hasError = bean.getOperations() == null && bean.getException() != null;
				boolean currentBeanLoading = bean == this.bean && pulldown.getItems().length == 1 && pulldown.getItems()[0].equals(LOADING_STRING_ARRAY[0]); 
				boolean finishedLoading = bean.getOperations() != null;

				this.bean = bean;
				if( requiresLoading ) {
					pulldown.setItems(LOADING_STRING_ARRAY);
					pulldown.select(0); // select Loading...
					loadChildrenRefreshProperties(bean);
				} else if( hasError ) {
					// some error
					showErrorComposite();
				} else if( finishedLoading ) { 
					// finished loading
					operations = bean.getOperations();
					pulldown.setItems(pulldownItems());
					pulldown.select(0); // select Loading...
				}
				main.layout();
			}
		}

		protected JMXBean getBeanFromInput(Object obj) {
			return obj instanceof JMXAttributesWrapper ? 
					((JMXAttributesWrapper) obj).getBean() : 
						((WrappedMBeanOperationInfo) obj).getBean();
		}

		protected void setComboSelectionFromInput(Object obj) {
			int index = -1;
			if (obj instanceof WrappedMBeanOperationInfo)
				index = pulldown
						.indexOf(getStringForOperation(((WrappedMBeanOperationInfo) obj)));
			if (index == -1)
				index = 0;
			pulldown.select(index);
		}

		// get the list of combo items based on the bean
		// The list should be Two larger than the number of operations
		// ex: {Attributes..., ------, op1, op2, op3... etc}
		protected String[] pulldownItems() {
			WrappedMBeanOperationInfo[] ops = bean.getOperations();
			String[] vals = null;
			if (ops != null) {
				if( ops.length == 0 ) return new String[]{ATTRIBUTES_STRING};
				vals = new String[ops.length + 2];
				vals[0] = ATTRIBUTES_STRING;
				vals[1] = "---------------";
				for (int i = 0; i < ops.length; i++) {
					vals[i + 2] = getStringForOperation(ops[i]);
				}
			}
			return vals;
		}

		protected String getStringForOperation(WrappedMBeanOperationInfo op) {
			return op.getInfo().getReturnType() + " " + op.getInfo().getName();
		}

		protected void loadChildrenRefreshProperties(final Object bean) {
			new Thread() {
				public void run() {
					loadChildren(bean);
					Display.getDefault().asyncExec(new Runnable() { public void run() { 
						setInputObject(bean);
					}});
				}
			}.start();
		}
		
		protected void showDomainComposite(JMXDomain domain) {
			// nothing
		}
		protected void showErrorComposite() {
			book.showPage(errorGroup);
		}
		
		protected void pulldownSelectionChanged() {
			if( pulldown.getSelectionIndex() != -1 ) {
				int index = pulldown.getSelectionIndex();
				if( index == 0 && pulldown.getItem(0).equals(ATTRIBUTES_STRING)) {
					book.showPage(attributeGroup);
				} else if( index != 1 && index <= operations.length) {
					book.showPage(operationGroup);
				}
				String selected = pulldown.getItem(pulldown.getSelectionIndex());
				System.out.println(selected);
			}
		}

	}
	
	protected static class ErrorGroup extends Composite {
		protected Group group;
		public ErrorGroup(Composite parent, int style) {
			super(parent, style);
			setLayout(new FillLayout());
			group = new Group(this, SWT.NONE);
			group.setText("Error");
		}
	}

	protected static class OperationGroup extends Composite {
		protected Group group;
		public OperationGroup(Composite parent, int style) {
			super(parent, style);
			setLayout(new FillLayout());
			group = new Group(this, SWT.NONE);
			group.setText("Operation");
		}
	}

	protected static class AttributeGroup extends Composite {
		protected Group group;
		public AttributeGroup(Composite parent, int style) {
			super(parent, style);
			setLayout(new FillLayout());
			group = new Group(this, SWT.NONE);
			group.setText("Attributes");
		}
	}

}
