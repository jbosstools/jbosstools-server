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
