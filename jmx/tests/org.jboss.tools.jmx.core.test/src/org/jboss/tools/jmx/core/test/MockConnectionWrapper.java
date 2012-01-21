/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    "Rob Stryker" <rob.stryker@redhat.com> - Initial implementation
 *******************************************************************************/
package org.jboss.tools.jmx.core.test;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.jmx.core.IConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXException;
import org.jboss.tools.jmx.core.tree.Root;


public class MockConnectionWrapper implements IConnectionWrapper {

    public boolean canControl() {
        return false;
    }

    public void connect() throws IOException {
    }

    public void disconnect() throws IOException {
    }

    public IConnectionProvider getProvider() {
        return null;
    }

    public Root getRoot() {
        return null;
    }

    public boolean isConnected() {
        return false;
    }

    public void run(IJMXRunnable runnable) throws JMXException {
    }

	public void loadRoot(IProgressMonitor monitor) {
	}

}
