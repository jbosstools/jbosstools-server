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
