/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.jmx.local.ui;

import org.eclipse.swt.graphics.Image;
import org.jboss.tools.jmx.jvmmonitor.core.IActiveJvm;

/**
 * An interface representing an object capable of 
 * customizing the user-visible strings and icons for 
 * a given jvm connection.
 */
public interface JVMLabelProviderDelegate {
	public abstract boolean accepts(IActiveJvm jvm);
	public abstract Image getImage(IActiveJvm jvm);
	public abstract String getDisplayString(IActiveJvm jvm);

}
