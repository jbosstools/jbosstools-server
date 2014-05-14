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

package org.jboss.tools.jmx.ui;

import org.eclipse.swt.graphics.Image;

/**
 * An interface for any tree node that wishes to provide an image.
 * This may be useful for an IConnectionWrapper that wishes to have 
 * a variable image depending on what it discovers about its connection,
 * rather than one static image for the connection type. 
 */
public interface ImageProvider {

	Image getImage();

}
