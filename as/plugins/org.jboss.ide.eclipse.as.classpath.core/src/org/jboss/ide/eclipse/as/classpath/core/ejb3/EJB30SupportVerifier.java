/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.classpath.core.ejb3;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.classpath.core.runtime.WebtoolsProjectJBossClasspathContainerInitializer.WebtoolsProjectJBossClasspathContainer;
import org.jboss.ide.eclipse.as.core.server.internal.AbstractJBossServerRuntime;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class EJB30SupportVerifier {
	public static boolean verify(IRuntime rt) {
		AbstractJBossServerRuntime ajbsr = (AbstractJBossServerRuntime)rt.loadAdapter(AbstractJBossServerRuntime.class, null);
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
