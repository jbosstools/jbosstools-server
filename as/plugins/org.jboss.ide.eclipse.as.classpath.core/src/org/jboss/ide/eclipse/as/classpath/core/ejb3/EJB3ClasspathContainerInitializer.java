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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author Marshall
 * @author Rob Stryker
 */
public class EJB3ClasspathContainerInitializer extends ClasspathContainerInitializer {

   public void initialize(IPath containerPath, IJavaProject project) throws CoreException {
      String containerId = containerPath.segment(0);
      IClasspathContainer container = null;

      if (containerId.equals(EJB3ClasspathContainer.CONTAINER_ID)) {
         container = new EJB3ClasspathContainer(containerPath, project);
      }

      if (container != null)
         JavaCore.setClasspathContainer(containerPath, 
        		 new IJavaProject[] {project}, 
        		 new IClasspathContainer[] {container}, null);
   }

   public String getDescription(IPath containerPath, IJavaProject project) {
      String containerId = containerPath.segment(0);
      if (containerId.equals(EJB3ClasspathContainer.CONTAINER_ID)) {
         return EJB3ClasspathContainer.DESCRIPTION;
      }

      return ""; //$NON-NLS-1$
   }
}
