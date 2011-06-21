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

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.ControlContentAssistHelper;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.JavaPackageCompletionProcessor;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;
import org.eclipse.ui.help.WorkbenchHelp;
import org.jboss.ide.eclipse.as.ui.mbeans.Activator;
import org.jboss.ide.eclipse.as.ui.mbeans.Messages;

public class NewSessionBeanWizardPage extends NewTypeWizardPage
{

   public static final String BEAN_PACKAGE_NAME = "NewSessionBeanWizard.beanPackageName"; //$NON-NLS-1$

   public static final String BEAN_NAME = "NewSessionBeanWizard.beanName"; //$NON-NLS-1$

   public static final String REMOTE_INTERFACE_PACKAGE_NAME = "NewSessionBeanWizard.remoteInterfacePackageName"; //$NON-NLS-1$

   public static final String STATEFUL = "Stateful"; //$NON-NLS-1$

   public static final String STATELESS = "Stateless"; //$NON-NLS-1$

   private String remoteInterfacePackage, beanClass, remoteInterfaceName, beanPackage;

   private Label remoteInterfaceNameLabel;

   private Label beanClassName;

   private Text beanNameText;

   private Text beanPackageName;

   private Text remoteInterfacePackageText;

   private JavaPackageCompletionProcessor packageCompletionProcessor;

   private Button statelessButton, statefulButton, useCustomRemoteInterfacePackageButton;

   private boolean useCustomRemoteInterfacePackage;

   private ModifyListener listener;

   private IPackageFragment beanPackageFragment, remoteInterfacePackageFragment;

   private String beanType;

   private IStructuredSelection selection;

   public NewSessionBeanWizardPage()
   {
      super(true, Messages.NewSessionBeanWizardTitle);
      setTitle(Messages.NewSessionBeanWizardMessage);
      setDescription(Messages.NewSessionBeanWizardDescription);
      
      packageCompletionProcessor = new JavaPackageCompletionProcessor();
      beanClass = ""; //$NON-NLS-1$
   }

   public void createControl(Composite parent)
   {
      initializeDialogUnits(parent);

      Composite composite = new Composite(parent, SWT.NONE);
      listener = new ModifyListener()
      {
         public void modifyText(ModifyEvent e)
         {
            updateTypeNames();
            if (e.getSource() == remoteInterfacePackageText)
            {
               handleFieldChanged(REMOTE_INTERFACE_PACKAGE_NAME);
            }
            else if (e.getSource() == beanPackageName)
            {
               handleFieldChanged(BEAN_PACKAGE_NAME);
               if (!useCustomRemoteInterfacePackage)
               {
                  handleFieldChanged(REMOTE_INTERFACE_PACKAGE_NAME);
               }
            }
            else if (e.getSource() == beanNameText)
            {
               handleFieldChanged(BEAN_NAME);
            }
         }
      };

      int nColumns = 4;

      GridLayout layout = new GridLayout();
      layout.numColumns = nColumns;
      composite.setLayout(layout);

      createContainerControls(composite, nColumns);
      createEnclosingTypeControls(composite, nColumns);
      createBeanTypeControls(composite, nColumns);
      createSeparator(composite, nColumns);

      createBeanNameControls(composite, nColumns);
      createRemoteInterfaceControls(composite, nColumns);

      createSeparator(composite, nColumns);

      createModifierControls(composite, nColumns);

      createSuperClassControls(composite, nColumns);
      createSuperInterfacesControls(composite, nColumns);

      setControl(composite);

      IJavaElement element = getInitialJavaElement(selection);
      if (element != null && element.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
         beanPackageName.setText(element.getElementName());
         remoteInterfacePackageText.setText(element.getElementName());
      }

      Dialog.applyDialogFont(composite);
      WorkbenchHelp.setHelp(composite, IJavaHelpContextIds.NEW_CLASS_WIZARD_PAGE);
   }

