package org.jboss.ide.eclipse.as.ui.viewproviders;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.ServerUICore;
import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.util.ASDebug;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin.ServerViewProvider;
import org.jboss.ide.eclipse.as.ui.views.JBossServerViewExtension;

public class ModuleViewProvider extends JBossServerViewExtension {

	private ModuleContentProvider contentProvider;
	private ModuleLabelProvider labelProvider;
	
	private ModulePropertiesContentProvider propContentProvider;
	private ModulePropertiesLabelProvider propLabelProvider;
	
	private Object propertiesInput;
	
	public ModuleViewProvider() {
		contentProvider = new ModuleContentProvider();
		labelProvider = new ModuleLabelProvider();
	}

	public void fillJBContextMenu(Shell shell, IMenuManager menu) {
		// TODO Auto-generated method stub

	}

	public ITreeContentProvider getContentProvider() {
		return contentProvider;
	}

	public LabelProvider getLabelProvider() {
		return labelProvider;
	}

	
	class ModuleContentProvider implements ITreeContentProvider {

		private IServer input;
		
		public Object[] getChildren(Object parentElement) {
			
			if (parentElement instanceof ModuleServer) {
				ModuleServer ms = (ModuleServer) parentElement;
				try {
					IModule[] children = ms.server.getChildModules(ms.module, null);
					int size = children.length;
					ModuleServer[] ms2 = new ModuleServer[size];
					for (int i = 0; i < size; i++) {
						int size2 = ms.module.length;
						IModule[] module = new IModule[size2 + 1];
						System.arraycopy(ms.module, 0, module, 0, size2);
						module[size2] = children[i];
						ms2[i] = new ModuleServer(ms.server, module);
					}
					return ms2;
				} catch (Exception e) {
					return null;
				}
			}

			
			
			if( parentElement instanceof ServerViewProvider ) {
				IModule[] modules = input.getModules(); 
				int size = modules.length;
				ModuleServer[] ms = new ModuleServer[size];
				for (int i = 0; i < size; i++) {
					ms[i] = new ModuleServer(input, new IModule[] { modules[i] });
				}
				return ms;
			}
			return null;
		}

		public Object getParent(Object element) {
			if( element instanceof ModuleServer ) {
				return provider;
			}
			
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0 ? true : false; 
		}

		// unused
		public Object[] getElements(Object inputElement) {
			return null;
		}

		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub
			input = (IServer)newInput;
			ASDebug.p("New input is: " + newInput, this);
		}
		
	}
	
	class ModuleLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			if( obj instanceof ModuleServer ) {
				ModuleServer ms = (ModuleServer)obj;
				int size = ms.module.length;
				return ms.module[size - 1].getName();
			}
			if( obj instanceof JBossServer) {
				JBossServer server = (JBossServer)obj;
				String ret = server.getServer().getName(); 
				ret += "  (";
				String home = server.getRuntimeConfiguration().getServerHome(); 
				ret += (home.length() > 30 ? home.substring(0,30) + "..." : home);
				ret += ", " + server.getRuntimeConfiguration().getJbossConfiguration() + ")";
				return ret;
			}
			return null;
		}
		public Image getImage(Object obj) {
			if( obj instanceof ModuleServer ) {
				ModuleServer ms = (ModuleServer)obj;
				int size = ms.module.length;
				return ServerUICore.getLabelProvider().getImage(ms.module[ms.module.length - 1]);
			}
			if( obj instanceof JBossServer) {
				return ServerUICore.getLabelProvider().getImage(((JBossServer)obj).getServer());
			}
			return null;
		}

	}




	
	
	public int selectedObjectViewType(Object o) {
		return JBossServerViewExtension.PROPERTIES;
	}

	public ITableLabelProvider getPropertiesLabelProvider() {
		if( propLabelProvider == null )
			propLabelProvider = new ModulePropertiesLabelProvider();
		return propLabelProvider;
	}
	
	public ITreeContentProvider getPropertiesContentProvider() {
		if( propContentProvider == null ) 
			propContentProvider = new ModulePropertiesContentProvider();
		
		return propContentProvider;
	}

	public String getPropertiesText(Object o) {
		return null;
	}
	
	public class ModulePropertiesContentProvider implements ITreeContentProvider {

		public Object[] getElements(Object inputElement) {
			return new Object[] {
					Messages.ModulePropertyType, Messages.ModulePropertyProject	};
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			propertiesInput = newInput;
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
		
	}
	
	public class ModulePropertiesLabelProvider extends LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if( columnIndex == 0 ) return element.toString();
			if( columnIndex == 1 ) {
				if( element.equals(Messages.ModulePropertyType)) {
					return ((ModuleServer)propertiesInput).module[0].getModuleType().getId();
				}
				if( element.equals(Messages.ModulePropertyProject)) {
					return ((ModuleServer)propertiesInput).module[0].getProject().getName();
				}
			}
			
			return null;
		}
	}

}
