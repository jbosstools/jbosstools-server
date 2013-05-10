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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.jboss.jdf.stacks.model.Stacks;
import org.jboss.tools.as.runtimes.integration.Messages;
import org.jboss.tools.runtime.core.model.DownloadRuntime;
import org.jboss.tools.runtime.core.model.IDownloadRuntimesProvider;
import org.jboss.tools.stacks.core.model.StacksManager;

/**
 * Pull runtimes from a stacks file and return them to runtimes framework
 */
public class DownloadRuntimesProvider implements IDownloadRuntimesProvider {

	private static final String LABEL_FILE_SIZE = "runtime-size";
	private static final String LABEL_WTP_RUNTIME = "wtp-runtime-type";
	public static final String PROP_WTP_RUNTIME = LABEL_WTP_RUNTIME;
	
	
	public DownloadRuntimesProvider() {
	}

	private Stacks getStacks(IProgressMonitor monitor) {
		return getStacksManager().getStacks(monitor);
	}
	
	private ArrayList<DownloadRuntime> downloads = null;
	
	@Override
	public DownloadRuntime[] getDownloadableRuntimes(String requestType, IProgressMonitor monitor) {
		if( downloads == null )
			loadDownloadableRuntimes(monitor);
		return (DownloadRuntime[]) downloads.toArray(new DownloadRuntime[downloads.size()]);
	}
	
	private synchronized void loadDownloadableRuntimes(IProgressMonitor monitor) {
		monitor.beginTask(Messages.LoadRemoteRuntimes, 200);
		Stacks stacks = getStacks(new SubProgressMonitor(monitor, 100));
		if( stacks != null ) {
			ArrayList<DownloadRuntime> tmp = new ArrayList<DownloadRuntime>();

			List<org.jboss.jdf.stacks.model.Runtime> runtimes = stacks.getAvailableRuntimes();
			Iterator<org.jboss.jdf.stacks.model.Runtime> i = runtimes.iterator();
			org.jboss.jdf.stacks.model.Runtime workingRT = null;
			IProgressMonitor creationMonitor = new SubProgressMonitor(monitor, 100);
			creationMonitor.beginTask(Messages.CreateDownloadRuntimes, runtimes.size() * 100);
			while(i.hasNext()) {
				workingRT = i.next();
				String wtpRT = workingRT.getLabels().getProperty(LABEL_WTP_RUNTIME);
				String url = workingRT.getDownloadUrl();
				if( wtpRT != null && url != null && !"".equals(url)) {
					// We can make a DL out of this
					String fileSize = workingRT.getLabels().getProperty(LABEL_FILE_SIZE);
					String license = workingRT.getLicense();
					String id = workingRT.getId();
					String legacyId = getLegacyId(id);
					String effectiveId = legacyId == null ? id : legacyId;
					
					String name = workingRT.getName();
					String version = workingRT.getVersion();
					DownloadRuntime dr = new DownloadRuntime(effectiveId, name, version, url);
					dr.setLicenseURL(license);
					dr.setSize(fileSize);
					dr.setProperty(PROP_WTP_RUNTIME, wtpRT);
					if( legacyId != null )
						dr.setProperty(DownloadRuntime.PROPERTY_ALTERNATE_ID, id);
					tmp.add(dr);
				}
				creationMonitor.worked(100);
			}
			creationMonitor.done();
			monitor.done();
			downloads = tmp;
		}
	}
	
	private HashMap<String, String> LEGACY_HASHMAP = null;
	
	// Given a stacks.yaml runtime id, get the legacy 
	// downloadRuntimes id that's required
	private synchronized String getLegacyId(String id) {
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
	
	
	// Let's pull from my topic branch for now
	// This is almost no different than us pulling from my hard-coded plugin.xml. 
	private static final String STACKS_YAML_TOPIC_BRANCH_URL = "https://raw.github.com/robstryker/jdf-stack/e81a884195be83d063c9bb147d1b7bf4fb333e7d/stacks.yaml";
	
	// return a custom stacks manager, until our data is upstream in stacks.yaml
	private StacksManager getStacksManager() {
		// TODO LATER just return the ORIGINAL stacks manager, once we have a coherent stacks decision!
		return new StacksManager() {
			public Stacks getStacks(IProgressMonitor monitor) {
				return getStacks(STACKS_YAML_TOPIC_BRANCH_URL, "stacks", "yaml", monitor);
			}
		};
	}

}
