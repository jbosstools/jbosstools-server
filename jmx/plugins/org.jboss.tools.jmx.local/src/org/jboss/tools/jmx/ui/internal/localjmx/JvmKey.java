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

package org.jboss.tools.jmx.ui.internal.localjmx;

import org.jboss.tools.jmx.jvmmonitor.core.IActiveJvm;

public class JvmKey {
	private final String hostName;
	private final int pid;

	// The jvm is stored here, but is not part of the hashcode or .equals
	private final IActiveJvm jvm;

	public JvmKey(String hostName, int pid, IActiveJvm jvm) {
		this.hostName = hostName;
		this.pid = pid;
		this.jvm = jvm;
	}

	public String getHostName() {
		return hostName;
	}

	public int getPid() {
		return pid;
	}

	public IActiveJvm getJvm() {
		return jvm;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hostName == null) ? 0 : hostName.hashCode());
		result = prime * result + pid;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JvmKey other = (JvmKey) obj;
		if (hostName == null) {
			if (other.hostName != null)
				return false;
		} else if (!hostName.equals(other.hostName))
			return false;
		if (pid != other.pid)
			return false;
		return true;
	}

}
