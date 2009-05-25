/*
 * JBoss, a division of Red Hat
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
package org.jboss.ide.eclipse.as.ui.mbeans.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.jboss.ide.eclipse.as.ui.mbeans.Messages;
import org.jboss.ide.eclipse.as.ui.mbeans.wizards.pages.NewSessionBeanWizardPage;

/**
 * @author Marshall
 */
public class NewSessionBeanWizard extends NewElementWizard
{

   NewSessionBeanWizardPage page;

   private IStructuredSelection selection;

   public NewSessionBeanWizard ()
   {
	   super();
	   setDefaultPageImageDescriptor(JavaPluginImages.DESC_WIZBAN_NEWCLASS);
	   setWindowTitle(Messages.NewSessionBeanWizard_WindowTitle);
   }
   
   public void init(IWorkbench workbench, IStructuredSelection selection)
   {
      page = new NewSessionBeanWizardPage();
      this.selection = selection;
      
      super.init(workbench, selection);
   }

   public boolean performFinish()
   {
      warnAboutTypeCommentDeprecation();
      boolean res = super.performFinish();
      if (res)
      {
         IResource resource = page.getModifiedResource();
         if (resource != null)
         {
            selectAndReveal(resource);
            openResource((IFile) resource);
         }
      }
      return true;
   }

   public void addPages()
   {
      addPage(page);
      page.init(selection);
   }

   protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException
   {
      page.createType(monitor);
   }

   public IJavaElement getCreatedElement()
   {
      return page.getCreatedType();
   }
}
