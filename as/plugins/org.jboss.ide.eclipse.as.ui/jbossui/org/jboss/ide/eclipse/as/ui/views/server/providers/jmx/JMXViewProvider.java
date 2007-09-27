package org.jboss.ide.eclipse.as.ui.views.server.providers.jmx;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXClassLoaderRepository;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXBean;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXDomain;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListenerManager;
import org.jboss.ide.eclipse.as.ui.views.server.JBossServerView;
import org.jboss.ide.eclipse.as.ui.views.server.ExtensionTableViewer.ContentWrapper;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.JBossServerViewExtension;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.ServerViewProvider;
import org.jboss.ide.eclipse.as.ui.views.server.util.ViewUtilityMethods;

public class JMXViewProvider extends JBossServerViewExtension {
	public static final Object LOADING = new Object();
	public static final String[] LOADING_STRING_ARRAY = new String[] { "Loading..." };
	public static final String ATTRIBUTES_STRING = "Attributes...";

	public static final Object CLASSLOADING_TOKEN = new Object();
	
	protected JMXPropertySheetPage propertyPage;
	protected JMXServerListener serverListener;
	protected ISelectionChangedListener jbossServerViewSelectionListener;
	protected JMXTreeContentProvider contentProvider;
	protected JMXLabelProvider labelProvider;
	protected IServer server;

	public JMXViewProvider() {
	}

	public void setServer(IServer server) {
		this.server = server;
	}
	
	public IServer getServer() {
		return this.server;
	}
	
	public void enable() {
		addListeners();
	}
	public void disable() {
		removeListeners();
	}

	
	protected void addListeners() {
		jbossServerViewSelectionListener = 	new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object o = JBossServerView.getDefault()
						.getExtensionFrame().getViewer()
						.getSelectedElement();
				if (o instanceof JMXBean) {
					Display.getDefault().asyncExec(new Runnable() { public void run() {
						ViewUtilityMethods.activatePropertiesView(null);
					}});
				}
			}
		};

		JBossServerView.addExtensionFrameListener(jbossServerViewSelectionListener);

		// make sure we know about server events
		serverListener = new JMXServerListener();
		UnitedServerListenerManager.getDefault().addListener(serverListener);
	}
	
	protected void removeListeners() {
		UnitedServerListenerManager.getDefault().removeListener(serverListener);
		JBossServerView.removeExtensionFrameListener(jbossServerViewSelectionListener);
	}

	protected class JMXServerListener extends UnitedServerListener {
		public void init(IServer server) {
			if( server.getServerState() == IServer.STATE_STARTED ) 
				JMXClassLoaderRepository.getDefault().addConcerned(
						server, CLASSLOADING_TOKEN);
			
		}
		public void serverRemoved(IServer server) {
			JMXClassLoaderRepository.getDefault()
					.removeConcerned(server, JMXModel.getDefault());
		}
		public void serverChanged(ServerEvent event) {
			if ((event.getKind() & ServerEvent.SERVER_CHANGE) != 0) {
				if ((event.getKind() & ServerEvent.STATE_CHANGE) != 0) {
					if (event.getState() == IServer.STATE_STARTED) {
						JMXClassLoaderRepository.getDefault().addConcerned(
								event.getServer(), CLASSLOADING_TOKEN);
					} else {
						JMXClassLoaderRepository.getDefault().removeConcerned(
								event.getServer(), CLASSLOADING_TOKEN);
					}
				}
			}
		}
		public void cleanUp(IServer server) {
			JMXClassLoaderRepository.getDefault().removeConcerned(
					server, CLASSLOADING_TOKEN);
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
			contentProvider = new JMXTreeContentProvider(this);
		return contentProvider;
	}

	public LabelProvider getLabelProvider() {
		if (labelProvider == null)
			labelProvider = new JMXLabelProvider();
		return labelProvider;
	}


	protected void loadChildren(Object parent) {
		if (parent instanceof ServerViewProvider)
			JMXModel.getDefault().getModel(server).loadDomains();
		else if (parent instanceof JMXDomain)
			((JMXDomain) parent).loadBeans();
		else if (parent instanceof JMXBean)
			((JMXBean) parent).loadInfo();
	}

	public void refreshViewerAsync(final Object parent) {
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
			JMXModel.getDefault().clearModel(server);
		} else if (object instanceof JMXDomain) {
			((JMXDomain) object).resetChildren();
		} else if (object instanceof JMXBean) {
			((JMXBean) object).resetChildren();
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


}
