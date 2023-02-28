/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.as.test.core.internal.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.internal.ModuleResourceDelta;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ModuleDelegate;



/**
 * This class is an internal test util class meant for 
 * quickly creating mock modules and heirarchies.
 */
public class MockModuleUtil {

	
	public static MockModule createMockWebModule() {
		return createMockWebModule("jst.web", "WebProj");
	}
	public static MockModule createMockWebModule5() {
		return createMockWebModule("jst.web", "WebProj", "5.0");
	}
	public static MockModule createMockWebModule(String name) {
		return createMockWebModule("jst.web", name);
	}
	public static MockModule createMockWebModule(final String id, final String name ) {
		return createMockModule(id, name, "jst.web", "jst.web", "2.5");
	}
	public static MockModule createMockWebModule(final String id, final String name, final String version ) {
		return createMockModule(id, name, "jst.web", "jst.web", version);
	}
	public static MockModule createMockUtilModule() {
		return createMockUtilModule("jst.utility", "UtilProj");
	}
	public static MockModule createMockUtilModule(final String id, final String name ) {
		return createMockModule(id, name, "jst.utility", "jst.utility", "1.0");
	}
	public static MockModule createMockEarModule() {
		return createMockEarModule("jst.ear", "EarProj");
	}
	public static MockModule createMockEarModule(final String id, final String name ) {
		return createMockModule(id, name, "jst.ear", "jst.ear", "1.2");
	}
	
	public static MockModule createMockModule(final String id, final String name, 
			final String typeId, final String typeName, final String typeVersion) {
		return new MockModule(id, name, typeId, typeName, typeVersion);
	}
	



	/**
	 * Create some resource deltas. Mark all resources as changed
	 * 
	 * @param list  A list of *all* resources, even those not in the delta
	 * @return
	 */
	public static IModuleResourceDelta[] createMockResourceDeltas(
			List<IModuleResource> list) {
		IPath[] all = new IPath[list.size()];
		int[] kind = new int[list.size()];
		for( int i = 0; i < list.size(); i++ ) {
			all[i] = list.get(i).getModuleRelativePath().append(list.get(i).getName());
			kind[i] = IModuleResourceDelta.CHANGED;
		}
		return createMockResourceDeltas(list, all, kind);
	}
	
	public static IModuleResource[] getAllResources(IModule module)  throws Exception {
		ModuleDelegate dg = (ModuleDelegate)module.loadAdapter(ModuleDelegate.class, null);
		return getAllResources(dg.members());
	}
	
	public static IModuleResource[] getAllResources(IModuleResource[] list) {
		ArrayList<IModuleResource> collector = new ArrayList<IModuleResource>();
		for( int i = 0; i < list.length; i++ ) {
			collector.add(list[i]);
			if( list[i] instanceof IModuleFolder) {
				IModuleResource[] children = ((IModuleFolder)list[i]).members();
				collector.addAll(Arrays.asList(getAllResources(children)));
			}
		}
		return (IModuleResource[]) collector.toArray(new IModuleResource[collector.size()]);
	}
	
	
	/**
	 * Create some resource deltas. 
	 * 
	 * @param list  A list of *all* resources, even those not in the delta. All must be top-level in the list
	 * @param leafNodes  The path to the leaf nodes that have a delta
	 * @param kind   The kind of change for each leaf node changed
	 * @return
	 */
	public static IModuleResourceDelta[] createMockResourceDeltas(
			List<IModuleResource> list,  IPath[] leafNodes, int[] kind) {
		// create a collector
		ArrayList<IModuleResourceDelta> deltaList = new ArrayList<IModuleResourceDelta>();
		for( int i = 0; i < leafNodes.length; i++ ) {
			// add deltas for each path and kind
			addDeltaToList(deltaList, list, leafNodes[i], kind[i]);
		}
		return deltaList.toArray(new IModuleResourceDelta[deltaList.size()]);
	}
	
