package org.jboss.ide.eclipse.as.core.extensions.jmx;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.InitialContext;

import org.eclipse.wst.server.core.IServer;

public class JMXModel {
	protected static JMXModel instance;
	public static JMXModel getDefault() {
		if( instance == null )
			instance = new JMXModel();
		return instance;
	}
	
	
	protected HashMap<String, JMXModelRoot> root;

	protected JMXModel() {
		root = new HashMap<String, JMXModelRoot>();
	}

	public JMXModelRoot getModel(IServer server) {
		if (root.get(server.getId()) == null) {
			JMXModelRoot serverRoot = new JMXModelRoot(server);
			root.put(server.getId(), serverRoot);
		}
		return root.get(server.getId());
	}

	public void clearModel(IServer server) {
		root.remove(server.getId());
	}

	
	public static class JMXModelRoot {
		protected IServer server;
		protected JMXDomain[] domains = null;
		protected JMXException exception = null;

		public JMXModelRoot(IServer server) {
			this.server = server;
		}

		public JMXDomain[] getDomains() {
			return domains;
		}

		public JMXException getException() {
			return exception;
		}

		public void loadDomains() {
			exception = null;
			JMXRunnable run = new JMXRunnable() {
				public void run(MBeanServerConnection connection) {
					try {
						String[] domainNames = connection.getDomains();
						JMXDomain[] domains = new JMXDomain[domainNames.length];
						for (int i = 0; i < domainNames.length; i++) {
							domains[i] = new JMXDomain(server, domainNames[i]);
						}
						JMXModelRoot.this.domains = domains;
					} catch (IOException ioe) {
						exception = new JMXException(ioe);
					}
				}
			};
			JMXSafeRunner.run(server, run);
		}
	}

	public static class JMXDomain {
		protected String name;
		protected IServer server;
		protected JMXBean[] mbeans = null;
		protected JMXException exception = null;

		public JMXDomain(IServer server, String name) {
			this.server = server;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public JMXException getException() {
			return exception;
		}

		public JMXBean[] getBeans() {
			return mbeans;
		}

		public void resetChildren() {
			mbeans = null;
			exception = null;
		}
		
		public void loadBeans() {
			exception = null;
			JMXRunnable run = new JMXRunnable() {
				public void run(MBeanServerConnection connection) {
					try {
						Set<?> s = connection.queryMBeans(new ObjectName(name
								+ ":*"), null);
						Iterator<?> i = s.iterator();
						JMXBean[] beans = new JMXBean[s.size()];
						int count = 0;
						while (i.hasNext()) {
							ObjectInstance tmp = (ObjectInstance) i.next();
							beans[count++] = new JMXBean(server, tmp);
						}
						mbeans = beans;
					} catch (MalformedObjectNameException mone) {
						exception = new JMXException(mone);
					} catch (IOException ioe) {
						exception = new JMXException(ioe);
					}
				}
			};
			JMXSafeRunner.run(server, run);
		}
	}

	public static class JMXBean {
		protected String domain;
		protected String name;
		protected String clazz;
		protected IServer server;
		protected MBeanInfo info;
		protected WrappedMBeanOperationInfo[] operations;
		protected WrappedMBeanAttributeInfo[] attributes;
		
		protected JMXException exception;

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
		
		public void resetChildren() {
			info = null;
			exception = null;
		}
		
		public WrappedMBeanOperationInfo[] getOperations() {
			return operations;
		}
		

		public WrappedMBeanAttributeInfo[] getAttributes() {
			return attributes;
		}

		public JMXException getException() {
			return this.exception;
		}

		public void loadInfo() {
			exception = null;
			JMXRunnable run = new JMXRunnable() {
				public void run(MBeanServerConnection connection) {
					Exception tmp = null;
					try {
						info = connection.getMBeanInfo(new ObjectName(name));
						loadAttributes(connection);
						loadOperations(connection);
					} catch (InstanceNotFoundException e) {
						tmp = e;
					} catch (IntrospectionException e) {
						tmp = e;
					} catch (MalformedObjectNameException e) {
						tmp = e;
					} catch (ReflectionException e) {
						tmp = e;
					} catch (NullPointerException e) {
						tmp = e;
					} catch (IOException e) {
						tmp = e;
					} catch( UndeclaredThrowableException e) {
						tmp = e;
					}
					if (tmp != null) {
						exception = new JMXException(tmp);
					}
				}
			};
			JMXSafeRunner.run(server, run);
		}
		

		protected void loadOperations(MBeanServerConnection connection) {
			if (info == null)
				return;
			MBeanOperationInfo[] ops = info.getOperations();
			WrappedMBeanOperationInfo[] wrappedOps = new WrappedMBeanOperationInfo[ops.length];
			for (int i = 0; i < ops.length; i++) {
				wrappedOps[i] = new WrappedMBeanOperationInfo(server, this,
						ops[i]);
			}
			operations = wrappedOps;
		}

