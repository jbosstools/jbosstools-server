/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.jmx.jvmmonitor.internal.agent;

import javax.management.MXBean;

/**
 * The MXBean to transfer data.
 */
@SuppressWarnings("nls")
@MXBean
public interface DataTransferMXBean {

    /** The data transfer MXBean name. */
    final static String DATA_TRANSFER_MXBEAN_NAME = "org.jboss.tools.jmx.jvmmonitor:type=Data Transfer";

    /**
     * Reads the data from file on host where target JVM is running.
     * 
     * @param fileName
     *            The file name
     * @param pos
     *            The offset position of data in bytes to start reading data
     * @param maxSize
     *            The max size in bytes to read data
     * @return The file data
     */
    byte[] read(String fileName, int pos, int maxSize);

    /**
     * Gets the version.
     * 
     * @return The version
     */
    String getVersion();
}
