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
package org.jboss.ide.eclipse.as.ui.marker;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;

public class MarkerResolutionGenerator implements IMarkerResolutionGenerator2 {

	public boolean hasResolutions(IMarker marker) {
		return true;
	}

	public IMarkerResolution[] getResolutions(IMarker marker) {
		return new IMarkerResolution[] { new ConfigureRuntimeMarkerResolution() };
	}

}
