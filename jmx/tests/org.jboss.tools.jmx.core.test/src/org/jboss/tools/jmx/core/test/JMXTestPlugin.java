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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;


public class JMXTestPlugin extends Plugin {

    public static final String PLUGIN_ID = "org.jboss.tools.jmx.core.test"; //$NON-NLS-1$
	private static JMXTestPlugin plugin;
	public JMXTestPlugin() {
		super();
	}

    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }
    public static JMXTestPlugin getDefault() {
        return plugin;
    }

    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }

    public static void log(Throwable e) {
    	log(e.getMessage(), e);
    }

    public static void log(String message, Throwable e) {
        log(IStatus.ERROR, message, e);
    }

    public static void log(int severity, String message, Throwable e) {
        log(new Status(severity, PLUGIN_ID, 0, message, e));
    }
}
