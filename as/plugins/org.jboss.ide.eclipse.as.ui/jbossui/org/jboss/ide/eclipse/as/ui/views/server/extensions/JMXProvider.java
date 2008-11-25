package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JBossServerConnectionProvider;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.ui.internal.views.navigator.MBeanExplorerContentProvider;
import org.jboss.tools.jmx.ui.internal.views.navigator.MBeanExplorerLabelProvider;

public class JMXProvider {

	public static class ContentProvider implements ITreeContentProvider {
		private MBeanExplorerContentProvider delegate;
		public ContentProvider() {
			delegate = new MBeanExplorerContentProvider();
		}
		public Object[] getChildren(Object parentElement) {
			if( parentElement instanceof IServer ) {
				Object sel = JBossServerConnectionProvider.getConnection((IServer)parentElement);
				if( sel != null )
					return new Object[] { sel };
			}
			return delegate.getChildren(parentElement);
		}
		public Object getParent(Object element) {
			return delegate.getParent(element);
		}
		public boolean hasChildren(Object element) {
			return delegate.hasChildren(element);
		}
		public Object[] getElements(Object inputElement) {
			return delegate.getElements(inputElement);
		}
		public void dispose() {
			delegate.dispose();
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			delegate.inputChanged(viewer, oldInput, newInput);
		}
	}
	
	public static  class LabelProvider extends org.eclipse.jface.viewers.LabelProvider {
		private MBeanExplorerLabelProvider delegate;
		public LabelProvider() {
			delegate = new MBeanExplorerLabelProvider();
		}
		
		public void dispose() {
			delegate.dispose();
		}
		
		public String getText(Object element) {
			if( element instanceof IConnectionWrapper ) {
				return "JMX"; 
			}
			return delegate.getText(element);
		}
		
		public Image getImage(Object element) {
			if( element instanceof IConnectionWrapper ) {
				return JBossServerUISharedImages.getImage(JBossServerUISharedImages.JMX_IMAGE); 
			}
			return delegate.getImage(element);
		}
	}
	
}
