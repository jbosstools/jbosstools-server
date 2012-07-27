package org.jboss.tools.as.test.core.utiltests;

import junit.framework.TestCase;

import org.jboss.ide.eclipse.as.core.util.ExpressionResolverUtil;
import org.junit.Test;

public class ExpressionResolverUtilTest extends TestCase {
	@Test
	public void testExpressionResolver() {
		assertEquals("aaa", re("aaa"));
		assertEquals("t9", re("t${something:9}"));
		assertEquals("t9a", re("t${something:9}a"));
		
		// NO recursive checking
		assertEquals("5:notfound}", re("${twoPoint${someNumber:5}:notfound}"));
		
		// unresolvable, so no change
		assertEquals("t${something}", re("t${something}"));
		assertEquals("t${something}a", re("t${something}a"));
		assertEquals("t$ablah", re("t$ablah"));
		
		// custom handling
		assertEquals("t$stuff", re("t$$stuff"));
	}
	
	private String re(String s) {
		return ExpressionResolverUtil.safeReplaceProperties(s);
	}
}
