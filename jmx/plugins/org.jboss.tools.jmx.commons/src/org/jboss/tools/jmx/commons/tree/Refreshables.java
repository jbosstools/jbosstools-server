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

package org.jboss.tools.jmx.commons.tree;

public class Refreshables {

	/**
	 * Refreshes the object if its refreshable
	 */
	public static void refresh(Object object) {
		if (object instanceof Refreshable) {
			Refreshable refreshable = (Refreshable) object;
			refreshable.refresh();
		}
	}

}
