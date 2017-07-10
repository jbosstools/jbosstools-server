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

public interface AttributeCheckingMBean {

    void setAnAttribute(String attributeValue);
    String getAnAttribute();
    void setASecondAttribute(String attributeValue);
    String getASecondAttribute();
	int getAnIntAttribute();
	void setAnIntAttribute(int anIntAttribute);
	Date getADateAttribute();
	void setADateAttribute(Date aDateAttribute);
	long getALongAttribute();
	void setALongAttribute(long aLongAttribute);
}
