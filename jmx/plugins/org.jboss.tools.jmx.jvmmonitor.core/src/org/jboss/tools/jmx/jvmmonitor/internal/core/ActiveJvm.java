/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jboss.tools.jmx.jvmmonitor.internal.core;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Properties;

import javax.management.remote.JMXServiceURL;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.jboss.tools.jmx.jvmmonitor.core.AbstractJvm;
import org.jboss.tools.jmx.jvmmonitor.core.Activator;
import org.jboss.tools.jmx.jvmmonitor.core.IActiveJvm;
import org.jboss.tools.jmx.jvmmonitor.core.IHost;
import org.jboss.tools.jmx.jvmmonitor.core.IJvm;
import org.jboss.tools.jmx.jvmmonitor.core.ISWTResourceMonitor;
import org.jboss.tools.jmx.jvmmonitor.core.IThreadElement;
import org.jboss.tools.jmx.jvmmonitor.core.JvmCoreException;
import org.jboss.tools.jmx.jvmmonitor.core.JvmModel;
import org.jboss.tools.jmx.jvmmonitor.core.JvmModelEvent;
import org.jboss.tools.jmx.jvmmonitor.core.JvmModelEvent.State;
import org.jboss.tools.jmx.jvmmonitor.core.cpu.ICpuProfiler;
import org.jboss.tools.jmx.jvmmonitor.core.mbean.IMBeanServer;
import org.jboss.tools.jmx.jvmmonitor.internal.core.cpu.CpuProfiler;

/**
 * The active JVM.
 */
public class ActiveJvm extends AbstractJvm implements IActiveJvm {

    /** The URL path. */
    private static final String URL_PATH = "/jndi/rmi://%s:%d/jmxrmi"; //$NON-NLS-1$

    /** The RMI protocol. */
    private static final String RMI_PROTOCOL = "rmi"; //$NON-NLS-1$

    /** The main thread. */
    private static final String MAIN_THREAD = "main"; //$NON-NLS-1$

    /** The state indicating if attach mechanism is supported. */
    private boolean isAttachSupported;

    /** The state indicating if simply connecting is supported. */
    private boolean isConnectSupported;

    /** The error state message. */
    private String errorStateMessage;

    /** The state indicating if JVM is running on remote host. */
    private boolean isRemote;

    /** The state indicating if JVM is connected with JMX. */
    private boolean isConnected;

    /** The state indicating if JVM has agent attached. */
    private boolean isAttached;
    
    /** Has this been initialized yet */
    private boolean isInitialized;

    /** The MXBean server. */
    private MBeanServer mBeanServer;

    /** The CPU profiler. */
    private ICpuProfiler cpuProfiler;

    /** The SWT resource monitor. */
    private ISWTResourceMonitor swtResourceMonitor;
    
    private Object monitoredVm;

    /**
     * The constructor for local JVM.
     * 
     * @param pid
     *            The process ID
     * @param url
     *            The JMX service URL
     * @param host
     *            The host
     * @throws JvmCoreException
     */
    public ActiveJvm(int pid, String url, IHost host) throws JvmCoreException {
        super(pid, host);
        isRemote = false;
        saveJvmProperties();
        isConnectSupported = true;
        isAttachSupported = true;
        initialize( url, pid);
    }
    
    /**
     * The constructor for local JVM.
     * 
     * @param pid
     *            The process ID
     * @param url
     *            The JMX service URL
     * @param host
     *            The host
     * @throws JvmCoreException
     */
    public ActiveJvm(int pid, Object monitoredVm, IHost host) throws JvmCoreException {
        super(pid, host);
        this.isRemote = false;
        this.monitoredVm = monitoredVm;
        this.isAttachSupported = true;
        this.isConnectSupported = true;
        saveJvmProperties();
    }

    /**
     * The constructor for JVM communicating with RMI protocol.
     * 
     * @param port
     *            The port
     * @param userName
     *            The user name
     * @param password
     *            The password
     * @param host
     *            The host
     * @param updatePeriod
     *            The update period
     * @throws JvmCoreException
     */
    public ActiveJvm(int port, String userName, String password, IHost host,
            int updatePeriod) throws JvmCoreException {
        super(port, userName, password, host);

        isRemote = true;

        String urlPath = String.format(URL_PATH, host.getName(), port);
        JMXServiceURL url = null;
        try {
            url = new JMXServiceURL(RMI_PROTOCOL, "", 0, urlPath); //$NON-NLS-1$
            isConnectSupported = true;
            isAttachSupported = true;
        } catch (IOException e) {
            throw new JvmCoreException(IStatus.ERROR, NLS.bind(
                    Messages.getJmxServiceUrlForPortFailedMsg, port), e);
        }
        initialize(url);

        // refresh
        connect(updatePeriod);
        refreshPid();
        refreshMainClass();
        refreshSnapshots();
        disconnect();

        saveJvmProperties();
    }

