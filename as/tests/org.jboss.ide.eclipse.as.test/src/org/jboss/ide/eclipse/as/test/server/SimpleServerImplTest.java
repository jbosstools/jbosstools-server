package org.jboss.ide.eclipse.as.test.server;

import junit.framework.TestCase;

import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectUtility;

/**
 * Superclass for testing simple bits of functionality for each
 * and every server type
 * 
 * @author rob
 *
 */

public abstract class SimpleServerImplTest extends TestCase {
	
	protected abstract void serverTestImpl(String type);
	
	public void tearDown() {
		try {
			ServerRuntimeUtils.deleteAllServers();
			ServerRuntimeUtils.deleteAllRuntimes();
			ProjectUtility.deleteAllProjects();
			ASTest.clearStateLocation();
		} catch(Exception ce ) {
			// ignore
		}
	}

	public void test32Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_32);
	}

	public void test40Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_40);
	}

	public void test42Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_42);
	}
	
	public void test50Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_50);
	}
	public void test51Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_51);
	}
	public void test60Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_AS_60);
	}
	public void testEap43Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_EAP_43);
	}	
	public void testEap50Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_EAP_50);
	}
	public void testEap60Mock() {
		serverTestImpl(IJBossToolingConstants.SERVER_EAP_60);
	}
}
