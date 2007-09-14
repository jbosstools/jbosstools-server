package org.jboss.ide.eclipse.as.ui.views.server.providers;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXClassLoaderRepository;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.JBossServerViewExtension;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.ServerViewProvider;

public class JMXViewProvider extends JBossServerViewExtension {
	protected static final Object LOADING = new Object();
	
	protected JMXServerLifecycleListener lcListener;
	protected JMXServerListener serverListener;
	protected JMXTreeContentProvider contentProvider;
	protected JMXLabelProvider labelProvider;
	protected IServer server;
	protected JMXModel model;
	public JMXViewProvider() {
		model = new JMXModel();
		
		// make sure we know about server events
		serverListener = new JMXServerListener();
		lcListener = new JMXServerLifecycleListener();
		ServerCore.addServerLifecycleListener(lcListener);
		IServer[] servers = ServerCore.getServers();
		for( int i = 0; i < servers.length; i++ ) {
			servers[i].addServerListener(serverListener);
		}
	}
	
	protected class JMXServerLifecycleListener implements IServerLifecycleListener {
		public void serverAdded(IServer server) {
			server.addServerListener(serverListener);
		}
		public void serverChanged(IServer server) {
		}
		public void serverRemoved(IServer server) {
			server.removeServerListener(serverListener);
			JMXClassLoaderRepository.getDefault().
				removeConcerned(server, model);
		}
	}
	
	
	protected class JMXServerListener implements IServerListener {
		public void serverChanged(ServerEvent event) {
			if((event.getKind() & ServerEvent.SERVER_CHANGE) != 0)  {
				if((event.getKind() & ServerEvent.STATE_CHANGE) != 0) {
					if( event.getState() == IServer.STATE_STARTED) {
						JMXClassLoaderRepository.getDefault().
							addConcerned(event.getServer(), model);
					} else {
						JMXClassLoaderRepository.getDefault().
							removeConcerned(event.getServer(), model);
					}
				}
			}
		}
	}
	
	public ITreeContentProvider getContentProvider() {
		if( contentProvider == null ) 
			contentProvider = new JMXTreeContentProvider();
		return contentProvider;
	}
	public LabelProvider getLabelProvider() {
		if( labelProvider == null ) 
			labelProvider = new JMXLabelProvider();
		return labelProvider;
	}
	
