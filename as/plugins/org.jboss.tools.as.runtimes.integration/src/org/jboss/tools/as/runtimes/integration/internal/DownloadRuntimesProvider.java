/*************************************************************************************
 * Copyright (c) 2013 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.tools.as.runtimes.integration.internal;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.jdf.stacks.model.Stacks;
import org.jboss.tools.as.runtimes.integration.util.AbstractStacksDownloadRuntimesProvider;
import org.jboss.tools.runtime.core.model.DownloadRuntime;
import org.jboss.tools.stacks.core.model.StacksManager;

/**
 * Pull runtimes from a stacks file and return them to runtimes framework
 */
public class DownloadRuntimesProvider extends AbstractStacksDownloadRuntimesProvider {
	
	public DownloadRuntimesProvider() {
		super();
	}

	private HashMap<String, String> LEGACY_HASHMAP = null;
	
	// Given a stacks.yaml runtime id, get the legacy downloadRuntimes id that's required
	protected synchronized String getLegacyId(String id) {
		if( LEGACY_HASHMAP == null )
			loadLegacy();
		return LEGACY_HASHMAP.get(id);
	}
	
	private synchronized void loadLegacy() {
		LEGACY_HASHMAP = new HashMap<String, String>();
		LEGACY_HASHMAP.put("jboss-as328SP1runtime", "org.jboss.tools.runtime.core.as.328" );
		LEGACY_HASHMAP.put("jboss-as405runtime", "org.jboss.tools.runtime.core.as.405" );
		LEGACY_HASHMAP.put("jboss-as423runtime", "org.jboss.tools.runtime.core.as.423" );
		LEGACY_HASHMAP.put("jboss-as501runtime", "org.jboss.tools.runtime.core.as.501" );
		LEGACY_HASHMAP.put("jboss-as510runtime", "org.jboss.tools.runtime.core.as.510" );
		LEGACY_HASHMAP.put("jboss-as610runtime", "org.jboss.tools.runtime.core.as.610" );
		LEGACY_HASHMAP.put("jboss-as701runtime", "org.jboss.tools.runtime.core.as.701" );
		LEGACY_HASHMAP.put("jboss-as702runtime", "org.jboss.tools.runtime.core.as.702" );
		LEGACY_HASHMAP.put("jboss-as710runtime", "org.jboss.tools.runtime.core.as.710" );
		LEGACY_HASHMAP.put("jboss-as711runtime", "org.jboss.tools.runtime.core.as.711" );
	}
	
	
	protected Stacks[] getStacks(IProgressMonitor monitor) {
		return new StacksManager().getStacks("Loading Downloadable Runtimes", monitor, StacksManager.StacksType.PRESTACKS_TYPE, StacksManager.StacksType.STACKS_TYPE);
	}
	
	protected void traverseStacks(Stacks stacks, ArrayList<DownloadRuntime> list, IProgressMonitor monitor) {
		traverseStacks(stacks, list, "SERVER", monitor);
	}
}
