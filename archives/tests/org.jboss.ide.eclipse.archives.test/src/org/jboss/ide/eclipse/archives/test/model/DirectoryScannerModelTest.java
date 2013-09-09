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
package org.jboss.ide.eclipse.archives.test.model;

import junit.framework.TestCase;

import org.jboss.tools.archives.scanner.ITreeNode;
import org.jboss.tools.archives.scanner.VirtualDirectoryScanner;

/**
 * Tests the ability for the directory scanner model to take
 * something like arbitrary models, so long as they are wrapped in a 
 * java.io.File object. 
 */ 
public class DirectoryScannerModelTest extends TestCase {

	
	public class TreeNode implements ITreeNode {
		private String name, path;
		private ITreeNode[] children;
		
		public TreeNode(String p, String n) {
			name = n;
			path = p;
		}
		public void setChildren(ITreeNode[] children) {
			this.children = children;
		}
		public String getName() {
			return name;
		}
		public String getPath() {
			return path;
		}
		public ITreeNode[] listChildren() {
			return children;
		}
		public boolean isLeaf() {
			return children == null;
		}
		public ITreeNode getChild(String name) {
			for( int i = 0; i < children.length; i++ ) {
				if( name.equals(children[i].getName())) {
					return children[i];
				}
			}
			return null;
		}
	}
	
	private TreeNode createModel() {
		TreeNode root = new TreeNode("ab/c", "d");
		TreeNode f1 = new TreeNode("ab/c/d", "f1");
		TreeNode f2 = new TreeNode("ab/c/d", "f2");
		TreeNode f1_a = new TreeNode("ab/c/d/f1", "a");
		TreeNode f2_a = new TreeNode("ab/c/d/f2", "a");
		root.setChildren(new TreeNode[]{f1, f2});
		f1.setChildren(new TreeNode[]{f1_a});
		f2.setChildren(new TreeNode[]{f2_a});
		return root;
	}
	
	protected void setUp() throws Exception {
	}
	protected void tearDown() throws Exception {
	}
	
	private class CustomScanner1 extends VirtualDirectoryScanner<TreeNode> {
	    protected boolean isDirectory(ITreeNode file) {
	    	return !((TreeNode)file).isLeaf();
	    }
	}
	
	public void testScannerModel() {
		CustomScanner1 scanner = new CustomScanner1();
		// Basedir is ab/c/d,  so all paths must be relative to that
		scanner.setBasedir(createModel());
		// matches ab/c/d/f1/a,  ab/c/d/f2/a
		scanner.setIncludes("%regex[.*/a]");
		scanner.scan();
		String[] results = scanner.getIncludedFiles();
		assertNotNull(results);
		assertEquals(results.length, 2);
	}
}
