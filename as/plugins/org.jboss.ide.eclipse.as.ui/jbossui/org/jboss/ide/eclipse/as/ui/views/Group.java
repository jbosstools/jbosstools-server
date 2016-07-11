/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.views;

import java.io.PrintWriter;

/**
 * This class is marked for deletion. There is no alternative. 
 * Please use the official error log. 
 */
@Deprecated
public class Group extends AbstractEntry {

	private String name;

	public Group(String name) {
		this.name = name;
	}

	public void write(PrintWriter writer) {
		Object[] children = getChildren(null);
		for (int i = 0; i < children.length; i++) {
			AbstractEntry entry = (AbstractEntry) children[i];
			entry.write(writer);
			writer.println();
		}
	}

	public String toString() {
		return name;
	}

}
