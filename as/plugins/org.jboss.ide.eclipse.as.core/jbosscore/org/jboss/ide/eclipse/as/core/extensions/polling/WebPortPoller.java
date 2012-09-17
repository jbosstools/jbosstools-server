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
package org.jboss.ide.eclipse.as.core.extensions.polling;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller2;
import org.jboss.ide.eclipse.as.core.server.IServerStatePollerType;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;

public class WebPortPoller implements IServerStatePoller2 {

	public static final String WEB_POLLER_ID = "org.jboss.ide.eclipse.as.core.runtime.server.WebPoller"; //$NON-NLS-1$
	private IServer server;
	private IServerStatePollerType type;
	private boolean canceled, done;
	private boolean state;
	private boolean expectedState;

	public void beginPolling(IServer server, boolean expectedState) {
		this.server = server;
		this.canceled = done = false;
		this.expectedState = expectedState;
		this.state = !expectedState;
		launchThread();
	}

	protected void launchThread() {
		Thread t = new Thread(new Runnable(){
			public void run() {
				pollerRun();
			}
		}, "Web Poller"); //$NON-NLS-1$
		t.start();
	}
	
	private void pollerRun() {
		done = false;
		String url = getURL(getServer());
		while(!canceled && !done) {
			boolean up = onePing(url);
			if( up == expectedState ) {
				done = true;
				state = expectedState;
			}
			try {
				Thread.sleep(100);
			} catch(InterruptedException ie) {} // ignore
		}
	}
	
	private static String getURL(IServer server) {
		String host = server.getHost();
		// v6_safe
		String host2 = ServerUtil.formatPossibleIpv6Address(server.getHost());
		String url = "http://"+host2; //$NON-NLS-1$
		JBossServer jbs = ServerConverter.getJBossServer(server);
		int port = jbs.getJBossWebPort();
		url += ":" + port; //$NON-NLS-1$
		return url;
	}
	
	public static boolean onePing(IServer server) {
		return onePing(getURL(server));
	}
	
	private static boolean onePing(String url) {
		URLConnection conn = null;
		try {
			URL pingUrl = new URL(url);
			conn = pingUrl.openConnection();
			((HttpURLConnection)conn).getResponseCode();
			return true;
		} catch( FileNotFoundException fnfe ) {
			return true;
		} catch (MalformedURLException e) {
			// Should NEVER happen since the URL's are hand-crafted, but whatever
			Status s = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, e.getMessage(), e);
			JBossServerCorePlugin.getDefault().log(s);
		} catch (IOException e) {
			// Does not need to be logged
			return false;
		} finally {
			if( conn != null ) {
				((HttpURLConnection)conn).disconnect();
			}
		}
		return false;
	}
	
	public IServerStatePollerType getPollerType() {
		return type;
	}

	public void setPollerType(IServerStatePollerType type) {
		this.type = type;
	}

	public IServer getServer() {
		return server;
	}

	public boolean isComplete() throws PollingException, RequiresInfoException {
		return done;
	}

	public boolean getState() throws PollingException, RequiresInfoException {
		return state;
	}

	public void cleanup() {
	}

	public List<String> getRequiredProperties() {
		return new ArrayList<String>();
	}

	public void provideCredentials(Properties properties) {
	}

	public void cancel(int type) {
		canceled = true;
	}

	public int getTimeoutBehavior() {
		return TIMEOUT_BEHAVIOR_FAIL;
	}

	public IStatus getCurrentStateSynchronous(IServer server) {
		String url = getURL(server);
		boolean b = onePing(url);
		Status s;
		if( b ) {
			s = new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, 
					NLS.bind(Messages.WebPollerServerFound, url));
		} else {
			s = new Status(IStatus.INFO, JBossServerCorePlugin.PLUGIN_ID, 
				NLS.bind(Messages.WebPollerServerNotFound, url));
		}
		return s;
	}
}
