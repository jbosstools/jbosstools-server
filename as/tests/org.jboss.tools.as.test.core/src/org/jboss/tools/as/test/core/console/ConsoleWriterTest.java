/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.console;

import org.jboss.ide.eclipse.as.core.server.IServerConsoleWriter;
import org.jboss.ide.eclipse.as.wtp.core.console.ServerConsoleModel;

import junit.framework.TestCase;

public class ConsoleWriterTest extends TestCase {
	public void testConsoleWriter() {
		IServerConsoleWriter listener = ServerConsoleModel.getDefault().getConsoleWriter();
		assertNotNull(listener);
	}
}
