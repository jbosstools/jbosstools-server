/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.jboss.tools.jmx.ui.test.interactive;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public interface ComplexTypeMBean {
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    Map getMap();

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    HashMap getHashMap();

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    Collection getCollection();

    Properties getSystemProperties();

}
