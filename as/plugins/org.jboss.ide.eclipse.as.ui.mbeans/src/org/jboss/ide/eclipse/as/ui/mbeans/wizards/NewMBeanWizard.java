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
package org.jboss.ide.eclipse.as.ui.mbeans.wizards;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.Resources;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.dialogs.TextFieldNavigationHandler;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.wst.sse.core.internal.encoding.CommonEncodingPreferenceNames;
import org.eclipse.wst.xml.core.internal.XMLCorePlugin;
import org.eclipse.wst.xml.ui.internal.wizards.NewModelWizard;
import org.jboss.ide.eclipse.as.ui.mbeans.Messages;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class NewMBeanWizard extends NewModelWizard implements INewWizard {

	private IStructuredSelection sel;
	private MBeanInterfacePage interfacePage;
	private MBeanPage mbeanPage;
	private NewFilePageExtension newFilePage;
	private static final String INTERFACE_NAME = "__INTERFACE_NAME__"; //$NON-NLS-1$
	
	public NewMBeanWizard() {
	   setWindowTitle(Messages.NewMBeanWizard_WindowTitle);
	}

    public void createPageControls(Composite pageContainer) {
    	super.createPageControls(pageContainer);
    	newFilePage.setVisible(false);
    }

    // TODO:  Clean up after 158060, 153135
	public class NewFilePageExtension extends NewFilePage {
		private IFile newFile;
		private IPath fullPath;
		public NewFilePageExtension(IStructuredSelection selection) {
			super(selection);
		}
		
	    public IFile createNewFile() {
	        if (newFile != null) {
				return newFile;
			}

	        // create the new file and cache it if successful

	        final IPath containerPath = getContainerFullPath();
	        IPath newFilePath = containerPath.append(getFileName());
	        final IFile newFileHandle = createFileHandle(newFilePath);
	        final InputStream initialContents = getInitialContents();

	        createLinkTarget();
	        WorkspaceModifyOperation op = new WorkspaceModifyOperation(createRule(newFileHandle)) {
	            protected void execute(IProgressMonitor monitor)
	                    throws CoreException {
	                try {
	                    monitor.beginTask(IDEWorkbenchMessages.WizardNewFileCreationPage_progress, 2000);
	                    ContainerGenerator generator = new ContainerGenerator(
	                            containerPath);
	                    generator.generateContainer(new SubProgressMonitor(monitor,
	                            1000));
	                    createFile(newFileHandle, initialContents,
	                            new SubProgressMonitor(monitor, 1000));
	                } finally {
	                    monitor.done();
	                }
	            }
	        };

	        try {
	            getContainer().run(true, true, op);
	        } catch (InterruptedException e) {
	            return null;
	        } catch (InvocationTargetException e) {
	            if (e.getTargetException() instanceof CoreException) {
	                ErrorDialog
	                        .openError(
	                                getContainer().getShell(), // Was Utilities.getFocusShell()
	                                IDEWorkbenchMessages.WizardNewFileCreationPage_errorTitle,
	                                null, // no special message
	                                ((CoreException) e.getTargetException())
	                                        .getStatus());
	            } else {
	                // CoreExceptions are handled above, but unexpected runtime exceptions and errors may still occur.
	                IDEWorkbenchPlugin.log(getClass(),
	                        "createNewFile()", e.getTargetException()); //$NON-NLS-1$
	                MessageDialog
	                        .openError(
	                                getContainer().getShell(),
	                                IDEWorkbenchMessages.WizardNewFileCreationPage_internalErrorTitle, NLS.bind(IDEWorkbenchMessages.WizardNewFileCreationPage_internalErrorMessage, e.getTargetException().getMessage()));
	            }
	            return null;
	        }

	        newFile = newFileHandle;

	        return newFile;
	    }	
	    public void setContainerFullPath(IPath path) {
	    	fullPath = path;
	    }
	    public IPath getContainerFullPath() {
	    	return fullPath == null ? super.getContainerFullPath() : fullPath;
	    }

	}
	
	public boolean performFinish() {
		if( !canFinish() ) return false;
		
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					
					monitor.beginTask(Messages.NewMBeanWizard_CreatingTaskName, 100);
					SubProgressMonitor interfaceMonitor = new SubProgressMonitor(monitor, 40);
					SubProgressMonitor mbeanMonitor = new SubProgressMonitor(monitor, 40);
					SubProgressMonitor xmlMonitor = new SubProgressMonitor(monitor, 20);
					
					
					interfacePage.createType(interfaceMonitor);
					mbeanPage.createType(mbeanMonitor);
					
					xmlMonitor.beginTask(Messages.NewMBeanWizard_XMLTaskName, 1);
					if( mbeanPage.shouldCreateDescriptor()) {
						newFilePage.setFileName(mbeanPage.getCreatedType().getElementName() + "-service.xml"); //$NON-NLS-1$
						
						IPath fullPath = newFilePage.getContainerFullPath();

						IPath newPath = new Path(fullPath.segment(0)).append("META-INF"); //$NON-NLS-1$
						createContainer(newPath);
						
						newFilePage.setContainerFullPath(newPath);
						
						IFile newFile = newFilePage.createNewFile();
						createStubServiceDescriptor(newFile);
					} else {
					}
					xmlMonitor.worked(1);
					xmlMonitor.done();
				} catch( Throwable jme) {
					jme.printStackTrace();
				}
				
				monitor.done();
			}
		};
		try {
			new ProgressMonitorDialog(new Shell()).run(false, true, op);
		} catch( Exception e) {
			e.printStackTrace();
		}

		
		return true;
	}
	
	protected void createContainer(final IPath containerPath) {
		IFile fileHandle = IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getFile(containerPath);
        WorkspaceModifyOperation op = new WorkspaceModifyOperation(createRule(fileHandle)) {
            protected void execute(IProgressMonitor monitor)
                    throws CoreException {
                try {
                    ContainerGenerator generator = new ContainerGenerator(containerPath);
                    generator.generateContainer(new NullProgressMonitor());
                } catch( Exception e ) {
                }
            }
        };
        
        try {
        	getContainer().run(true, true, op);
        } catch( Exception e) {}
	}
    protected ISchedulingRule createRule(IResource resource) {
		IResource parent = resource.getParent();
    	while (parent != null) {
    		if (parent.exists()) {
				return resource.getWorkspace().getRuleFactory().createRule(resource);
			}
    		resource = parent;
    		parent = parent.getParent();
    	}
		return resource.getWorkspace().getRoot();
	}


	public void init(IWorkbench workbench, IStructuredSelection selection) {
		sel = selection;
	}

	public void addPages() {
		interfacePage = new MBeanInterfacePage();
		mbeanPage = new MBeanPage();
		addPage(interfacePage);
		addPage(mbeanPage);
		interfacePage.init(sel);
		mbeanPage.init(sel);
		
		
		newFilePage = new NewFilePageExtension(sel);
		Preferences preference = XMLCorePlugin.getDefault().getPluginPreferences();
		String ext = "xml"; //$NON-NLS-1$
		newFilePage.defaultFileExtension = "."+ext; //$NON-NLS-1$
		newFilePage.filterExtensions = new String[] {"*.xml"}; //$NON-NLS-1$
		addPage(newFilePage);
		// 
	}
	
	private class MBeanInterfacePage extends NewTypeWizardPage {

		private StringDialogField fMBeanNameDialogField;
		private StringDialogField fMBeanInterfaceNameDialogField;
		private IStatus fMBeanNameStatus;
		
		
		public MBeanInterfacePage() {
			super(false, Messages.NewMBeanInterface);
			
			setTitle(Messages.NewMBeanInterface); 
			setDescription(Messages.NewMBeanInterfaceDesc); 

			
			fMBeanNameDialogField= new StringDialogField();
			fMBeanNameDialogField.setDialogFieldListener(new MBeanPage1DialogFieldAdapter());
			fMBeanNameDialogField.setLabelText(Messages.NewMBeanName); 

			fMBeanInterfaceNameDialogField= new StringDialogField();
			fMBeanInterfaceNameDialogField.setDialogFieldListener(new MBeanPage1DialogFieldAdapter());
			fMBeanInterfaceNameDialogField.setLabelText(Messages.NewMBeanInterfaceName); 
		}

		private class MBeanPage1DialogFieldAdapter implements IDialogFieldListener {
			public void dialogFieldChanged(DialogField field) {
				String fieldName = null;
				if( field == fMBeanNameDialogField ) {
					String txt = fMBeanNameDialogField.getText();
					fMBeanInterfaceNameDialogField.getTextControl(null).setText(txt + "MBean"); //$NON-NLS-1$
					fieldName = INTERFACE_NAME;
					fTypeNameStatus = typeNameChanged(getTypeName());
					fMBeanNameStatus = typeNameChanged(fMBeanNameDialogField.getText());
				}
				
				handleFieldChanged(fieldName);
				
			}
		}
		public void createControl(Composite parent) {
		      this.initializeDialogUnits(parent);

		      Composite composite = new Composite(parent, SWT.NONE);

		      int nColumns = 4;

		      GridLayout layout = new GridLayout();
		      layout.numColumns = nColumns;
		      composite.setLayout(layout);

		      this.createContainerControls(composite, nColumns);
		      this.createPackageControls(composite, nColumns);
		      this.createSeparator(composite, nColumns);
		      this.createMBeanNameControls(composite, nColumns);
		      this.createTypeNameControls(composite, nColumns);
		      this.createSuperClassControls(composite, nColumns);
		      this.createSuperInterfacesControls(composite, nColumns);
		      
		      fMBeanInterfaceNameDialogField.getTextControl(null).setEditable(false);

		      
		      this.setControl(composite);
		} 
		
		public String getTypeName() {
			return fMBeanInterfaceNameDialogField.getText();
		}

		
		protected void createTypeNameControls(Composite composite, int nColumns) {
			fMBeanInterfaceNameDialogField.doFillIntoGrid(composite, nColumns - 1);
			DialogField.createEmptySpace(composite);
			
			Text text= fMBeanInterfaceNameDialogField.getTextControl(null);
			LayoutUtil.setWidthHint(text, getMaxFieldWidth());
			TextFieldNavigationHandler.install(text);
		}

		
		protected void createMBeanNameControls(Composite composite, int nColumns) {
			fMBeanNameDialogField.doFillIntoGrid(composite, nColumns - 1);
			DialogField.createEmptySpace(composite);
			
			Text text= fMBeanNameDialogField.getTextControl(null);
			LayoutUtil.setWidthHint(text, getMaxFieldWidth());
			TextFieldNavigationHandler.install(text);
		}


		public void init(IStructuredSelection selection) {
			IJavaElement jelem= getInitialJavaElement(selection);
			initContainerPage(jelem);
			initTypePage(jelem);
		}
		
		protected void handleFieldChanged(String fieldName) {
			super.handleFieldChanged(fieldName);
			
			if( INTERFACE_NAME.equals(fieldName)) {
				mbeanPage.setMBeanName(fMBeanNameDialogField.getText());
			}
			doStatusUpdate();
		}

		// ------ validation --------
		private void doStatusUpdate() {
			// status of all used components
			IStatus[] status= new IStatus[] {
				fContainerStatus,
				fPackageStatus, 
				fTypeNameStatus,
				fMBeanNameStatus,
				fSuperClassStatus,
				fSuperInterfacesStatus
			};
			
			// the mode severe status will be displayed and the OK button enabled/disabled.
			updateStatus(status);
		}

		
		protected IStatus typeNameChanged(String typeNameWithParameters) {
			StatusInfo status= new StatusInfo();
			IType currType = null;


			// must not be empty
			if (typeNameWithParameters.length() == 0) {
				status.setError(NewWizardMessages.NewTypeWizardPage_error_EnterTypeName); 
				return status;
			}
			
			String typeName= getTypeNameWithoutParameters(typeNameWithParameters);
			if (typeName.indexOf('.') != -1) {
				status.setError(NewWizardMessages.NewTypeWizardPage_error_QualifiedName); 
				return status;
			}
			IStatus val= JavaConventions.validateJavaTypeName(typeName);
			if (val.getSeverity() == IStatus.ERROR) {
				status.setError(
						org.eclipse.jdt.internal.corext.util.Messages.format(NewWizardMessages.NewTypeWizardPage_error_InvalidTypeName, val.getMessage())); 
				return status;
			} else if (val.getSeverity() == IStatus.WARNING) {
				status.setWarning(org.eclipse.jdt.internal.corext.util.Messages.format(NewWizardMessages.NewTypeWizardPage_warning_TypeNameDiscouraged, val.getMessage())); 
				// continue checking
			}		

			// must not exist
			IPackageFragment pack= getPackageFragment();
			if (pack != null) {
				ICompilationUnit cu= pack.getCompilationUnit(getCompilationUnitName(typeName));
				currType= cu.getType(typeName);
				IResource resource= cu.getResource();

				if (resource.exists()) {
					status.setError(NewWizardMessages.NewTypeWizardPage_error_TypeNameExists + "(" + typeName + ")");  //$NON-NLS-1$ //$NON-NLS-2$
					return status;
				}
				URI location= resource.getLocationURI();
				if (location != null) {
					try {
						IFileStore store= EFS.getStore(location);
						if (store.fetchInfo().exists()) {
							status.setError(NewWizardMessages.NewTypeWizardPage_error_TypeNameExistsDifferentCase); 
							return status;
						}
					} catch (CoreException e) {
						status.setError(org.eclipse.jdt.internal.corext.util.Messages.format(
							NewWizardMessages.NewTypeWizardPage_error_uri_location_unkown, 
							Resources.getLocationString(resource)));
					}
				}
			}
			
			if (typeNameWithParameters != typeName) {
				IPackageFragmentRoot root= getPackageFragmentRoot();
				if (root != null) {
					if (!JavaModelUtil.is50OrHigher(root.getJavaProject())) {
						status.setError(NewWizardMessages.NewTypeWizardPage_error_TypeParameters); 
						return status;
					}
					String typeDeclaration= "class " + typeNameWithParameters + " {}"; //$NON-NLS-1$//$NON-NLS-2$
					ASTParser parser= ASTParser.newParser(AST.JLS3);
					parser.setSource(typeDeclaration.toCharArray());
					parser.setProject(root.getJavaProject());
					CompilationUnit compilationUnit= (CompilationUnit) parser.createAST(null);
					IProblem[] problems= compilationUnit.getProblems();
					if (problems.length > 0) {
						status.setError(org.eclipse.jdt.internal.corext.util.Messages.format(NewWizardMessages.NewTypeWizardPage_error_InvalidTypeName, problems[0].getMessage())); 
						return status;
					}
				}
			}
			return status;
		}
		
		private String getTypeNameWithoutParameters(String typeNameWithParameters) {
			int angleBracketOffset= typeNameWithParameters.indexOf('<');
			if (angleBracketOffset == -1) {
				return typeNameWithParameters;
			} else {
				return typeNameWithParameters.substring(0, angleBracketOffset);
			}
		}

	
	}
	
	private class MBeanPage extends NewTypeWizardPage {

		private StringDialogField fMBeanNameDialogField;
		private SelectionButtonDialogField fDescriptorDialogField;
		public MBeanPage() {
			super(true, Messages.NewMBeanClass);
			
			setTitle(Messages.NewMBeanClass); 
			setDescription(Messages.MBeanClassDescription); 

			MBeanPage2DialogFieldAdapter adapter = new MBeanPage2DialogFieldAdapter();
			fMBeanNameDialogField= new StringDialogField();
			fMBeanNameDialogField.setDialogFieldListener(adapter);
			fMBeanNameDialogField.setLabelText(Messages.NewMBeanName); 
			
			fDescriptorDialogField = new SelectionButtonDialogField(SWT.CHECK);
			fDescriptorDialogField.setDialogFieldListener(adapter);
			fDescriptorDialogField.setLabelText(Messages.MBeanServiceXML);
		}

		private class MBeanPage2DialogFieldAdapter implements IDialogFieldListener {
			public void dialogFieldChanged(DialogField field) {
			}
		}

		public void createControl(Composite parent) {
		      this.initializeDialogUnits(parent);

		      Composite composite = new Composite(parent, SWT.NONE);

		      int nColumns = 4;

		      GridLayout layout = new GridLayout();
		      layout.numColumns = nColumns;
		      composite.setLayout(layout);

		      this.createContainerControls(composite, nColumns);
		      this.createPackageControls(composite, nColumns);
		      this.createSeparator(composite, nColumns);
		      this.createMBeanTypeNameControls(composite, nColumns);
		      this.createSuperClassControls(composite, nColumns);
		      this.createSuperInterfacesControls(composite, nColumns);
		      this.createSeparator(composite, nColumns);
		      this.createDescriptorControls(composite, nColumns);
		      
		      fMBeanNameDialogField.getTextControl(null).setEditable(false);

		      this.setControl(composite);
		}
		
		public List getSuperInterfaces() {
			List interfaces = super.getSuperInterfaces();
			if( interfacePage.getCreatedType() != null ) {
				IType t = interfacePage.getCreatedType();
				interfaces.add(t.getFullyQualifiedName());
			}
			return interfaces;
		}

		protected void createDescriptorControls(Composite composite, int nColumns) {
			fDescriptorDialogField.doFillIntoGrid(composite, nColumns);
		}
		
		protected void createMBeanTypeNameControls(Composite composite, int nColumns) {
			fMBeanNameDialogField.doFillIntoGrid(composite, nColumns - 1);
			DialogField.createEmptySpace(composite);
			
			Text text= fMBeanNameDialogField.getTextControl(null);
			LayoutUtil.setWidthHint(text, getMaxFieldWidth());
			TextFieldNavigationHandler.install(text);
		}

		
		public String getTypeName() {
			return fMBeanNameDialogField.getText();
		}

		public void setMBeanName(String s) {
			fMBeanNameDialogField.getTextControl(null).setText(s);
		}
		
		public void init(IStructuredSelection selection) {
			IJavaElement jelem= getInitialJavaElement(selection);
			initContainerPage(jelem);
			initTypePage(jelem);
		}
		
		protected void createTypeMembers(IType type, ImportsManager imports, IProgressMonitor monitor) throws CoreException {
			createInheritedMethods(type, true, true, imports, new SubProgressMonitor(monitor, 1));
		}

		public boolean shouldCreateDescriptor() {
			return fDescriptorDialogField.isSelected();
		}
		
		public IWizardPage getNextPage() {
			return null;
		}
	}
	
	
	private void createStubServiceDescriptor(IFile newFile) throws Exception {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			String charSet = getUserPreferredCharset();

			PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, charSet));
			writer.println("<?xml version=\"1.0\" encoding=\"" + charSet + "\"?>"); //$NON-NLS-1$ //$NON-NLS-2$
			writer.println("<server>"); //$NON-NLS-1$
			writer.println("\t<mbean code=\"" + mbeanPage.getCreatedType().getFullyQualifiedName() + "\" name=\"your.domain:key=value\">"); //$NON-NLS-1$ //$NON-NLS-2$
			writer.println("\t</mbean>"); //$NON-NLS-1$
			writer.println("</server>"); //$NON-NLS-1$
			writer.flush();
			outputStream.close();

			ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
			newFile.setContents(inputStream, true, true, null);
			inputStream.close();
	}
	
	private String getUserPreferredCharset() {
		Preferences preference = XMLCorePlugin.getDefault().getPluginPreferences();
		String charSet = preference.getString(CommonEncodingPreferenceNames.OUTPUT_CODESET);
		return charSet;
	}


}
