/******************************************************************************* 
 * Copyright (c) 2018 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.as.jmx.ui.bot.itests.util;

import org.eclipse.reddeer.common.util.Display;
import org.eclipse.reddeer.junit.requirement.RequirementException;
import org.eclipse.ui.PlatformUI;

/**
 * Utility class
 * @author odockal
 *
 */
public class JMXUtils {

	public static void closeAllEditors() {
		Display.syncExec(new Runnable() {

			@Override
			public void run() {
				boolean result = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().closeAllEditors(true);
				
				if (!result){
					throw new RequirementException("Some editors remained open");
				}
			}
		});
	}

}
