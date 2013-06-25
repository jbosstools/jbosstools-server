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
package org.jboss.tools.as.runtimes.integration.util;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBean;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.tools.as.runtimes.integration.internal.DownloadRuntimesProvider;
import org.jboss.tools.runtime.core.RuntimeCoreActivator;
import org.jboss.tools.runtime.core.model.DownloadRuntime;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

public class RuntimeMatcher {
	
	private static class RuntimeRangeRepresentation {
		private String wtpId, stacksRuntimeType, beginVersion, endVersion;
		private boolean bInclusive, eInclusive;
		private VersionRange osgiRange;
		
		public RuntimeRangeRepresentation(String wtpId, String subType, 
				String beginVersion, boolean bInclusive, 
				String endVersion, boolean eInclusive) {
			this.wtpId = wtpId;
			this.stacksRuntimeType = subType;
			this.beginVersion = beginVersion;
			this.endVersion = endVersion;
			this.bInclusive = bInclusive;
			this.eInclusive = eInclusive;
		}
		public RuntimeRangeRepresentation(String pattern) {
			int openCurly = pattern.indexOf('{');
			wtpId = pattern;
			if( openCurly != -1 ) {
				if( pattern.charAt(pattern.length()-1) != '}') {
					throw new IllegalArgumentException();
				}
				wtpId = pattern.substring(0, openCurly);
				String remainder = pattern.substring(openCurly+1, pattern.length()-1);
				// AS:[4.3,4.5)
				int colon = remainder.indexOf(':');
				if( colon == -1 ) {
					stacksRuntimeType = remainder;
				} else {
					stacksRuntimeType = remainder.substring(0, colon);
					String versionRange = remainder.substring(colon+1).trim();
					// [4.3,4.5)
					int char0 = versionRange.charAt(0);
					if(char0 == '[' || char0 == '(') {
						bInclusive = (char0 == '[');
						int endChar = versionRange.charAt(versionRange.length() -1);
						if(endChar != ']' && endChar != ')' )
							throw new IllegalArgumentException();
						eInclusive = (endChar == ']');
						String range = versionRange.substring(1, versionRange.length() - 1);
						String[] ranges = range.split(",");
						beginVersion = ranges[0].trim();
						endVersion = ranges[1].trim();
					} else {
						// we dont have a range. Just a number
						beginVersion = versionRange;
						endVersion = null;
					}
					osgiRange = new VersionRange(versionRange);
				}
			}
		}
		
		public boolean matchesVersion(String version) {
			Version v = new Version(version);
			return osgiRange.includes(v);
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(wtpId);
			if( stacksRuntimeType != null ) {
				sb.append('{');
				sb.append(stacksRuntimeType);
				if( beginVersion != null ) {
					sb.append(":");
					if( endVersion == null ) {
						sb.append(beginVersion);
					} else {
						sb.append(bInclusive ? '[' : '(');
						sb.append(beginVersion);
						sb.append(',');
						sb.append(endVersion);
						sb.append(eInclusive ? ']' : ')');
					}
				}
				sb.append('}');
			}
			return sb.toString();
		}
	}
	
	//org.jboss.ide.eclipse.as.runtime.71{AS:[5.2,5.3)}
	public String createPattern(String wtpId, String subType) {
		return createPattern(wtpId, subType, null,false,null,false);
	}

	public String createPattern(String wtpId, String subType, String beginVersion) {
		return createPattern(wtpId, subType, beginVersion,false,null,false);
	}
	
	public String createPattern(String wtpId, String otherType, String beginVersion, boolean bInclusive, 
			String endVersion, boolean eInclusive) {
		return new RuntimeRangeRepresentation(wtpId, otherType, beginVersion, bInclusive, endVersion, eInclusive).toString();
	}
	
	
	/**
	 * Find existing wtp IRuntime objects that match the given search pattern
	 * 
	 * @param wtpId
	 * @param subType
	 * @param beginVersion
	 * @param bInclusive
	 * @param endVersion
	 * @param eInclusive
	 * @return
	 */
	public IRuntime[] findExistingRuntimes(String wtpId, String subType, String beginVersion, boolean bInclusive, 
			String endVersion, boolean eInclusive) {
		RuntimeRangeRepresentation rep = new RuntimeRangeRepresentation(wtpId, subType, beginVersion, bInclusive, endVersion, eInclusive);
		return findExistingRuntimes(rep);
	}

