package org.jboss.ide.eclipse.archives.webtools.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.wst.common.componentcore.internal.DependencyType;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;
import org.jboss.ide.eclipse.archives.webtools.filesets.vcf.WorkspaceFilesetVirtualComponent;
import org.jboss.ide.eclipse.as.wtp.ui.propertypage.NewReferenceWizard;

public class FilesetReferenceWizardFragment extends WizardFragment {
	public boolean hasComposite() {
		return true;
	}
	
	private boolean hasEntered = false;
	public boolean isComplete() {
		return hasEntered;
	}
	
	private Text incText, excText, rootText;
	private String includes, excludes, folder;
	public Composite createComposite(Composite parent, IWizardHandle handle) {
		hasEntered = true;
		handle.setTitle("Add a fileset"); //$NON-NLS-1$
		handle.setDescription("This will let you select a root folder and some matching patterns"); //$NON-NLS-1$
		
		Composite child = new Composite(parent, SWT.NONE);
		
		// root folder, text, browse
		child.setLayout(new FormLayout());
		Control top = null;
		addLabel("Root Folder", child, top); //$NON-NLS-1$
		top = rootText = addText(child, top, true);
		addLabel("Includes", child, top); //$NON-NLS-1$
		top = incText = addText(child, top, false);
		addLabel("Excludes", child, top); //$NON-NLS-1$
		top = excText = addText(child, top, false);
		ModifyListener listener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				textModified();
			}
		};
		IProject p = (IProject)getTaskModel().getObject(NewReferenceWizard.PROJECT);
		rootText.setText(p.getFullPath().toString());
		incText.setText("**"); //$NON-NLS-1$
		rootText.addModifyListener(listener);
		incText.addModifyListener(listener);
		excText.addModifyListener(listener);
		textModified();
		return child; 
	}
	
	protected void textModified() {
		includes = incText.getText();
		excludes = excText.getText();
		folder = rootText.getText();
	}
	
	protected Text addText(Composite parent, Control top, boolean includeBrowse) {
		Text t = new Text(parent, SWT.BORDER);
		Button b = null;
		if( includeBrowse ) {
			b = new Button(parent, SWT.NONE);
			b.setText("Browse..."); //$NON-NLS-1$
			addBrowseListener(b,t);
			FormData bData = new FormData();
			bData.right = new FormAttachment(100,-5);
			bData.top = top == null ? new FormAttachment(0, 5) : new FormAttachment(top, 5);
			b.setLayoutData(bData);
		}
		FormData fd = new FormData();
		fd.top = top == null ? new FormAttachment(0,5) : new FormAttachment(top, 5);
		fd.left = new FormAttachment(25, 0);
		fd.right = b == null ? new FormAttachment(100, -5) : new FormAttachment(b, -5);
		t.setLayoutData(fd);
		return t;
	}
	
	protected void addBrowseListener(final Button b, final Text t) {
		b.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				browsePressed(b, t);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	protected void browsePressed(Button b, Text t) {
		// show the dialog, then set the proper text
		ContainerSelectionDialog d = new ContainerSelectionDialog(b.getShell(), null, 
				false, "Please select a root folder"); //$NON-NLS-1$
		if(d.open() == Dialog.OK) {
			Object[] o = d.getResult();
			if( o != null && o.length > 0) { 
				IPath path = (IPath)o[0];
				t.setText(path.toString());
			}
		}
	}
	
	protected Label addLabel(String text, Composite parent, Control top) {
		Label l = new Label(parent, SWT.NONE);
		l.setText(text);
		FormData fd = new FormData();
		fd.left = new FormAttachment(0, 5);
		fd.top = top == null ? new FormAttachment(0,5) : new FormAttachment(top, 5);
		l.setLayoutData(fd);
		return l;
	}
	
	public void performFinish(IProgressMonitor monitor) throws CoreException {
		getTaskModel().putObject(NewReferenceWizard.COMPONENT, getFilesetComponent());
		getTaskModel().putObject(NewReferenceWizard.COMPONENT_PATH, "/"); //$NON-NLS-1$
		getTaskModel().putObject(NewReferenceWizard.DEPENDENCY_TYPE, DependencyType.CONSUMES_LITERAL);
	}
	
	protected IVirtualComponent getFilesetComponent() {
		IVirtualComponent root = (IVirtualComponent)getTaskModel().getObject(NewReferenceWizard.ROOT_COMPONENT);
		WorkspaceFilesetVirtualComponent vc = new WorkspaceFilesetVirtualComponent(
				root.getProject(), root, new Path(folder).makeAbsolute().toString()); 
		vc.setIncludes(includes);
		vc.setExcludes(excludes);
		return vc;
	}

}
