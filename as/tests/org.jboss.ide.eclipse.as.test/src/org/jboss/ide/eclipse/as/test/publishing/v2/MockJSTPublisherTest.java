/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.test.publishing.v2;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.ServerPreferences;
import org.jboss.ide.eclipse.as.test.util.IOUtil;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.tools.test.util.JobUtils;

public class MockJSTPublisherTest extends AbstractJSTDeploymentTester {
	private boolean initialAutopublishVal;
	public void setUp() throws Exception {
		initialAutopublishVal = ServerPreferences.getInstance().isAutoPublishing();
		ServerPreferences.getInstance().setAutoPublishing(false);
		super.setUp();
	}
	public void tearDown() throws Exception {
		super.tearDown();
		ServerPreferences.getInstance().setAutoPublishing(initialAutopublishVal);
	}

	private static int INDEX = 1;
	private String MY_NAME = null;
	protected String getModuleName() {
		if( MY_NAME == null ) {
			MY_NAME = "MockJSTPublisherTestModule" + INDEX;
			INDEX++;
		}
		return MY_NAME;
	}

	
	public void testNormalLogic() throws CoreException, IOException, Exception {
		server = ServerRuntimeUtils.useMockPublishMethod(server);
		IServerWorkingCopy wc = server.createWorkingCopy();
		wc.setAttribute(Server.PROP_AUTO_PUBLISH_SETTING, Server.AUTO_PUBLISH_DISABLE);
		server = wc.save(true, null);
		JobUtils.waitForIdle(1000);
		project = createProject();
		JobUtils.waitForIdle(1000);
		MockPublishMethod.reset();
		theTest(false);
	}

	/* There is no way to force as7 deployment onto a deploy-only server */
//	public void testForced7Logic() throws CoreException, IOException, Exception {
//		server = ServerRuntimeUtils.useMockPublishMethod(server);
//		IServerWorkingCopy wc = server.createWorkingCopy();
//		wc.setAttribute(Server.PROP_AUTO_PUBLISH_SETTING, Server.AUTO_PUBLISH_DISABLE);
//		server = wc.save(true, null);
//		JobUtils.waitForIdle(1000);
//		project = createProject();
//		JobUtils.waitForIdle(1000);
//		MockPublishMethod.reset();
//		theTest(true);
//	}

	protected void theTest(boolean isAs7) throws CoreException, IOException {

		JobUtils.delay(5000);
		JobUtils.waitForIdle();
		IModule mod = ServerUtil.getModule(project);
		server = ServerRuntimeUtils.addModule(server, mod);
		ServerRuntimeUtils.publish(server);
		assertChanged(
				isAs7, 
				new String[] { MY_NAME + ".ear", MY_NAME + ".ear/META-INF/application.xml" }, 
				new String[] { MY_NAME + ".ear", MY_NAME + ".ear/META-INF/application.xml", MY_NAME + ".ear.dodeploy" });
		assertRemoved(
				isAs7, 
				new String[] { MY_NAME + ".ear" }, 
				// jst publisher always removes the prior deployed artifact since we could have switched from zipped to exploded
				new String[] { MY_NAME + ".ear", MY_NAME + ".ear.failed" }); 
		MockPublishMethod.reset();

		IFile textFile = project.getFile(getContentTextFilePath());
		IOUtil.setContents(textFile, 0);
		assertEquals(0, MockPublishMethod.getChanged().length);
		ServerRuntimeUtils.publish(server);
		assertChanged(
				isAs7,
				new String[] { MY_NAME + ".ear", MY_NAME + ".ear/test.txt" }, 
				new String[] { MY_NAME + ".ear", MY_NAME + ".ear/test.txt" });
		assertRemoved(
				isAs7,
				new String[] {}, new String[] { MY_NAME + ".ear.failed" });
		MockPublishMethod.reset();
		IOUtil.setContents(textFile, 1);
		ServerRuntimeUtils.publish(server);
		assertChanged(
				isAs7,
				new String[] { MY_NAME + ".ear", MY_NAME + ".ear/test.txt" }, 
				new String[] { MY_NAME + ".ear", MY_NAME + ".ear/test.txt" });
		assertRemoved(
				isAs7,
				new String[] {}, 
				new String[] { MY_NAME + ".ear.failed" });
		MockPublishMethod.reset();
		textFile.delete(true, null);
		ServerRuntimeUtils.publish(server);
		assertRemoved(
				isAs7,
				new String[] { MY_NAME + ".ear/test.txt" }, 
				new String[] { MY_NAME + ".ear.failed", MY_NAME + ".ear/test.txt" });
		assertChanged(
				isAs7,
				new String[] {}, 
				new String[] {});
		MockPublishMethod.reset();

		IModule[] all = server.getModules();
		server = ServerRuntimeUtils.removeModule(server, mod);
		assertEquals(0, MockPublishMethod.getRemoved().length);

		ServerRuntimeUtils.publish(server);
		assertRemoved(
				isAs7,
				new String[] { MY_NAME + ".ear" }, 
				new String[] { MY_NAME + ".ear", MY_NAME + ".ear.deployed", MY_NAME + ".ear.failed" });
	}
	
	protected void assertRemoved(boolean isAs7, String[] nonAs7, String[] as7) {
		assertExpectedArtifacts(isAs7, nonAs7, as7, MockPublishMethod.getRemoved());
	}

	protected void assertChanged(boolean isAs7, String[] nonAs7, String[] as7) {
		assertExpectedArtifacts(isAs7, nonAs7, as7, MockPublishMethod.getChanged());
	}

	protected void assertExpectedArtifacts(boolean isAs7, String[] nonAs7, String[] as7, IPath[] artifacts) {
		if (isAs7) {
			assertEquals(as7.length, artifacts.length);
		} else {
			assertEquals(nonAs7.length, artifacts.length);
		}

		if (isAs7) {
			for (String expectedPath : as7) {
				if (contains(MockPublishMethod.MOCK_ROOT + "/" + expectedPath, artifacts)) {
					continue;
				}
				fail(expectedPath + " was not among the changed/removed artifacts");
			}
		} else {
			for (String expectedPath : nonAs7) {
				if (contains(MockPublishMethod.MOCK_ROOT + "/" + expectedPath, artifacts)) {
					continue;
				}
				fail(expectedPath + " was not among the changed/removed artifacts");
			}
		}
	}

	protected boolean contains(String expectedPath, IPath[] paths) {
		for (IPath path : paths) {
			if (expectedPath.equals(path.toString())) {
				return true;
			}
		}
		return false;
	}
}