	/**
	 * Find existing wtp IRuntime objects that match the given search pattern
	 * 
	 * @param pattern
	 * @return
	 */
	public IRuntime[] findExistingRuntimes(String pattern) {
		RuntimeRangeRepresentation rep = new RuntimeRangeRepresentation(pattern);
		return findExistingRuntimes(rep);
	}
	
	private IRuntime[] findExistingRuntimes(RuntimeRangeRepresentation rep) {
		ArrayList<IRuntime> list = new ArrayList<IRuntime>();
		IRuntime[] all = ServerCore.getRuntimes();
		for( int i = 0; i < all.length; i++ ) {
			String rtType =all[i].getRuntimeType().getId();
			if( rtType.equals(rep.wtpId)) {
				if( rep.stacksRuntimeType == null )
					list.add(all[i]);
				else {
					ServerBeanLoader loader = new ServerBeanLoader(all[i].getLocation().toFile());
					if( matchesServerBeanType(rep.stacksRuntimeType, loader.getServerBean())) {
						if( rep.beginVersion == null ) {
							list.add(all[i]);
						} else {
							String version = loader.getFullServerVersion();
							if( rep.matchesVersion(version))
								list.add(all[i]);
						}
					}
				}
			}
		}
		return (IRuntime[]) list.toArray(new IRuntime[list.size()]);
	};
	
	/* This can be replaced with some mapping if there's an disconnect between ServerBean and the stacks.yaml file */
	private boolean matchesServerBeanType(String stacksRuntimeType, ServerBean serverBean) {
		return stacksRuntimeType.equals(serverBean.getType().getId());
	}
	
	/**
	 * 
	 * Return an array of DownloadRuntime objects which match the given attributes 
	 * @param wtpId
	 * @param otherType
	 * @param beginVersion
	 * @param bInclusive
	 * @param endVersion
	 * @param eInclusive
	 * @param monitor
	 * @return
	 */
	public DownloadRuntime[] findDownloadRuntimes(String wtpId, String subType, String beginVersion, boolean bInclusive, 
			String endVersion, boolean eInclusive, IProgressMonitor monitor) {
		RuntimeRangeRepresentation rep = new RuntimeRangeRepresentation(wtpId, subType, beginVersion, bInclusive, endVersion, eInclusive);
		return findDownloadRuntimes(rep, monitor);
	}

	/**
	 * Return an array of DownloadRuntime objects which match a given pattern string, as defined above. 
	 * @param pattern
	 * @param monitor
	 * @return
	 */
	public DownloadRuntime[] findDownloadRuntimes(String pattern, IProgressMonitor monitor) {
		RuntimeRangeRepresentation rep = new RuntimeRangeRepresentation(pattern);
		return findDownloadRuntimes(rep, monitor);
	}
	
	private DownloadRuntime[] findDownloadRuntimes(RuntimeRangeRepresentation rep, IProgressMonitor monitor) {
		monitor.beginTask("Locating Matching Downloadable Runtimes", 1000);
		DownloadRuntime[] arr = RuntimeCoreActivator.getDefault().getDownloadRuntimeArray(new SubProgressMonitor(monitor, 800));
		
		
		IProgressMonitor subMon = new SubProgressMonitor(monitor, 200);
		subMon.beginTask("Filtering Downloadable Runtimes", arr.length * 100);
		ArrayList<DownloadRuntime> list = new ArrayList<DownloadRuntime>(arr.length);
		for( int i = 0; i < arr.length; i++ ) {
			String rtType = (String)arr[i].getProperty(DownloadRuntimesProvider.LABEL_WTP_RUNTIME);
			if( rep.wtpId.equals(rtType)) {
				if( rep.stacksRuntimeType == null )
					list.add(arr[i]);
				else {
					if( rep.stacksRuntimeType.equals(arr[i].getProperty(DownloadRuntimesProvider.LABEL_RUNTIME_TYPE))) {
						if( rep.beginVersion == null ) {
							list.add(arr[i]);
						} else {
							String version = arr[i].getVersion();
							if( rep.matchesVersion(version))
								list.add(arr[i]);
						}
					}
				}
			}
			subMon.worked(100);
		}
		subMon.done();
		monitor.done();
		return (DownloadRuntime[]) list.toArray(new DownloadRuntime[list.size()]);
	}
}
