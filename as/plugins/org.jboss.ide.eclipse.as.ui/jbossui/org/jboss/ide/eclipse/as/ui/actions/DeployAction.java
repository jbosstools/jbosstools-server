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
package org.jboss.ide.eclipse.as.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.eclipse.wst.server.ui.internal.ImageResource;
import org.jboss.ide.eclipse.as.core.modules.SingleDeployableFactory;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.ide.eclipse.as.ui.Messages;

/**
 * 
 * @author Rob Stryker
 *
 */
public class DeployAction implements IObjectActionDelegate {

	protected ISelection selection;
	public DeployAction() {
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void run(IAction action) {
		IServer server = getServer();
		if( selection instanceof IStructuredSelection ) {
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if( server == null ) {
				MessageBox messageBox = new MessageBox (new Shell(), SWT.OK );
				messageBox.setText ("Cannot Publish To Server");
				messageBox.setMessage ("No deployable servers located.");
				messageBox.open();
			} else if( element != null && element instanceof IFile ) {
				IFile tmp = (IFile)element;
				SingleDeployableFactory factory = SingleDeployableFactory.getFactory();
				factory.makeDeployable(tmp.getFullPath());
				IModule module = factory.findModule(tmp.getFullPath());
				DeployableServerBehavior behavior = (DeployableServerBehavior)
					server.loadAdapter(DeployableServerBehavior.class, new NullProgressMonitor());
				if( module != null && behavior != null ) {
					behavior.publishOneModule(new IModule[]{module}, IServer.PUBLISH_FULL, ServerBehaviourDelegate.CHANGED, false, new NullProgressMonitor());
				}
			}
		}
	}

	
	protected IServer getServer() {
		
		IServer[] servers = ServerConverter.getDeployableServersAsIServers();
		if( servers.length == 0 ) return null;
		if( servers.length == 1 ) return servers[0];

		// Throw up a dialog
		SelectServerDialog d = new SelectServerDialog(null); 
		int result = d.open();
		if( result == Dialog.OK ) {
			return d.getSelectedServer();
		}
		return null;
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
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
				return "unknown object type";
			}
		}

		protected SelectServerDialog(Shell parentShell) {
			super(parentShell);
		}
		
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("Select a server to publish to");
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
