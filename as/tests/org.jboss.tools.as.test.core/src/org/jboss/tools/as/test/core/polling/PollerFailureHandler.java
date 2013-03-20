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
package org.jboss.tools.as.test.core.polling;

import java.util.List;

import org.jboss.ide.eclipse.as.core.server.INeedCredentials;
import org.jboss.ide.eclipse.as.core.server.IProvideCredentials;
import org.jboss.ide.eclipse.as.core.server.IServerProvider;

public class PollerFailureHandler implements IProvideCredentials {

	public PollerFailureHandler() {
	}

	@Override
	public boolean accepts(IServerProvider serverProvider,
			List<String> requiredProperties) {
		int size = requiredProperties.size();
		if( size <= 2 ) 
			return false;
		if( size > 3 ) 
			return false;
		return true;
	}

	@Override
	public void handle(INeedCredentials inNeed, List<String> requiredProperties) {
	}

}
