/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jboss.tools.jmx.jvmmonitor.internal.tools;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.NLS;
import org.jboss.tools.common.jdt.debug.RemoteDebugActivator;
import org.jboss.tools.common.jdt.debug.tools.ToolsCore;
import org.jboss.tools.common.jdt.debug.tools.ToolsCore.AttachedVM;
import org.jboss.tools.common.jdt.debug.tools.ToolsCoreException;
import org.jboss.tools.jmx.jvmmonitor.core.IActiveJvm;
import org.jboss.tools.jmx.jvmmonitor.core.IHost;
import org.jboss.tools.jmx.jvmmonitor.core.IJvmAttachHandler;
import org.jboss.tools.jmx.jvmmonitor.core.JvmCoreException;
import org.jboss.tools.jmx.jvmmonitor.tools.Activator;


/**
 * The JVM attach handler that contributes to the extension point
 * <tt>org.jboss.tools.jmx.jvmmonitor.core.jvmAttachHandler</tt>.
 */
public class JvmAttachHandler implements IJvmAttachHandler,
	IPreferenceChangeListener, IConstants {

    /** The local host. */
    private IHost localhost;

    /** The timer. */
    Timer timer;

    /*
     * @see IJvmAttachHandler#setHost(IHost)
     */
    @Override
    public void setHost(IHost host) {
        this.localhost = host;
    	IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
    	prefs.addPreferenceChangeListener(this);
    }

    /*
     * @see IJvmAttachHandler#hasValidJdk()
     */
    @Override
    public boolean hasValidJdk() {
        return ToolsCore.isToolsReady();
    }

    /**
     * Starts monitoring.
     */
    private void startMonitoring() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer(true);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    updatesActiveJvms();
                } catch (Throwable t) {
                    Activator.log(IStatus.ERROR,
                            Messages.updateTimerCanceledMsg, t);
                    timer.cancel();
                }
            }
        };

    	IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        long period = prefs.getLong(IConstants.UPDATE_PERIOD, IConstants.DEFAULT_UPDATE_PERIOD);
        timer.schedule(timerTask, 0, period);
    }

    /**
     * Updates the active JVMs.
     * 
     * @throws JvmCoreException
     */
    void updatesActiveJvms() throws JvmCoreException {
    	try {
        	// There is a big bug here where force-killed pid's are still being returned. 
	        Set<Integer> activeJvms = ToolsCore.getActiveProcessIds(IHost.LOCALHOST);
	
	        // add JVMs 
	        List<IActiveJvm> previousVms = localhost.getActiveJvms();
	        for (int pid : activeJvms) {
	            if (containJvm(previousVms, pid)) {
	            	// So we need to check for false-positives here
			        boolean terminated = RemoteDebugActivator.getDefault().getVmModel(IHost.LOCALHOST, pid, true, new NullProgressMonitor()) == null;
			        if( terminated ) 
			        	localhost.removeJvm(pid);
	                continue;
	            }
	
	            addActiveJvm(pid, IHost.LOCALHOST);
	        }
	        
	        // remove JVMs
	        for (IActiveJvm jvm : previousVms) {
	            Integer pid = jvm.getPid();
	            if (!activeJvms.contains(pid)) {
	                localhost.removeJvm(pid);
	            }
	        }
    	} catch(ToolsCoreException tce) {
    		throw new JvmCoreException(tce.getStatus().getSeverity(), tce.getMessage(), tce);
    	}
    }

    /**
     * Checks if the given list of JVMs contains the given pid.
     * 
     * @param jvms
     *            The list of active JVMs
     * @param pid
     *            The pid
     * @return True if the given list of JVMs contains the given pid
     */
    private static boolean containJvm(List<IActiveJvm> jvms, int pid) {
        for (IActiveJvm jvm : jvms) {
            if (jvm.getPid() == pid) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds the active JVM.
     * 
     * @param pid
     *            The pid
     * @param monitoredHost
     *            The monitored host
     */
    private void addActiveJvm(int pid, String host) {
    	ToolsCore.MonitoredVM vm = null;
        try {
        	vm = ToolsCore.getMonitoredVm(host, pid);
        } catch (ToolsCoreException e) {
        	// We do not need to log an error here. Odds are the 
        	// VM has either already terminated or has some other issue
        	// preventing us from connecting to it. Regardless, I feel 
        	// we should ignore this error and not create the connection at all.
        	return;
        }

        String mainClass = null;
        String launchCommand = null;
        try {
	        if (vm != null) {
	        	try {
		            mainClass = getMainClass(host, pid);
		            launchCommand = getJavaCommand(host, pid);
	            } catch(Exception tce) {
	                Activator.log(IStatus.WARNING, NLS.bind(Messages.connectTargetJvmFailedMsg, pid), tce);
	            }
	        }
	        
	        // Still add a stub if possible
	    	localhost.addLocalActiveJvm(pid, mainClass, launchCommand, 
	    			vm == null ? vm : vm.getMonitoredVM(), null);
        } catch (JvmCoreException e) {
            Activator.log(IStatus.WARNING, NLS.bind(Messages.connectTargetJvmFailedMsg, pid), e);
        }
    }

    /**
     * Gets the main class name.
     * 
     * @param monitoredVm
     *            The monitored JVM.
     * @param pid
     *            The pid
     * @return The main class name.
     */
    private static String getMainClass(String host, int pid) throws ToolsCoreException {
    	return ToolsCore.getMainClass(host, pid);
    }
    

    private static String getJavaCommand(String host, int pid)  throws ToolsCoreException {
    	return ToolsCore.getJavaCommand(host, pid);
    }

    /**
     * Gets the local connector address.
     * This involves **attaching the agent** and discovering the connection url
     * via jmx!
     * 
     * @param monitoredVm
     *            The monitored JVM
     * @param pid
     *            The process ID
     * 
     * @return The local connector address
     * @throws JvmCoreException
     */
    public String getLocalConnectorAddress(Object monitoredVm, int pid) throws JvmCoreException {
    	
        try {
            return getLocalConnectorAddressInternal(monitoredVm, pid);
        } catch (JvmCoreException e) {
            String message = NLS.bind(
                    Messages.getLocalConnectorAddressFailedMsg, pid);
            Activator.log(IStatus.WARNING, message, e);
            return null;
        }
    }

    public String getLocalConnectorAddressInternal(Object monitoredVm, int pid) throws JvmCoreException {
        String url = null;

        AttachedVM virtualMachine = null;
        try {
            virtualMachine = ToolsCore.attach(pid);

            if(!ToolsCore.isJigsawRunning(virtualMachine)) {
                 String javaHome = ToolsCore.getSystemProperties(virtualMachine)
            	        .getProperty(IConstants.JAVA_HOME_PROPERTY_KEY);

                File file = new File(javaHome + IConstants.MANAGEMENT_AGENT_JAR);

                if (!file.exists()) {
                    String message = NLS.bind(Messages.fileNotFoundMsg,
                            file.getPath());
                    throw new JvmCoreException(IStatus.ERROR, message,
                            new Exception());
                }

                ToolsCore.loadAgent(virtualMachine, file.getAbsolutePath(),
                        IConstants.JMX_REMOTE_AGENT);
            }

            Properties props = ToolsCore.getAgentProperties(virtualMachine);
            url = (String) props.get(LOCAL_CONNECTOR_ADDRESS);
        } catch(ToolsCoreException tce) {
        	throw new JvmCoreException(IStatus.ERROR, tce.getMessage(), tce);
        } finally {
            if (virtualMachine != null) {
                try {
                    ToolsCore.detach(virtualMachine);
                } catch (ToolsCoreException e) {
                    // ignore
                }
            }
        }
        return url;
    }

	@Override
	public synchronized void beginPolling() {
        startMonitoring();
	}

	@Override
	public synchronized void suspendPolling() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
	}

	@Override
	public void refreshJVMs() throws JvmCoreException {
		updatesActiveJvms();
	}

	@Override
	public synchronized boolean isPolling() {
		return timer != null;
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		 startMonitoring();
	}
}
