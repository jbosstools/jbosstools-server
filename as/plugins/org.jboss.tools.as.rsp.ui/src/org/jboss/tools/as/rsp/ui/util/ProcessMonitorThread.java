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
package org.jboss.tools.as.rsp.ui.util;

/**
 * Monitors a system process, waiting for it to terminate, and
 * then notifies the associated runtime process.
 */
public class ProcessMonitorThread extends Thread {
    public interface IProcessMonitorCallback {
        public void processTerminated(Process p);
    }
    /**
     * Whether the thread has been told to exit.
     */
    protected boolean fExit;
    /**
     * The underlying <code>java.lang.Process</code> being monitored.
     */
    protected Process fOSProcess;

    /**
     * The <code>Thread</code> which is monitoring the underlying process.
     */
    protected Thread fThread;

    /**
     * A lock protecting access to <code>fThread</code>.
     */
    private final Object fThreadLock = new Object();

    private IProcessMonitorCallback terminatedCallback;

    /**
     * Creates a new process monitor and starts monitoring the process for
     * termination.
     *
     * @param process process to monitor for termination
     */
    public ProcessMonitorThread(Process process, IProcessMonitorCallback callback) {
        super("Process monitor");
        setDaemon(true);
        this.terminatedCallback = callback;
        this.fOSProcess= process;
    }

    /**
     * @see Thread#run()
     */
    @Override
    public void run() {
        synchronized (fThreadLock) {
            if (fExit) {
                return;
            }
            fThread = Thread.currentThread();
        }
        while (fOSProcess != null) {
            try {
                fOSProcess.waitFor();
            } catch (InterruptedException ie) {
                // clear interrupted state
                Thread.interrupted();
            } finally {
                if( terminatedCallback != null )
                    terminatedCallback.processTerminated(fOSProcess);
                fOSProcess = null;
            }
        }
        fThread = null;
    }

    /**
     * Kills the monitoring thread.
     *
     * This method is to be useful for dealing with the error
     * case of an underlying process which has not informed this
     * monitor of its termination.
     */
    protected void killThread() {
        synchronized (fThreadLock) {
            if (fThread == null) {
                fExit = true;
            } else {
                fThread.interrupt();
            }
        }
    }
}