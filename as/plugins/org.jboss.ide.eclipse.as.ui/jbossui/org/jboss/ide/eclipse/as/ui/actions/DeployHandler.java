/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.PublishServerJob;
import org.eclipse.wst.server.ui.internal.ImageResource;
import org.jboss.ide.eclipse.as.core.modules.SingleDeployableFactory;
import org.jboss.ide.eclipse.as.core.modules.SingleDeployableFactory.UndeployFromServerJob;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.Messages;

/**
 * 
 * @author Rob Stryker
 *
 */
public class DeployHandler extends AbstractHandler {

	protected Shell shell;
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		shell = HandlerUtil.getActiveShell(event);
		ISelection selection = getSelection(event);
		
		makeDeployable(selection);
		return null;
	}
	
	public ISelection getSelection(ExecutionEvent event) {
		return getSelectionFromEvent(event);
	}
	
	protected void makeDeployable(ISelection selection) {
		IStructuredSelection sel2 = (IStructuredSelection)selection;
		Object[] objs = sel2.toArray();
		IModule[] modules = new IModule[objs.length];
		HashSet<IProject> alreadyDeployable = new HashSet<IProject>();
		for( int i = 0; i < objs.length; i++ ) {
			IProject p=null;
			if(objs[i] instanceof IResource){
				p = ((IResource)objs[i]).getProject();
			}
			if(p != null){
				IModule[] mods = ServerUtil.getModules(p);
				if( mods != null && mods.length > 0 && ModuleCoreNature.isFlexibleProject(p))
					alreadyDeployable.add(p);
			}
		}
		
		if( alreadyDeployable.size() > 0 ) {
			if( !showAreYouSureDialog(alreadyDeployable))
				return;
		}
		
		for( int i = 0; i < objs.length; i++ ) {
			if(objs[i] instanceof IResource){
				SingleDeployableFactory.makeDeployable(((IResource)objs[i]).getFullPath());
				modules[i] = SingleDeployableFactory.findModule(((IResource)objs[i]).getFullPath());
			}
		}
		
		tryToPublish(selection);
	}

	private boolean showAreYouSureDialog(HashSet<IProject> set) {
		Iterator<IProject> i = set.iterator();
		String projs = ""; //$NON-NLS-1$
		while(i.hasNext())
			projs += i.next().getName() + ", "; //$NON-NLS-1$
		projs = projs.substring(0, projs.length() - 2);
		
		boolean ret = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(),
				ServerActionMessages.DeployActionMessageBoxTitle, 
				ServerActionMessages.DeployActionMessageBoxMsg);
		return ret;
	}
	
	protected void makeUndeployable(ISelection selection) {
		IStructuredSelection sel2 = (IStructuredSelection)selection;
		Object[] objs = sel2.toArray();
		ArrayList<IPath> paths = new ArrayList<IPath>();
		for( int i = 0; i < objs.length; i++ ){
			if(objs[i] instanceof IResource ){
				paths.add(((IResource)objs[i]).getFullPath());
			}
		}
		new UndeployFromServerJob(paths).schedule();
	}
	
	protected void tryToPublish(ISelection selection) {
		IServer[] deployableServersAsIServers = ServerConverter.getDeployableServersAsIServers();
		if(deployableServersAsIServers.length==0) {
			MessageDialog.openInformation(shell, 
					Messages.ActionDelegateDeployableServersNotFoundTitle,
					Messages.ActionDelegateDeployableServersNotFoundDesc);
		}
		IServer server = getServer(shell,deployableServersAsIServers);
		if(server==null) { // User pressed cancel. 
			return; 
		}
		IStatus errorStatus = null;
		String errorTitle, errorMessage;
		errorTitle = errorMessage = null;

		if( selection instanceof IStructuredSelection ) {
			IStructuredSelection sel2 = (IStructuredSelection)selection;
			Object[] objs = sel2.toArray();
			if( objs == null || !allFiles(objs) ) {
				errorStatus = new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, Messages.ActionDelegateFileResourcesOnly);
				errorTitle = Messages.ActionDelegateCannotPublish;
				errorMessage = Messages.ActionDelegateFileResourcesOnly;
			} else {
				IModule[] modules = new IModule[objs.length];
				for( int i = 0; i < objs.length; i++ ) {
					modules[i] = SingleDeployableFactory.findModule(((IResource)objs[i]).getFullPath());
				}
				try {
					IServerWorkingCopy copy = server.createWorkingCopy();
					copy.modifyModules(modules, new IModule[0], new NullProgressMonitor());
					IServer saved = copy.save(false, new NullProgressMonitor()); 
					new PublishServerJob(saved, IServer.PUBLISH_INCREMENTAL, true).schedule();
				} catch( CoreException ce ) {
					errorStatus = new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, Messages.ActionDelegatePublishFailed, ce);
					errorTitle = Messages.ActionDelegateCannotPublish;
					errorMessage = Messages.ActionDelegatePublishFailed;
				}
			}
		}
		if( errorStatus != null ) {
			ErrorDialog dialog = new ErrorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), errorTitle, errorMessage, errorStatus, 0xFFFF);
			dialog.open();
		}
	}
	
	protected boolean allFiles(Object[] objs) {
		for( int i = 0; i < objs.length; i++ ) 
			if( !(objs[i] instanceof IResource) )
				return false;
		return true;
	}

	
	protected IServer getServer(Shell shell, IServer[] servers) {
		
		if( servers.length == 0 ) return null;
		if( servers.length == 1 ) return servers[0];

		// Throw up a dialog
		SelectServerDialog d = new SelectServerDialog(shell); 
		int result = d.open();
		if( result == Dialog.OK ) {
			return d.getSelectedServer();
		}
		return null;
	}
	
	public class SelectServerDialog extends Dialog {
		private Object selected;
		private final class LabelProviderExtension extends BaseLabelProvider implements ILabelProvider {  
			public Image getImage(Object element) {
				if( element instanceof IServer )
					return ImageResource.getImage(((IServer)element).getServerType().getId());
				return null;
			}
			
			public String getText(Object element) {
				if( element instanceof IServer )
					return ((IServer)element).getName();
				return ""; //$NON-NLS-1$
			}
		}

		protected SelectServerDialog(Shell parentShell) {
			super(parentShell);
		}
		
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(Messages.ActionDelegateSelectServer);
		}

		public IServer getSelectedServer() {
			if( selected instanceof IServer ) 
				return ((IServer)selected);
			return null;
		}
		
		protected Control createDialogArea(Composite parent) {
			Composite c = (Composite)super.createDialogArea(parent);
			Tree tree = new Tree(c, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			tree.setLayoutData(new GridData(GridData.FILL_BOTH));
			final TreeViewer viewer = new TreeViewer(tree);
			viewer.setContentProvider(new ITreeContentProvider() {

				public Object[] getChildren(Object parentElement) {
					return null;
				}
				public Object getParent(Object element) {
					return null;
				}
				public boolean hasChildren(Object element) {
					return false;
				}
				public Object[] getElements(Object inputElement) {
					return ServerConverter.getDeployableServersAsIServers();
				}
				public void dispose() {
				}
				public void inputChanged(Viewer viewer, Object oldInput,
						Object newInput) {
				} 
			});

			viewer.setLabelProvider(new LabelProviderExtension());
			
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					ISelection sel = viewer.getSelection();
					if( sel instanceof IStructuredSelection ) {
						selected = ((IStructuredSelection)sel).getFirstElement();
					}
				} 
			});
			
			viewer.setInput(new Boolean(true));
			return c;
		}
	}
	

	public static ISelection getSelectionFromEvent(ExecutionEvent event){
		ISelection selection = null;
		
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if(part instanceof ITextEditor){
			IEditorInput input = ((ITextEditor)part).getEditorInput();
			if(input instanceof FileEditorInput){
				selection = new StructuredSelection(((FileEditorInput)input).getFile());
			}
		}else{
			selection = HandlerUtil.getCurrentSelection(event);
			Object[] objs = ((IStructuredSelection)selection).toArray();
			for(int i = 0; i < objs.length; i++){
				if(objs[i] instanceof IJavaProject){
					objs[i] = ((IJavaProject)objs[i]).getProject();
				}
			}
			selection = new StructuredSelection(objs);
		}
		return selection;
	}	
}


