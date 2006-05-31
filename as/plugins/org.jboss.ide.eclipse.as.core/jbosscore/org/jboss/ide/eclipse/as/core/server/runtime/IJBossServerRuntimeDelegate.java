package org.jboss.ide.eclipse.as.core.server.runtime;

import java.util.List;

public interface IJBossServerRuntimeDelegate {
	public static final int ACTION_START = 1;
	public static final int ACTION_SHUTDOWN = 2;
	public static final int ACTION_TWIDDLE = 3;
	public static final int ACTION_OTHER = 4;
	


	
	public String getId();
	public String getStartArgs();
	public String getStopArgs();
	public String getVMArgs();
	public List getRuntimeClasspath(int action);
	public String getStartJar();
	public String getShutdownJar();
	public String getStartMainType();
	public String getStopMainType();
}
