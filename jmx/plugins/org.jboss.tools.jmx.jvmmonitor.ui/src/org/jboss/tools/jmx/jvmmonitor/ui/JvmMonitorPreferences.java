/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.jmx.jvmmonitor.ui;

import org.jboss.tools.jmx.jvmmonitor.internal.ui.IConstants;

public class JvmMonitorPreferences {

	public static int getJvmUpdatePeriod() {
		int answer = Activator.getDefault()
		        .getPreferenceStore()
		        .getInt(IConstants.UPDATE_PERIOD);
		return answer;
	}

}
