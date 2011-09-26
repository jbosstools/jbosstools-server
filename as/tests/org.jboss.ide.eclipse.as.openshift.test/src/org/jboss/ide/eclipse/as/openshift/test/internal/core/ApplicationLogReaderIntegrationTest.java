/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.openshift.test.internal.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.ide.eclipse.as.openshift.core.ApplicationLogReader;
import org.jboss.ide.eclipse.as.openshift.core.IApplication;
import org.jboss.ide.eclipse.as.openshift.core.ICartridge;
import org.jboss.ide.eclipse.as.openshift.core.internal.Application;
import org.jboss.ide.eclipse.as.openshift.core.internal.IOpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.internal.OpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.internal.utils.StreamUtils;
import org.jboss.ide.eclipse.as.openshift.test.internal.core.fakes.TestUser;
import org.jboss.ide.eclipse.as.openshift.test.internal.core.utils.ApplicationUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author André Dietisheim
 */
public class ApplicationLogReaderIntegrationTest {

	private static final long TIMEOUT = 6 * 1024;

	private IOpenshiftService service;
	private TestUser user;

	@Before
	public void setUp() {
		this.service = new OpenshiftService();
		this.user = new TestUser();
	}

	/**
	 * Asserts the service implementation: getStatus returns the same log if no
	 * new log entry is available
	 * 
	 * @throws Exception
	 */
	@Ignore
	@Test
	public void getStatusReturnsTheWholeLogIfNoNewLogEntryOnServer() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			Application application = service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			String applicationStatus = service.getStatus(application.getName(), application.getCartridge(), user);
			String applicationStatus2 = service.getStatus(application.getName(), application.getCartridge(), user);
			assertEquals(applicationStatus, applicationStatus2);
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, user, service);
		}
	}

	/**
	 * Asserts the service implementation: getStatus returns the new entries
	 * (and a tailing-header) if new log entries are available
	 */
	@Ignore
	@Test
	public void getStatusReturnsNewEntriesIfNewLogEntriesOnServer() throws Exception {
		String applicationName = ApplicationUtils.createRandomApplicationName();
		try {
			Application application = service.createApplication(applicationName, ICartridge.JBOSSAS_7, user);
			String applicationStatus = service.getStatus(application.getName(), application.getCartridge(), user);
			application.restart();
			String applicationStatus2 = service.getStatus(application.getName(), application.getCartridge(), user);
			assertFalse(applicationStatus.equals(applicationStatus2));
		} finally {
			ApplicationUtils.silentlyDestroyAS7Application(applicationName, user, service);
		}
	}

	@Test
	public void logReaderReturnsNewEntriesAfterApplicationRestart() throws Exception {
		IApplication application = null;
		ExecutorService executor = null;
		long startTime = System.currentTimeMillis();
		try {
			application = user.createTestApplication();
			ApplicationLogReader logReader = application.getLog();
			String log = StreamUtils.readToString(logReader);
			System.err.println(log);
			LogReaderRunnable logReaderRunnable = new LogReaderRunnable(logReader);
			executor = Executors.newSingleThreadExecutor();
			executor.submit(logReaderRunnable);
			boolean logAvailable = waitForLog(startTime, System.currentTimeMillis() + TIMEOUT, logReaderRunnable);
			assertTrue(logReaderRunnable.isRunning());
			assertFalse(logAvailable);
			application.restart();
			logAvailable = waitForLog(startTime, System.currentTimeMillis() + TIMEOUT, logReaderRunnable);
			assertTrue(logAvailable);
			assertTrue(logReaderRunnable.isRunning());
		} finally {
			if (executor != null) {
				executor.shutdownNow();
			}
			if (application != null) {
				user.silentlyDestroyApplication(application);
			}
		}
	}

	protected boolean waitForLog(long startTime, long timeout, LogReaderRunnable logReaderRunnable)
			throws InterruptedException {
		while (logReaderRunnable.isEmpty()
				&& System.currentTimeMillis() <= timeout) {
			Thread.sleep(1 * 1024);
		}
		return logReaderRunnable.isEmpty();
	}

	private static class LogReaderRunnable implements Runnable {

		private ApplicationLogReader logReader;
		private StringBuilder builder;
		private boolean running;

		public LogReaderRunnable(ApplicationLogReader logReader) {
			this.logReader = logReader;
			this.builder = new StringBuilder();
		}

		@Override
		public void run() {
			this.running = true;
			try {
				for (int data = -1; (data = logReader.read()) != -1;) {
					builder.append((char) data);
				}
			} catch (Exception e) {
				this.running = false;
			}
		}

		public boolean isRunning() {
			return running;
		}

		public String getLog() {
			return builder.toString();
		}

		public boolean isEmpty() {
			return builder.length() == 0;
		}
	}

}