    /**
     * The constructor for JVM communicating with RMI protocol.
     * 
     * @param url
     *            The JMX URL
     * @param userName
     *            The user name
     * @param password
     *            The password
     * @param updatePeriod
     *            The update period
     * @throws JvmCoreException
     */
    public ActiveJvm(String url, String userName, String password,
            int updatePeriod) throws JvmCoreException {
        super(userName, password);

        isRemote = true;

        JMXServiceURL jmxUrl = null;
        try {
            jmxUrl = new JMXServiceURL(url);
            isConnectSupported = true;
            isAttachSupported = true;
        } catch (MalformedURLException e) {
            throw new JvmCoreException(IStatus.ERROR, NLS.bind(
                    Messages.getJmxServiceUrlForUrlFailedMsg, url), e);
        }

        initialize(jmxUrl);

        // refresh
        connect(updatePeriod);
        refreshPid();
        refreshMainClass();
        boolean jvmAddedToHost = refreshHost();
        disconnect();

        if (jvmAddedToHost) {
            refreshSnapshots();
        }
    }

    /*
     * @see IActiveJvm#connect(int)
     */
    @Override
    public void connect(int updatePeriod) throws JvmCoreException {
    	connect(updatePeriod, true);
    }


    /*
     * @see IActiveJvm#connect(int, boolean)
     */
	@Override
	public void connect(int updatePeriod, boolean attach)
			throws JvmCoreException {
		// If you won't let us attach, and we're not already attached, error out
		if( !attach && !isAttached ) {
    		 throw new IllegalStateException(Messages.connectNotSupportedMsg);
		}
		
    	if( !isConnectionSupported() ) {
     		 throw new IllegalStateException(Messages.connectNotSupportedMsg);
     	}

    	if( isAttached || attach ) {
        	initialize();
        	attach();
    	}
    	
    	
        mBeanServer.connect(updatePeriod);
        isConnected = true;
        
        
        JvmModel.getInstance().fireJvmModelChangeEvent(
                new JvmModelEvent(State.JvmConnected, this));
	}
    
	
	/*
	 * (non-Javadoc)
	 * @see org.jboss.tools.jmx.jvmmonitor.core.IActiveJvm#attach()
	 */
    public void attach() throws JvmCoreException {

        if (!isAttachSupported) {
            throw new IllegalStateException(Messages.attachNotSupportedMsg);
        }

        if( !isAttached ) {
	        if (!isRemote) {
	            JvmModel.getInstance().getAgentLoadHandler().loadAgent(this);
	            isAttached = true;
	        }
	        if (swtResourceMonitor.isSupported()) {
	            swtResourceMonitor.setTracking(true);
	        }
        }
    }
    
    /*
     * @see IActiveJvm#disconnect()
     */
    @Override
    public void disconnect() {
        isConnected = false;

        mBeanServer.dispose();
        try {
            if (swtResourceMonitor.isSupported()) {
                swtResourceMonitor.setTracking(false);
            }
        } catch (JvmCoreException e) {
            // do nothing
        }

        JvmModel.getInstance().fireJvmModelChangeEvent(
                new JvmModelEvent(State.JvmDisconnected, this));
    }
    
    /*
     * @see IActiveJvm#isAttached()
     */
	@Override
	public boolean isAttached() {
		return isAttached;
	}

    /*
     * @see IActiveJvm#isConnected()
     */
    @Override
    public boolean isConnected() {
        return isConnected;
    }

    /*
     * @see IActiveJvm#isAttachSupported()
     */
    @Override
    public boolean isConnectionSupported() {
        return isAttachSupported;
    }

    /*
     * @see IActiveJvm#getErrorStateMessage()
     */
    @Override
    public String getErrorStateMessage() {
        return errorStateMessage;
    }

    /*
     * @see IActiveJvm#isRemote()
     */
    @Override
    public boolean isRemote() {
        return isRemote;
    }

    /*
     * @see IActiveJvm#getMBeanServer()
     */
    @Override
    public IMBeanServer getMBeanServer() {
        return mBeanServer;
    }

    /*
     * @see IActiveJvm#getCpuProfiler()
     */
    @Override
    public ICpuProfiler getCpuProfiler() {
        return cpuProfiler;
    }

    /*
     * @see IActiveJvm#getSWTResourceMonitor()
     */
    @Override
    public ISWTResourceMonitor getSWTResourceMonitor() {
        return swtResourceMonitor;
    }

