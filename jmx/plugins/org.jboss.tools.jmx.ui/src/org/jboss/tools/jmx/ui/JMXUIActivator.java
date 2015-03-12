/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.jboss.tools.jmx.ui;

import javax.management.MBeanServerConnection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.jboss.tools.foundation.ui.plugin.BaseUIPlugin;
import org.jboss.tools.foundation.ui.plugin.BaseUISharedImages;
import org.jboss.tools.jmx.ui.internal.adapters.JMXAdapterFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class JMXUIActivator extends BaseUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.jboss.tools.jmx.ui"; //$NON-NLS-1$

    // The shared instance
    private static JMXUIActivator plugin;

    private JMXAdapterFactory adapterFactory;
    private MBeanServerConnection connection;


    /**
     * The constructor
     */
    public JMXUIActivator() {
    	super();
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        registerAdapters();
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        unregisterAdapters();
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static JMXUIActivator getDefault() {
        return plugin;
    }

    public static Shell getActiveWorkbenchShell() {
         IWorkbenchWindow window= getActiveWorkbenchWindow();
         if (window != null) {
            return window.getShell();
         }
         return null;
    }

    public static IWorkbenchWindow getActiveWorkbenchWindow() {
        return getDefault().getWorkbench().getActiveWorkbenchWindow();
    }

    public static IWorkbenchPage getActivePage() {
        return getDefault().internalGetActivePage();
    }

    private IWorkbenchPage internalGetActivePage() {
        IWorkbenchWindow window= getWorkbench().getActiveWorkbenchWindow();
        if (window == null)
            return null;
        return window.getActivePage();
    }

    public void setCurrentConnection(MBeanServerConnection connection) {
	this.connection  =  connection;
    }

    public MBeanServerConnection getCurrentConnection() {
	return this.connection;
    }

    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }

    /**
     * Log the given exception along with the provided message and severity
     * indicator
     */
    public static void log(int severity, String message, Throwable e) {
        log(new Status(severity, PLUGIN_ID, 0, message, e));
    }

    private void registerAdapters() {
        adapterFactory = new JMXAdapterFactory();
        for (Class<?> aClass : adapterFactory.getAdapterClasses()) {
        	Platform.getAdapterManager().registerAdapters(adapterFactory, aClass);
		}
    }

    private void unregisterAdapters() {
    	for (Class<?> aClass : adapterFactory.getAdapterClasses()) {
    		Platform.getAdapterManager().unregisterAdapters(adapterFactory, aClass);
    	}
    }
    
    
	
	public static final String CONNECT_DEBUGGER_SHARED_IMAGE = "icons/full/obj16/debug_view.gif"; //$NON-NLS-1$
	private static class JMXUISharedImages extends BaseUISharedImages {
		public JMXUISharedImages(Bundle pluginBundle) {
			super(pluginBundle);
			addImage(CONNECT_DEBUGGER_SHARED_IMAGE,CONNECT_DEBUGGER_SHARED_IMAGE);
		}
	}
	protected BaseUISharedImages createSharedImages() {
		return new JMXUISharedImages(getBundle());
	}
	
}
