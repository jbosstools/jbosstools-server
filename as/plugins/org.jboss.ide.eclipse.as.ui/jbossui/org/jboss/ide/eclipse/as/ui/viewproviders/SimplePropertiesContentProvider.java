package org.jboss.ide.eclipse.as.ui.viewproviders;

import java.util.Properties;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

public class SimplePropertiesContentProvider extends LabelProvider 
	implements ITableLabelProvider, ITreeContentProvider  {

	protected Properties properties;
	protected ISimplePropertiesHolder holder;
	protected Object input;
	
	public SimplePropertiesContentProvider( ISimplePropertiesHolder propertyHolder ) {
		this.holder = propertyHolder;
	}
	


	public String getPropertiesText(Object o) {
		return null;
	}

	public int selectedObjectViewType(Object o) {
		return JBossServerViewExtension.PROPERTIES;
	}
	
	
	public Object[] getElements(Object inputElement) {
		return holder.getPropertyKeys(inputElement);
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		input = newInput;
		properties = holder.getProperties(newInput);
	}

	public Object[] getChildren(Object parentElement) {
		return new Object[0];
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return false;
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if( columnIndex == 0 ) return element.toString();
		if( columnIndex == 1 && element instanceof String ) {
			return properties.getProperty((String)element);
		}
		return null;
	}
		
	

}
