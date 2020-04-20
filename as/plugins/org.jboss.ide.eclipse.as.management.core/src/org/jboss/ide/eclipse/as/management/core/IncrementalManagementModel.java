/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.ide.eclipse.as.management.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IncrementalManagementModel {
	
	private HashMap<String, Module> map;
	public IncrementalManagementModel() {
		map = new HashMap<String, Module>();
	}
	
	public void setDeploymentChanges(String deploymentId, Map<String, String> changedContent, List<String> removedContent) {
		Module m = new Module(changedContent, removedContent);
		map.put(deploymentId,  m);
	}
	
	public void addSubDeploymentChanges(String deploymentId, String relative, 
			Map<String, String> changedContent, List<String> removedContent) {
		Module m = map.get(deploymentId); 
		if( m == null ) {
			m = new Module(new HashMap<String,String>(), new ArrayList<String>());
			map.put(deploymentId, m);
		}
		Map<String,String> changes = m.changedContent;
		List<String> removed = m.removedContent;
		
		Iterator<String> newChanges = changedContent.keySet().iterator();
		while( newChanges.hasNext()) {
			String k = newChanges.next();
			String v = changedContent.get(k);
			changes.put(relative + "/" + k, v);
		}
		
		Iterator<String> newRemoved = removedContent.iterator();
		while(newRemoved.hasNext()) {
			removed.add(relative + "/" + newRemoved.next());
		}
		
		m.toExplode.add(relative);
	}
	
	public String[] getChildrenToExplode(String deploymentId) {
		Module m = map.get(deploymentId);
		if( m == null )
			return new String[0];
		Set<String> s = m.toExplode;
		return (String[]) s.toArray(new String[s.size()]);
	}
	
	public String[] getDeploymentIds() {
		Set<String> s = map.keySet();
		return (String[]) s.toArray(new String[s.size()]);
	}
	
	public Map<String,String> getChanged(String deploymentId) {
		Module m = map.get(deploymentId);
		if( m != null ) {
			return m.changedContent;
		}
		return null;
	}

	public List<String> getRemoved(String deploymentId) {
		Module m = map.get(deploymentId);
		if( m != null ) {
			return m.removedContent;
		}
		return null;
	}

	
	
	private static class Module {
		Map<String, String> changedContent;
		List<String> removedContent;
		Set<String> toExplode;
		public Module(Map<String, String> changedContent, List<String> removedContent) {
			this.changedContent = changedContent; 
			this.removedContent = removedContent;
			toExplode = new HashSet<String>();
		}
	}

}
