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

public class TelemetryService {
//
//    public enum TelemetryResult {
//        SUCCESS, ERROR, ABORTED
//    }
//    public static final String TELEMETRY_START_RSP = "server.startRSP";
//    public static final String TELEMETRY_DISCONNECT_RSP = "server.disconnectRSP";
//    public static final String TELEMETRY_STOP_RSP = "server.stopRSP";
//    public static final String TELEMETRY_SERVER_START = "server.start";
//    public static final String TELEMETRY_SERVER_STOP = "server.stop";
//    public static final String TELEMETRY_SERVER_REMOVE = "server.remove";
//    public static final String TELEMETRY_SERVER_CREATE = "server.create";
//    public static final String TELEMETRY_SERVER_OUTPUT = "server.output";
//    public static final String TELEMETRY_SERVER_RESTART = "server.restart";
//    public static final String TELEMETRY_DEPLOYMENT_ADD = "server.addDeployment";
//    public static final String TELEMETRY_DEPLOYMENT_REMOVE = "server.removeDeployment";
//    public static final String TELEMETRY_PUBLISH = "server.publish";
//    public static final String TELEMETRY_DOWNLOAD_RUNTIME = "server.add.download";
//
//    public static final String TELEMETRY_DOWNLOAD_RSP = "server.rsp.download";
//    public static final String TELEMETRY_SERVER_ACTION = "server.actions";
//    public static final String TELEMETRY_SERVER_EDIT = "server.editServer";
//    public static final String TELEMETRY_RUN_ON_SERVER = "server.runOnServer";
//
//
//    private static TelemetryService instance;
//
//    private final Lazy<TelemetryMessageBuilder> builder = new Lazy<>(() -> new TelemetryMessageBuilder(TelemetryService.class.getClassLoader()));
//
//    private TelemetryService() {
//        // prevent instantiation
//    }
//
//    public static TelemetryService instance() {
//        if (instance == null) {
//            instance = new TelemetryService();
//        }
//        return instance;
//    }
//
//    public TelemetryMessageBuilder getBuilder(){
//        return instance.builder.get();
//    }
//
//    public void send(String actionName, String[] keys, String[] values) {
//        sendWithType(actionName, null, null, null, new String[0], new String[0]);
//    }
//    public void send(String actionName, Status s) {
//        sendWithType(actionName, null, s, null, new String[0], new String[0]);
//    }
//    public void send(String actionName, Throwable t) {
//        sendWithType(actionName, null, null, t, new String[0], new String[0]);
//    }
//    public void sendWithType(String actionName, Status status, String type, String[] keys, String[] values) {
//        sendWithType(actionName, type, status, null, keys, values);
//    }
//    public void sendWithType(String actionName, String type, Throwable t, String[] keys, String[] values) {
//        sendWithType(actionName, type, null, t, keys, values);
//    }
//    public void sendWithType(String actionName, String type, Status status, Throwable t, String[] keys, String[] values) {
//
//        TelemetryMessageBuilder.ActionMessage msg = generateTelemetry(actionName, keys, values);
//        if( type != null ) {
//            msg.property("type", type);
//        }
//        if( t != null ) {
//            msg.property("throwableError", t.getMessage());
//        }
//        if( status != null ) {
//            msg.property("statusOk", Boolean.toString(status.isOK()));
//            if( !status.isOK()) {
//                msg.property("statusMsg", status.getMessage());
//            }
//        }
//        sendMsg(msg);
//    }
//    public void sendWithType(String actionName, String type, Throwable t) {
//        sendWithType(actionName, type, null, t, new String[]{}, new String[]{});
//    }
//    public void sendWithType(String actionName, String type, Status status) {
//        sendWithType(actionName, type, status, null, new String[]{}, new String[]{});
//    }
//    public void sendWithType(String actionName, String type) {
//        sendMsg(generateTelemetry(actionName, new String[]{"type"}, new String[]{type}));
//    }
//
//    public void sendMsg(TelemetryMessageBuilder.ActionMessage msg) {
//        try {
//            msg.send();
//        } catch( RuntimeException e) {
//            if (e.getMessage() != null) {
//                msg.error(e).send();
//            } else {
//                msg.error(e.toString()).send();
//            }
//        }
//    }
//    public TelemetryMessageBuilder.ActionMessage generateTelemetry(String actionName, String[] keys, String[] values) {
//        TelemetryMessageBuilder.ActionMessage telemetry = TelemetryService.instance().getBuilder().action(actionName);
//        if( keys != null && values != null ) {
//            for (int i = 0; i < keys.length; i++) {
//                String k = keys[i];
//                if (values.length >= i) {
//                    telemetry.property(k, values[i]);
//                } else {
//                    telemetry.property(k, "MISSING");
//                }
//            }
//        }
//        return telemetry;
//    }
//    public void getExampleDeleteMe() {
//        String[] keys = {KUBERNETES_VERSION, IS_OPENSHIFT, OPENSHIFT_VERSION};
//        String[] vals = {"1.2.3", "true", "6.9.420"};
//        sendTelemetry("server.startRSP", keys, vals);
//    }
}