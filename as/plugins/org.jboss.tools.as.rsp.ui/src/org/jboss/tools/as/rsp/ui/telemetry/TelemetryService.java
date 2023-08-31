/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.rsp.ui.telemetry;

import java.util.HashMap;
import java.util.Map;

import org.jboss.tools.as.rsp.ui.RspUiActivator;
import org.jboss.tools.usage.event.UsageEventType;
import org.jboss.tools.usage.event.UsageReporter;

public class TelemetryService {
    private static UsageEventType createType(String key, String v) {
    	UsageEventType e = new UsageEventType(RspUiActivator.USAGE_COMPONENT_NAME, 
    			UsageEventType.getVersion(RspUiActivator.getDefault()), 
				null, key, v);
    	return e;
    }

    private static UsageEventType createType(String key, String labelDesc, String valDesc) {
    	UsageEventType e = new UsageEventType(RspUiActivator.USAGE_COMPONENT_NAME, 
    			UsageEventType.getVersion(RspUiActivator.getDefault()), 
				null, key, labelDesc, valDesc);
    	return e;
    }

    public static final UsageEventType TELEMETRY_PLUGIN_START = createType("server.startBundle", "RSP UI Bundle started");
    public static final UsageEventType TELEMETRY_START_RSP = createType("server.startRSP", "RSP start event", "Error Count"); 
    public static final UsageEventType TELEMETRY_DISCONNECT_RSP = createType("server.disconnectRSP", "RSP Disconnect event", "Error Count");
    public static final UsageEventType TELEMETRY_STOP_RSP = createType("server.stopRSP", "RSP stop event", "Error Count");
    public static final UsageEventType TELEMETRY_SERVER_START = createType("server.start", "Server start", "Error Count");
    public static final UsageEventType TELEMETRY_SERVER_STOP = createType("server.stop", "Server stop", "Error Count");
    public static final UsageEventType TELEMETRY_SERVER_REMOVE = createType("server.remove", "Server remove", "Error Count");
    public static final UsageEventType TELEMETRY_SERVER_CREATE = createType("server.create", "Create server", "Error Count");
    public static final UsageEventType TELEMETRY_SERVER_OUTPUT = createType("server.output", "Server Output", "Error Count");
    public static final UsageEventType TELEMETRY_SERVER_RESTART = createType("server.restart", "Server restart", "Error Count");
    public static final UsageEventType TELEMETRY_DEPLOYMENT_ADD = createType("server.addDeployment", "Add Deployment", "Error Count");
    public static final UsageEventType TELEMETRY_DEPLOYMENT_REMOVE = createType("server.removeDeployment", "Remove Deployment", "Error Count");
    public static final UsageEventType TELEMETRY_PUBLISH = createType("server.publish", "Publish Server", "Error Count");
    public static final UsageEventType TELEMETRY_DOWNLOAD_RUNTIME = createType("server.add.download", "Download Runtime workflow", "Error Count");

    public static final UsageEventType TELEMETRY_DOWNLOAD_RSP = createType("server.rsp.download", "Download RSP", "Error Count");
    public static final UsageEventType TELEMETRY_SERVER_ACTION = createType("server.actions", "Run Server Action", "Error Count");
    public static final UsageEventType TELEMETRY_SERVER_EDIT = createType("server.editServer", "Edit Server", "Error Count");
    public static final UsageEventType TELEMETRY_RUN_ON_SERVER = createType("server.runOnServer", "Run on Server", "Error Count");
    
    
    public static final void logEvent(UsageEventType type, String label) {
    	logEvent(type, label, 0);
    }
    public static final void logEvent(UsageEventType type, String label, int value) {
		UsageReporter.getInstance().trackEvent(type.event(label, value));
    }
    
    public static final void logEvent(String actionName, String label, String desc) {
    	UsageEventType e = new UsageEventType(RspUiActivator.USAGE_COMPONENT_NAME, 
    			UsageEventType.getVersion(RspUiActivator.getDefault()), 
				null, actionName, label, desc);
		UsageReporter.getInstance().registerEvent(e);
    }
}