/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.jmx.integration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Properties;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
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
							IJBossServerConstants.CLASS_SIMPLE_PRINCIPAL);
			Class securityAssoc = Thread.currentThread()
					.getContextClassLoader().loadClass(
							IJBossServerConstants.CLASS_SECURITY_ASSOCIATION);
			securityAssoc.getMethods(); // force-init the methods since the
			// class hasn't been initialized yet.

			Constructor newSimplePrincipal = simplePrincipal
					.getConstructor(new Class[] { String.class });
			Object newPrincipalInstance = newSimplePrincipal
					.newInstance(new Object[] { principal });

			// set the principal
			Method setPrincipalMethod = securityAssoc.getMethod(
					IJBossServerConstants.METHOD_SET_PRINCIPAL,
					new Class[] { Principal.class });
			setPrincipalMethod.invoke(null,
					new Object[] { newPrincipalInstance });

			// set the credential
			Method setCredentialMethod = securityAssoc.getMethod(
					IJBossServerConstants.METHOD_SET_CREDENTIAL, 
					new Class[] { Object.class });
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
		IJBossServer jbs = ServerConverter.getJBossServer(server);
		Properties props = new Properties();
		if( jbs != null ) {
			
			int port = jbs.getJNDIPort();
			props.put(IJBossServerConstants.NAMING_FACTORY_KEY,
					IJBossServerConstants.NAMING_FACTORY_VALUE);
			props.put(IJBossServerConstants.NAMING_FACTORY_PKGS,
					IJBossServerConstants.NAMING_FACTORY_INTERFACES);
			props.put(IJBossServerConstants.NAMING_FACTORY_PROVIDER_URL, 
					"jnp://" + jbs.getHost() + ":" + port); //$NON-NLS-1$ //$NON-NLS-2$
			props.put(IJBossServerConstants.JNP_DISABLE_DISCOVERY, new Boolean(true).booleanValue());
		} 
		return props;
	}
}
