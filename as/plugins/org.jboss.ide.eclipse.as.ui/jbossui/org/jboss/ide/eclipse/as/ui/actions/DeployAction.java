/**
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
package org.jboss.ide.eclipse.as.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
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
public class DeployAction implements IObjectActionDelegate {

	protected ISelection selection;
	private Shell shell;
	
	public DeployAction() {
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
		action.setEnabled(verifyEnablement());
		action.setText(getText(verifyType()));
	}

	protected boolean verifyEnablement() {
		if( selection instanceof IStructuredSelection ) {
			IStructuredSelection sel = (IStructuredSelection)selection;
			if( sel.isEmpty())
				return false;
			Iterator i = sel.iterator();
			while(i.hasNext())
				if( !(i.next() instanceof IFile ))
					return false;
		}
		return true;
	}

	protected String getText(boolean type) {
		if( type )
			return Messages.ActionDelegateMakeUndeployable;
		return Messages.ActionDelegateMakeDeployable;
	}
	
	// True if we want to unpublish, false if we wantt to publish
	protected boolean verifyType() {
		if( selection instanceof IStructuredSelection ) {
			IStructuredSelection sel = (IStructuredSelection)selection;
			if( sel.isEmpty())
				return false;
			Iterator i = sel.iterator();
			Object o;
			while(i.hasNext()) {
				o = i.next();
				if( !(o instanceof IFile) || SingleDeployableFactory.findModule(((IFile)o).getFullPath()) == null)
					return false;
			}
		}
		return true;
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		if(targetPart!=null && targetPart.getSite()!=null) {
			shell = targetPart.getSite().getShell();
		}
	}

	public void run(IAction action) {
		if( verifyType())
			makeUndeployable();
		else
			makeDeployable();
	}
	
	protected void makeDeployable() {
		IStructuredSelection sel2 = (IStructuredSelection)selection;
		Object[] objs = sel2.toArray();
		IModule[] modules = new IModule[objs.length];
		for( int i = 0; i < objs.length; i++ ) {
			SingleDeployableFactory.makeDeployable(((IFile)objs[i]).getFullPath());
			modules[i] = SingleDeployableFactory.findModule(((IFile)objs[i]).getFullPath());
		}
		
		tryToPublish();
	}

	protected void makeUndeployable() {
		IStructuredSelection sel2 = (IStructuredSelection)selection;
		Object[] objs = sel2.toArray();
		ArrayList<IPath> paths = new ArrayList<IPath>();
		for( int i = 0; i < objs.length; i++ )
			if(objs[i] instanceof IFile )
				paths.add(((IFile)objs[i]).getFullPath());
		new UndeployFromServerJob(paths).schedule();
	}
	
	protected void tryToPublish() {
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
			if( server == null ) {
				errorStatus = new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, Messages.ActionDelegateDeployableServersNotFoundTitle);
				errorTitle = Messages.ActionDelegateCannotPublish;
				errorMessage = Messages.ActionDelegateDeployableServersNotFoundDesc;
			} else if( objs == null || !allFiles(objs) ) {
				errorStatus = new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, Messages.ActionDelegateFileResourcesOnly);
				errorTitle = Messages.ActionDelegateCannotPublish;
				errorMessage = Messages.ActionDelegateFileResourcesOnly;
			} else {
				IModule[] modules = new IModule[objs.length];
				for( int i = 0; i < objs.length; i++ ) {
					modules[i] = SingleDeployableFactory.findModule(((IFile)objs[i]).getFullPath());
				}
				try {
					IServerWorkingCopy copy = server.createWorkingCopy();
					copy.modifyModules(modules, new IModule[0], new NullProgressMonitor());
					IServer saved = copy.save(false, new NullProgressMonitor()); 
					new PublishServerJob(saved).schedule();
				} catch( CoreException ce ) {
					errorStatus = new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, Messages.ActionDelegatePublishFailed, ce);
					errorTitle = Messages.ActionDelegateCannotPublish;
					errorMessage = Messages.ActionDelegatePublishFailed;
				}
			}
		}
		if( errorStatus != null ) {
			ErrorDialog dialog = new ErrorDialog(new Shell(), errorTitle, errorMessage, errorStatus, 0xFFFF);
			dialog.open();
		}
	}
	
	protected boolean allFiles(Object[] objs) {
		for( int i = 0; i < objs.length; i++ ) 
			if( !(objs[i] instanceof IFile) )
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
	
	
}