   protected void createBeanTypeControls(Composite composite, int nColumns)
   {
      SelectionListener listener = new SelectionListener()
      {
         public void widgetDefaultSelected(SelectionEvent e)
         {
            widgetSelected(e);
         }

         public void widgetSelected(SelectionEvent e)
         {
            if (statelessButton.getSelection())
               beanType = STATELESS;
            else
               beanType = STATEFUL;
         }
      };

      Label beanTypeLabel = createLabel(composite, Messages.NewSessionBeanWizardBeanTypeLabel);
      statefulButton = new Button(composite, SWT.RADIO);
      statefulButton.setText(Messages.NewSessionBeanWizardStatefulButtonLabel);
      statefulButton.addSelectionListener(listener);

      statelessButton = new Button(composite, SWT.RADIO);
      statelessButton.setText(Messages.NewSessionBeanWizardStatelessButtonLabel);
      statelessButton.setSelection(true);
      beanType = STATELESS;
      statelessButton.addSelectionListener(listener);
   }

   protected void createBeanNameControls(Composite composite, int nColumns)
   {
      Label sessionPackageNameLabel = createLabel(composite, Messages.NewSessionBeanWizardBeanPackageLabel);
      beanPackageName = new Text(composite, SWT.BORDER);
      beanPackageName.addModifyListener(listener);
      fillAcrossColumns(beanPackageName, nColumns - 1);
      ControlContentAssistHelper.createTextContentAssistant(beanPackageName, packageCompletionProcessor);

      Label beanNameLabel = createLabel(composite, Messages.NewSessionBeanWizardBeanNameLabel);
      beanNameText = new Text(composite, SWT.BORDER);
      beanNameText.addModifyListener(listener);
      fillAcrossColumns(beanNameText, nColumns - 1);

      Label sessionClassNameLabel = createLabel(composite, Messages.NewSessionBeanWizardBeanClassNameLabel);
      beanClassName = createLabelWithImage(composite, "", JavaUI.getSharedImages().getImage( //$NON-NLS-1$
            ISharedImages.IMG_OBJS_CLASS), nColumns - 1);
   }

   protected void createRemoteInterfaceControls(Composite composite, int nColumns)
   {
      //Composite remoteLabelComposite = new Composite(composite, SWT.NONE);
      //remoteLabelComposite.setLayout(new GridLayout(2, false));

      useCustomRemoteInterfacePackageButton = new Button(composite, SWT.CHECK);
      useCustomRemoteInterfacePackageButton.setText(Messages.NewSessionBeanWizardUseCustomInterfacePackageButtonLabel);
      //Label remoteInterfacePackageLabel = createLabel(remoteLabelComposite, "Remote Interface Package:");
      //remoteInterfacePackageLabel.setLayoutData(new GridData(SWT.FILL, GridData.VERTICAL_ALIGN_CENTER, true, false));
      useCustomRemoteInterfacePackageButton.addSelectionListener(new SelectionListener()
      {
         public void widgetDefaultSelected(SelectionEvent e)
         {
            widgetSelected(e);
         }

         public void widgetSelected(SelectionEvent e)
         {
            useCustomRemoteInterfacePackage = !useCustomRemoteInterfacePackage;

            Display.getDefault().asyncExec(new Runnable()
            {
               public void run()
               {
                  if (!useCustomRemoteInterfacePackage)
                  {
                     remoteInterfacePackageText.removeModifyListener(listener);
                  }
                  else
                  {
                     remoteInterfacePackageText.addModifyListener(listener);
                  }

                  remoteInterfacePackageText.setEnabled(useCustomRemoteInterfacePackage);
               }
            });
         }
      });

      remoteInterfacePackageText = new Text(composite, SWT.BORDER);
      remoteInterfacePackageText.setEnabled(false);

      fillAcrossColumns(remoteInterfacePackageText, nColumns - 1);
      ControlContentAssistHelper.createTextContentAssistant(remoteInterfacePackageText, packageCompletionProcessor);

      createLabel(composite, Messages.NewSessionBeanWizardRemoteInterfaceNameLabel);
      remoteInterfaceNameLabel = createLabelWithImage(composite, "", JavaUI.getSharedImages().getImage( //$NON-NLS-1$
            ISharedImages.IMG_OBJS_INTERFACE), nColumns - 1);
   }

