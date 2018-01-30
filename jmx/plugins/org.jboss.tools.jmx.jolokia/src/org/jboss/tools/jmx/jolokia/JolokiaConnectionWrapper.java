/*******************************************************************************
 * Copyright (c) 2013-2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.jmx.jolokia;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.jboss.tools.jmx.core.AbstractConnectionProvider;
import org.jboss.tools.jmx.core.ExtensionManager;
import org.jboss.tools.jmx.core.IConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXActivator;
import org.jboss.tools.jmx.core.JMXCoreMessages;
import org.jboss.tools.jmx.core.JMXException;
import org.jboss.tools.jmx.core.tree.NodeUtils;
import org.jboss.tools.jmx.core.tree.Root;
import org.jboss.tools.jmx.jolokia.internal.Activator;
import org.jboss.tools.jmx.jolokia.internal.JolokiaConnectionProvider;
import org.jboss.tools.jmx.jolokia.internal.connection.JolokiaMBeanServerConnection;
import org.jboss.tools.jmx.jolokia.internal.model.builder.CustomClientBuilder;
import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.exception.J4pRemoteException;
import org.jolokia.client.request.J4pReadRequest;
import org.jolokia.client.request.J4pReadResponse;
import org.json.simple.JSONObject;

/**
 * A class representing a Jolokia jmx connection as per the jmx.core API. 
 * It stores key information, like an id, headers, get or post type, and url. 
 * It will also run arbitrary jmx runnables. 
 * 
 */
public class JolokiaConnectionWrapper implements IConnectionWrapper, IAdaptable {

	public static final String ID = "id";
	public static final String URL = "url";
	public static final String HEADERS = "headers";
	public static final String IGNORE_SSL_ERRORS = "ignoreSSLErrors";
	public static final String GET_OR_POST = "getOrPost";
	
	private String id;
	private String url;
	private Map<String, String> headers;
	private boolean ignoreSSLErrors = false;

	private Root root;
	private boolean connected;
	private String type = "POST"; // GET or POST
	
	private JolokiaMBeanServerConnection connection = null;

	public JolokiaConnectionWrapper() {
	}

	@Override
	public String toString() {
		return getName();
	}

	public MBeanServerConnection getConnection() {
		if( connection == null ) {
			J4pClient j4pClient = createJ4pClient();
			connection = new JolokiaMBeanServerConnection(j4pClient, type);
		}
		return connection;
	}

	public synchronized void connect() throws IOException {
		verifyServerReachable();
		fireConnectionChanged();
	}
	
	public synchronized void disconnect() throws IOException {
		root = null;
		setConnected(false);
		fireConnectionChanged();
	}

	public boolean isConnected() {
		return connected;
	}

	public Root getRoot() {
		return root;
	}

	public void loadRoot() {
		loadRoot(new NullProgressMonitor());
	}
	
	@Override
	public void loadRoot(IProgressMonitor monitor) {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		if (isConnected() && root == null) {
			try {
				root = NodeUtils.createObjectNameTree(this, monitor);
			} catch (Throwable e) {
				Activator.pluginLog().logWarning("Failed to load JMX tree for " + this + ". " + e, e);
			}
		}
	}
	
	
	protected J4pClient createJ4pClient() {
		CustomClientBuilder jb = new CustomClientBuilder() {
			@Override
			public void clientBuilderAdditions(HttpClientBuilder builder) {
				Set<Header> defaultHeaders = headers.entrySet().stream()
						.map(entry -> new BasicHeader(entry.getKey(),entry.getValue()))
						.collect(Collectors.toSet());
				builder.setDefaultHeaders(defaultHeaders);
			}
			
			@Override
		    protected SSLConnectionSocketFactory createDefaultSSLConnectionSocketFactory() {
		    	if( ignoreSSLErrors) {
		            try {
						final SSLContext sslcontext = SSLContext.getInstance( "TLS");
				        sslcontext.init(null, new TrustManager[]{ new AcceptAllTrustManager()},null);
				        return new JolokiaSSLConnectionSocketFactory(sslcontext, new NoopHostnameVerifier());
					} catch (NoSuchAlgorithmException e1) {
						Activator.pluginLog().logWarning(e1);
					} catch (KeyManagementException e) {
						Activator.pluginLog().logWarning(e);
					}
		    	} 
		        SSLContext sslcontext = SSLContexts.createSystemDefault();
		        X509HostnameVerifier hostnameVerifier = new BrowserCompatHostnameVerifier();
		        return new SSLConnectionSocketFactory(sslcontext, hostnameVerifier);
		    }
		};
		jb.url(url);
		return jb.build();
	}
	
	
	
