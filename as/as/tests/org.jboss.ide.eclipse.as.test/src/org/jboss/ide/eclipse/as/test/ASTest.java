/**
 * JBoss by Red Hat
 * Copyright 2006-2009, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.test;

import org.eclipse.core.runtime.Plugin;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ASTest extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.ide.eclipse.as.test";

	// The shared instance
	private static ASTest plugin;
	
	/**
	 * The constructor
	 */
	public ASTest() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ASTest getDefault() {
		return plugin;
	}

	
	
	// set some constants for wide-use
	public static final String TOMCAT_RUNTIME_55 = "org.eclipse.jst.server.tomcat.runtime.55";
	public static final String JBOSS_AS_32_HOME = System.getProperty("jbosstools.test.jboss.home.3.2", "C:\\apps\\jboss\\jboss-3.2.8.SP1\\");
	public static final String JBOSS_AS_40_HOME = System.getProperty("jbosstools.test.jboss.home.4.0", "C:\\apps\\jboss\\jboss-4.0.5.GA\\");
	public static final String JBOSS_AS_42_HOME = System.getProperty("jbosstools.test.jboss.home.4.2", "C:\\apps\\jboss\\jboss-4.2.1.GA\\");
	public static final String JBOSS_AS_50_HOME = System.getProperty("jbosstools.test.jboss.home.5.0", "C:\\apps\\jboss\\jboss-5.0.0.GA\\");
	public static final String JBOSS_AS_51_HOME = System.getProperty("jbosstools.test.jboss.home.5.1", "C:\\apps\\jboss\\jboss-5.1.0.GA\\");
	public static final String JBOSS_AS_52_HOME = System.getProperty("jbosstools.test.jboss.home.5.2", "C:\\apps\\jboss\\jboss-5.2.0.GA\\");

	public static final String JBOSS_AS_HOME = System.getProperty("jbosstools.test.jboss.home", JBOSS_AS_42_HOME);
}
