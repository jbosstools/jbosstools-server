/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 * 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.core.server.publish;

import java.util.HashMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;

public class ModulePublishErrorCache {
	private static ModulePublishErrorCache cache = new ModulePublishErrorCache();
	public static ModulePublishErrorCache getDefault() {
		return cache;
	}
	
	private HashMap<String, ServerResults> model = new HashMap<String, ServerResults>();
	public IStatus getPublishErrorStatus(IServer server, IModule[] module) {
		ModuleContainer mc = new ModuleContainer(module);
		ServerResults sr = model.get(server.getId());
		IStatus s = null;
		if( sr != null ) {
			s = sr.getPublishErrorStatusForModule(mc);
		}
		return s == null ? Status.OK_STATUS : s;
	}
	
	public void setPublishErrorState(IServer server, IModule[] module, int state, IStatus status) {
		ModuleContainer mc = new ModuleContainer(module);
		ServerResults sr = model.get(server.getId());
		if( sr == null ) {
			sr = new ServerResults();
			model.put(server.getId(), sr);
		}
		
		sr.addStatusForModule(mc, state, status);
	}
	
	private static class ServerResults {
		private HashMap<ModuleContainer, Integer> perModuleState;
		private HashMap<ModuleContainer, IStatus> perModuleStatus;
		public ServerResults() {
			perModuleState = new HashMap<ModuleContainer, Integer>();
			perModuleStatus = new HashMap<ModuleContainer, IStatus>();
		}
		public Integer getPublishErrorStateForModule(ModuleContainer mc) {
			return perModuleState.get(mc);
		}
		public IStatus getPublishErrorStatusForModule(ModuleContainer mc) {
			return perModuleStatus.get(mc);
		}
		public void addStatusForModule(ModuleContainer mc, Integer result, IStatus status) {
			perModuleState.put(mc,  result);
			perModuleStatus.put(mc, status);
		}
	}
	
	private static class ModuleContainer {
		private IModule[] mod;
		private String stringRep;
		public ModuleContainer(IModule[] module) {
			this.mod = module;
			stringRep = toString();
		}
		public int hashCode() {
			return stringRep.hashCode();
		}
		public boolean equals(Object other) {
			if( other instanceof ModuleContainer) {
				if( ((ModuleContainer)other).toString().equals(stringRep)) {
					return true;
				}
			}
			return false;
		}
		public String toString() {
			StringBuffer sb = new StringBuffer();
			for( int i = 0; i < mod.length; i++ ) {
				sb.append(mod[i].getId());
				if(i == (mod.length - 1)) {
					sb.append("\n");
				}
			}
			return sb.toString();
		}
	}
	
}
