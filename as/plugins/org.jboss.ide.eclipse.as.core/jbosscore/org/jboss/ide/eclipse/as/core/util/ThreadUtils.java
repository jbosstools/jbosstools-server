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

/**
 * @Rob Stryker
 * @author André Dietisheim
 */
public class ThreadUtils {

	/**
	 * Sleeps the current thread for the given amount of milliseconds. InterruptedException are swallowed.
	 * 
	 * @param delay
	 */
	public static void sleepFor(int delay /* in ms */) {
		int x = 0;
		while( x < delay) {
			x+=200;
			try {
				Thread.sleep(200);
			} catch(InterruptedException ie) {
			}
		}
	}
	
}
