package org.jboss.ide.eclipse.as.ui.views.server.providers.jmx;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXBean;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXRunnable;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.JMXSafeRunner;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXModel.WrappedMBeanAttributeInfo;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;

public class AttributeGroup extends Composite {

	protected Tree tree;
	protected TreeColumn nameColumn, typeColumn, accessColumn, valueColumn,
			descriptionColumn;
	protected TreeViewer treeViewer;
	protected JMXPropertySheetPage page;

	public AttributeGroup(Composite parent, int style, JMXPropertySheetPage page) {
		super(parent, style);
		this.page = page;
		setLayout(new FillLayout());

		tree = new Tree(this, SWT.BORDER | SWT.FULL_SELECTION);

		nameColumn = new TreeColumn(tree, SWT.NONE);
		typeColumn = new TreeColumn(tree, SWT.NONE);
		accessColumn = new TreeColumn(tree, SWT.NONE);
		valueColumn = new TreeColumn(tree, SWT.NONE);
		descriptionColumn = new TreeColumn(tree, SWT.NONE);

		nameColumn.setWidth(100);
		typeColumn.setWidth(150);
		accessColumn.setWidth(50);
		valueColumn.setWidth(200);
		descriptionColumn.setWidth(300);

		nameColumn.setText("Name");
		typeColumn.setText("Type");
		accessColumn.setText("Access");
		valueColumn.setText("Value");
		descriptionColumn.setText("Description");

		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		treeViewer = new TreeViewer(tree);
		treeViewer.setContentProvider(new AttributeViewerContentProvider());
		treeViewer.setLabelProvider(new AttributeViewerLabelProvider());

		JMXAttributePropertySelListener selListener = new JMXAttributePropertySelListener();
		tree.addListener(SWT.MouseDoubleClick, selListener);
	}

	public void setBean(JMXBean bean) {
		treeViewer.setInput(bean);
	}

	protected class JMXAttributePropertySelListener implements Listener {
		private final Color black;
		private final TreeEditor editor;

		public JMXAttributePropertySelListener() {
			black = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
			editor = new TreeEditor(tree);
		}

		public void handleEvent(Event event) {
			TreeItem[] selectedItems = tree.getSelection();
			if (selectedItems.length != 1)
				return;
			final TreeItem item = selectedItems[0];

			if (!(item.getData() instanceof WrappedMBeanAttributeInfo))
				return;
			if (!((WrappedMBeanAttributeInfo) item.getData()).getInfo()
					.isWritable())
				return;

			final WrappedMBeanAttributeInfo attInfo = (WrappedMBeanAttributeInfo) item
					.getData();

			final int column = 3;
			boolean isCarbon = SWT.getPlatform().equals("carbon");
			final Composite composite = new Composite(tree, SWT.NONE);
			if (!isCarbon)
				composite.setBackground(black);
			final Text text = new Text(composite, SWT.NONE);
			final int inset = isCarbon ? 0 : 1;
			composite.addListener(SWT.Resize, new Listener() {
				public void handleEvent(Event e) {
					Rectangle rect = composite.getClientArea();
					text.setBounds(rect.x + inset, rect.y + inset, rect.width
							- inset * 2, rect.height - inset * 2);
				}
			});
			Listener textListener = new Listener() {
				public void handleEvent(final Event e) {
					switch (e.type) {
					case SWT.FocusOut:
						composite.dispose();
						break;
					case SWT.Verify:
						String newText = text.getText();
						String leftText = newText.substring(0, e.start);
						String rightText = newText.substring(e.end, newText
								.length());
						GC gc = new GC(text);
						Point size = gc.textExtent(leftText + e.text
								+ rightText);
						gc.dispose();
						size = text.computeSize(size.x, SWT.DEFAULT);
						editor.horizontalAlignment = SWT.LEFT;
						Rectangle itemRect = item.getBounds(),
						rect = tree.getClientArea();
						editor.minimumWidth = Math.max(size.x, itemRect.width)
								+ inset * 2;
						int left = itemRect.x,
						right = rect.x + rect.width;
						editor.minimumWidth = Math.min(editor.minimumWidth,
								right - left);
						editor.minimumHeight = size.y + inset * 2;
						editor.setColumn(column);
						editor.layout();
						break;
					case SWT.Traverse:
						switch (e.detail) {
						case SWT.TRAVERSE_RETURN:
							saveAttributeChange(attInfo, text);
							// FALL THROUGH
						case SWT.TRAVERSE_ESCAPE:
							composite.dispose();
							e.doit = false;
						}
						break;
					}
				}
			};
			text.addListener(SWT.FocusOut, textListener);
			text.addListener(SWT.Traverse, textListener);
			text.addListener(SWT.Verify, textListener);
			editor.setEditor(composite, item);
			text.setText(item.getText(column));
			text.selectAll();
			text.setFocus();

		}
	}

