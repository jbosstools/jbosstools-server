/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.ide.eclipse.as.classpath.core.ejb3;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.classpath.core.runtime.WebtoolsProjectJBossClasspathContainerInitializer.WebtoolsProjectJBossClasspathContainer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class EJB30SupportVerifier {
	public static boolean verify(IRuntime rt) {
		IJBossServerRuntime ajbsr = (IJBossServerRuntime)rt.loadAdapter(IJBossServerRuntime.class, null);
		//		 i refuse to verify. if they say they support, believe them
		if( ajbsr == null ) return true;  

		// one of ours. verify
		IPath path = new Path("org.jboss.ide.eclipse.as.core.runtime.ProjectInitializer");
		path = path.append(rt.getId()).append("jst.ejb").append("3.0");

		WebtoolsProjectJBossClasspathContainer container =
			new WebtoolsProjectJBossClasspathContainer(path);
		IClasspathEntry[] entries = container.getClasspathEntries();
		if( entries.length == 0 ) return false;
		IPath p;
		for( int i = 0; i < entries.length; i++ ) {
			p = entries[i].getPath();
			if( !p.toFile().exists())
				return false;
		}
		return true;
	}
}
