/*******************************************************************************
 * Copyright (c) 2007 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.jboss.tools.jmx.ui.test.interactive;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.StandardMBean;

public class NotifEmitter extends StandardMBean implements NotifEmitterMBean,
        NotificationEmitter {

    private boolean emitNotification = false;

    private long sequence = 0;

    private NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

    public NotifEmitter() throws NotCompliantMBeanException {
        super(NotifEmitterMBean.class);
        Thread emitter = new Thread() {
            public void run() {
                while (true) {
                    if (emitNotification) {
                        Notification notification = new Notification("notif",
                                this, sequence, "this is message " + sequence);
                        notification
                                .setSource("org.jboss.tools.jmx.test:type=NotifEmitter");
                        broadcaster.sendNotification(notification);
                    }
                    sequence++;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        emitter.setDaemon(true);
        emitter.start();
    }

    public void startEmit() {
        emitNotification = true;
    }

    public void stopEmit() {
        emitNotification = false;
    }

    public boolean isEmmitting() {
        return emitNotification;
    }

    public void removeNotificationListener(NotificationListener listener,
            NotificationFilter filter, Object handback)
            throws ListenerNotFoundException {
        if( broadcaster != null ) 
        	broadcaster.removeNotificationListener(listener, filter, handback);
    }

    public void addNotificationListener(NotificationListener listener,
            NotificationFilter filter, Object handback)
            throws IllegalArgumentException {
        if( broadcaster != null )
        	broadcaster.addNotificationListener(listener, filter, handback);
    }

    public MBeanNotificationInfo[] getNotificationInfo() {
        return broadcaster == null ? new MBeanNotificationInfo[]{} : broadcaster.getNotificationInfo();
    }

    public void removeNotificationListener(NotificationListener listener)
            throws ListenerNotFoundException {
        if( broadcaster != null )
        	broadcaster.removeNotificationListener(listener);
    }

}
