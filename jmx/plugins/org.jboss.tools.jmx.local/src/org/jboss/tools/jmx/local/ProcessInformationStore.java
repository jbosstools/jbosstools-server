/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.jmx.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class ProcessInformationStore {
	
	private static ProcessInformationStore singleton;
	public static synchronized ProcessInformationStore getDefault() {
		if( singleton == null ) {
			singleton = new ProcessInformationStore();
		}
		return singleton;
	}
	
	protected Map<Integer, String> processInformationStore = new HashMap<Integer, String>();

	/**
	 *
	 * @param pid
	 * @return
	 */
	public synchronized String queryProcessInformation(int pid) {
		if (!processInformationStore.containsKey(pid)) {
			refreshProcessInformationStoreAsync();
		}
		return processInformationStore.get(pid);
	}

	/**
	 * rebuilds the local process information store
	 * @deprecated Please use the signature with a progress monitor
	 */
	public void refreshProcessInformationStore() {
		refreshProcessInformationStore(new NullProgressMonitor());
	}
	
	/**
	 * rebuilds the local process information store
	 */
	public void refreshProcessInformationStore(IProgressMonitor monitor) {
		setProcessStore(loadProcessStore(monitor));
	}
	
	/**
	 * Store the new map as long as it's not null
	 * @param store
	 */
	private synchronized void setProcessStore(Map<Integer, String> store) {
		if( store != null ) {
			processInformationStore = store;
		}
	}
	
	/**
	 * Return a map of the currently running processes, or null in case of an error.
	 * @param monitor
	 * @return
	 */
	private Map<Integer, String> loadProcessStore(IProgressMonitor monitor) {

		Map<Integer, String> tmp = new HashMap<Integer, String>();
		String javaHome = System.getProperty("java.home");
		IPath jHomePath = new Path(javaHome);
		File jHome = jHomePath.toFile();
		File jps = null;
		if( jHome.getName().equalsIgnoreCase("jre")) {
			jps = jHomePath.removeLastSegments(1).append("bin").append("jps").toFile();
		}
		if( jps == null || !jps.exists()) {
			jps = jHomePath.append("bin").append("jps").toFile();
		}
		BufferedReader br = null;
		try {
			String[] cmd = new String[] {
					jps.getAbsolutePath(),
					"-v"
			};

			Process p = Runtime.getRuntime().exec(jps.getAbsolutePath() + " -v");
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while (!monitor.isCanceled() && (line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, " ");
				int pid = -1;
				if (st.hasMoreElements()) {
					String sVal = st.nextToken();
					try {
						pid = Integer.parseInt(sVal);
					} catch (NumberFormatException e) {
						pid = -1;
					}
				}

				if (pid != -1) {
					tmp.put(pid, line);
				}
			}
			return tmp;
		} catch (Exception ex) {
			// we don't want to scare the user with this
			ex.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
					// we don't want to scare the user with this
				}
			}
		}
		return null;
	}
	
	public void refreshProcessInformationStoreAsync() {
		new Job("Refreshing Process Information") {
			protected IStatus run(IProgressMonitor monitor) {
				refreshProcessInformationStore(monitor);
				return Status.OK_STATUS;
			}
		}.schedule();
	}
}
