/*******************************************************************************
 * Copyright (c) 2007 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.jboss.tools.jmx.ui.test.interactive;

import java.lang.management.ManagementFactory;

import javax.management.ObjectName;
import javax.management.StandardMBean;

public class Registration extends StandardMBean implements RegistrationMBean {

    private ObjectName tempOn;

    public Registration() throws Exception {
        super(RegistrationMBean.class);
        tempOn = ObjectName.getInstance("org.jboss.tools.jmx.test:Type=Temporary");
    }

    public void registerTemporary() throws Exception {
        Temporary temp = new Temporary();
        ManagementFactory.getPlatformMBeanServer().registerMBean(temp, tempOn);
    }

    public void unregisterTemporary() throws Exception {
        ManagementFactory.getPlatformMBeanServer().unregisterMBean(tempOn);
    }

}
