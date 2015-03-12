/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jboss.tools.jmx.jvmmonitor.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.jboss.tools.jmx.jvmmonitor.core.JvmModelEvent.State;
import org.jboss.tools.jmx.jvmmonitor.internal.core.Host;
import org.jboss.tools.jmx.jvmmonitor.internal.core.Messages;
import org.jboss.tools.jmx.jvmmonitor.internal.core.Snapshot;
import org.jboss.tools.jmx.jvmmonitor.internal.core.Util;

/**
 * The JVM.
 */
abstract public class AbstractJvm implements IJvm, IAdaptable {

    /** The process ID. */
    private int pid;

    /** The port. */
    private int port;

    /** The user name. */
    private String userName;

    /** The password. */
    private String password;

    /** The main class. */
    private String mainClass;

    /** The full launch command */
    private String launchCommand;
    
    /** The host. */
    private IHost host;

    /** The snapshots. */
    protected List<ISnapshot> snapshots;

    /**
     * The constructor.
     * 
     * @param pid
     *            The process ID
     * @param port
     *            The port
     * @param userName
     *            The user name
     * @param password
     *            The password
     * @param host
     *            The host
     */
    public AbstractJvm(int pid, int port, String userName, String password,
            IHost host) {
        this.pid = pid;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.host = host;

        if (pid != -1) {
            refreshSnapshots();
        }
    }

    /**
     * The constructor.
     * 
     * @param pid
     *            The process ID
     * @param host
     *            The host
     */
    public AbstractJvm(int pid, IHost host) {
        this(pid, -1, null, null, host);
    }

    /**
     * The constructor.
     * 
     * @param port
     *            The port
     * @param userName
     *            The user name
     * @param password
     *            The password
     * @param host
     *            The host
     */
    public AbstractJvm(int port, String userName, String password, IHost host) {
        this(-1, port, userName, password, host);
    }

    /**
     * The constructor.
     * 
     * @param userName
     *            The user name
     * @param password
     *            The password
     */
    public AbstractJvm(String userName, String password) {
        this(-1, -1, userName, password, null);
    }

    /*
     * @see IJvm#getProcessId()
     */
    @Override
    public int getPid() {
        return pid;
    }

    /*
     * @see IJvm#getPort()
     */
    @Override
    public int getPort() {
        return port;
    }

    /*
     * @see IJvm#getMainClass()
     */
    @Override
    public String getMainClass() {
        if (mainClass == null) {
            return ""; //$NON-NLS-1$
        }
        return mainClass;
    }

    /*
     * @see IJvm#getMainClass()
     */
    @Override
    public String getLaunchCommand() {
        if (launchCommand == null) {
            return ""; //$NON-NLS-1$
        }
        return launchCommand;
    }
    
    /*
     * @see IJvm#getHost()
     */
    @Override
    public IHost getHost() {
        return host;
    }

    /*
     * @see IJvm#getShapshots()
     */
    @Override
    public List<ISnapshot> getShapshots() {
        return snapshots;
    }

    /*
     * @see IJvm#deleteSnapshot(ISnapshot)
     */
    @Override
    public void deleteSnapshot(ISnapshot snapshot) {
        snapshots.remove(snapshot);
        try {
            snapshot.getFileStore().delete(EFS.NONE, null);
        } catch (CoreException e) {
            Activator.log(IStatus.ERROR, NLS.bind(Messages.deleteFileFailedMsg,
                    snapshot.getFileStore().getName()), e);
        }
        JvmModel.getInstance().fireJvmModelChangeEvent(
                new JvmModelEvent(State.ShapshotRemoved, this));
    }

    /**
     * Adds the given snapshot to JVM.
     * 
     * @param snapshot
     *            The snapshot
     */
    public void addSnapshot(ISnapshot snapshot) {
        snapshots.add(snapshot);
    }

    /**
     * Gets the base directory.
     * 
     * @return The base directory
     * @throws JvmCoreException
     */
    public IPath getPersistenceDirectory() throws JvmCoreException {
        IPath stateLocation = Activator.getDefault().getStateLocation();
        IPath dirPath = stateLocation.append(File.separator + host.getName()
                + Host.DIR_SUFFIX + File.separator + pid + IJvm.DIR_SUFFIX);
        if (!dirPath.toFile().exists() && !dirPath.toFile().mkdir()) {
            throw new JvmCoreException(IStatus.ERROR, NLS.bind(
                    Messages.createDirectoryFailedMsg, dirPath.toFile()
                            .getName()), null);
        }
        return dirPath;
    }

    /**
     * Gets the user name.
     * 
     * @return The user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Gets the pass word.
     * 
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the PID.
     * 
     * @param pid
     *            The process ID
     */
    public void setPid(int pid) {
        this.pid = pid;
    }

    /**
     * Sets the main class.
     * 
     * @param mainClass
     *            The main class
     */
    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    

    /**
     * Sets the launch command
     * 
     * @param mainClass
     *            The main class
     */
    public void setLaunchCommand(String command) {
        this.launchCommand = command;
    }

    /**
     * Sets the host.
     * 
     * @param host
     *            The host
     */
    public void setHost(IHost host) {
        this.host = host;
    }

    /**
     * Refreshes the snapshots.
     */
    public void refreshSnapshots() {
        snapshots = new ArrayList<ISnapshot>();

        IPath baseDirectory;
        try {
            baseDirectory = getPersistenceDirectory();
        } catch (JvmCoreException e) {
            // do nothing
            return;
        }

        File[] files = baseDirectory.toFile().listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            String fileName = file.getName();
            if (Snapshot.isValidFile(fileName)) {
                Snapshot snapshot = new Snapshot(Util.getFileStore(fileName,
                        baseDirectory), this);
                snapshots.add(snapshot);
            }
        }
    }
    
    public void saveJvmProperties() {
    	
    }
    
    // Stub, please override in subclasses
    public Object getAdapter(Class adapter) {
    	return null;
    }
}
