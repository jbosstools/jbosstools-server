/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.subsystems;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllerEnvironment;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.SubsystemModel;
import org.jboss.tools.as.test.core.subsystems.impl.System1aSubsystem;
import org.jboss.tools.as.test.core.subsystems.impl.System6Subsystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This class will test properties of a default created server and runtime 
 * for properties that should never be null.
 * 
 * @author rob
 *
 */
public class ServerSubsystemTest1 extends TestCase {
	public ServerSubsystemTest1() {
	}
	
	@Before
	public void setUp() {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testSystem1Type() {
		try {
			// ensure a basic implementation is found
			assertNotNull(convenience("customServer1", "system1"));
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
	}

	@Test
	public void testSystem2DefaultFlag() {
		ModelSubclass c = new ModelSubclass();
		try {
			Object[] types = c.getSubsystemMappings("customServer1", "system2");
			assertTrue(types.length == 2);
			ISubsystemController controller = c.createSubsystemController(null, "customServer1", "system2",null, null, null);
			assertNotNull(controller);
			assertTrue(controller.getSubsystemMappedId().equals("system2.implB"));
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
	}

	@Test
	public void testMissingSystem() {
		ModelSubclass c = new ModelSubclass();
		try {
			Object[] types = c.getSubsystemMappings("customServer1", "missingSystem");
			assertTrue(types.length == 0);
			ISubsystemController controller = c.createSubsystemController(null, "customServer1", "missingSystem", null, null, null);
			fail();
		} catch(CoreException ce) {
			// expected to fail
		}
	}

	public void testSystem2DefaultParameter() {
		ModelSubclass c = new ModelSubclass();
		try {
			Object[] types = c.getSubsystemMappings("customServer1", "system2");
			assertTrue(types.length == 2);
			ISubsystemController controller = c.createSubsystemController(null, "customServer1", "system2", null, "system2.implA", null);
			assertNotNull(controller);
			assertTrue(controller.getSubsystemMappedId().equals("system2.implA"));
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
	}

	public void testSystem2RequiredParameter() {
		ModelSubclass c = new ModelSubclass();
		try {
			Object[] types = c.getSubsystemMappings("customServer1", "system2");
			assertTrue(types.length == 2);
			Map<String, String> map = new HashMap<String, String>();
			map.put("someKey", "someVal");
			ISubsystemController controller = c.createSubsystemController(null, "customServer1", "system2", map,null, null);
			assertNotNull(controller);
			assertTrue(controller.getSubsystemMappedId().equals("system2.implA"));
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}

		try {
			Object[] types = c.getSubsystemMappings("customServer1", "system2");
			assertTrue(types.length == 2);
			Map<String, String> map = new HashMap<String, String>();
			map.put("otherKey", "otherVal");
			ISubsystemController controller = c.createSubsystemController(null, "customServer1", "system2", map, null, null);
			assertNotNull(controller);
			assertTrue(controller.getSubsystemMappedId().equals("system2.implB"));
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
	}
	
	public void testSystemMissingDependency() {
		ModelSubclass c = new ModelSubclass();
		try {
			Object[] types = c.getSubsystemMappings("customServer1", "system3");
			assertTrue(types.length == 2);
			ISubsystemController controller = c.createSubsystemController(null, "customServer1", "system3", null, null, null);
			assertNotNull(controller);
			// system3.implB  is marked as the default in plugin.xml.
			// However it is missing a dependency and is therefore invalid,
			// so it is not chosen on a search for system3
			assertEquals(controller.getSubsystemMappedId(), ("system3.implA"));
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
	}

	
	/* 
	 * Test that a request for system4 will return the one subsystem available,
	 * and that system4's implementation can alternately choose which
	 * system its dependency resolves to (system4a) based on a required property 
	 */
	public void testChildSystemRequiredProperty() {
		ModelSubclass c = new ModelSubclass();
		try {
			//First create with no environment, and make sure we get SOMETHING system as a result
			Object[] types = c.getSubsystemMappings("customServer1", "system4");
			assertTrue(types.length == 1);
			HashMap<String, Object> env = new HashMap<String, Object>();
			ISubsystemController controller = c.createSubsystemController(null, "customServer1", "system4", null, null, env);
			assertNotNull(controller);
			assertTrue(controller.getSubsystemMappedId().equals("system4.implSingleton"));
			assertTrue(controller instanceof System1aSubsystem);
			System1aSubsystem tmp = (System1aSubsystem)controller;
			assertNotNull(tmp.findDependency("system4a", "customServer1"));
			
			
			// Repeat with an environment indicating our system4a dependency must have prop1=val1
			env.put("system4a.RESERVED_requiredProperties", "animal=tiger");
			controller = c.createSubsystemController(null, "customServer1", "system4", null, null, env);
			assertNotNull(controller);
			assertTrue(controller.getSubsystemMappedId().equals("system4.implSingleton"));
			assertTrue(controller instanceof System1aSubsystem);
			tmp = (System1aSubsystem)controller;
			ISubsystemController tmp2 = tmp.findDependency("system4a", "customServer1");
			assertNotNull(tmp2);
			assertTrue(tmp2.getSubsystemMappedId().equals("system4a.tiger"));

			
			// Repeat with an environment indicating our system4a dependency must have prop1=val2
			env.put("system4a.RESERVED_requiredProperties", "animal=mantis");
			controller = c.createSubsystemController(null, "customServer1", "system4", null, null, env);
			assertNotNull(controller);
			assertTrue(controller.getSubsystemMappedId().equals("system4.implSingleton"));
			assertTrue(controller instanceof System1aSubsystem);
			tmp = (System1aSubsystem)controller;
			tmp2 = tmp.findDependency("system4a", "customServer1");
			assertNotNull(tmp2);
			assertTrue(tmp2.getSubsystemMappedId().equals("system4a.mantis"));

			
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
	}

	
	
	/* 
	 * Test that a request for system4 will return the one subsystem available,
	 * and that system4's implementation can alternately choose which
	 * system its dependency resolves to (system4a) based on a required property 
	 */
	public void testGrandChildSystemRequiredProperty() {
		ModelSubclass c = new ModelSubclass();
		try {
			//First create with no environment, and make sure we get SOMETHING system as a result
			Object[] types = c.getSubsystemMappings("customServer1", "system5");
			assertTrue(types.length == 1);
			HashMap<String, Object> env = new HashMap<String, Object>();
			ISubsystemController controller = c.createSubsystemController(null, "customServer1", "system5", null, null, env);
			assertNotNull(controller);
			assertTrue(controller.getSubsystemMappedId().equals("system5.implSingleton5"));
			assertTrue(controller instanceof System1aSubsystem);
			System1aSubsystem tmp = (System1aSubsystem)controller;
			ISubsystemController system4 =tmp.findDependency("system4", "customServer1"); 
			assertNotNull(system4);
			assertTrue(system4.getSubsystemMappedId().equals("system4.implSingleton"));
			assertTrue(system4 instanceof System1aSubsystem);
			System1aSubsystem tmp4 = (System1aSubsystem)system4;
			ISubsystemController system4a =tmp4.findDependency("system4a", "customServer1"); 
			assertNotNull(system4a);
			
			
			// Repeat with an environment indicating our system4a dependency must have prop1=val1
			env.put("system4a.RESERVED_requiredProperties", "animal=tiger");
			controller = c.createSubsystemController(null, "customServer1", "system5", null, null, env);
			assertNotNull(controller);
			assertTrue(controller.getSubsystemMappedId().equals("system5.implSingleton5"));
			assertTrue(controller instanceof System1aSubsystem);
			tmp = (System1aSubsystem)controller;
			system4 = tmp.findDependency("system4", "customServer1"); 
			assertNotNull(system4);
			assertTrue(system4.getSubsystemMappedId().equals("system4.implSingleton"));
			assertTrue(system4 instanceof System1aSubsystem);
			tmp4 = (System1aSubsystem)system4;
			system4a =tmp4.findDependency("system4a", "customServer1"); 
			assertNotNull(system4a);
			assertTrue(system4a.getSubsystemMappedId().equals("system4a.tiger"));

			
			// Repeat with an environment indicating our system4a dependency must have prop1=val2
			env.put("system4a.RESERVED_requiredProperties", "animal=mantis");
			controller = c.createSubsystemController(null, "customServer1", "system5", null, null, env);
			assertNotNull(controller);
			assertTrue(controller.getSubsystemMappedId().equals("system5.implSingleton5"));
			assertTrue(controller instanceof System1aSubsystem);
			tmp = (System1aSubsystem)controller;
			system4 = tmp.findDependency("system4", "customServer1"); 
			assertNotNull(system4);
			assertTrue(system4.getSubsystemMappedId().equals("system4.implSingleton"));
			assertTrue(system4 instanceof System1aSubsystem);
			tmp4 = (System1aSubsystem)system4;
			system4a =tmp4.findDependency("system4a", "customServer1"); 
			assertNotNull(system4a);
			assertTrue(system4a.getSubsystemMappedId().equals("system4a.mantis"));

			
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
	}

	/* 
	 * Same as previous test, except verifies the convenience methods used to 
	 * quickly create the required property environments while hiding the details 
	 * of the internal property keys
	 */
	public void testGrandChildSystemRequiredPropertySecondApi() {
		ModelSubclass c = new ModelSubclass();
		try {
			Object[] types = c.getSubsystemMappings("customServer1", "system5");
			assertTrue(types.length == 1);
			ISubsystemController controller = 
					c.createControllerForSubsystem(null, "customServer1", "system5", "system5.implSingleton5", 
					new ControllerEnvironment().addRequiredProperty("system4a", "animal", "mantis").getMap());
			assertNotNull(controller);
			assertTrue(controller.getSubsystemMappedId().equals("system5.implSingleton5"));
			assertTrue(controller instanceof System1aSubsystem);
			System1aSubsystem tmp = (System1aSubsystem)controller;
			ISubsystemController system4 = tmp.findDependency("system4", "customServer1"); 
			assertNotNull(system4);
			assertTrue(system4.getSubsystemMappedId().equals("system4.implSingleton"));
			assertTrue(system4 instanceof System1aSubsystem);
			System1aSubsystem tmp4 = (System1aSubsystem)system4;
			ISubsystemController system4a =tmp4.findDependency("system4a", "customServer1"); 
			assertNotNull(system4a);
			assertTrue(system4a.getSubsystemMappedId().equals("system4a.mantis"));
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
	}	

	
	
	/* 
	 * Test arbitrary dependency resolution customized by controller's logic
	 * 
	 * This test also tests the validate() method for the subsystem
	 */
	public void testArbitraryDependencyResolution() {
		ModelSubclass c = new ModelSubclass();
		try {
			Object[] types = c.getSubsystemMappings("customServer1", "system6");
			assertTrue(types.length == 1);
			ISubsystemController controller = 
					c.createControllerForSubsystem(null, "customServer1", "system6", "system6.implSingleton6", 
					new ControllerEnvironment()
						.addProperty(System6Subsystem.PROP_LEGS, new Integer(4)).getMap());
			assertNotNull(controller);
			assertTrue(controller.getSubsystemMappedId().equals("system6.implSingleton6"));
			assertTrue(controller instanceof System6Subsystem);
			System6Subsystem tmp = (System6Subsystem)controller;
			ISubsystemController system4 = tmp.findDependency("system4a", "customServer1"); 
			assertNotNull(system4);
			assertTrue(system4.getSubsystemMappedId().equals("system4a.tiger"));
			assertTrue(system4.validate().isOK());
			
			
			controller = 
					c.createControllerForSubsystem(null, "customServer1", "system6", "system6.implSingleton6", 
					new ControllerEnvironment()
						.addProperty(System6Subsystem.PROP_LEGS, new Integer(2)).getMap());
			assertNotNull(controller);
			assertTrue(controller.getSubsystemMappedId().equals("system6.implSingleton6"));
			assertTrue(controller instanceof System6Subsystem);
			tmp = (System6Subsystem)controller;
			system4 = tmp.findDependency("system4a", "customServer1"); 
			assertNotNull(system4);
			assertTrue(system4.getSubsystemMappedId().equals("system4a.mantis"));
			assertTrue(system4.validate().isOK());

			// Test with an invalid number of legs
			controller = 
					c.createControllerForSubsystem(null, "customServer1", "system6", "system6.implSingleton6", 
					new ControllerEnvironment()
						.addProperty(System6Subsystem.PROP_LEGS, new Integer(666)).getMap());
			assertNotNull(controller);
			assertTrue(controller.getSubsystemMappedId().equals("system6.implSingleton6"));
			assertTrue(controller instanceof System6Subsystem);
			tmp = (System6Subsystem)controller;
			assertTrue(!tmp.validate().isOK());

			
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
	}	

	
	public void testSubsystemDependencyResolved() {
		// test 7
		ModelSubclass c = new ModelSubclass();
		try {
			Object[] types = c.getSubsystemMappings("customServer1", "system7");
			assertTrue(types.length == 1);
			ISubsystemController controller = 
					c.createControllerForSubsystem(null, "customServer1", "system7", "system7.implSingleton7", null);
			assertNotNull(controller);
			assertTrue(controller.getSubsystemMappedId().equals("system7.implSingleton7"));
			assertTrue(controller instanceof System1aSubsystem);
			System1aSubsystem tmp = (System1aSubsystem)controller;
			ISubsystemController system4 = tmp.findDependency("system4a", "customServer1"); 
			assertNotNull(system4);
			assertTrue(system4.getSubsystemMappedId().equals("system4a.mantis"));
			assertTrue(system4 instanceof System1aSubsystem);
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
		
		// test 8
		c = new ModelSubclass();
		try {
			Object[] types = c.getSubsystemMappings("customServer1", "system8");
			assertTrue(types.length == 1);
			ISubsystemController controller = 
					c.createControllerForSubsystem(null, "customServer1", "system8", "system8.implSingleton8", null);
			assertNotNull(controller);
			assertTrue(controller.getSubsystemMappedId().equals("system8.implSingleton8"));
			assertTrue(controller instanceof System1aSubsystem);
			System1aSubsystem tmp = (System1aSubsystem)controller;
			ISubsystemController system4 = tmp.findDependency("system4a", "customServer1"); 
			assertNotNull(system4);
			assertTrue(system4.getSubsystemMappedId().equals("system4a.tiger"));
			assertTrue(system4 instanceof System1aSubsystem);
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}

		
	}	

	
	
	static class ModelSubclass extends SubsystemModel {
		public SubsystemMapping[] getSubsystemMappings(String serverType, String system) {
			return super.getSubsystemMappings(serverType, system);
		}
		public String getSubsystemMappedId(Object o) {
			if( o instanceof SubsystemMapping) {
				return ((SubsystemMapping)o).getMappedId();
			}
			return null;
		}
	}
	private ISubsystemController convenience(String serverType, String system) throws CoreException {
		return SubsystemModel.getInstance().createSubsystemController(null, serverType, system, null, null, null);
	}

	
}