	private static class AcceptAllTrustManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			// TODO Auto-generated method stub
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			// TODO Auto-generated method stub
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			// TODO Auto-generated method stub
			return null;
		}

	}
	private static class JolokiaSSLConnectionSocketFactory extends SSLConnectionSocketFactory {
		private javax.net.ssl.SSLSocketFactory sockFact;
		
	    public JolokiaSSLConnectionSocketFactory(final SSLContext sslContext) {
	        this(sslContext, getDefaultHostnameVerifier());
	    }

	    /**
	     * @deprecated (4.4) Use {@link #SSLConnectionSocketFactory(javax.net.ssl.SSLContext,
	     *   javax.net.ssl.HostnameVerifier)}
	     */
	    @Deprecated
	    public JolokiaSSLConnectionSocketFactory(
	            final SSLContext sslContext, final X509HostnameVerifier hostnameVerifier) {
	        this(Args.notNull(sslContext, "SSL context").getSocketFactory(),
	                null, null, hostnameVerifier);
	    }

	    /**
	     * @deprecated (4.4) Use {@link #SSLConnectionSocketFactory(javax.net.ssl.SSLContext,
	     *   String[], String[], javax.net.ssl.HostnameVerifier)}
	     */
	    @Deprecated
	    public JolokiaSSLConnectionSocketFactory(
	            final SSLContext sslContext,
	            final String[] supportedProtocols,
	            final String[] supportedCipherSuites,
	            final X509HostnameVerifier hostnameVerifier) {
	        this(Args.notNull(sslContext, "SSL context").getSocketFactory(),
	                supportedProtocols, supportedCipherSuites, hostnameVerifier);
	    }

	    /**
	     * @deprecated (4.4) Use {@link #SSLConnectionSocketFactory(javax.net.ssl.SSLSocketFactory,
	     *   javax.net.ssl.HostnameVerifier)}
	     */
	    @Deprecated
	    public JolokiaSSLConnectionSocketFactory(
	            final javax.net.ssl.SSLSocketFactory socketfactory,
	            final X509HostnameVerifier hostnameVerifier) {
	        this(socketfactory, null, null, hostnameVerifier);
	    }

	    /**
	     * @deprecated (4.4) Use {@link #SSLConnectionSocketFactory(javax.net.ssl.SSLSocketFactory,
	     *   String[], String[], javax.net.ssl.HostnameVerifier)}
	     */
	    @Deprecated
	    public JolokiaSSLConnectionSocketFactory(
	            final javax.net.ssl.SSLSocketFactory socketfactory,
	            final String[] supportedProtocols,
	            final String[] supportedCipherSuites,
	            final X509HostnameVerifier hostnameVerifier) {
	        this(socketfactory, supportedProtocols, supportedCipherSuites, (HostnameVerifier) hostnameVerifier);
	    }

	    /**
	     * @since 4.4
	     */
	    public JolokiaSSLConnectionSocketFactory(
	            final SSLContext sslContext, final HostnameVerifier hostnameVerifier) {
	        this(Args.notNull(sslContext, "SSL context").getSocketFactory(),
	                null, null, hostnameVerifier);
	    }

	    /**
	     * @since 4.4
	     */
	    public JolokiaSSLConnectionSocketFactory(
	            final SSLContext sslContext,
	            final String[] supportedProtocols,
	            final String[] supportedCipherSuites,
	            final HostnameVerifier hostnameVerifier) {
	        this(Args.notNull(sslContext, "SSL context").getSocketFactory(),
	                supportedProtocols, supportedCipherSuites, hostnameVerifier);
	    }

	    /**
	     * @since 4.4
	     */
	    public JolokiaSSLConnectionSocketFactory(
	            final javax.net.ssl.SSLSocketFactory socketfactory,
	            final HostnameVerifier hostnameVerifier) {
	        this(socketfactory, null, null, hostnameVerifier);
	    }

	    /**
	     * @since 4.4
	     */
	    public JolokiaSSLConnectionSocketFactory(
	            final javax.net.ssl.SSLSocketFactory socketfactory,
	            final String[] supportedProtocols,
	            final String[] supportedCipherSuites,
	            final HostnameVerifier hostnameVerifier) {
	    	super(socketfactory, supportedProtocols, supportedCipherSuites, hostnameVerifier);
	    	sockFact = socketfactory;
	    }
	    @Override
	    public Socket createSocket(final HttpContext context) throws IOException {
	        return sockFact.createSocket();
	    }

	}
	
	
	protected void verifyServerReachable() throws IOException {
		J4pClient j4pClient = createJ4pClient();
		try {
			J4pReadRequest req = new J4pReadRequest("java.lang:type=Memory", "HeapMemoryUsage");
			J4pReadResponse resp = j4pClient.execute(req, type);
			Map<String, String> vals = resp.getValue();
			Object used = vals.get("used");
			Object max = vals.get("max");
			
			int used2 = (used instanceof Number ? ((Number)used).intValue() : -1);
			int max2 = (max instanceof Number ? ((Number)used).intValue() : -1);
			int usage = (int) (used2 * 100 / max2);
			//System.out.println("Memory usage: used: " + used2 + " / max: " + max2 + " = " + usage + "%");
			setConnected(true);
		} catch (MalformedObjectNameException mone) {
			throw new IOException(mone);
		} catch (J4pRemoteException e) {
			int jsonStatus = extractJSONStatus(e.getResponse());
			if( jsonStatus == -1 ) {
				int httpStatus = e.getStatus();
				throw new IOException("Remote error. No status code could be found in JSON response. HTTP Status Code = " + httpStatus + " (" + e.getLocalizedMessage() + ")", e);
			} else {
				throw new IOException("Remote error. Status code in JSON = " + jsonStatus + " (" + e.getLocalizedMessage() + ")", e);
			}
		} catch (J4pException e) {
            throw new IOException(e);
		}
	}
	
	/**
	 * Extract the status code from the JSON response. Use the status and code fields as the JSON payload
	 * maybe generated by OpenShift in cause of authentication or other errors.
	 * 
	 * Return -1 if no status code could be extracted.
	 * 
	 * @param response the JSON response
	 * @return the status code
	 */
	private int extractJSONStatus(JSONObject response) {
        if( response == null )
        	return -1;
        int status = 500;
        
        Object val = response.get("status");
        if (val instanceof Long) {
            status = ((Long)val).intValue();
        } else {
            val = response.get("code");
            if (val instanceof Long) {
                status = ((Long)val).intValue();
            }
        }
        return status;
    }

    protected void setConnected(boolean b) {
		connected = b;
	}

	@Override
	public void run(IJMXRunnable runnable) throws JMXException {
		try {
			runnable.run(getConnection());
		} catch (Exception ce) {
			IStatus s = new Status(IStatus.ERROR, JMXActivator.PLUGIN_ID,
					JMXCoreMessages.DefaultConnection_ErrorRunningJMXCode, ce);
			throw new JMXException(s);
		}
	}
	
	@Override
	public void run(IJMXRunnable runnable, HashMap<String, String> prefs) throws JMXException {
		run(runnable);
	}
	
	@Override
	public boolean canControl() {
		return true;
	}

	protected void fireConnectionChanged() {
		AbstractConnectionProvider provider = (AbstractConnectionProvider) getProvider();
		provider.fireChanged(this);
	}
	
	@Override
	public IConnectionProvider getProvider() {
		return ExtensionManager.getProvider(JolokiaConnectionProvider.PROVIDER_ID);
	}


	@Override
	public int hashCode() {
		return super.hashCode(); // TODO
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof JolokiaConnectionWrapper; // TODO And other stuff
	}

	// UI Integration with properties page
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		ITabbedPropertySheetPageContributor contributor = new ITabbedPropertySheetPageContributor() {
			@Override
			public String getContributorId() {
				return "org.jboss.tools.jmx.jvmmonitor.ui.JvmExplorer";
			}
		};
		if (adapter == IPropertySheetPage.class) {
			return new TabbedPropertySheetPage(contributor);
		} else if (adapter == ITabbedPropertySheetPageContributor.class) {
			return contributor;
		}
		return null;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		connection = null;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
		connection = null;
	}

	public boolean isIgnoreSSLErrors() {
		return ignoreSSLErrors;
	}

	public void setIgnoreSSLErrors(boolean ignoreSSLErrors) {
		this.ignoreSSLErrors = ignoreSSLErrors;
		connection = null;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
		connection = null;
	}
}
