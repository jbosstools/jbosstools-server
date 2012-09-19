/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.internal.utils.classpath;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ClasspathVariableInitializer;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.utils.BundleUtils;

/**
 * @author Rob Stryker
 *
 */
public class ASToolsInternalVariableInitializer extends ClasspathVariableInitializer {

	public static final String ASTOOLS_TEST_HOME_VAR = "ASTOOLS_TEST_HOME";

	private static final String twiddle_suffix = ".mf.twiddle.jar";
	private static final String twiddle_3_2_8 = "3.2.8" + twiddle_suffix;

	public static void ensureFoldersCreated() {
		IPath jarFolder = getPath();
		jarFolder.toFile().mkdirs();
		
		try {
			File source = BundleUtils.getFileLocation("serverMock/" + twiddle_3_2_8);
			File dest = jarFolder.append("junit.jar").toFile();
			FileUtil.fileSafeCopy(source, dest);
		} catch(CoreException ce) {
			ce.printStackTrace();
		}
	}
	
	public void initialize(String variable) {
		ensureFoldersCreated();
		if( variable.equals(ASTOOLS_TEST_HOME_VAR)) {
			IPath newPath = getPath();
			try {
				JavaCore.setClasspathVariable(variable, newPath, new NullProgressMonitor());
			} catch(JavaModelException jme) {
				jme.printStackTrace();
			}
		}
	}
	protected static IPath getPath() {
		IPath state = ASMatrixTests.getDefault().getStateLocation();
		IPath jarFolder = state.append(".astools_test");
		return jarFolder;
	}
}
