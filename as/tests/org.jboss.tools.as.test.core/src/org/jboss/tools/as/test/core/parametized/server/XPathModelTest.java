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
package org.jboss.tools.as.test.core.parametized.server;

import java.io.File;
import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.test.util.JobUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * This class will test properties of a default created server and runtime 
 * for properties that should never be null.
 * 
 * @author rob
 *
 */
@RunWith(value = Parameterized.class)
public class XPathModelTest extends TestCase {
	public static int serverCount = 0;
	static {
		JobUtils.waitForIdle(5000);
	}
	
	private String serverType;
	@Parameters
	public static Collection<Object[]> data() {
		 return ServerParameterUtils.asCollection(ServerParameterUtils.getJBossServerTypeParamterers());
	}
	@Before
	public void setUp() {
		JobUtils.waitForIdle();
	}
	
	 
	public XPathModelTest(String serverType) {
		this.serverType = serverType;
	}
	
	@After
	public void tearDown() throws Exception {
		try {
			ASMatrixTests.cleanup();
		} catch(Exception ce ) {
			// ignore
		}
	}
	
	private class TestListener implements IJobChangeListener {
		private Job xpathJob = null;
		public void aboutToRun(IJobChangeEvent event) {
		}
		public void awake(IJobChangeEvent event) {
		}
		public void done(IJobChangeEvent event) {
		}
		public void running(IJobChangeEvent event) {
		}
		public void scheduled(IJobChangeEvent event) {
			Job sched = event.getJob();
			if( sched.getName().equals("Add Server XPath Details")) {
				xpathJob = sched;
			}
		}
		public void sleeping(IJobChangeEvent event) {
		}
		public Job getJob() {
			return xpathJob;
		}
	}
	
	@Test
	public void serverTestImpl() {
		serverCount++;
		TestListener listener = new TestListener();
		Job.getJobManager().addJobChangeListener(listener);
		
		System.out.println("creating server for type " + serverType);
		IServer server = ServerCreationTestUtils.createMockServerWithRuntime(serverType, "server" + serverCount);
		System.out.println("Expecting server created and xpath model created");
		File xpathFile = JBossServerCorePlugin.getServerStateLocation(server).append(IJBossToolingConstants.XPATH_FILE_NAME).toFile();
		
		Job j = listener.getJob();
		try {
			j.join();
		} catch(InterruptedException ie) {
			// ignore
		}
		Job.getJobManager().removeJobChangeListener(listener);
		
		boolean found = xpathFile.exists();

		if( !found) {
			System.out.println("The XPath File has not been created for servertype=" + serverType + ". Xpaths will be lost on workspace restart");
			fail("The XPath File has not been created for servertype=" + serverType + ". Xpaths will be lost on workspace restart");
		}
	}

}