	/*
	 * This is a recursive method
	 */
	private static IModuleResourceDelta addDeltaToList(ArrayList<IModuleResourceDelta> deltaList, 
			List<IModuleResource> resourceList, IPath p, int kind) {
		// Find the resource for our given path, and if it doesnt exist, error
		IModuleResource mr = findModuleResource(resourceList, p);
		if( mr == null )
			throw new RuntimeException("Resource " + p.toOSString() + " does not exist in mocked model");
		
		// Create our delta
		IModuleResourceDelta thisNodeDelta = new ModuleResourceDelta(mr, kind);
		
		// Recursion end
		if( p.segmentCount()== 1 ) {
			// pull it from the deltaList or create it
			// a 1-segment path such as "folder1"
			IModuleResourceDelta possibleDuplicate = findDelta(deltaList, p);
			if( possibleDuplicate == null ) {
				deltaList.add(thisNodeDelta);
				return thisNodeDelta;
			}
			return possibleDuplicate;
		}
		
		// ensure parent delta is added
		IModuleResourceDelta parentDelta = addDeltaToList(deltaList, resourceList, p.removeLastSegments(1), IModuleResourceDelta.CHANGED);
		
		// Now add ourselves
		IModuleResourceDelta[] existingChildren = parentDelta.getAffectedChildren();
		ArrayList<IModuleResourceDelta> tmpChildren = new ArrayList<IModuleResourceDelta>();
		if( existingChildren != null ) {
			// If the existing children already have this node, return that one
			for( int k = 0; k < existingChildren.length; k++ ) {
				IModuleResource kResource = existingChildren[k].getModuleResource();
				if( kResource.getName().equals(mr.getName()) && kResource.getModuleRelativePath().equals(mr.getModuleRelativePath())){
					//they are equal, duplicate, do not add again
					return existingChildren[k];
				}
			}
			// Not found, add all existing children as our brothers
			tmpChildren.addAll(Arrays.asList(existingChildren));
		}
		// Add our new delta and set it as parents new child array
		tmpChildren.add(thisNodeDelta);
		IModuleResourceDelta[] newChildren = tmpChildren.toArray(new IModuleResourceDelta[tmpChildren.size()]);
		((ModuleResourceDelta)parentDelta).setChildren(newChildren);
		return thisNodeDelta;
	}
	
	// Convenience method for verifying a path is found in the delta array
	public static IModuleResourceDelta findDelta(IModuleResourceDelta[] deltas, IPath p) {
		ArrayList<IModuleResourceDelta> tmp = new ArrayList<IModuleResourceDelta>();
		tmp.addAll(Arrays.asList(deltas));
		return findDelta(tmp, p);
	}
	
	// Convenience method for verifying a path is found in a delta list
	public static IModuleResourceDelta findDelta(List<IModuleResourceDelta> list, IPath p) {
		if( p.segmentCount() == 1 ) {
			for( int i = 0; i < list.size(); i++ ) {
				IModuleResource kDelta = list.get(i).getModuleResource();
				if( kDelta.getName().equals(p.lastSegment()) && kDelta.getModuleRelativePath().equals(p.removeLastSegments(1))){
					//they are equal, duplicate, do not add again
					return list.get(i);
				}
			}
			return null;
		}
		IModuleResourceDelta parentDelta = findDelta(list, p.removeLastSegments(1));
		IModuleResourceDelta[] siblings = parentDelta.getAffectedChildren();
		for( int i = 0; i < siblings.length; i++ ) {
			IModuleResource kDelta = siblings[i].getModuleResource();
			if( kDelta.getName().equals(p.lastSegment()) && kDelta.getModuleRelativePath().equals(p.removeLastSegments(1))){
				//they are equal, duplicate, do not add again
				return siblings[i];
			}
		}
		return null;
	}
	
	// Convenience method for verifying a module resource is found in a list
	private static IModuleResource findModuleResource(List<IModuleResource> list, IPath p) {
		if( p.segmentCount() == 1 ) 
			return getFromList(list, p.lastSegment());
		MockModuleFolder mf= getExistingMockFolder(list, p.removeLastSegments(1));
		IModuleResource[] children = mf.members();
		return getFromArray(children, p.lastSegment());
	}
	
	/*
	 * Build a custom IModuleResource[] model based on paths that you want created
	 */
	public static IModuleResource[] createMockResources(IPath[] leafNodes, IPath[] emptyFolders) {
		return createMockResources(leafNodes, emptyFolders, null);
	}
	
