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
package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.preferencepages.ViewProviderPreferenceComposite;
import org.jboss.ide.eclipse.as.ui.views.server.ExtensionTableViewer;
import org.jboss.ide.eclipse.as.ui.views.server.JBossServerView;
import org.jboss.ide.eclipse.as.ui.views.server.ExtensionTableViewer.ContentWrapper;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public abstract class JBossServerViewExtension {
	protected ServerViewProvider provider;
	
	/**
	 * Which extension point is mine.
	 * @param provider
	 */
	public void setViewProvider(ServerViewProvider provider) {
		this.provider = provider;
	}
	
	/**
	 * Should query preferencestore to see if I'm enabled or not
	 * @return
	 */
	public boolean isEnabled() {
		return provider.isEnabled();
	}
	
	
	public void init() {
	}
	public void enable() {
	}
	public void disable() {
	}
	public void dispose() {
		if( getPropertySheetPage() != null ) 
			getPropertySheetPage().dispose();
	}
	
	
	public void fillContextMenu(Shell shell, IMenuManager menu, Object[] selected) {
	}
	
	public ITreeContentProvider getContentProvider() {
		return null;
	}
	public  LabelProvider getLabelProvider() {
		return null;
	}
	
	public IPropertySheetPage getPropertySheetPage() {
		return null;
	}
	
	public ViewProviderPreferenceComposite createPreferenceComposite(Composite parent) {
		return null;
	}
	
	public Image createIcon() {
		return null;
	}
	
	public void refreshModel(Object object) {
		// override me
	}

	protected void suppressingRefresh(Runnable runnable) {
		JBossServerView.getDefault().getExtensionFrame().getViewer().suppressingRefresh(runnable);
	}
	
	protected void refreshViewer() {
		refreshViewer(null);
	}
	protected void refreshViewer(final Object o) {
		Runnable r = new Runnable() { 
			public void run() {
				if( isEnabled() ) {
					try {
						if( o == null || o == provider ) {
							JBossServerView.getDefault().getExtensionFrame().getViewer().refresh(provider);
						} else {
							ExtensionTableViewer viewer = JBossServerView.getDefault().getExtensionFrame().getViewer();
							ContentWrapper wrapped = new ContentWrapper(o, provider);
							if( viewer.elementInTree(wrapped))
								viewer.refresh(new ContentWrapper(o, provider));
							else
								viewer.refresh(provider);
						}
					} catch(Exception e) {
						JBossServerUIPlugin.log("Error refreshing viewer (object=" + o + ")", e);
					}
				}
			}
		};
		
		if( JBossServerView.getDefault() == null ) return;
		
		if( Display.getCurrent() == null ) 
			Display.getDefault().asyncExec(r);
		else
			r.run();
	}
	protected void removeElement(Object o) {
		JBossServerView.getDefault().getServerFrame().getViewer().remove(new ContentWrapper(o, provider));
	}
	protected void addElement(Object parent, Object child) {
		JBossServerView.getDefault().getServerFrame().getViewer().add(new ContentWrapper(parent, provider), new ContentWrapper(child, provider));
	}
	
	// what servers should i show for?
	protected boolean supports(IServer server) {
		if( server == null ) return false;
		return isJBossDeployable(server);
	}
	
	// show for anything that's jboss deployable
	protected boolean isJBossDeployable(IServer server) {
		return (IDeployableServer)server.loadAdapter(IDeployableServer.class, new NullProgressMonitor()) != null;
	}
	
	// show only for full jboss servers
	protected boolean isJBossServer(IServer server) {
		return (JBossServer)server.loadAdapter(JBossServer.class, new NullProgressMonitor()) != null;
	}
	
	protected boolean allAre(Object[] list, Class clazz) {
		for( int i = 0; i < list.length; i++ )
			if( list[i].getClass() != clazz )
				return false;
		return true;
	}
}