   public void createType(IProgressMonitor monitor) throws CoreException, InterruptedException
   {
      super.createType(monitor);

      IType createdBeanType = getCreatedType();
      //			
      //			AST ast = new AST();
      //			MarkerAnnotation annotation = ast.newMarkerAnnotation();
      //			annotation.setTypeName(ast.newSimpleName("Stateless"));
      //			beanType.getCompilationUnit().becomeWorkingCopy()

      ICompilationUnit remoteInterfaceUnit = createRemoteInterface(monitor);
      ICompilationUnit beanUnit = createdBeanType.getCompilationUnit();

      Document doc = new Document(beanUnit.getSource());

      ASTParser c = ASTParser.newParser(AST.JLS3);
      c.setSource(beanUnit.getSource().toCharArray());
      c.setResolveBindings(true);
      CompilationUnit beanAstUnit = (CompilationUnit) c.createAST(null);
      AST ast = beanAstUnit.getAST();
      beanAstUnit.recordModifications();

      ImportDeclaration importDecl = ast.newImportDeclaration();
      importDecl.setName(ast.newName(new String[]
      {"javax", "ejb", beanType})); //$NON-NLS-1$ //$NON-NLS-2$
      importDecl.setOnDemand(false);
      beanAstUnit.imports().add(importDecl);

      importDecl = ast.newImportDeclaration();

      String pkgElements[] = remoteInterfacePackage.split("\\."); //$NON-NLS-1$
      String fullImport[] = new String[pkgElements.length + 1];
      System.arraycopy(pkgElements, 0, fullImport, 0, pkgElements.length);
      fullImport[fullImport.length - 1] = remoteInterfaceName;

      importDecl.setName(ast.newName(fullImport));
      importDecl.setOnDemand(false);
      beanAstUnit.imports().add(importDecl);

      MarkerAnnotation sessionAnnotation = ast.newMarkerAnnotation();
      sessionAnnotation.setTypeName(ast.newSimpleName(beanType));
      TypeDeclaration type = (TypeDeclaration) beanAstUnit.types().get(0);
      type.modifiers().add(sessionAnnotation);

      type.superInterfaceTypes().add(ast.newSimpleType(ast.newSimpleName(remoteInterfaceName)));
      TextEdit edit = beanAstUnit.rewrite(doc, null);
      try
      {
         UndoEdit undo = edit.apply(doc);
         String source = doc.get();
         beanUnit.getBuffer().setContents(source);
         beanUnit.getBuffer().save(monitor, true);

      } catch (MalformedTreeException e) {
			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, MessageFormat.format(Messages.NewSessionBeanWizardPage_could_not_create_type, beanUnit.getElementName()), e);
			Activator.getDefault().getLog().log(status);
	} catch (BadLocationException e) {
		IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, MessageFormat.format(Messages.NewSessionBeanWizardPage_could_not_create_type, beanUnit.getElementName()), e); //$NON-NLS-1$
		Activator.getDefault().getLog().log(status);
	}
   }

   public ICompilationUnit createRemoteInterface(IProgressMonitor monitor) throws CoreException, InterruptedException
   {
      // lame.
      String source = "package " + remoteInterfacePackage + ";\n" + "\n" + "import javax.ejb.Remote;\n" + "\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            + "@Remote\n" + "public interface " + remoteInterfaceName + " {\n" + "\n" + "}\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$


      return remoteInterfacePackageFragment.createCompilationUnit(remoteInterfaceName + ".java", source, true, monitor); //$NON-NLS-1$
      //			AST ast = new AST();
      //			CompilationUnit unit = ast.newCompilationUnit();
      //			PackageDeclaration packageDeclaration = ast.newPackageDeclaration();
      //			packageDeclaration.setName(ast.newSimpleName(remoteInterfacePackageFragment.getElementName()));
      //			unit.setPackage(packageDeclaration);
      //			ImportDeclaration importDecl = ast.newImportDeclaration();
      //			importDecl.setName(ast.newName(new String[] { "javax", "ejb", "Remote"}));
      //			importDecl.setOnDemand(false);
      //			unit.imports().add(importDecl);
      //			
      //			TypeDeclaration type = ast.newTypeDeclaration();
      //			type.setInterface(true);
      //			type.setModifiers(Modifier.PUBLIC);
      //			type.setName(ast.newSimpleName(remoteInterfaceName.getText()));
      //			MarkerAnnotation remoteAnnotation = ast.newMarkerAnnotation();
      //			remoteAnnotation.setTypeName(ast.newSimpleName("Remote"));
      //			type.modifiers().add(remoteAnnotation);
      //			
      //			unit.types().add(type);
      //			System.out.println(ast.toString());

   }

   protected void handleFieldChanged(String fieldName)
   {
      super.handleFieldChanged(fieldName);

      try
      {
         if (fieldName.equals(BEAN_PACKAGE_NAME))
         {
            beanPackageFragment = getPackageFragment(beanPackageName.getText());
            beanPackage = beanPackageName.getText();
            if (!useCustomRemoteInterfacePackage)
            {
               updateRemoteInterfaceValues();
            }
         }
         else if (fieldName.equals(REMOTE_INTERFACE_PACKAGE_NAME))
         {
            updateRemoteInterfaceValues();
         }
         else if (fieldName.equals(BEAN_NAME))
         {
            remoteInterfaceName = beanNameText.getText();
            beanClass = beanNameText.getText() + "Bean"; //$NON-NLS-1$
         }
      }
      catch (CoreException e)
      {
         // Error parsing the text -- just ignore it
      }
   }

   public IPackageFragment getPackageFragment()
   {
      return beanPackageFragment;
   }

   protected IPackageFragment getPackageFragment(String packageName) throws CoreException
   {
      // This was copied over from NewTypeWizardPage.packageChanged, and made more generic so we could use it for multiple package inputs
      StatusInfo status = new StatusInfo();

      if (packageName.length() > 0)
      {
         IStatus val = JavaConventions.validatePackageName(packageName);
         if (val.getSeverity() == IStatus.ERROR)
         {
            status.setError(NewWizardMessages.bind(NewWizardMessages.NewTypeWizardPage_error_InvalidPackageName, val
                  .getMessage()));
            throw new CoreException(status);
         }
         else if (val.getSeverity() == IStatus.WARNING)
         {
            status.setWarning(NewWizardMessages.bind(
                  NewWizardMessages.NewTypeWizardPage_warning_DiscouragedPackageName, val.getMessage())); 
            // continue
         }
      }
      else
      {
         status.setWarning(NewWizardMessages.NewTypeWizardPage_warning_DefaultPackageDiscouraged); 
      }

      IPackageFragmentRoot root = getPackageFragmentRoot();
      if (root != null)
      {
         if (root.getJavaProject().exists() && packageName.length() > 0)
         {
            try
            {
               IPath rootPath = root.getPath();
               IPath outputPath = root.getJavaProject().getOutputLocation();
               if (rootPath.isPrefixOf(outputPath) && !rootPath.equals(outputPath))
               {
                  // if the bin folder is inside of our root, don't allow to name a package
                  // like the bin folder
                  IPath packagePath = rootPath.append(packageName.replace('.', '/'));
                  if (outputPath.isPrefixOf(packagePath))
                  {
                     status.setError(NewWizardMessages.NewTypeWizardPage_error_ClashOutputLocation); 
                     throw new CoreException(status);
                  }
               }
            }
            catch (JavaModelException e)
            {
               JavaPlugin.log(e);
               // let pass			
            }
         }

         return root.getPackageFragment(packageName);
      }
      else
      {
         status.setError(""); //$NON-NLS-1$
      }

      throw new CoreException(status);
   }

   protected IStatus containerChanged()
   {
      IStatus status = super.containerChanged();
      packageCompletionProcessor.setPackageFragmentRoot(getPackageFragmentRoot());

      return status;
   }

   class TypeNameGetter implements Runnable
   {
      public String typeName;

      public TypeNameGetter()
      {
         typeName = ""; //$NON-NLS-1$
      }

      public void run()
      {
         if (beanNameText == null)
         {
            typeName = ""; //$NON-NLS-1$
            return;
         }

         if (!beanNameText.isDisposed())
         {
            typeName = beanNameText.getText() + "Bean"; //$NON-NLS-1$
         }
      }
   }

   public String getTypeName()
   {
      // Overridden so we can provide the type name from our text box instead
      //			TypeNameGetter getter = new TypeNameGetter();
      //			Display.getDefault().asyncExec(getter);
      //			return getter.typeName;

      return beanClass;
   }

   String getClassName(String pkg, String clazz)
   {
      if (pkg != null && pkg.length() > 0)
      {
         return pkg + "." + clazz; //$NON-NLS-1$
      }
      else
         return clazz;
   }

   void updateTypeNames()
   {
      Display.getDefault().asyncExec(new Runnable()
      {
         public void run()
         {
            String packageName = beanPackageName.getText();
            if (!useCustomRemoteInterfacePackage)
            {
               remoteInterfacePackageText.setText(packageName);
            }

            String interfacePackageName = remoteInterfacePackageText.getText();

            String beanName = beanNameText.getText();
            beanClassName.setText(getClassName(packageName, beanName)
                  + (beanName != null && beanName.length() > 0 ? "Bean" : "")); //$NON-NLS-1$ //$NON-NLS-2$
            remoteInterfaceNameLabel.setText(getClassName(interfacePackageName, beanName));

            updateRemoteInterfaceValues();
         }
      });

   }

   void updateRemoteInterfaceValues()
   {
      try
      {
         remoteInterfaceName = beanNameText.getText();
         remoteInterfacePackage = remoteInterfacePackageText.getText();
         remoteInterfacePackageFragment = getPackageFragment(remoteInterfacePackage);
      }
      catch (CoreException e)
      {
		IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, MessageFormat.format(Messages.NewSessionBeanWizardPage_could_not_update_remoteinterface_value, remoteInterfaceName), e);
		Activator.getDefault().getLog().log(status);
      }
   }

   protected void fillAcrossColumns(Control control, int nColumns)
   {
      GridData data = new GridData(SWT.FILL, GridData.VERTICAL_ALIGN_CENTER, true, false);
      data.horizontalSpan = nColumns;

      control.setLayoutData(data);
   }

   protected Label createLabel(Composite parent, String text)
   {
      Label label = new Label(parent, SWT.NONE);
      label.setText(text);
      return label;
   }

   protected Label createLabelWithImage(Composite parent, String text, Image image, int compositeColSpan)
   {
      Composite c = new Composite(parent, SWT.NONE);
      c.setLayout(new GridLayout(2, false));
      fillAcrossColumns(c, compositeColSpan);

      Label imgLabel = new Label(c, SWT.NONE);
      imgLabel.setImage(image);

      Label label = new Label(c, SWT.NONE);
      label.setText(text);

      GridData d = new GridData();
      d.grabExcessHorizontalSpace = true;
      d.horizontalAlignment = SWT.FILL;
      label.setLayoutData(d);
      return label;
   }

   public void init(IStructuredSelection selection)
   {
      this.selection = selection;

      IJavaElement element = getInitialJavaElement(selection);
      initContainerPage(element);
      initTypePage(element);
   }
}