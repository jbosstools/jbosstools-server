package org.jboss.ide.eclipse.as.ui.viewproviders;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin.ServerViewProvider;
import org.jboss.ide.eclipse.as.ui.views.JBossServerView;

public abstract class JBossServerViewExtension {
	protected ServerViewProvider provider;
	public static final int PROPERTIES = 1;
	public static final int TEXT = 2;
	public static final int PROPERTIES_AND_TEXT = 3;
	
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
	
	
	public abstract void fillContextMenu(Shell shell, IMenuManager menu, Object selection);

	
	public abstract ITreeContentProvider getContentProvider();
	public abstract LabelProvider getLabelProvider();
	
	public abstract IPropertySheetPage getPropertySheetPage();
	
	protected void refreshViewer() {
		if( isEnabled() ) {
			try {
				JBossServerView.getDefault().refreshJBTree(provider);
			} catch(Exception e) {
			}
		}
	}
}
