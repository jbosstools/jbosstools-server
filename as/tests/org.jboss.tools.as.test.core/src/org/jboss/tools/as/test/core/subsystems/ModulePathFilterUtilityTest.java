/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.subsystems;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.jboss.ide.eclipse.as.core.publishers.patterns.ModuleDirectoryScannerPathFilter;
import org.jboss.ide.eclipse.as.core.util.ModuleResourceUtil;
import org.jboss.tools.as.test.core.internal.utils.MockModule;
import org.jboss.tools.as.test.core.internal.utils.MockModuleUtil;

/**
 * This is not an exhaustive test of the scanner / matcher, 
 * as those tests are already done in project archives. 
 * 
 * Instead, this test is simply to ensure that the 
 * getFilteredMembers and getFilteredDelta methods
 * accurately return the expected values after
 * using a filter. 
 */
public class ModulePathFilterUtilityTest extends TestCase {
	private IModule m;
	public void setUp() {
		m = createTestMockModule();
	}
	
	public void testStandard() throws Exception {
		// Set up the premise
		IModule m = createTestMockModule();
		ModuleDelegate md = (ModuleDelegate)m.loadAdapter(ModuleDelegate.class, null);
		IModuleResource[] members = md.members();
		int count = ModuleResourceUtil.countMembers(members);
		int countWithFolders = ModuleResourceUtil.countMembers(members, true);
		assertEquals(count, 20);
		assertEquals(countWithFolders, 25);
	}
	
	// Create a scanner with all included, verify no change
	public void testFilteredMembersIncludeAll() throws Exception {
		testStandard();
		ModuleDirectoryScannerPathFilter filter = new ModuleDirectoryScannerPathFilter(m, "**", "");
		IModuleResource[] filtered = filter.getFilteredMembers();
		int count = ModuleResourceUtil.countMembers(filtered);
		int countWithFolders = ModuleResourceUtil.countMembers(filtered, true);
		assertEquals(count, 20);
		assertEquals(countWithFolders, 25);
	}
	
	public void testFilteredMembersExcludeFiles() throws Exception {
		testStandard();
		
		ModuleDirectoryScannerPathFilter filter = new ModuleDirectoryScannerPathFilter(m, "**", "b/**");
		IModuleResource[] filtered = filter.getFilteredMembers();
		int count = ModuleResourceUtil.countMembers(filtered);
		int countWithFolders = ModuleResourceUtil.countMembers(filtered, true);
		assertEquals(count, 16);
		assertEquals(countWithFolders,20);
	}

	public void testFilteredMembersExcludeFolderB() throws Exception {
		testStandard();
		ModuleDirectoryScannerPathFilter filter = new ModuleDirectoryScannerPathFilter(m, "**", "b/");
		IModuleResource[] filtered = filter.getFilteredMembers();
		int count = ModuleResourceUtil.countMembers(filtered);
		int countWithFolders = ModuleResourceUtil.countMembers(filtered, true);
		assertEquals(count, 16);
		assertEquals(countWithFolders, 20);
	}

	public void testFilteredMembersIncludeNestedF() throws Exception {
		testStandard();
		
		ModuleDirectoryScannerPathFilter filter = new ModuleDirectoryScannerPathFilter(m, "d/F/**", "");
		IModuleResource[] filtered = filter.getFilteredMembers();
		int count = ModuleResourceUtil.countMembers(filtered);
		int countWithFolders = ModuleResourceUtil.countMembers(filtered, true);
		assertEquals(count, 4);
		assertEquals(countWithFolders, 6);
	}

	public void testFilteredMembersIncludeNestedFWithExcludes() throws Exception {
		testStandard();
		
		ModuleDirectoryScannerPathFilter filter = new ModuleDirectoryScannerPathFilter(
				m, new String[]{"d/F/**"}, new String[]{"**/f1","**/f2"});
		IModuleResource[] filtered = filter.getFilteredMembers();
		int count = ModuleResourceUtil.countMembers(filtered);
		int countWithFolders = ModuleResourceUtil.countMembers(filtered, true);
		assertEquals(count, 2);
		assertEquals(countWithFolders, 4);
	}

	
	// This test ensures that comma-separated strings are automatically
	// exploded into an array of patterns
	public void testIncludeNestedFWithExcludes_AutoExplode() throws Exception {
		testStandard();
		
		ModuleDirectoryScannerPathFilter filter = 
				new ModuleDirectoryScannerPathFilter(m, "d/F/**", "**/f1,**/f2");
		IModuleResource[] filtered = filter.getFilteredMembers();
		int count = ModuleResourceUtil.countMembers(filtered);
		int countWithFolders = ModuleResourceUtil.countMembers(filtered, true);
		assertEquals(count, 2);
		assertEquals(countWithFolders, 4);
	}

	public void testFilteredDelta1() throws Exception {
		testStandard();
		
		ModuleDirectoryScannerPathFilter filter = new ModuleDirectoryScannerPathFilter(
				m, "**", "b/**");
		IModuleResource[] filtered = filter.getFilteredMembers();
		int count = ModuleResourceUtil.countMembers(filtered);
		int countWithFolders = ModuleResourceUtil.countMembers(filtered, true);
		assertEquals(count, 16);
		assertEquals(countWithFolders, 20);
		
		
		IModuleResource[] members = ((MockModule)m).members();
		List<IModuleResource> asList = Arrays.asList(members);
		
		// Create a delta that changes a random file in a
		IPath[] changed = new IPath[]{ new Path("w"), new Path("a/a1"), new Path("b/b1"), new Path("d/F/f1")};
		int changedFlag = IModuleResourceDelta.CHANGED;
		int[] kind = new int[]{changedFlag, changedFlag, changedFlag, changedFlag};
		IModuleResourceDelta[] mockDelta = MockModuleUtil.createMockResourceDeltas(asList, changed, kind);
		
		// All 4 of these should be in the mock delta
		assertEquals(4, ModuleResourceUtil.countChanges(mockDelta));
		for( int i = 0; i < changed.length; i++ ) {
			IModuleResourceDelta found = MockModuleUtil.findDelta(mockDelta, changed[i]);
			assertNotNull(found);
		}
		
		// But only 3 of them should be in the filtered delta
		IModuleResourceDelta[] afterFilter = filter.getFilteredDelta(mockDelta);
		assertEquals(3, ModuleResourceUtil.countChanges(afterFilter));
	}
	
	
	private IModule createTestMockModule() {
		// Create a custom mock project structure
		MockModule m = MockModuleUtil.createMockWebModule();
		IPath[] leafs = new IPath[] {
				new Path("w"),
				new Path("x"),
				new Path("y"),
				new Path("z"),
				new Path("a/a1"),
				new Path("a/a2"),
				new Path("a/q1"),
				new Path("a/q2"),
				new Path("b/b1"),
				new Path("b/b2"),
				new Path("b/b3"),
				new Path("b/b4"),
				new Path("c/y1"),
				new Path("c/y2"),
				new Path("c/y3"),
				new Path("c/y4"),
				new Path("d/F/f1"),
				new Path("d/F/f2"),
				new Path("d/F/f3"),
				new Path("d/F/f4")
		};
		IModuleResource[] all = MockModuleUtil.createMockResources(leafs, new IPath[0]);
		m.setMembers(all);
		return m;
	}
	
}
