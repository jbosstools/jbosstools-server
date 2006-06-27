package org.jboss.ide.eclipse.as.ui.viewproviders;

import java.util.Properties;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.jboss.ide.eclipse.as.ui.viewproviders.PropertySheetFactory.ISimplePropertiesHolder;
import org.jboss.ide.eclipse.as.ui.viewproviders.PropertySheetFactory.SimplePropertiesPropertySheetPage;

public abstract class SimplePropertiesViewExtension 
	extends JBossServerViewExtension implements ISimplePropertiesHolder {

	private SimplePropertiesPropertySheetPage propertiesSheet;
	
	public SimplePropertiesViewExtension() {
	}
	
	
	public abstract void fillContextMenu(Shell shell, IMenuManager menu, Object selection);
	public abstract ITreeContentProvider getContentProvider();
	public abstract LabelProvider getLabelProvider();

	
	public IPropertySheetPage getPropertySheetPage() {
		if( propertiesSheet == null ) {
			propertiesSheet = PropertySheetFactory.createSimplePropertiesSheet(this);
		}
		return propertiesSheet;
	}
	
	public abstract String[] getPropertyKeys(Object selected);
	public abstract Properties getProperties(Object selected);


}
