package org.jboss.ide.eclipse.as.ui.viewproviders;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin.ServerViewProvider;
import org.jboss.ide.eclipse.as.ui.views.JBossServerView;

public abstract class JBossServerViewExtension {
	protected ServerViewProvider provider;
	public static final int PROPERTIES = 1;
	public static final int TEXT = 2;
	public static final int PROPERTIES_AND_TEXT = 3;
	
	
	public void setViewProvider(ServerViewProvider provider) {
		this.provider = provider;
	}
	
	public boolean isEnabled() {
		return true;
	}
	
	public abstract void fillContextMenu(Shell shell, IMenuManager menu, Object selection);
	public abstract ITreeContentProvider getContentProvider();
	public abstract LabelProvider getLabelProvider();
	
	
	// Should return one of the three static fields
	public abstract int selectedObjectViewType(Object o);
	public abstract String getPropertiesText(Object o);

	public abstract ITreeContentProvider getPropertiesContentProvider();
	public abstract ITableLabelProvider getPropertiesLabelProvider();
	
	
	protected void refreshViewer() {
		if( isEnabled() ) {
			try {
				JBossServerView.getDefault().refreshJBTree(provider);
			} catch(Exception e) {
			}
		}
	}
}
