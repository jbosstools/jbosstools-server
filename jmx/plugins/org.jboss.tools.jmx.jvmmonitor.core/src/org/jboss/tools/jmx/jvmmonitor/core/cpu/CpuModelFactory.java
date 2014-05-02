/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jboss.tools.jmx.jvmmonitor.core.cpu;

import org.jboss.tools.jmx.jvmmonitor.internal.core.cpu.CpuModel;

/**
 * The CPU model factory.
 */
public class CpuModelFactory {

    /**
     * Creates the CPU model.
     * 
     * @return The CPU model
     */
    public ICpuModel createCpuModel() {
        return new CpuModel();
    }
}
