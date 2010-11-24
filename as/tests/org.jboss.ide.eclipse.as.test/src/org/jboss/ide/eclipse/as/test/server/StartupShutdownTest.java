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
package org.jboss.ide.eclipse.as.test.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;

/**
 * These tests are for a simple startup / shutdown of a basic, 
 * default configuration. The deploy folder should be normal, 
 * with only required deployments that should not fail at all. 
 * 
 * @author Rob Stryker
 *
 */
public class StartupShutdownTest extends ServerRuntimeUtils {
	

	protected IServer currentServer;
	
	public void setUp() {
	}
	
	public void tearDown() {
		try {
			if( currentServer != null )
				currentServer.delete();
		} catch( CoreException ce ) {
			// report
		}
	}
	
	public void test32() {
		try {
			currentServer = create32Server();
			startup(currentServer);
			shutdown(currentServer);
		} catch( CoreException ce ) {
			fail("Failed during setUp for " + getName() + ": " + ce.getMessage());
		}
	}
	
	public void test40() {
		try {
			currentServer = create40Server();
			startup(currentServer);
			shutdown(currentServer);
		} catch( CoreException ce ) {
			fail("Failed during setUp for " + getName() + ": " + ce.getMessage());
		}
	}
	
	public void test42() {
		try {
			currentServer = create42Server();
			startup(currentServer);
			shutdown(currentServer);
		} catch( CoreException ce ) {
			fail("Failed during setUp for " + getName() + ": " + ce.getMessage());
		}
	}

	public void test50() {
		try {
			currentServer = create50Server();
			startup(currentServer);
			shutdown(currentServer);
		} catch( CoreException ce ) {
			fail("Failed during setUp for " + getName() + ": " + ce.getMessage());
		}
	}

	public void test51() {
		try {
			currentServer = create51Server();
			startup(currentServer);
			shutdown(currentServer);
		} catch( CoreException ce ) {
			fail("Failed during setUp for " + getName() + ": " + ce.getMessage());
		}
	}
	
}