	public static IModuleResource[] createMockResources(IPath[] leafNodes, IPath[] emptyFolders, File underlying) {
		ArrayList<IModuleResource> resources = new ArrayList<IModuleResource>();
		for( int i = 0; i < leafNodes.length; i++ ) {
			IPath p = leafNodes[i]; // ex:  folder1/folder2/leaf.txt
			int count = p.segmentCount(); // ex: count = 3
			for( int j = 1; j <= count;j++) {
				// remove last 2 segments, end up w folder1
				IPath trimmed = p.removeLastSegments(count-j); 
				if( j == count) {
					// make a file if we're last segment
					addMockFile(resources, trimmed, underlying);
				} else {
					// make a folder
					addMockFolder(resources, trimmed);
				}
			}
		}
		for( int i = 0; i < emptyFolders.length; i++ ) {
			IPath p = emptyFolders[i]; // ex:  folder1/folder2/leaf.txt
			int count = p.segmentCount(); // ex: count = 3
			for( int j = 1; j <= count;j++) {
				// remove last 2 segments, end up w folder1
				IPath trimmed = p.removeLastSegments(count-j); 
				// always make a folder 
				addMockFolder(resources, trimmed);
			}
		}
		
		
		return resources.toArray(new IModuleResource[resources.size()]);
	}
	
	private static void addMockFile(ArrayList<IModuleResource> resources, IPath p, File underlying) {
		MockModuleFile newMockFile =new MockModuleFile(p.removeLastSegments(1), p.lastSegment());
		newMockFile.setFile(underlying);
		
		if( p.segmentCount() == 1 ) {
			// path is "a" or "b", so root is parent and does not exist
			if( !listContains(resources, p.lastSegment()))
				resources.add(newMockFile);
			return;
		}
		
		IModuleResource parent = getExistingMockFolder(resources, p.removeLastSegments(1));
		MockModuleFolder parentF = (MockModuleFolder)parent;
		IModuleResource[] existing = parentF.members();
		if( existing == null ) {
			existing = new IModuleResource[]{newMockFile};
			parentF.setMembers(existing);
			return;
		}
		// Node already exists
		if( existingContains(existing, p.lastSegment()))
			return;
		// add this new resource to parent's members
		ArrayList<IModuleResource> l = new ArrayList<IModuleResource>();
		l.addAll(Arrays.asList(existing));
		l.add(newMockFile);
		parentF.setMembers( l.toArray(new IModuleResource[l.size()]));
	}
	
	private static boolean existingContains(IModuleResource[] all, String name) {
		for( int i = 0; i < all.length; i++ ) {
			if( all[i].getName().equals(name))
				return true;
		}
		return false;
	}
	
	private static boolean listContains(ArrayList<IModuleResource> all, String name) {
		return getFromList(all, name) != null;
	}
	
	private static IModuleResource getFromList(List<IModuleResource> all, String name) {
		for( int i = 0; i < all.size(); i++ ) {
			if( all.get(i).getName().equals(name))
				return all.get(i);
		}
		return null;
	}
	private static IModuleResource getFromArray(IModuleResource[] all, String name) {
		for( int i = 0; i < all.length; i++ ) {
			if( all[i].getName().equals(name))
				return all[i];
		}
		return null;
	}
	
	private static void addMockFolder(ArrayList<IModuleResource> resources, IPath p) {
		MockModuleFolder newMockFolder =new MockModuleFolder(p.removeLastSegments(1), p.lastSegment());
		if( p.segmentCount() == 1 ) {
			// path is "a" or "b", so root is parent and does not exist
			if( !listContains(resources, p.lastSegment()))
				resources.add(newMockFolder);
			return;
		}
		
		MockModuleFolder parent = getExistingMockFolder(resources, p.removeLastSegments(1));
		IModuleResource[] existing = parent.members();
		if( existing == null ) {
			existing = new IModuleResource[]{newMockFolder};
			parent.setMembers(existing);
			return;
		}

		
		// Node already exists
		if( existingContains(existing, p.lastSegment()))
			return;
		// add to parent's members
		ArrayList<IModuleResource> l = new ArrayList<IModuleResource>();
		l.addAll(Arrays.asList(existing));
		l.add(newMockFolder);
		parent.setMembers( l.toArray(new IModuleResource[l.size()]));
	}
	
	private static MockModuleFolder getExistingMockFolder(List<IModuleResource> resources, IPath p) {
		if( p.segmentCount() == 1 ) {
			IModuleResource parent = getFromList(resources, p.lastSegment());
			return (MockModuleFolder)parent;
		}
		MockModuleFolder parent = getExistingMockFolder(resources, p.removeLastSegments(1));
		IModuleResource[] existing = parent.members();
		return (MockModuleFolder) getFromArray(existing, p.lastSegment());
	}
	
	

	
}
