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

	/* The following constants are marked public but are in an internal package. */
	public static final String LABEL_FILE_SIZE = "runtime-size";
	public static final String LABEL_WTP_RUNTIME = "wtp-runtime-type";
	public static final String LABEL_RUNTIME_CATEGORY = "runtime-category";
	public static final String LABEL_RUNTIME_TYPE = "runtime-type";
	public static final String PROP_WTP_RUNTIME = LABEL_WTP_RUNTIME;
	
	
	public DownloadRuntimesProvider() {
	}

	private Stacks[] getStacks(IProgressMonitor monitor) {
		return new StacksManager().getStacks("Loading Downloadable Runtimes", monitor, StacksManager.StacksType.PRESTACKS_TYPE, StacksManager.StacksType.STACKS_TYPE);
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
		Stacks[] stacksArr = getStacks(new SubProgressMonitor(monitor, 100));
		ArrayList<DownloadRuntime> all = new ArrayList<DownloadRuntime>();
		monitor.beginTask(Messages.CreateDownloadRuntimes, stacksArr.length * 100);		
		for( int i = 0; i < stacksArr.length; i++ ) {
			IProgressMonitor inner = new SubProgressMonitor(monitor, 100);
			if( stacksArr[i] != null ) {
				traverseStacks(stacksArr[i], all, inner);
			}
		}
		monitor.done();
		downloads = all;
	}
	
	private void traverseStacks(Stacks stacks, ArrayList<DownloadRuntime> list, IProgressMonitor monitor) {
		List<org.jboss.jdf.stacks.model.Runtime> runtimes = stacks.getAvailableRuntimes();
		Iterator<org.jboss.jdf.stacks.model.Runtime> i = runtimes.iterator();
		org.jboss.jdf.stacks.model.Runtime workingRT = null;
		monitor.beginTask(Messages.CreateDownloadRuntimes, runtimes.size() * 100);
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
				dr.setProperty(LABEL_RUNTIME_CATEGORY, workingRT.getLabels().getProperty(LABEL_RUNTIME_CATEGORY));
				dr.setProperty(LABEL_RUNTIME_TYPE, workingRT.getLabels().getProperty(LABEL_RUNTIME_TYPE));
				if( legacyId != null )
					dr.setProperty(DownloadRuntime.PROPERTY_ALTERNATE_ID, id);
				list.add(dr);
			}
			monitor.worked(100);
		}
		monitor.done();
	}
	
	
	private HashMap<String, String> LEGACY_HASHMAP = null;
	
	// Given a stacks.yaml runtime id, get the legacy downloadRuntimes id that's required
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
}
