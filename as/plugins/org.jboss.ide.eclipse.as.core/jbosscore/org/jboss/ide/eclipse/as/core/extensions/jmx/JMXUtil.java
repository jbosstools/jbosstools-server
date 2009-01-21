/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.core.extensions.jmx;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Properties;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

/**
 * Utility class
 * 
 * @author Rob Stryker rob.stryker@redhat.com
 * 
 */
public class JMXUtil {

	/**
	 * In the current thread, set the credentials from some server
	 * 
	 * @param server
	 */
	public static void setCredentials(IServer server)
			throws CredentialException {
		String user = ServerConverter.getJBossServer(server).getUsername();
		String pass = ServerConverter.getJBossServer(server).getPassword();
		setCredentials(server, user, pass);
	}

	public static void setCredentials(IServer server, Object principal,
			Object credential) throws CredentialException {
		Exception temp = null;
		try {
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
					.newInstance(new Object[] { principal });

			// set the principal
			Method setPrincipalMethod = securityAssoc.getMethod("setPrincipal",
					new Class[] { Principal.class });
			setPrincipalMethod.invoke(null,
					new Object[] { newPrincipalInstance });

			// set the credential
			Method setCredentialMethod = securityAssoc.getMethod(
					"setCredential", new Class[] { Object.class });
			setCredentialMethod.invoke(null, new Object[] { credential });
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

		public Exception getWrapped() {
			return wrapped;
		}
	}

	public static Properties getDefaultProperties(IServer server) {
		JBossServer jbs = ServerConverter.getJBossServer(server);
		Properties props = new Properties();
		if( jbs != null ) {
			
			int port = jbs.getJNDIPort();
			props.put("java.naming.factory.initial",
					"org.jnp.interfaces.NamingContextFactory");
			props.put("java.naming.factory.url.pkgs",
					"org.jboss.naming:org.jnp.interfaces");
			props.put("java.naming.provider.url", "jnp://" + jbs.getHost() + ":"
					+ port);
			props.put("jnp.disableDiscovery", "true");
		} 
		return props;
	}
}