	protected void saveAttributeChange(final WrappedMBeanAttributeInfo attInfo, Text text) {
		if (text.isDisposed())
			return;
		final String text2 = text.getText();
		final Attribute att = createAttribute(attInfo, text2);
		final Boolean[] errorBool = new Boolean[1];
		if( att == null ) {
			// throw up a message box and say no can do, for now
			MessageBox messageBox = new MessageBox (new Shell(), SWT.OK);
			messageBox.setText ("Cannot update bean");
			messageBox.setMessage ("Bean update cannot proceed. Plug-in cannot convert " + text2 + " into " + attInfo.getInfo().getType());
			messageBox.open();
			return;
		}
		
		final JMXRunnable run = new JMXRunnable() {
			public void run(MBeanServerConnection connection) {
				try {
					connection.setAttribute(new ObjectName(
							attInfo.getBean().getName()), att);
				} catch (final Exception e) {
					Display.getDefault().asyncExec(new Runnable() { public void run() {
						IStatus status = new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, e.getMessage(), e);
						JBossServerUIPlugin.getDefault().getLog().log(status);
						errorBool[0] = new Boolean(true);
					}});
				}
			}
		};
		
		new Thread() { public void run() { 
			JMXSafeRunner.run(attInfo.getBean().getServer(), run);
			if( errorBool[0] == null ) {
				page.provider.loadChildren(attInfo.getBean());
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						page.setInputObject(attInfo.getBean());
					}
				});
			}
		}}.start();
	}

	protected Attribute createAttribute(WrappedMBeanAttributeInfo attInfo, String text) {
		String type = attInfo.getInfo().getType();
		Object val = null;
		
		try {
			if( type.equals("java.lang.String")) val = text;
			else if( type.equals("boolean")) val = new Boolean(text);
			else if( type.equals("int")) val = new Integer(text);
			else if( type.equals("long")) val = new Long(text);
		} catch( Exception e ) {}
		
		return val == null ? null :  
			new Attribute(attInfo.getInfo().getName(), val);
	}
	
	protected class AttributeViewerContentProvider implements
			ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof JMXBean)
				return ((JMXBean) parentElement).getAttributes();
			return new Object[] {};
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return false;
		}

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof JMXBean)
				return ((JMXBean) inputElement).getAttributes();
			return new Object[] {};
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	protected class AttributeViewerLabelProvider extends LabelProvider
			implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof WrappedMBeanAttributeInfo) {
				WrappedMBeanAttributeInfo element2 = (WrappedMBeanAttributeInfo) element;
				if (columnIndex == 0)
					return element2.getInfo().getName();
				if (columnIndex == 1)
					return element2.getInfo().getType();
				if (columnIndex == 3)
					return (element2.getValue() != null ? element2.getValue()
							.toString() : "");
				if (columnIndex == 4)
					return element2.getInfo().getDescription();
				if (columnIndex == 2) {
					String ret = element2.getInfo().isReadable() ? "R" : "";
					ret += element2.getInfo().isWritable() ? "W" : "";
					return ret;
				}

			}
			return null;
		}

	}

}
