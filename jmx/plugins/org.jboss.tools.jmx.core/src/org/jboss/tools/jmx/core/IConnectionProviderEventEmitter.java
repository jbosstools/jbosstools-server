/*******************************************************************************
 * Copyright (c) 2012 Rob Stryker
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.jboss.tools.jmx.core;


public interface IConnectionProviderEventEmitter {
	void fireAdded(IConnectionWrapper wrapper);
	void fireChanged(IConnectionWrapper wrapper);
	void fireRemoved(IConnectionWrapper wrapper);
}
