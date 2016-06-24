/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.wtp.core.server.launch;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.wtp.core.Messages;

/**
 * A fake process attached to launch when there is no real process running when the server
 * is launched. This is required because the Eclipse launch/debug framework does not allow
 * terminate commands on launch without process.
 * This is inspired by org.eclipse.wst.server.ui.internal.actions.RunOnServerProcess.
 * The state of this object is linked to the state of the corresponding object.
 * 
 * @author Jeff Maury
 *
 */
public class ServerProcess implements IProcess, IServerListener {
    protected ILaunch launch;
    protected IServer server;
    private   String  type;

    public ServerProcess(ILaunch launch, IServer server, String type) {
        this.launch = launch;
        this.server = server;
        server.addServerListener(this, ServerEvent.SERVER_CHANGE | ServerEvent.STATE_CHANGE);
        this.type = type;
    }
    
    public ServerProcess(ILaunch launch, IServer server) {
        this(launch, server, Messages.server);
    }

    /**
     * Fires a creation event.
     */
    protected void fireCreationEvent() {
        fireEvent(new DebugEvent(this, DebugEvent.CREATE));
    }

    /**
     * Fires the given debug event.
     *
     * @param event debug event to fire
     */
    protected void fireEvent(DebugEvent event) {
        DebugPlugin manager= DebugPlugin.getDefault();
        if (manager != null) {
            manager.fireDebugEventSet(new DebugEvent[]{event});
        }
    }

    /**
     * Fires a terminate event.
     */
    protected void fireTerminateEvent() {
        fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
    }

    @Override
    public String getAttribute(String key) {
        return null;
    }

    @Override
    public int getExitValue() throws DebugException {
        return 0;
    }

    /**
     * Return the pattern used to generate the label.
     * 
     * @return the string pattern
     */
    protected String getPattern() {
        String pattern = Messages.RunOnMessage;
        String mode = getLaunch().getLaunchMode();
        if (ILaunchManager.PROFILE_MODE.equals(mode)) {
            pattern = Messages.ProfileOnMessage;
        } else if (ILaunchManager.DEBUG_MODE.equals(mode)) {
            pattern = Messages.DebugOnMessage;
        }
        return pattern;
    }
    
    @Override
    public String getLabel() {
        return NLS.bind(getPattern(), type);
    }

    @Override
    public ILaunch getLaunch() {
        return launch;
    }

    @Override
    public IStreamsProxy getStreamsProxy() {
        return null;
    }

    @Override
    public void setAttribute(String key, String value) {
    }

    @Override
    public <T> T getAdapter(Class<T> arg0) {
        return null;
    }

    @Override
    public boolean canTerminate() {
        return server.getServerState() == IServer.STATE_STARTED;
    }

    @Override
    public boolean isTerminated() {
        return server.getServerState() == IServer.STATE_STOPPED;
    }

    @Override
    public void terminate() throws DebugException {
        server.stop(true);
    }

    @Override
    public void serverChanged(ServerEvent event) {
        if (event.getState() == IServer.STATE_STARTED) {
            fireCreationEvent();
        } else if (event.getState() == IServer.STATE_STOPPED) {
            fireTerminateEvent();
            server.removeServerListener(this);
        }
        
    }
}