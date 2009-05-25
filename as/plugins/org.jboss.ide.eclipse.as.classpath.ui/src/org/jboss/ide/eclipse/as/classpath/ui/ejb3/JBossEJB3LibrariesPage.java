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
package org.jboss.ide.eclipse.as.classpath.ui.ejb3;

import java.text.MessageFormat;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.jboss.ide.eclipse.as.classpath.core.ejb3.EJB30SupportVerifier;
import org.jboss.ide.eclipse.as.classpath.core.ejb3.EJB3ClasspathContainer;
import org.jboss.ide.eclipse.as.classpath.ui.ClasspathUIPlugin;
import org.jboss.ide.eclipse.as.classpath.ui.Messages;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class JBossEJB3LibrariesPage extends JBossSelectionPage implements IClasspathContainerPage
{

   private IClasspathEntry classpathEntry;

   public JBossEJB3LibrariesPage() {
      super();
      setTitle(Messages.ejb3ClasspathPageTitle);
      setDescription(Messages.ejb3ClasspathPageDescription);
      
   }

   private boolean jbossServerHasEJB3(JBossServer jbossServer) {
	   return EJB30SupportVerifier.verify(jbossServer.getServer().getRuntime());
   }

   public boolean finish() {

      if (jbossServer != null) {
         if (jbossServerHasEJB3(jbossServer)) {
            classpathEntry = JavaCore.newContainerEntry(new Path(EJB3ClasspathContainer.CONTAINER_ID)
                  .append(jbossServer.getServer().getName()), true);
            return true;
         } else {
            ClasspathUIPlugin
                  .error(MessageFormat
						.format(
								Messages.JBossEJB3LibrariesPage_ConfigurationDoesNotContainEJB3Libraries,
								jbossServer.getServer().getName()));
         }
      }

      return false;
   }

   public boolean isPageComplete()  {
      return jbossServer != null && isCurrentPage();
   }

   public IClasspathEntry getSelection() {
      return classpathEntry;
   }

   public void setSelection(IClasspathEntry containerEntry)  {
      classpathEntry = containerEntry;
   }

}
