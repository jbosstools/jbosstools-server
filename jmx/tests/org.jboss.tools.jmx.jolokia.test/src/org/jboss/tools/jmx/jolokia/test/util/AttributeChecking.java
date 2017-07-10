/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.jmx.jolokia.test.util;

import java.util.Date;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public class AttributeChecking implements AttributeCheckingMBean,MBeanRegistration {

    private String domain;
	private String anAttribute;
	private String aSecondAttribute;
	private int anIntAttribute;
	private long aLongAttribute;
	private Date aDateAttribute;

    public AttributeChecking(String pDomain) {
        domain = pDomain;
    }

    @Override
    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
        return new ObjectName(domain + ":type=attributetest");

    }

    @Override
    public void postRegister(Boolean registrationDone) {
    	/* Do nothing */
    }

    @Override
    public void preDeregister() throws Exception {
    	/* Do nothing */
    }

    @Override
    public void postDeregister() {
    	/* Do nothing */
    }

	@Override
	public void setAnAttribute(String attributeValue) {
		this.anAttribute = attributeValue;
	}
	
	@Override
	public String getAnAttribute() {
		return anAttribute;
	}

	@Override
	public void setASecondAttribute(String attributeValue) {
		this.aSecondAttribute = attributeValue;
	}

	@Override
	public String getASecondAttribute() {
		return aSecondAttribute;
	}

	@Override
	public int getAnIntAttribute() {
		return anIntAttribute;
	}

	@Override
	public void setAnIntAttribute(int anIntAttribute) {
		this.anIntAttribute = anIntAttribute;
	}

	@Override
	public Date getADateAttribute() {
		return aDateAttribute;
	}

	@Override
	public void setADateAttribute(Date aDateAttribute) {
		this.aDateAttribute = aDateAttribute;
	}

	@Override
	public long getALongAttribute() {
		return aLongAttribute;
	}

	@Override
	public void setALongAttribute(long aLongAttribute) {
		this.aLongAttribute = aLongAttribute;
	}

}
