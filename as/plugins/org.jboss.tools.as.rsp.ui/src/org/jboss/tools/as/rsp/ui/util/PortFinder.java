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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Find a free port in some range
 */
public class PortFinder {
    public static int nextFreePort(int from, int to) {
        for( int port = from; port < to; port ++ ) {
            if (isLocalPortFree(port)) {
                return port;
            }
        }
        return -1;
    }

    public static boolean isLocalPortFree(int port) {
        try {
            new ServerSocket(port).close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void waitForServer(String host, int port, int timeout) {
        long endTime = System.currentTimeMillis() + timeout;
        while( System.currentTimeMillis() < endTime) {
            try {
                Socket socket = new Socket(host, port);
                try {
                    socket.close();
                } catch (IOException ioe) {
                    // ignore
                }
                return;
            } catch (IOException e) {
                // ignore
            }
            try {
                Thread.sleep(200);
            } catch(InterruptedException ie) {
                Thread.interrupted();
                return;
            }
        }
    }
}
