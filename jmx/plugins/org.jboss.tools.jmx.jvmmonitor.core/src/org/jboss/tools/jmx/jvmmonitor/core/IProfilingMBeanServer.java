/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jboss.tools.jmx.jvmmonitor.core;

import org.jboss.tools.jmx.jvmmonitor.core.cpu.ICpuProfiler.ProfilerState;
import org.jboss.tools.jmx.jvmmonitor.core.mbean.IMBeanServer;

public interface IProfilingMBeanServer extends IMBeanServer {
    public void suspendSampling();
    public void resumeSampling();
    public ProfilerState getProfilerState();
    public Integer getSamplingPeriod();
    public void setSamplingPeriod(Integer i);
}
