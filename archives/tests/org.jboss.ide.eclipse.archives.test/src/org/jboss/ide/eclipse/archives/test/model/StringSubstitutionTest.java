/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.test.model;

import junit.framework.TestCase;

import org.eclipse.core.internal.variables.ValueVariable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.VariablesPlugin;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.other.internal.WorkspaceVFS;

public class StringSubstitutionTest extends TestCase {

	private static final String ONE = "/this/is/a/test";
	private static final String TWO = "${nonexistant}/this/is/a/test";

	public void testNoVariables() {
		WorkspaceVFS vfs = (WorkspaceVFS)ArchivesCore.getInstance().getVFS();
		try {
			String out1 = vfs.performStringSubstitution(ONE, null, true);
			assertEquals(ONE, out1);
		} catch( CoreException ce ) {
			fail();
		}
	}

	public void testVariableNotSet() {
		WorkspaceVFS vfs = (WorkspaceVFS)ArchivesCore.getInstance().getVFS();
		try {
			vfs.performStringSubstitution(TWO, null, true);
		} catch( CoreException ce ) {
			return;
		}
		fail();
	}

	public void testVariableNotSet2() {
		WorkspaceVFS vfs = (WorkspaceVFS)ArchivesCore.getInstance().getVFS();
		try {
			String out2 = vfs.performStringSubstitution(TWO, null, false);
			assertEquals(TWO, out2);
		} catch( CoreException ce ) {
			fail(ce.getMessage());
		}
	}

	public void testSetVariable() {
		try {
			IValueVariable[] variables = new IValueVariable[] { 
					new ValueVariable("test_variable", null, false, "/here")
			};
			VariablesPlugin.getDefault().getStringVariableManager().addVariables(variables);
			WorkspaceVFS vfs = (WorkspaceVFS)ArchivesCore.getInstance().getVFS();
			String out = vfs.performStringSubstitution("${test_variable}",null, true);
			assertEquals("/here", out);
			VariablesPlugin.getDefault().getStringVariableManager().removeVariables(variables);
		} catch( CoreException ce ) {
			fail(ce.getMessage());
		}
	}
}
