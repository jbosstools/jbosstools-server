/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    "Rob Stryker" <rob.stryker@redhat.com> - Initial implementation
 *******************************************************************************/

package org.jboss.tools.jmx.ui.internal.actions;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.action.Action;
import org.jboss.tools.jmx.core.ConnectJob;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.ui.JMXUIActivator;
import org.jboss.tools.jmx.ui.Messages;
import org.jboss.tools.jmx.ui.internal.JMXImages;
import org.jboss.tools.usage.event.UsageEventType;
import org.jboss.tools.usage.event.UsageReporter;

/**
 * The connect action
 */
public class MBeanServerConnectAction extends Action {
	private static UsageEventType EVENT_TYPE  = null;
	
	private IConnectionWrapper[] connection;
    public MBeanServerConnectAction(IConnectionWrapper[] wrapper) {
        super(Messages.MBeanServerConnectAction_text, AS_PUSH_BUTTON);
        JMXImages.setLocalImageDescriptors(this, "attachAgent.gif"); //$NON-NLS-1$
        this.connection = wrapper;
    }

	public void run() {
		if( connection != null ) {
			logJmxUsage(connection);
			new ConnectJob(connection).schedule();
		}
    }
	
	private void logJmxUsage(IConnectionWrapper[] connection) {
		if( EVENT_TYPE == null ) {
			EVENT_TYPE = new UsageEventType("jmx", UsageEventType.getVersion(JMXUIActivator.getDefault()), 
					null, "jmx_connect", "JMX Provider type", 
					"JMX Provider type");
			UsageReporter.getInstance().registerEvent(EVENT_TYPE);
		}
		
		try {
			String providers = Arrays.asList(connection).stream().map(val -> {
				return val == null || val.getProvider() == null || val.getProvider().getId() == null ? "null" : val.getProvider().getId();
			}).collect(Collectors.joining(","));
			UsageReporter.getInstance().trackEvent(EVENT_TYPE.event(providers));
		} catch( Throwable t ) {
			// Do nothing
		}
	}

}
