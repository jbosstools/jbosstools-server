package org.jboss.tools.jmx.core.tests;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.jboss.tools.jmx.core.IConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.IJMXRunnable;
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

    public void run(IJMXRunnable runnable) throws CoreException {
    }

	public void loadRoot() {
	}

}
