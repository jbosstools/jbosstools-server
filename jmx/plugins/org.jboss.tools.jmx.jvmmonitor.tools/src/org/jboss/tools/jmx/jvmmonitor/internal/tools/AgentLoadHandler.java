/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jboss.tools.jmx.jvmmonitor.internal.tools;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.jboss.tools.common.jdt.debug.tools.ToolsCore;
import org.jboss.tools.common.jdt.debug.tools.ToolsCoreException;
import org.jboss.tools.jmx.jvmmonitor.core.IActiveJvm;
import org.jboss.tools.jmx.jvmmonitor.core.IAgentLoadHandler;
import org.jboss.tools.jmx.jvmmonitor.core.JvmCoreException;
import org.jboss.tools.jmx.jvmmonitor.tools.Activator;


/**
 * The agent load handler that loads the agent jar file
 * <tt>lib/jvmmonitor-agent.jar</tt> to target JVM.
 */
public class AgentLoadHandler implements IAgentLoadHandler {

    /** The path for agent jar. */
    private String agentJarPath;

    /** The state indicating if agent is loaded. */
    private boolean isAgentLoaded;

    /**
     * The constructor.
     */
    public AgentLoadHandler() {
        isAgentLoaded = false;
        searchAgentJar();
    }

    /*
     * @see IAgentLoadHandler#loadAgent(IActiveJvm)
     */
    @Override
    public void loadAgent(IActiveJvm jvm) throws JvmCoreException {
        if (agentJarPath == null) {
            return;
        }
        ToolsCore.AttachedVM virtualMachine = null;

        try {
            virtualMachine = ToolsCore.attach(jvm.getPid());
            ToolsCore.loadAgent(virtualMachine, agentJarPath, agentJarPath);
            isAgentLoaded = true;
        } catch (ToolsCoreException e) {
            Activator.log(IStatus.ERROR,
                    NLS.bind(Messages.loadAgentFailedMsg, agentJarPath), e);
        } finally {
            if (virtualMachine != null) {
                try {
                    ToolsCore.detach(virtualMachine);
                } catch (ToolsCoreException e) {
                    // ignore
                }
            }
        }
    }

    /*
     * @see IAgentLoadHandler#isAgentLoaded()
     */
    @Override
    public boolean isAgentLoaded() {
        return isAgentLoaded;
    }

    /**
     * Searches the agent jar file.
     */
    private void searchAgentJar() {
        File agentJar = null;
        try {
            URL entry = org.jboss.tools.jmx.jvmmonitor.core.Activator.getDefault().getBundle()
                    .getEntry(IConstants.JVMMONITOR_AGENT_JAR);
        	URL entryFileUrl = FileLocator.toFileURL(entry);
        	agentJar = new File(entryFileUrl.getPath());
        } catch (IOException e) {
            Activator.log(IStatus.ERROR, Messages.corePluginNoFoundMsg,
                    new Exception(e));
            return;
        }
        if( agentJar != null ) {
	        agentJarPath = agentJar.getAbsolutePath();
	        if (!agentJar.exists()) {
	            Activator.log(
	                    IStatus.ERROR,
	                    NLS.bind(Messages.agentJarNotFoundMsg,
	                            agentJar.getAbsolutePath()), new Exception());
	        } else {
		        Activator.log(IStatus.INFO,
		                NLS.bind(Messages.agentJarFoundMsg, agentJarPath),
		                new Exception());
	        }
        }
    }
}
