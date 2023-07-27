/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.rsp.ui.util.ui;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IFlushableStreamMonitor;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.jboss.tools.as.rsp.ui.actions.TerminateServerAction;
import org.jboss.tools.as.rsp.ui.model.IRsp;
import org.jboss.tools.as.rsp.ui.model.impl.RspCore;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerProcess;
import org.jboss.tools.rsp.api.dao.ServerState;

public class RemoteServerProcess implements IProcess {
	private static String getDefaultLaunchLabel(ServerProcess sp, String launchMode) {
		return sp.getProcessId() + " in mode " + launchMode;
	}

	protected ILaunch launch;
	private String label;
	protected HashMap<String, String> attributes;
	protected boolean complete = false;
	private ServerProcess serverProcess;
	private IRsp rsp;

	private RemoteProcessStreamMonitor errorStream;
	private RemoteProcessStreamMonitor outStream;

	private IStreamsProxy proxy;

	public RemoteServerProcess(ILaunch launch, IRsp rsp, ServerProcess sp) {
		this(launch, rsp, sp, getDefaultLaunchLabel(sp, launch.getLaunchMode()));
	}

	public RemoteServerProcess(ILaunch launch, IRsp rsp, ServerProcess sp, String label) {
		this.launch = launch;
		this.rsp = rsp;
		this.serverProcess = sp;
		this.label = label;
		this.attributes = new HashMap<String, String>(5);
		this.outStream = new RemoteProcessStreamMonitor();
		this.errorStream = new RemoteProcessStreamMonitor();
		this.proxy = new IStreamsProxy() {

			@Override
			public void write(String input) throws IOException {
				// TODO do nothing
			}

			@Override
			public IStreamMonitor getOutputStreamMonitor() {
				return outStream;
			}

			@Override
			public IStreamMonitor getErrorStreamMonitor() {
				return errorStream;
			}
		};
	}

	public String getAttribute(String arg0) {
		return attributes.get(arg0);
	}

	public void setAttribute(String arg0, String arg1) {
		attributes.put(arg0, arg1);
	}

	/**
	 * Fires a creation event.
	 */
	protected void fireCreationEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.CREATE));
	}

	/**
	 * Fires the given debug event.
	 *
	 * @param event debug event to fire
	 */
	protected void fireEvent(DebugEvent event) {
		DebugPlugin manager = DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(new DebugEvent[] { event });
		}
	}

	/**
	 * Fires a terminate event.
	 */
	protected void fireTerminateEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
	}

	/**
	 * Fires a change event.
	 */
	protected void fireChangeEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.CHANGE));
	}

	@Override
	public int getExitValue() throws DebugException {
		return 0;
	}

	/*
	 * Return the pattern used to generate the label.
	 * 
	 * @return the string pattern
	 */

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public ILaunch getLaunch() {
		return launch;
	}

	@Override
	public IStreamsProxy getStreamsProxy() {
		return this.proxy;
	}

	@Override
	public <T> T getAdapter(Class<T> arg0) {
		return null;
	}

	@Override
	public boolean canTerminate() {
		if (this.complete)
			return false;
		ServerState[] servers = RspCore.getDefault().getServersInRsp(this.rsp);
		ServerHandle target = this.serverProcess.getServer();
		for (int i = 0; i < servers.length; i++) {
			if (servers[i].getServer().getId().equals(target.getId())) {
				return servers[i].getState() == ServerManagementAPIConstants.STATE_STARTED;
			}
		}

		return complete;
	}

	@Override
	public boolean isTerminated() {
		if (this.complete)
			return true;
		ServerState[] servers = RspCore.getDefault().getServersInRsp(this.rsp);
		ServerHandle target = this.serverProcess.getServer();
		for (int i = 0; i < servers.length; i++) {
			if (servers[i].getServer().getId().equals(target.getId())) {
				return servers[i].getState() == ServerManagementAPIConstants.STATE_STOPPED;
			}
		}
		return complete;
	}

	protected String getServerId() {
		return this.serverProcess.getServer().getId();
	}

	@Override
	public void terminate() throws DebugException {
		TerminateServerAction.terminateServer(rsp, getServerId());
	}

	public void setComplete(boolean b) {
		this.complete = b;
		if( b ) {
			fireTerminateEvent();
		}
	}

	public String getServerProcessId() {
		return this.serverProcess.getProcessId();
	}

	public void outAppended(String msg) {
		this.outStream.append(msg);
	}

	public void errAppended(String msg) {
		this.errorStream.append(msg);
	}

	private class RemoteProcessStreamMonitor implements IFlushableStreamMonitor {

		private StringBuffer fContents = new StringBuffer();
		private ListenerList<IStreamListener> fListeners = new ListenerList<>(1);
		private boolean fBuffered = true;

		@Override
		public void addListener(IStreamListener listener) {
			fListeners.add(listener);
		}

		@Override
		public String getContents() {
			return fContents.toString();
		}

		@Override
		public void removeListener(IStreamListener listener) {
			fListeners.remove(listener);
		}

		/**
		 * Appends the given message to this stream, and notifies listeners.
		 *
		 * @param message
		 */
		public void append(String message) {
			if (isBuffered()) {
				fContents.append(message);
			}
			for (IStreamListener iStreamListener : fListeners) {
				iStreamListener.streamAppended(message, this);
			}
		}

		@Override
		public void flushContents() {
			fContents.setLength(0);
		}

		@Override
		public boolean isBuffered() {
			return fBuffered;
		}

		@Override
		public void setBuffered(boolean buffer) {
			fBuffered = buffer;
		}
	}

}
