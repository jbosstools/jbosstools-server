/*
 * JBoss by Red Hat
 * Copyright 2006-2009, Red Hat Middleware, LLC, and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.ui.mbeans.wizards.pages;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;
import org.eclipse.ui.help.WorkbenchHelp;
import org.jboss.ide.eclipse.as.ui.mbeans.Messages;

/**
 * @author Marshall
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class NewMessageDrivenBeanWizardPage extends NewTypeWizardPage
{
   private IStructuredSelection selection;

   public NewMessageDrivenBeanWizardPage()
   {
      super(true, Messages.NewMessageDrivenBeanWizardPage_Name);
      setTitle(Messages.NewMessageBeanWizardMessage);
      setDescription(Messages.NewMessageBeanWizardDescription);
   }

   public void createControl(Composite parent)
   {
      initializeDialogUnits(parent);

      Composite composite = new Composite(parent, SWT.NONE);
      int nColumns = 4;

      GridLayout layout = new GridLayout();
      layout.numColumns = nColumns;
      composite.setLayout(layout);

      createContainerControls(composite, nColumns);
      createEnclosingTypeControls(composite, nColumns);
      createSeparator(composite, nColumns);

      createPackageControls(composite, nColumns);
      createTypeNameControls(composite, nColumns);

      createSeparator(composite, nColumns);

      createModifierControls(composite, nColumns);

      createSuperClassControls(composite, nColumns);
      createSuperInterfacesControls(composite, nColumns);

      createSeparator(composite, nColumns);

      setControl(composite);

      Dialog.applyDialogFont(composite);
      WorkbenchHelp.setHelp(composite, IJavaHelpContextIds.NEW_CLASS_WIZARD_PAGE);

      ArrayList superInterfaces = new ArrayList();
      superInterfaces.add("javax.jms.MessageListener"); //$NON-NLS-1$
      setSuperInterfaces(superInterfaces, true);
   }

   public void init(IStructuredSelection selection)
   {
      this.selection = selection;

      IJavaElement element = getInitialJavaElement(selection);
      initContainerPage(element);
      initTypePage(element);
   }

   public void createType(IProgressMonitor monitor) throws CoreException, InterruptedException
   {
      super.createType(monitor);
      IType createdBeanType = getCreatedType();

      ICompilationUnit beanUnit = createdBeanType.getCompilationUnit();

      Document doc = new Document(beanUnit.getSource());

      ASTParser c = ASTParser.newParser(AST.JLS3);
      c.setSource(beanUnit.getSource().toCharArray());
      c.setResolveBindings(true);
      CompilationUnit beanAstUnit = (CompilationUnit) c.createAST(null);
      AST ast = beanAstUnit.getAST();
      beanAstUnit.recordModifications();

      ImportDeclaration importDecl = ast.newImportDeclaration();
      importDecl.setOnDemand(false);
      importDecl.setName(ast.newName(new String[]
      {"javax", "ejb", "MessageDriven"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      beanAstUnit.imports().add(importDecl);

      MarkerAnnotation sessionAnnotation = ast.newMarkerAnnotation();
      sessionAnnotation.setTypeName(ast.newSimpleName("MessageDriven")); //$NON-NLS-1$
      TypeDeclaration type = (TypeDeclaration) beanAstUnit.types().get(0);
      type.modifiers().add(sessionAnnotation);

      TextEdit edit = beanAstUnit.rewrite(doc, null);
      try
      {
         UndoEdit undo = edit.apply(doc);
         String source = doc.get();
         beanUnit.getBuffer().setContents(source);
         beanUnit.getBuffer().save(monitor, true);

      }
      catch (BadLocationException e)
      {
         e.printStackTrace();
      }
   }

   protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
         throws CoreException
   {
      super.createTypeMembers(newType, imports, monitor);

      createInheritedMethods(newType, false, true, imports, new SubProgressMonitor(monitor, 1));
   }
}
