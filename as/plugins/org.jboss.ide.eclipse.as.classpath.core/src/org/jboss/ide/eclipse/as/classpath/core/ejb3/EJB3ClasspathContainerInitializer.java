/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
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

      return "";
   }
}
