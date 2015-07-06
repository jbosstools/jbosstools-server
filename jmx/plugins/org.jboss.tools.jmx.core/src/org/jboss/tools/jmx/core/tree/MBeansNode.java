/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.jmx.core.tree;



public class MBeansNode extends Node {

	public MBeansNode(Node parent) {
		super(parent);
	}

	@Override
	public String toString() {
		return "MBeans"; //$NON-NLS-1$
	}

	public boolean equals(Object o) {
		return o instanceof MBeansNode && compareTo(o) == 0;
	}
	
	public int hashCode() {
		if( getConnection() != null && getConnection().getProvider() != null ) {
			return ("MBeansNode" + getConnection().getProvider().getName(getConnection())).hashCode();
		}
		return super.hashCode();
	}
	
	@Override
	public int compareTo(Object o) {
		if( o instanceof MBeansNode ) {
			Root r = ((MBeansNode)o).getRoot();
			if( r.getConnection() != null && getConnection() != null ) {
				if( r.getConnection().getProvider() != null && getConnection().getProvider() != null ) {
					String oName = r.getConnection().getProvider().getName(r.getConnection());
					return getConnection().getProvider().getName(getConnection()).compareTo(oName);
				}
			}
		}
		return 0;
	}


}
