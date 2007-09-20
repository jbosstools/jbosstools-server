package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import java.util.Properties;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.views.server.ExtensionTableViewer.ContentWrapper;

public class PropertySheetFactory {
	

	/**
	 * Simple properties sheet requiring just content and label providers.
	 * @return
	 */
	public static TreeTablePropertySheetPage createTreeTablePropertySheetPage() {
		return new TreeTablePropertySheetPage();
	}
	
	public static class TreeTablePropertySheetPage implements IPropertySheetPage {

		protected TreeViewer propertiesViewer;
		protected Composite control;

		public void createControl(Composite parent) {
			control = new Composite(parent, SWT.NONE);
			control.setLayout(new FillLayout());
			Tree tTable = new Tree(control, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.NONE);
			tTable.setHeaderVisible(true);
			tTable.setLinesVisible(true);
			tTable.setLayoutData(new GridData(GridData.FILL_BOTH));
			tTable.setFont(control.getFont());

			TreeColumn column = new TreeColumn(tTable, SWT.SINGLE);
			column.setText(Messages.property);
			TreeColumn column2 = new TreeColumn(tTable, SWT.SINGLE);
			column2.setText(Messages.value);

			final Tree tree2 = tTable;
	        tree2.addControlListener(new ControlAdapter() {
	            public void controlResized(ControlEvent e) {
	                Rectangle area = tree2.getClientArea();
	                TreeColumn[] columns = tree2.getColumns();
	                if (area.width > 0) {
	                    columns[0].setWidth(area.width * 40 / 100);
	                    columns[1].setWidth(area.width - columns[0].getWidth() - 4);
	                    tree2.removeControlListener(this);
	                }
	            }
	        });
			
			propertiesViewer = new TreeViewer(tTable);
		}

		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		public Control getControl() {
			return control;
		}

		public void setActionBars(IActionBars actionBars) {
		}

		public void setFocus() {
		}

		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if( selection instanceof IStructuredSelection ) {
				Object o = ((IStructuredSelection)selection).getFirstElement();
				if( o != null ) {
					propertiesViewer.setInput(o);
				}
			}
		}
		
		public void setContentProvider(ITreeContentProvider provider) {
			propertiesViewer.setContentProvider(provider);
		}
		
		public void setLabelProvider(ITableLabelProvider provider) {
			propertiesViewer.setLabelProvider(provider);
		}
	}
	
	
	
	public static interface ISimplePropertiesHolder {
		public Properties getProperties(Object selected);
	}
	
	
	/**
	 * Because this class is for simple property implementations, 
	 * only the actual element is passed down to the Properties Holder.
	 * 
	 * The other implementations (such as <code>TreeTablePropertySheetPage</code>
	 * which require the implementer to add their own content and label providers
	 * are not granted this luxury and must expect that their items return to them
	 * wrapped within a ContentWrapper. 
	 * 
	 * @author rstryker
	 *
	 */
	
	public static class SimplePropertiesContentProvider extends LabelProvider 
		implements ITableLabelProvider, ITreeContentProvider  {
	
		protected Properties properties;
		protected ISimplePropertiesHolder holder;
		protected Object input;
		
		public SimplePropertiesContentProvider( ISimplePropertiesHolder holder2 ) {
			this.holder = holder2;
		}
	
		public Object[] getElements(Object inputElement) {
			if( inputElement instanceof ContentWrapper ) {
				inputElement = ((ContentWrapper)inputElement).getElement();
			}
			if( properties != null ) 
				return properties.keySet().toArray();

			return new Object[0];
		}
	
		public void dispose() {
		}
	
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if( newInput instanceof ContentWrapper ) 
				newInput = ((ContentWrapper)newInput).getElement();
			
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
			if( element instanceof ContentWrapper ) 
				element = ((ContentWrapper)element).getElement();
			
			if( columnIndex == 0 ) return element.toString();
			if( columnIndex == 1 && element instanceof String && properties != null ) {
				return properties.getProperty((String)element);
			}
			return null;
		}
	}
	
	public static SimplePropertiesPropertySheetPage createSimplePropertiesSheet(ISimplePropertiesHolder holder) {
		return new SimplePropertiesPropertySheetPage(holder);
	}
	
	public static class SimplePropertiesPropertySheetPage implements IPropertySheetPage {
		
		private ISimplePropertiesHolder holder;
		private SimplePropertiesContentProvider provider;
		private TreeTablePropertySheetPage sheet;
		
		
		public SimplePropertiesPropertySheetPage(ISimplePropertiesHolder holder) {
			this.holder = holder;
			this.sheet = new TreeTablePropertySheetPage();
		}
		
		public void createControl(Composite parent) {
			sheet.createControl(parent);
			provider = new SimplePropertiesContentProvider(holder);
			sheet.setContentProvider(provider);
			sheet.setLabelProvider(provider);
		}
		public void dispose() {
			sheet.dispose();
		}
		public Control getControl() {
			return sheet.getControl();
		}
		public void setActionBars(IActionBars actionBars) {
			sheet.setActionBars(actionBars);
		}
		public void setFocus() {
			sheet.setFocus();
		}
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			sheet.selectionChanged(part, selection);
		}
		
		
	}
	

}
