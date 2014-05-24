/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.jmx.local.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.jvmmonitor.core.IActiveJvm;
import org.jboss.tools.jmx.jvmmonitor.core.IJvmFacade;
import org.jboss.tools.jmx.jvmmonitor.core.JvmCoreException;
import org.jboss.tools.jmx.local.internal.Activator;
import org.jboss.tools.jmx.ui.internal.JMXImages;

@SuppressWarnings("restriction")
public class AttachActionProvider extends CommonActionProvider {
	private ICommonActionExtensionSite site;
	private AttachAgentAction attachAction;
	
	@Override
	public void init(ICommonActionExtensionSite site) {
		super.init(site);
		this.site = site;
		attachAction = new AttachAgentAction();
	}
	
	

	public StructuredViewer getStructuredViewer() {
		return site.getStructuredViewer();
	}

	
	
	@Override
	public void fillContextMenu(IMenuManager menu) {
		Object firstSel = getFirstSelection();
		if( firstSel != null && firstSel instanceof IConnectionWrapper ) {
			IConnectionWrapper wrap = (IConnectionWrapper)firstSel;
			if( wrap.isConnected() && wrap instanceof IJvmFacade) {
				IActiveJvm active = ((IJvmFacade)wrap).getActiveJvm();
				if( active != null && active.isConnected() && !active.isAttached()) {
					menu.add(attachAction);
					menu.add(new Separator());
				}
			}
		}
	}
	
	

	protected Object getFirstSelection() {
		IStructuredSelection selection = getContextSelection();
		if (selection != null) {
			return selection.getFirstElement();
		}
		return null;
	}

	protected IStructuredSelection getContextSelection() {
		IStructuredSelection answer = null;
		if (getContext() != null && getContext().getSelection() != null) {
			ISelection sel = getContext().getSelection();
			if (sel instanceof IStructuredSelection) {
				answer = (IStructuredSelection) sel;
			}
		}
		return answer;
	}
	public class AttachAgentAction extends Action {
		public AttachAgentAction() {
			super("Attach Agent");
	        JMXImages.setLocalImageDescriptors(this, "attachAgent.gif");  //$NON-NLS-1$
		}

		public void run() {
			Object firstSel = getFirstSelection();
			if( firstSel != null && firstSel instanceof IConnectionWrapper ) {
				IConnectionWrapper wrap = (IConnectionWrapper)firstSel;
				if( wrap.isConnected() && wrap instanceof IJvmFacade) {
					IActiveJvm active = ((IJvmFacade)wrap).getActiveJvm();
					if( active != null && active.isConnected() && !active.isAttached()) {
						try {
							active.attach();
						} catch(JvmCoreException jvmce) {
							Activator.getDefault().getLog().log(jvmce.getStatus());
						}
					}
				}
			}
		}
	}

}
