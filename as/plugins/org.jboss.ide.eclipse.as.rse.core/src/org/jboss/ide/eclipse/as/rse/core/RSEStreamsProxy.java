/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.ide.eclipse.as.rse.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.IStreamsProxy2;
import org.eclipse.debug.internal.core.InputStreamMonitor;
import org.eclipse.debug.internal.core.OutputStreamMonitor;


/**
 * Standard implementation of a streams proxy for IStreamsProxy.
 */
public class RSEStreamsProxy implements IStreamsProxy, IStreamsProxy2 {
	/**
	 * The monitor for the output stream (connected to standard out of the process)
	 */
	private OSMonitor fOutputMonitor;
	/**
	 * The monitor for the error stream (connected to standard error of the process)
	 */
	private OSMonitor fErrorMonitor;
	/**
	 * The monitor for the input stream (connected to standard in of the process)
	 */
	private InputStreamMonitor fInputMonitor;
	/**
	 * Records the open/closed state of communications with
	 * the underlying streams.  Note: fClosed is initialized to
	 * <code>false</code> by default.
	 */
	private boolean fClosed;
	/**
	 * Creates a <code>StreamsProxy</code> on the streams
	 * of the given system process.
	 *
	 * @param process system process to create a streams proxy on
	 * @param charset stream charset or <code>null</code> if default
	 */
	public RSEStreamsProxy(InputStream sysout, InputStream syserr, OutputStream sysin, Charset charset) {
		if( sysout != null ) {
			fOutputMonitor= new OSMonitor(sysout, charset == null ? Charset.defaultCharset() : charset);
			fOutputMonitor.startMonitoring();
		}
		if( syserr != null ) {
			fErrorMonitor= new OSMonitor(syserr, charset == null ? Charset.defaultCharset() : charset);
			fErrorMonitor.startMonitoring();
		}
		if( sysin != null ) {
			fInputMonitor= new InputStreamMonitor(sysin, charset == null ? Charset.defaultCharset().name() : charset.name()); 
			fInputMonitor.startMonitoring();
		}
	}

	/**
	 * Causes the proxy to close all
	 * communications between it and the
	 * underlying streams after all remaining data
	 * in the streams is read.
	 */
	public void close() {
		if (!isClosed(true)) {
			if( fOutputMonitor != null )
				fOutputMonitor.close();
			if( fErrorMonitor != null )
				fErrorMonitor.close();
			if( fInputMonitor != null )
				fInputMonitor.close();
		}
	}

	/**
	 * Returns whether the proxy is currently closed.  This method
	 * synchronizes access to the <code>fClosed</code> flag.
	 *
	 * @param setClosed If <code>true</code> this method will also set the
	 * <code>fClosed</code> flag to true.  Otherwise, the <code>fClosed</code>
	 * flag is not modified.
	 * @return Returns whether the stream proxy was already closed.
	 */
	private synchronized boolean isClosed(boolean setClosed) {
	    boolean closed = fClosed;
	    if (setClosed) {
	        fClosed = true;
	    }
	    return closed;
	}

	/**
	 * @see IStreamsProxy#getErrorStreamMonitor()
	 */
	@Override
	public IStreamMonitor getErrorStreamMonitor() {
		return fErrorMonitor;
	}

	/**
	 * @see IStreamsProxy#getOutputStreamMonitor()
	 */
	@Override
	public IStreamMonitor getOutputStreamMonitor() {
		return fOutputMonitor;
	}

	/**
	 * @see IStreamsProxy#write(String)
	 */
	@Override
	public void write(String input) throws IOException {
		if (!isClosed(false) && fInputMonitor != null) {
			fInputMonitor.write(input);
		} else {
			throw new IOException();
		}
	}

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStreamsProxy2#closeInputStream()
     */
    @Override
	public void closeInputStream() throws IOException {
        if (!isClosed(false) && fInputMonitor != null) {
            fInputMonitor.closeInputStream();
        } else {
            throw new IOException();
        }

    }
    
    /**
     * Dumb super class has everything protected instead of public
     */
    private static class OSMonitor extends OutputStreamMonitor {
		public OSMonitor(InputStream stream, Charset charset) {
			super(stream, charset.name()); // TODO UNDO THIS CHANGE
		}
		public void startMonitoring() {
			super.startMonitoring("Output Stream Monitor");
		}
		public void close() {
			super.close();
		}
    }
}
