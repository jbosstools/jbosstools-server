package org.jboss.tools.as.test.core.utiltests;

import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;

import junit.framework.TestCase;

public class VersionStringUtilTest extends TestCase {
	public void testVersionMajorMinorTrim() {
		assertEquals("4.1", r("4.1.0.Alpha1"));
		assertEquals("4.10", r("4.10.0.Alpha3"));
		assertEquals("1.100", r("1.100.Alpha3"));
		assertEquals("100.3", r("100.3.GA"));
		assertEquals("100.3", r("100.3")); 
	}
	
	// Simple util method. 
	private String r(String v) {
		return ServerBeanLoader.getMajorMinorVersion(v);
	}
}
