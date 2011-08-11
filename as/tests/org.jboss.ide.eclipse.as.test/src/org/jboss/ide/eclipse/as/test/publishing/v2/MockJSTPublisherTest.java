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
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.test.util.IOUtil;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;

public class MockJSTPublisherTest extends AbstractJSTDeploymentTester {
	public void setUp() throws Exception {
	}

	public void testNormalLogic() throws CoreException, IOException, Exception {
		server = ServerRuntimeUtils.createMockDeployOnlyServer();
		server = ServerRuntimeUtils.useMockPublishMethod(server);
		project = createProject();
		MockPublishMethod.reset();
		theTest(false);
	}

	public void testForced7Logic() throws CoreException, IOException, Exception {
		server = ServerRuntimeUtils.createMockJBoss7Server();
		server = ServerRuntimeUtils.useMockPublishMethod(server);
		project = createProject();
		MockPublishMethod.reset();
		theTest(true);
	}

	protected void theTest(boolean isAs7) throws CoreException, IOException {

		IModule mod = ServerUtil.getModule(project);
		server = ServerRuntimeUtils.addModule(server, mod);
		ServerRuntimeUtils.publish(server);
		assertChanged(
				isAs7, 
				new String[] { "newModule.ear", "newModule.ear/META-INF/application.xml" }, 
				new String[] { "newModule.ear", "newModule.ear/META-INF/application.xml", "newModule.ear.dodeploy" });
		assertRemoved(
				isAs7, 
				new String[] { "newModule.ear" }, 
				// jst publisher always removes the prior deployed artifact since we could have switched from zipped to exploded
				new String[] { "newModule.ear", "newModule.ear.failed" }); 
		MockPublishMethod.reset();

		IFile textFile = project.getFile(getContentTextFilePath());
		IOUtil.setContents(textFile, 0);
		assertEquals(0, MockPublishMethod.getChanged().length);
		ServerRuntimeUtils.publish(server);
		assertChanged(
				isAs7,
				new String[] { "newModule.ear", "newModule.ear/test.txt" }, 
				new String[] { "newModule.ear", "newModule.ear/test.txt" });
		assertRemoved(
				isAs7,
				new String[] {}, new String[] { "newModule.ear.failed" });
		MockPublishMethod.reset();
		IOUtil.setContents(textFile, 1);
		ServerRuntimeUtils.publish(server);
		assertChanged(
				isAs7,
				new String[] { "newModule.ear", "newModule.ear/test.txt" }, 
				new String[] { "newModule.ear", "newModule.ear/test.txt" });
		assertRemoved(
				isAs7,
				new String[] {}, 
				new String[] { "newModule.ear.failed" });
		MockPublishMethod.reset();
		textFile.delete(true, null);
		ServerRuntimeUtils.publish(server);
		assertRemoved(
				isAs7,
				new String[] { "newModule.ear/test.txt" }, 
				new String[] { "newModule.ear.failed", "newModule.ear/test.txt" });
		assertChanged(
				isAs7,
				new String[] {}, 
				new String[] {});
		MockPublishMethod.reset();

		server = ServerRuntimeUtils.removeModule(server, mod);
		assertEquals(0, MockPublishMethod.getRemoved().length);

		ServerRuntimeUtils.publish(server);
		assertRemoved(
				isAs7,
				new String[] { "newModule.ear" }, 
				new String[] { "newModule.ear", "newModule.ear.deployed", "newModule.ear.failed" });
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