	class JMXLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			if( obj instanceof JMXDomain )
				return ((JMXDomain)obj).getName();
			if( obj instanceof JMXBean) {
				return ((JMXBean)obj).getName().substring(((JMXBean)obj).getDomain().length()+1); 
			}
			if( obj == LOADING ) 
				return "loading...";
			return "not sure yet";
		}
		public Image getImage(Object obj) {
			return null;
		}

	}

	public class JMXTreeContentProvider implements ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			if( parentElement instanceof ServerViewProvider ) {
				if( server == null ) return new Object[]{};
				if( server.getServerState() != IServer.STATE_STARTED ) {
					model.clearModel(server);
					return new Object[]{};
				}
				JMXDomain[] domains = model.getModel(server).getDomains();
				if( domains == null ) {
					loadChildren(parentElement);
					return new Object[]{LOADING};
				}
				return domains;
			}
			if( parentElement instanceof JMXDomain ) {
				JMXBean[] beans = ((JMXDomain)parentElement).getBeans();
				if( beans == null ) {
					loadChildren(parentElement);
					return new Object[]{LOADING};
				}
				return beans;
			}
			return new Object[0];
		}
		public Object getParent(Object element) {
			return null; // unused
		}
		public boolean hasChildren(Object element) {
			return true; // always true?
		}
		public Object[] getElements(Object inputElement) {
			return null; // unused here
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if( oldInput != newInput ) {
				server = (IServer)newInput;
			}
		}
		
		protected void loadChildren(final Object parent) {
			new Thread() {
				public void run() {
					if( parent instanceof ServerViewProvider )
						model.getModel(server).loadDomains();
					else if( parent instanceof JMXDomain ) 
						((JMXDomain)parent).loadBeans();
					else if( parent instanceof JMXBean ) 
						((JMXBean)parent).getName(); // temp
					
					Display.getDefault().asyncExec(new Runnable() { 
						public void run() {
							refreshViewer(parent);
						}
					});
				}
			}.start();
		}
		
	}
	
	
	
	
	
	
	protected class JMXModel {
		protected HashMap<String, JMXModelRoot> root;
		public JMXModel() {
			root = new HashMap<String, JMXModelRoot>();
		}
		public JMXModelRoot getModel(IServer server) {
			if( root.get(server.getId()) == null ) {
				JMXModelRoot serverRoot = new JMXModelRoot(server);
				root.put(server.getId(), serverRoot);
			}
			return root.get(server.getId());
		}
		public void clearModel(IServer server) {
			root.remove(server.getId());
		}
	}

	protected static class JMXModelRoot {
		protected IServer server;
		protected JMXDomain[] domains = null;
		public JMXModelRoot(IServer server) {
			this.server = server;
		}
		public JMXDomain[] getDomains() {
			return domains;
		}
		protected void loadDomains() {
			JMXRunnable run = new JMXRunnable() {
				public void run(MBeanServerConnection connection) {
					try {
						String[] domainNames = connection.getDomains();
						JMXDomain[] domains = new JMXDomain[domainNames.length];
						for( int i = 0; i < domainNames.length; i++ ) {
							domains[i] = new JMXDomain(server, domainNames[i]);
						}
						JMXModelRoot.this.domains = domains;
					} catch( IOException ioe ) {
						JMXModelRoot.this.domains = new JMXDomain[0];
					}
				}
			};
			JMXSafeRunner.run(server, run);
		}
	}
	
	protected static class JMXDomain {
		protected String name;
		protected IServer server;
		public JMXBean[] mbeans = null;
		public JMXDomain(IServer server, String name) {
			this.server = server;
			this.name = name;
		}
		public String getName() { return name; }
		public JMXBean[] getBeans() {
			return mbeans;
		}
		protected void loadBeans() {
			// etc
			JMXRunnable run = new JMXRunnable() {
				public void run(MBeanServerConnection connection) {
					try {
						String query = name + ":*";
						Set s = connection.queryMBeans(new ObjectName(name + ":*"), null);
						Iterator i = s.iterator();
						JMXBean[] beans = new JMXBean[s.size()];
						int count = 0;
						while(i.hasNext()) {
							ObjectInstance tmp = (ObjectInstance)i.next();
							beans[count++] = new JMXBean(server, tmp);
						}
						mbeans = beans;
					} catch( MalformedObjectNameException mone) {
					} catch (IOException e) {
					}
				}
			};
			JMXSafeRunner.run(server, run);
		}
	}
	
	protected static class JMXBean {
		protected String domain;
		protected String name;
		protected String clazz;
		protected IServer server;
		public JMXBean(IServer server, ObjectInstance instance) {
			this.server = server;
			this.domain = instance.getObjectName().getDomain();
			this.clazz = instance.getClassName();
			this.name = instance.getObjectName().getCanonicalName();
		}
		public String getDomain() {
			return domain;
		}
		public String getName() {
			return name;
		}
		public String getClazz() {
			return clazz;
		}
		public IServer getServer() {
			return server;
		}
		
	}

	protected interface JMXRunnable {
		public void run(MBeanServerConnection connection);
	}
	protected static class JMXSafeRunner {
		public static void run(IServer s, JMXRunnable r) {
			ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
			ClassLoader newLoader = JMXClassLoaderRepository.getDefault().getClassLoader(s);
			Thread.currentThread().setContextClassLoader(newLoader);
			InitialContext ic = null;
			try {
				setCredentials(s);
				Properties p = getProperties(s);
				ic = new InitialContext(p);
				Object obj = ic.lookup("jmx/invoker/RMIAdaptor");
				ic.close();
				if (obj instanceof MBeanServerConnection) {
					MBeanServerConnection connection = (MBeanServerConnection) obj;
					r.run(connection);
				}
			} catch( Exception e ) {
			}
			Thread.currentThread().setContextClassLoader(currentLoader);
		}
		protected static Properties getProperties(IServer s) {
			int port = ServerConverter.getJBossServer(s).getJNDIPort();

			Properties props = new Properties();
			props.put("java.naming.factory.initial",
					"org.jnp.interfaces.NamingContextFactory");
			props.put("java.naming.factory.url.pkgs",
					"org.jboss.naming:org.jnp.interfaces");
			props.put("java.naming.provider.url", "jnp://"
					+ s.getHost() + ":" + port);
			return props;
		}
		
		protected static void setCredentials(IServer s) {
			Exception temp = null;
			try {
				ILaunchConfiguration lc = s.getLaunchConfiguration(true,
						new NullProgressMonitor());
				// get user from the IServer, but override with launch
				// configuration
				String user = ServerConverter.getJBossServer(s).getUsername();
				
				// get password from the IServer, but override with launch
				// configuration
				String pass = ServerConverter.getJBossServer(s).getPassword();
				
				// get our methods
				Class simplePrincipal = Thread.currentThread()
						.getContextClassLoader().loadClass(
								"org.jboss.security.SimplePrincipal");
				Class securityAssoc = Thread.currentThread()
						.getContextClassLoader().loadClass(
								"org.jboss.security.SecurityAssociation");
				securityAssoc.getMethods(); // force-init the methods since the
				// class hasn't been initialized yet.

				Constructor newSimplePrincipal = simplePrincipal
						.getConstructor(new Class[] { String.class });
				Object newPrincipalInstance = newSimplePrincipal
						.newInstance(new Object[] { user });

				// set the principal
				Method setPrincipalMethod = securityAssoc.getMethod(
						"setPrincipal", new Class[] { Principal.class });
				setPrincipalMethod.invoke(null,
						new Object[] { newPrincipalInstance });

				// set the credential
				Method setCredentialMethod = securityAssoc.getMethod(
						"setCredential", new Class[] { Object.class });
				setCredentialMethod.invoke(null, new Object[] { pass });
			} catch (Exception e) {}
		}


	}
	
}
