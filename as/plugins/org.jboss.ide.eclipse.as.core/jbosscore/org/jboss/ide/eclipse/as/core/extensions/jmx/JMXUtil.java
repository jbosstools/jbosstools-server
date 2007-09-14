package org.jboss.ide.eclipse.as.core.extensions.jmx;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

/**
 * Utility class
 * @author Rob Stryker
 *
 */
public class JMXUtil {
	

	/**
	 * In the current thread, set the credentials from some server
	 * @param server
	 */
	public static void setCredentials(IServer server) throws CredentialException {
		Exception temp = null;
		try {
			ILaunchConfiguration lc = server.getLaunchConfiguration(true,
					new NullProgressMonitor());
			// get user from the IServer, but override with launch configuration
			String user = ServerConverter.getJBossServer(server).getUsername();
			
			// get password from the IServer, but override with launch configuration
			String pass = ServerConverter.getJBossServer(server).getPassword();
			
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
		} catch (CoreException e) {
			temp = e;
		} catch (ClassNotFoundException e) {
			temp = e;
		} catch (SecurityException e) {
			temp = e;
		} catch (NoSuchMethodException e) {
			temp = e;
		} catch (IllegalArgumentException e) {
			temp = e;
		} catch (InstantiationException e) {
			temp = e;
		} catch (IllegalAccessException e) {
			temp = e;
		} catch (InvocationTargetException e) {
			temp = e;
		}
		if( temp != null )
			throw new CredentialException(temp); 
	}
	
	public static class CredentialException extends Exception {
		private static final long serialVersionUID = 1L;
		protected Exception wrapped;
		public CredentialException(Exception wrapped) {
			this.wrapped = wrapped;
		}
		public Exception getWrapped() { return wrapped; }
	}
	
	public static Properties getDefaultProperties(IServer server) {
		int port = ServerConverter.getJBossServer(server).getJNDIPort();
		Properties props = new Properties();
		props.put("java.naming.factory.initial",
				"org.jnp.interfaces.NamingContextFactory");
		props.put("java.naming.factory.url.pkgs",
				"org.jboss.naming:org.jnp.interfaces");
		props.put("java.naming.provider.url", "jnp://"
				+ server.getHost() + ":" + port);
		return props;
	}
}