		protected void loadAttributes(MBeanServerConnection connection) {
			if (info == null)
				return;
			MBeanAttributeInfo[] atts = info.getAttributes();
			ArrayList wrapped = new ArrayList();
			WrappedMBeanAttributeInfo tmp;
			for (int i = 0; i < atts.length; i++) {
				try {
					tmp = new WrappedMBeanAttributeInfo(server, this, atts[i]);
					tmp.loadValue(connection);
					wrapped.add(tmp);
				} catch( Exception e ) {
				}
			}
			attributes = (WrappedMBeanAttributeInfo[]) wrapped.toArray(new WrappedMBeanAttributeInfo[wrapped.size()]);
		}



	}

	public static class WrappedMBeanOperationInfo {
		protected IServer server;
		protected JMXBean bean;
		protected MBeanOperationInfo info;
		protected WrappedMBeanOperationParameter[] params;

		public WrappedMBeanOperationInfo(IServer server, JMXBean bean,
				MBeanOperationInfo info) {
			this.server = server;
			this.bean = bean;
			this.info = info;
		}
		public MBeanOperationInfo getInfo() {
			return info;
		}
		public JMXBean getBean() {
			return bean;
		}
		
		public WrappedMBeanOperationParameter[] getParameters() {
			if( params == null ) {
				MBeanParameterInfo[] paramInfo = info.getSignature();
				params = new WrappedMBeanOperationParameter[paramInfo.length];
				for( int i = 0; i < paramInfo.length; i++ ) {
					params[i] = new WrappedMBeanOperationParameter(this, paramInfo[i]);
				}
			}
			return params;
		}
		
		public void clearParamValues() {
			if( params != null ) {
				for( int i = 0; i < params.length; i++ ) 
					params[i].setValue(null);
			}
		}
	}

	public static class WrappedMBeanOperationParameter {
		protected WrappedMBeanOperationInfo operation;
		protected MBeanParameterInfo parameterInfo;
		protected Object value;
		
		public WrappedMBeanOperationParameter(WrappedMBeanOperationInfo operation, MBeanParameterInfo param) {
			this.parameterInfo = param;
			this.operation = operation;
		}
		
		public IServer getServer() { return operation.server; }
		public JMXBean getBean() { return operation.bean; }

		public Object getValue() { return value; }
		public void setValue(Object o) { this.value = o; }
	}
	
	public static class WrappedMBeanAttributeInfo {
		protected IServer server;
		protected JMXBean bean;
		protected MBeanAttributeInfo info;
		protected Object value;
		
		public WrappedMBeanAttributeInfo(IServer server, JMXBean bean,
				MBeanAttributeInfo info) {
			this.server = server;
			this.bean = bean;
			this.info = info;
		}
		public MBeanAttributeInfo getInfo() {
			return info;
		}
		public JMXBean getBean() {
			return bean;
		}
		public void loadValue(MBeanServerConnection connection) throws Exception {
			value = connection.getAttribute(
					new ObjectName(bean.getName()), info.getName());
		}
		public Object getValue() {
			return value;
		}
	}

	
	public static class JMXException extends Exception {
		private static final long serialVersionUID = 1L;
		private Exception exception;

		public JMXException(Exception e) {
			this.exception = e;
		}

		public Exception getException() {
			return this.exception;
		}
	}

	public interface JMXRunnable {
		public void run(MBeanServerConnection connection);
	}

	public static class JMXSafeRunner {
		public static void run(IServer s, JMXRunnable r) {
			ClassLoader currentLoader = Thread.currentThread()
					.getContextClassLoader();
			ClassLoader newLoader = JMXClassLoaderRepository.getDefault()
					.getClassLoader(s);
			Thread.currentThread().setContextClassLoader(newLoader);
			InitialContext ic = null;
			try {
				JMXUtil.setCredentials(s);
				Properties p = JMXUtil.getDefaultProperties(s);
				ic = new InitialContext(p);
				Object obj = ic.lookup("jmx/invoker/RMIAdaptor");
				ic.close();
				if (obj instanceof MBeanServerConnection) {
					MBeanServerConnection connection = (MBeanServerConnection) obj;
					r.run(connection);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Thread.currentThread().setContextClassLoader(currentLoader);
		}
	}

	public static class JMXAttributesWrapper {
		protected JMXBean bean;

		public JMXAttributesWrapper(JMXBean bean) {
			this.bean = bean;
		}

		public JMXBean getBean() {
			return bean;
		}
	}

}
