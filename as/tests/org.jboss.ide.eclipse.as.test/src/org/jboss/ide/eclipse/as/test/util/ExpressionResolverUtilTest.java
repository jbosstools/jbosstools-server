package org.jboss.ide.eclipse.as.test.util;

import org.jboss.ide.eclipse.as.core.util.ExpressionResolverUtil;

import junit.framework.TestCase;

public class ExpressionResolverUtilTest extends TestCase {
	public void testExpressionResolver() {
		assertEquals("aaa", ExpressionResolverUtil.safeReplaceProperties("aaa"));
		assertEquals("t9", ExpressionResolverUtil.safeReplaceProperties("t${something:9}"));
		assertEquals("t9a", ExpressionResolverUtil.safeReplaceProperties("t${something:9}a"));
		
		// NO recursive checking
		assertEquals("5:notfound}", ExpressionResolverUtil.safeReplaceProperties("${twoPoint${someNumber:5}:notfound}"));
		
		// unresolvable, so no change
		assertEquals("t${something}", ExpressionResolverUtil.safeReplaceProperties("t${something}"));
		assertEquals("t${something}a", ExpressionResolverUtil.safeReplaceProperties("t${something}a"));
	}
}
