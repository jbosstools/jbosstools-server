/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.parametized.server.publishing.sar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager;
import org.eclipse.wst.server.core.IModuleType;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.ui.mbeans.project.JBossSARModuleFactory;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.as.test.core.internal.utils.wtp.JavaEEFacetConstants;
import org.jboss.tools.as.test.core.parametized.server.ServerParameterUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * This class will test properties of a default created server and runtime 
 * for properties that should never be null.
 * 
 * @author rob
 *
 */
@RunWith(value = Parameterized.class)
public class SarModuleTest extends TestCase {
	private String serverType;
	private IServer server;
	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		ArrayList<Object> list = new ArrayList<Object>();
		list.addAll(Arrays.asList(ServerParameterUtils.getAllJBossServerTypeParamterers()));
		list.remove(IJBossToolingConstants.SERVER_AS_32);
		return ServerParameterUtils.asCollection(list.toArray(new Object[list.size()]));
	}
	 
	public SarModuleTest(String serverType) {
		this.serverType = serverType;
	}
	
	@Before
	public void setUp() {
		server = ServerCreationTestUtils.createMockServerWithRuntime(serverType, getClass().getName() + serverType);
	}

	@After
	public void tearDown() throws Exception {
		ServerCreationTestUtils.deleteAllServersAndRuntimes();
	}
	
	@Test
	public void testServerSupportsModuleVersion1() throws CoreException {
		_testServerSupportsModuleVersion(JBossSARModuleFactory.V1_0);
	}
	
	protected void _testServerSupportsModuleVersion(String version) {
		IModuleType[] types = server.getServerType().getRuntimeType().getModuleTypes();
		for( int i = 0; i < types.length; i++ ) {
			if( types[i].getId().equals(JBossSARModuleFactory.MODULE_TYPE)) {
				if(version.equals(types[i].getVersion()))
					return;
			}
		}
		fail("JBoss server " + server.getServerType().getId() + " does not support sar " + version);
	}
	
	@Test
	public void testServerFacetSupport() throws CoreException {
		if( server.getServerType().getId().equals(IJBossToolingConstants.DEPLOY_ONLY_SERVER))
			// cannot target project to a deploy only runtime at this time
			return;
		_testServerFacetSupport(JavaEEFacetConstants.SAR_1);
	}
	protected void _testServerFacetSupport(IProjectFacetVersion fv) {
		Set<IRuntime> set = RuntimeManager.getRuntimes();
		Iterator<IRuntime> i = set.iterator();
		IRuntime next = null;
		String rtTypeId = server.getRuntime().getRuntimeType().getId();
		System.out.println(rtTypeId);
		if( rtTypeId.contains("eap")) {
			System.err.println("Break");
		}
		while(i.hasNext()) {
			next = i.next();
			System.out.println( "   " + next.getName());
			if( next.getName().equals(rtTypeId)) {
				assertTrue("Runtime type " +rtTypeId + " does not support facet version " + fv.toString(), next.supports(fv));
				return;
			}
		}
		fail("Runtime for server not found.");
	}
}
