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

import java.io.File;

import junit.framework.TestCase;

import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

/**
 * This class will test whether all of the pre-req
 * locations, such as a working JBoss 4.2, are set.
 * 
 * @author rob.stryker <rob.stryker@redhat.com>
 */
public class PreReqTest extends TestCase {

	public void testAS32Exists() {
		_testExists("JBoss 3.2", ASTest.JBOSS_AS_32_HOME);
	}
	
	public void testAS40Exists() {
		_testExists("JBoss 4.0", ASTest.JBOSS_AS_40_HOME);
	}

	public void testAS42Exists() {
		_testExists("JBoss 4.2", ASTest.JBOSS_AS_42_HOME);
	}
	
	public void testASHomeExists() {
		_testExists("Default JBoss Installation", ASTest.JBOSS_AS_HOME);
	}
	
	public void _testExists(String desc, String loc) {
		if (!new File(loc).exists())
			fail(desc + " (" + loc + ") does not exist.");
	}

	
	
	/*
	 * 
	 * Test runtime and server types are found
	 * 
	 */
	
	public void testRuntime32Found() {
		_testRuntime(IJBossToolingConstants.AS_32);
	}

	public void testRuntime40Found() {
		_testRuntime(IJBossToolingConstants.AS_40);
	}

	public void testRuntime42Found() {
		_testRuntime(IJBossToolingConstants.AS_42);
	}

	public void testRuntime50Found() {
		_testRuntime(IJBossToolingConstants.AS_50);
	}

	public void testRuntime51Found() {
		_testRuntime(IJBossToolingConstants.AS_51);
	}

	public void testServer32Found() {
		_testServer(IJBossToolingConstants.SERVER_AS_32);
	}

	public void testServer40Found() {
		_testServer(IJBossToolingConstants.SERVER_AS_40);
	}

	public void testServer42Found() {
		_testServer(IJBossToolingConstants.SERVER_AS_42);
	}
	
	public void testServer50Found() {
		_testServer(IJBossToolingConstants.SERVER_AS_50);
	}

	public void testServer51Found() {
		_testServer(IJBossToolingConstants.SERVER_AS_51);
	}


	public void _testRuntime(String typeId) {
		IRuntimeType rt = ServerCore.findRuntimeType(typeId);
		if( rt == null ) 
			fail("Runtime type " + typeId + " not found.");
	}

	public void _testServer(String typeId) {
		IServerType st = ServerCore.findServerType(typeId);
		if( st == null ) 
			fail("Server type " + typeId + " not found.");
	}

}
