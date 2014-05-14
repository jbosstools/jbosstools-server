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

	@Override
	public int compareTo(Object o) {
		return 0;
	}


}
