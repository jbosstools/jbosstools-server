package org.jboss.tools.as.management.itests;

import org.jboss.tools.as.management.itests.utils.JREParameterUtils;
import org.jboss.tools.as.management.itests.utils.ParameterUtils;
import org.junit.After;
import org.junit.Before;

import junit.framework.TestCase;

public class VerifyJREFlagsTest extends TestCase {
	
	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}
	
	public void testJavaHomesSet() throws RuntimeException {
		if( ParameterUtils.isSingleRuntime()) {
			String home = ParameterUtils.getSingleRuntimeHome();
			assertNotNull(home);
			JREParameterUtils.verifyJava8HomeSet();
		} else {
			JREParameterUtils.verifyJava8HomeSet();
		}
	}
}
