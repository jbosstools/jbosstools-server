/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.rsp.ui.util.ui;

import org.eclipse.swt.widgets.Display;

/**
 * Simple util to run stuff in UI thread
 */
public class UIHelper {
	public static void executeInUI(Runnable runnable) {
		Display.getDefault().asyncExec(runnable);
	}

	public static void executeInUISync(Runnable runnable) {
		Display.getDefault().syncExec(runnable);
	}
}
