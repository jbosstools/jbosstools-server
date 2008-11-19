/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jboss.tools.jmx.core.tree;

import org.jboss.tools.jmx.core.IConnectionWrapper;


public class Root extends Node {

    private IConnectionWrapper connection;
    Root(IConnectionWrapper connection) {
        super(null);
        this.connection = connection;
    }

    @Override
    public String toString() {
        return "Root"; //$NON-NLS-1$
    }

    public int compareTo(Object o) {
        return 0;
    }

    public IConnectionWrapper getConnection() {
    	return connection;
    }
}
