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
