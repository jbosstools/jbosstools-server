/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    "Rob Stryker" <rob.stryker@redhat.com> - Initial implementation
 *******************************************************************************/
package org.jboss.tools.jmx.core.test;

import static org.junit.Assert.*;

import javax.management.MBeanOperationInfo;

import junit.framework.TestCase;

import org.jboss.tools.jmx.core.Impact;
import org.junit.Test;

public class ImpactTest extends TestCase{

	public void testImpactParseInt() {
		Impact impact = Impact.parseInt(MBeanOperationInfo.ACTION);
		assertEquals(Impact.ACTION,impact);
		impact = Impact.parseInt(MBeanOperationInfo.ACTION_INFO);
		assertEquals(Impact.ACTION_INFO,impact);
		impact = Impact.parseInt(MBeanOperationInfo.INFO);
		assertEquals(Impact.INFO,impact);
		impact = Impact.parseInt(MBeanOperationInfo.UNKNOWN);
		assertEquals(Impact.UNKNOWN,impact);
	}

}
