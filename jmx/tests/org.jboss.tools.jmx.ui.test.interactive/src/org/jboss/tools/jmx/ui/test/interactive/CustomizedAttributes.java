/*******************************************************************************
 * Copyright (c) 2007 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.jboss.tools.jmx.ui.test.interactive;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

public class CustomizedAttributes extends StandardMBean implements CustomizedAttributesMBean {

    public CustomizedAttributes() throws NotCompliantMBeanException {
        super(CustomizedAttributesMBean.class);
    }

    public String getGreenString() {
        return "must be displayed in green in details section"; //$NON-NLS-1$
    }

    public String getRedString() {
        return "must be displayed in red in details section"; //$NON-NLS-1$
    }

    public String getUnmodifiedString() {
        return "must be displayed in normal color (black) in details section"; //$NON-NLS-1$
    }

}
