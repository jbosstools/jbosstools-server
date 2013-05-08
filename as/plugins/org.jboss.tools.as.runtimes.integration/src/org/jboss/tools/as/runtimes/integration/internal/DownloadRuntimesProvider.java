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
					String name = workingRT.getName();
					String version = workingRT.getVersion();
					DownloadRuntime dr = new DownloadRuntime(id, name, version, url);
					dr.setLicenseURL(license);
					dr.setSize(fileSize);
					tmp.add(dr);
				}
				creationMonitor.worked(100);
			}
			creationMonitor.done();
			monitor.done();
			downloads = tmp;
		}
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
