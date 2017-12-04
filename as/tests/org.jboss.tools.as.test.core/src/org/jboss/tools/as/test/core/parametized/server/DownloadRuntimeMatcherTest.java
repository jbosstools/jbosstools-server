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

import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.as.runtimes.integration.util.RuntimeMatcher;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.runtime.core.RuntimeCoreActivator;
import org.jboss.tools.runtime.core.model.DownloadRuntime;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.osgi.framework.Version;
/**
 * This test class will verify that RuntimeMatcher utility will properly identify and match various
 * matching patterns for DownloadRuntime objects, including subtypes such as gatein. 
 */
@RunWith(value = Parameterized.class)
public class DownloadRuntimeMatcherTest extends TestCase {
	@Parameters(name = "{0}")
	 public static Collection<Object[]> data() {
		 DownloadRuntime[] rts = RuntimeCoreActivator.getDefault().getDownloadRuntimeArray(new NullProgressMonitor());
		 return ServerParameterUtils.asCollection(rts);
	 }

	private DownloadRuntime runtime;
	public DownloadRuntimeMatcherTest(DownloadRuntime runtime) {
		this.runtime = runtime;
	}
	
	@After
	public void tearDown() throws Exception {
		ASMatrixTests.cleanup();
	}
	
	public static final String LABEL_WTP_RUNTIME = "wtp-runtime-type";
	public static final String LABEL_RUNTIME_CATEGORY = "runtime-category";
	public static final String LABEL_RUNTIME_TYPE = "runtime-type";

	/*
	 * Create a mock folder and verify the mock folder matches also
	 */
	@Test
	public void testDownloadRuntimesMatcher() {
		// Check if it's an expected runtime type
		if( runtime.getProperty(LABEL_WTP_RUNTIME) == null)
			return;
		
		// we have a wtp runtime id, so... we should test it.
		String wtpId = (String)runtime.getProperty(LABEL_WTP_RUNTIME);
		String category = (String)runtime.getProperty(LABEL_RUNTIME_CATEGORY);
		String type = (String)runtime.getProperty(LABEL_RUNTIME_TYPE);
		String vers = runtime.getVersion();
		if( !"SERVER".equals(category)){
			// This isn't a rt we need to test. It's not a server
			return;
		}
		
		RuntimeMatcher m = new RuntimeMatcher();
		verifyContains(m, m.createPattern(wtpId, null), true);
		verifyContains(m, m.createPattern(wtpId + "0", null), false);
		verifyContains(m, m.createPattern(wtpId, type), true);
		verifyContains(m, m.createPattern(wtpId, type + "0"), false);
		
		verifyContains(m, m.createPattern(wtpId, type, vers, true, incrementMinor(vers), false), true);
		verifyContains(m, m.createPattern(wtpId, type, incrementMajor(vers), true, incrementMajor(vers,2), false), false);
	}
	
	private void verifyContains(RuntimeMatcher m, String pattern, boolean shouldContain) {
		DownloadRuntime[] results = m.findDownloadRuntimes(pattern, new NullProgressMonitor());
		boolean contains = Arrays.asList(results).contains(runtime);
		boolean correct = (contains == shouldContain);
		if( !correct )
			fail("Results for pattern " + pattern + " " + (shouldContain ? "should contain" : "should not contain") + runtime.toString());
	}
	
	private String incrementMajor(String v) {
		return incrementMajor(v,1);
	}
	
	private String incrementMinor(String v) {
		return incrementMinor(v,1);
	}

	private String incrementMicro(String v) {
		return incrementMicro(v,1);
	}

	private String incrementMajor(String v, int c) {
		Version vers = new Version(v);
		Version vers2 = new Version(vers.getMajor()+c, 0, 0, "Final");
		return vers2.toString();
	}
	
	private String incrementMinor(String v, int c) {
		Version vers = new Version(v);
		Version vers2 = new Version(vers.getMajor(), vers.getMinor()+c, 0, "Final");
		return vers2.toString();
	}

	private String incrementMicro(String v, int c) {
		Version vers = new Version(v);
		Version vers2 = new Version(vers.getMajor(), vers.getMinor(), vers.getMicro() + c, "Final");
		return vers2.toString();
	}


	
}
