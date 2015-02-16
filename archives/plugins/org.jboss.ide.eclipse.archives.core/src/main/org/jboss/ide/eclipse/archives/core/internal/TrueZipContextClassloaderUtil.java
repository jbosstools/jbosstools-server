/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.core.internal;

/**
 * Class used to store the plugin's bundlewiring classloader
 * if run from eclipse, or return the current thread's context classloader in other situations
 */
public class TrueZipContextClassloaderUtil {
	private static TrueZipContextClassloaderUtil util;
	public static TrueZipContextClassloaderUtil getDefault() {
		if( util == null ) {
			util = new TrueZipContextClassloaderUtil();
		}
		return util;
	}
	
	private IClassLoaderProvider provider = null;
	TrueZipContextClassloaderUtil() {
		// Do nothing
	}
	public void setProvider(IClassLoaderProvider provider) {
		this.provider = provider;
	}
	public ClassLoader getClassLoader() {
		if( provider != null ) {
			return provider.getClassLoader();
		}
		// Should only happen from ant
		return Thread.currentThread().getContextClassLoader();
	}
}
