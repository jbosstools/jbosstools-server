package org.jboss.ide.eclipse.as.internal.management.as7.tests;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;

import org.jboss.ide.eclipse.as.internal.management.as7.tests.utils.ParameterUtils;
import org.jboss.ide.eclipse.as.internal.management.as7.tests.utils.StartupUtility;
import org.jboss.tools.as.test.core.internal.utils.MatrixUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class AS7ManagementServicePreReqTest extends Assert {

	@Parameters
	public static Collection<Object[]> data() {
//		Object[] s1 = new Object[]{"/home/rob/apps/jboss/unzipped/jboss-as-7.0.1.Final.zip.expanded"};
//		Object[] s2 = new Object[]{"/home/rob/apps/jboss/unzipped/jboss-as-7.1.1.Final.zip.expanded"};
//		Object[] s3 = new Object[]{"/home/rob/apps/jboss/unzipped/jboss-as-7.x.zip.expanded"};
//		Object[] s4 = new Object[]{"/home/rob/apps/jboss/unzipped/jboss-jpp-6.0.0.CR01.1.zip.expanded/jboss-jpp-6.0"};
//		Object[] s5 = new Object[]{"/home/rob/apps/jboss/unzipped/jboss-eap-6.1.0.ER1.zip.expanded"};
//		
//		Object[][] l = new Object[][]{s1,s2,s3,s4,s5};
//		return Arrays.asList(l);
//
		ArrayList<Object[]> l = MatrixUtils.toMatrix(new Object[][]{ParameterUtils.getAS7ServerHomes()});
		return l;
	}
	
	private static StartupUtility util;
	@BeforeClass 
	public static void init() {
		util = new StartupUtility();
	}
	@AfterClass 
	public static void cleanup() {
		util.dispose();
	}
	
	private String homeDir;
	public AS7ManagementServicePreReqTest(String home) {
		homeDir = home;
	}

	@Before
	public void before() {
		if( !homeDir.equals(util.getHomeDir())) {
			util.dispose();
			util.setHomeDir(homeDir);
			util.start(true);
		}
	}
	
	
	@Test
	public void dummyTest() {
		// DO nothing. This class only tests startup 
		// and shutdown of the server, which are already
		// handled above. 
	}
}
