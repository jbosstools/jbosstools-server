/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.util;

import org.eclipse.debug.core.model.IProcess;

/**
 * @Rob Stryker
 * @author André Dietisheim
 */
public class ThreadUtils {

	private static final int SLEEP_DELAY = 200;

	/**
	 * Sleeps the current thread for the given amount of milliseconds. InterruptedException are swallowed.
	 * 
	 * @param delay
	 */
	public static void sleepFor(int delay /* in ms */) {
		int x = 0;
		while( x < delay) {
			x += SLEEP_DELAY;
			try {
				Thread.sleep(SLEEP_DELAY);
			} catch(InterruptedException ie) {
			}
		}
	}

	public static void sleepWhileRunning(IProcess process) {
		while( !process.isTerminated()) {
			try {
				Thread.yield();
				Thread.sleep(SLEEP_DELAY);
			} catch(InterruptedException ie) {
			}
		}
	}
	
}
