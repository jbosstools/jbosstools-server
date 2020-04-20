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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * The MXBean to transfer data.
 */
@SuppressWarnings("nls")
public class DataTransferMXBeanImpl implements DataTransferMXBean {

    /*
     * @see DataTransferMXBean#read(String, int, int)
     */
    @Override
    public byte[] read(String fileName, int pos, int maxSize) {
        byte[] results = new byte[0];

        File file = new File(fileName);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            Agent.logError(new Exception(), Messages.CANNOT_READ_FILE, fileName);
            return results;
        }

        RandomAccessFile randomAccessFile = null;
        try {
            byte[] bytes = new byte[maxSize];

            randomAccessFile = new RandomAccessFile(file, "r");
            randomAccessFile.seek(pos);
            int size = randomAccessFile.read(bytes);

            if (size != -1) {
                results = Arrays.copyOf(bytes, size);
            }
        } catch (IOException e) {
            Agent.logError(e, Messages.CANNOT_READ_FILE, fileName);
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                // do nothing
            }
        }

        return results;
    }

    /*
     * @see DataTransferMXBean#getVersion()
     */
    @Override
    public String getVersion() {
        return Constants.VERSION;
    }
}
