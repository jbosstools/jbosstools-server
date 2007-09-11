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
import org.jboss.ide.eclipse.as.ui.preferencepages.ViewProviderPreferenceComposite;
import org.jboss.ide.eclipse.as.ui.views.server.JBossServerView;
import org.jboss.ide.eclipse.as.ui.views.server.ExtensionTableViewer.ContentWrapper;

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
	
	
	public void fillContextMenu(Shell shell, IMenuManager menu, Object selection) {
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
						if( o == null ) {
							JBossServerView.getDefault().getExtensionFrame().getViewer().refresh(provider);
						} else {
							JBossServerView.getDefault().getExtensionFrame().getViewer().refresh(new ContentWrapper(o, provider));
						}
					} catch(Exception e) {
					}
				}
			}
		};
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
}
