/******************************************************************************* 
 * Copyright (c) 2009 - 2011 Red Hat, Inc. 
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
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.internal.ServerPreferences;
import org.jboss.ide.eclipse.as.test.ASTest;
import org.jboss.ide.eclipse.as.test.util.IOUtil;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;
import org.jboss.ide.eclipse.as.test.util.wtp.ProjectUtility;
import org.jboss.ide.eclipse.as.ui.editor.ServerPasswordSection;
import org.jboss.tools.test.util.JobUtils;

public class JSTDeploymentTester extends AbstractJSTDeploymentTester {
	protected String getModuleName() {
		return "JSTDeploymentTester5";
	}
	private boolean initial;
	public void setUp() throws Exception {
		super.setUp();
		initial = ServerPreferences.getInstance().isAutoPublishing();
		ServerPreferences.getInstance().setAutoPublishing(false);
	}
	public void tearDown() throws Exception {
		super.tearDown();
		ServerPreferences.getInstance().setAutoPublishing(initial);
	}

	
	public void testMain() throws CoreException, IOException {
		IModule mod = ServerUtil.getModule(project);
		IModule[] module = new IModule[] { mod };
		verifyJSTPublisher(module);
		server = ServerRuntimeUtils.addModule(server,mod);
		ServerRuntimeUtils.publish(server);
		IPath deployRoot = new Path(ServerRuntimeUtils.getDeployRoot(server));
		System.out.println(deployRoot.toOSString());
		IPath rootFolder = deployRoot.append(getModuleName() + ".ear");
		assertTrue(rootFolder.toFile().exists());
		assertTrue(IOUtil.countFiles(rootFolder.toFile()) == 0);
		assertTrue(IOUtil.countAllResources(rootFolder.toFile()) == 1);
		IFile textFile = project.getFile(getContentTextFilePath());
		IOUtil.setContents(textFile, 0);
		assertEquals(IOUtil.countFiles(rootFolder.toFile()), 0);
		assertTrue(IOUtil.countAllResources(rootFolder.toFile()) == 1);
		ServerRuntimeUtils.publish(server);
		assertEquals(IOUtil.countFiles(rootFolder.toFile()), 1);
		assertTrue(IOUtil.countAllResources(rootFolder.toFile()) == 2);
		assertContents(rootFolder.append(getTextFile()).toFile(), 0);
		IOUtil.setContents(textFile, 1);
		ServerRuntimeUtils.publish(server);
		assertContents(rootFolder.append(getTextFile()).toFile(), 1);
		textFile.delete(true, null);
		assertEquals(IOUtil.countFiles(rootFolder.toFile()), 1);
		assertTrue(IOUtil.countAllResources(rootFolder.toFile()) == 2);
		ServerRuntimeUtils.publish(server);
		assertEquals(IOUtil.countFiles(rootFolder.toFile()), 0);
		assertTrue(IOUtil.countAllResources(rootFolder.toFile()) == 1);
		server = ServerRuntimeUtils.removeModule(server, mod);
		assertTrue(rootFolder.toFile().exists());
		ServerRuntimeUtils.publish(server);
		assertFalse(rootFolder.toFile().exists());
	}
}