    /*
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return getMainClass();
    }

    /**
     * Saves the JVM properties.
     */
    public void saveJvmProperties() {
        IFileStore fileStore;
        try {
            fileStore = Util.getFileStore(IJvm.PROPERTIES_FILE,
                    getPersistenceDirectory());
            if (fileStore.fetchInfo().exists()) {
                return;
            }
        } catch (JvmCoreException e) {
            Activator.log(IStatus.ERROR, Messages.savePropertiesFileFailedMsg,
                    e);
            return;
        }

        Properties props = new Properties();
        OutputStream os = null;
        try {
            os = fileStore.openOutputStream(EFS.NONE, null);

            int pid = getPid();
            int port = getPort();
            String mainClass = getMainClass();

            props.setProperty(IJvm.PID_PROP_KEY, String.valueOf(pid));
            props.setProperty(IJvm.PORT_PROP_KEY, String.valueOf(port));
            if (mainClass != null) {
                props.setProperty(IJvm.MAIN_CLASS_PROP_KEY, mainClass);
            }
            props.setProperty(IJvm.HOST_PROP_KEY, getHost().getName());

            props.storeToXML(os, "JVM Properties"); //$NON-NLS-1$
        } catch (CoreException e) {
            Activator.log(IStatus.ERROR, NLS.bind(
                    Messages.openOutputStreamFailedMsg, fileStore.toURI()
                            .getPath()), e);
        } catch (IOException e) {
            try {
                fileStore.delete(EFS.NONE, null);
            } catch (CoreException e1) {
                // do nothing
            }
            Activator.log(IStatus.ERROR, NLS.bind(
                    Messages.writePropertiesFileFailedMsg, fileStore.toURI()
                            .getPath()), e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
    }

    /**
     * Sets the error state message.
     * 
     * @param errorStateMessage
     *            The error state message
     */
    protected void setErrorStateMessage(String errorStateMessage) {
        this.errorStateMessage = errorStateMessage;
    }

    /**
     * Initializes the active JVM.
     * 
     * @param url
     *            The JMX service URL
     */
    private void initialize(JMXServiceURL url) {
        isConnected = false;
        cpuProfiler = new CpuProfiler(this);
        mBeanServer = new MBeanServer(url, this);
        swtResourceMonitor = new SWTResourceMonitor(this);
        isInitialized = true;
    }
    
    private void initialize() throws JvmCoreException {
    	if( monitoredVm != null ) {
    		String url = getLocalConnectorAddress(monitoredVm, getPid());
    		if( url != null )
    			initialize(url, getPid());
    	}
    }
    
    /**
     * Initializes the active JVM.
     * 
     * @param url
     *            The JMX service URL
     */
    private void initialize(String url, int pid) throws JvmCoreException {
        JMXServiceURL jmxUrl = null;
        try {
            if (url != null) {
                jmxUrl = new JMXServiceURL(url);
            } else {
            	// url is null
                throw new JvmCoreException(IStatus.ERROR, NLS.bind(
                        Messages.getJmxServiceUrlForPidFailedMsg, pid), null);

            }
        } catch (MalformedURLException e) {
            throw new JvmCoreException(IStatus.ERROR, NLS.bind(
                    Messages.getJmxServiceUrlForPidFailedMsg, pid), e);
        }
        initialize(jmxUrl);
    }

    /**
     * Refreshes the PID.
     * 
     * @throws JvmCoreException
     */
    private void refreshPid() throws JvmCoreException {
        String[] elements = mBeanServer.getRuntimeName().split("@"); //$NON-NLS-1$
        if (elements == null || elements.length != 2) {
            throw new JvmCoreException(IStatus.ERROR, Messages.getPidFailedMsg,
                    new Exception());
        }

        setPid(Integer.valueOf(elements[0]));
    }

    /**
     * Refreshes the host.
     * 
     * @return True if JVM has been added to host
     * @throws JvmCoreException
     */
    private boolean refreshHost() throws JvmCoreException {
        String[] elements = mBeanServer.getRuntimeName().split("@"); //$NON-NLS-1$
        if (elements == null || elements.length != 2) {
            throw new JvmCoreException(IStatus.ERROR,
                    Messages.getHostNameFailedMsg, new Exception());
        }

        String hostName = elements[1];
        Host host = (Host) JvmModel.getInstance().getHost(hostName);
        if (host == null) {
            host = new Host(hostName);
        } else {
            for (IJvm jvm : host.getActiveJvms()) {
                if (jvm.getPid() == getPid()) {
                    return false;
                }
            }
        }
        host.addActiveJvm(this);
        setHost(host);
        return true;
    }

    /**
     * Refreshes the main class.
     * 
     * @throws JvmCoreException
     */
    private void refreshMainClass() throws JvmCoreException {
        mBeanServer.refreshThreadCache();

        for (IThreadElement element : mBeanServer.getThreadCache()) {
            if (element.getThreadName().equals(MAIN_THREAD)) {
                StackTraceElement[] elements = element.getStackTraceElements();
                if (elements == null || elements.length == 0) {
                    return;
                }

                StackTraceElement lastElement = elements[elements.length - 1];
                setMainClass(lastElement.getClassName());
                break;
            }
        }
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
    private static String getLocalConnectorAddress(Object monitoredVm, int pid)
            throws JvmCoreException {
    	return JvmModel.getInstance().getAttachHandler().getLocalConnectorAddress(monitoredVm, pid);
    }
}
