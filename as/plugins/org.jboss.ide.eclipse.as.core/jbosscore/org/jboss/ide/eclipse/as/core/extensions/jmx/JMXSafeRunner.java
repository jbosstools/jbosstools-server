package org.jboss.ide.eclipse.as.core.extensions.jmx;

import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.naming.InitialContext;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXException;

public class JMXSafeRunner {
	private String user, pass;
	private IServer server;
	
	public JMXSafeRunner(IServer s) {
		this.server = s;
		user = ServerConverter.getJBossServer(s).getUsername();
		pass = ServerConverter.getJBossServer(s).getPassword();
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	
	public  void run(IJMXRunnable r) throws CoreException {
		run(server,r,user,pass);
	}
	
	public static void run(IServer s, IJMXRunnable r) throws JMXException {
		String user = ServerConverter.getJBossServer(s).getUsername();
		String pass = ServerConverter.getJBossServer(s).getPassword();
		run(s,r,user,pass);
	}
	
	public static void run(IServer s, IJMXRunnable r, String user, String pass) throws JMXException {
		JMXClassLoaderRepository.getDefault().addConcerned(s, r);
		ClassLoader currentLoader = Thread.currentThread()
				.getContextClassLoader();
		ClassLoader newLoader = JMXClassLoaderRepository.getDefault()
				.getClassLoader(s);
		Thread.currentThread().setContextClassLoader(newLoader);
		InitialContext ic = null;
		try {
			JMXUtil.setCredentials(s,user,pass);
			Properties p = JMXUtil.getDefaultProperties(s);
			ic = new InitialContext(p);
			Object obj = ic.lookup("jmx/invoker/RMIAdaptor"); //$NON-NLS-1$
			ic.close();
			if (obj instanceof MBeanServerConnection) {
				MBeanServerConnection connection = (MBeanServerConnection) obj;
				r.run(connection);
			}
		} catch( Exception e ) {  
			throw new JMXException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
					e.getMessage() == null ? e.getClass().getName() : e.getMessage(), e));
		} finally {
			JMXClassLoaderRepository.getDefault().removeConcerned(s, r);
			Thread.currentThread().setContextClassLoader(currentLoader);
		}
	}
}
