/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.server.internal.extendedproperties;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;

public class Wildfly90ExtendedProperties extends Wildfly80ExtendedProperties {
	public Wildfly90ExtendedProperties(IAdaptable obj) {
		super(obj);
	}
	@Override
	public String getRuntimeTypeVersionString() {
		return "9.x"; //$NON-NLS-1$
	}
	
	@Override
	public String getJMXUrl() {
			return getJMXUrl(9990, "service:jmx:http-remoting-jmx"); //$NON-NLS-1$
	}
	
	@Override
	public IExecutionEnvironment getDefaultExecutionEnvironment() {
		return JavaRuntime.getExecutionEnvironmentsManager().getEnvironment("JavaSE-1.8"); //$NON-NLS-1$
	}
}
