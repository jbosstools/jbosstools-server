/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jboss.tools.jmx.core.tree;

public class DomainNode extends Node {

    private String domain;

    DomainNode(Node root, String domain) {
        super(root);
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }

    @Override
    public String toString() {
        return "DomainNode[domain=" + domain + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((domain == null) ? 0 : domain.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final DomainNode other = (DomainNode) obj;
        if (domain == null) {
        	return other.domain == null;
        } else if (!domain.equals(other.domain))
            return false;
        return true;
    }

    public int compareTo(Object o) {
        DomainNode other = (DomainNode) o;
        return domain.compareTo(other.domain);
    }

}
