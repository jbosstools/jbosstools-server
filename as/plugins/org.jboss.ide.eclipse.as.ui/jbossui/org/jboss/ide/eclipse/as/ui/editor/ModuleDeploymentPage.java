package org.jboss.ide.eclipse.as.ui.editor;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.ui.editor.IServerEditorPartInput;
import org.eclipse.wst.server.ui.editor.ServerEditorPart;
import org.eclipse.wst.server.ui.internal.command.ServerCommand;
import org.eclipse.wst.server.ui.internal.editor.ServerEditorPartInput;
import org.eclipse.wst.server.ui.internal.editor.ServerResourceCommandManager;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentModulePrefs;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentPreferences;

public class ModuleDeploymentPage extends ServerEditorPart {
	private ServerResourceCommandManager commandManager;
	private ArrayList<IModule> possibleChildren;

	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		ArrayList<IModule> possibleChildren = new ArrayList<IModule>();
		IModule[] modules2 = org.eclipse.wst.server.core.ServerUtil.getModules(server.getServerType().getRuntimeType().getModuleTypes());
		if (modules2 != null) {
			int size = modules2.length;
			for (int i = 0; i < size; i++) {
				IModule module = modules2[i];
				IStatus status = server.canModifyModules(new IModule[] { module }, null, null);
				if (status != null && status.getSeverity() != IStatus.ERROR)
					possibleChildren.add(module);
			}
		}
		this.possibleChildren = possibleChildren;
		if (input instanceof IServerEditorPartInput) {
			IServerEditorPartInput sepi = (IServerEditorPartInput) input;
			server = sepi.getServer();
			commandManager = ((ServerEditorPartInput) sepi).getServerCommandManager();
			readOnly = sepi.isServerReadOnly();
		}

	}

	public void createPartControl(Composite parent) {
		createLocalControl(parent);
	}
	
	private TreeViewer viewer;
	private DeploymentPreferences preferences;
	private static final String LOCAL_COLUMN_NAME = IJBossToolingConstants.LOCAL_DEPLOYMENT_NAME; 
	private static final String LOCAL_COLUMN_LOC = IJBossToolingConstants.LOCAL_DEPLOYMENT_LOC;
	private static final String LOCAL_COLUMN_TEMP_LOC = IJBossToolingConstants.LOCAL_DEPLOYMENT_TEMP_LOC;
	
	protected void createLocalControl(Composite parent) {
		preferences = DeploymentPreferenceLoader.loadPreferences(server.getOriginal());		
		FormToolkit toolkit = getFormToolkit(parent.getDisplay());
		
		ScrolledForm form = toolkit.createScrolledForm(parent);
		toolkit.decorateFormHeading(form.getForm());
		form.setText("Module Deployment");
		//form.setImage(null);
		GridLayout layout = new GridLayout();
		layout.marginTop = 6;
		layout.marginLeft = 6;
		form.getBody().setLayout(layout);
		
		Composite random = toolkit.createComposite(form.getBody(), SWT.NONE);
		random.setLayout(new FillLayout());
		GridData randomData = new GridData(GridData.FILL_BOTH);
		random.setLayoutData(randomData);
		Composite root = toolkit.createComposite(random, SWT.NONE);
		root.setLayout(new FormLayout());
		
		viewer = new TreeViewer(root, SWT.BORDER);
		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);
		TreeColumn moduleColumn = new TreeColumn(viewer.getTree(), SWT.NONE);
		TreeColumn publishLocColumn = new TreeColumn(viewer.getTree(), SWT.NONE);
		TreeColumn publishTempLocColumn = new TreeColumn(viewer.getTree(), SWT.NONE);
		moduleColumn.setText("Module"); 
		publishLocColumn.setText("Publish Location");
		publishTempLocColumn.setText("Publish Temporary Location");
		
		moduleColumn.setWidth(200); 
		publishLocColumn.setWidth(200);
		publishTempLocColumn.setWidth(200);
		
		
		FormData treeData = new FormData();
		treeData.top = new FormAttachment(0, 100);
		treeData.bottom = new FormAttachment(100, -100);
		treeData.left = new FormAttachment(0,5);
		treeData.right = new FormAttachment(100,-5);
		viewer.getTree().setLayoutData(treeData);
		viewer.setContentProvider(new ModulePageContentProvider());
				
		viewer.setLabelProvider( new ModulePageLabelProvider());
		viewer.setColumnProperties(new String[] { 
				LOCAL_COLUMN_NAME,
				LOCAL_COLUMN_LOC, LOCAL_COLUMN_TEMP_LOC
		});
		viewer.setInput("");
		CellEditor[] editors = new CellEditor[] { 
				new TextCellEditor(viewer.getTree()), 
				new TextCellEditor(viewer.getTree()),
				new TextCellEditor(viewer.getTree())
		};
		viewer.setCellModifier(new LocalDeploymentCellModifier());
		viewer.setCellEditors(editors);
	}

	private class LocalDeploymentCellModifier implements ICellModifier {
		public boolean canModify(Object element, String property) {
			return true;
		}
		public Object getValue(Object element, String property) {
			DeploymentModulePrefs p = preferences.getPreferences("local").getModulePrefs((IModule)element);
			if( property == LOCAL_COLUMN_LOC) {
				String ret = p.getProperty(LOCAL_COLUMN_LOC);
				return ret == null ? "" : ret;
			}
			if( property == LOCAL_COLUMN_TEMP_LOC) {
				String ret = p.getProperty(LOCAL_COLUMN_TEMP_LOC);
				return ret == null ? "" : ret;
			}

			return "";
		}
		public void modify(Object element, String property, Object value) {
			
			IModule module = (IModule) ((TreeItem)element).getData();
			DeploymentModulePrefs p = preferences.getPreferences("local").getModulePrefs(module);
			if( property == LOCAL_COLUMN_LOC) {
				firePropertyChangeCommand(p, LOCAL_COLUMN_LOC, (String)value);
			} else
			if( property == LOCAL_COLUMN_TEMP_LOC) {
				firePropertyChangeCommand(p, LOCAL_COLUMN_TEMP_LOC, (String)value);
			}
		}
	}
	
	protected void firePropertyChangeCommand(DeploymentModulePrefs p, String key, String val) {
		commandManager.execute(new ChangePropertyCommand(p,key,val));
	}
	
	private String makeGlobal(String path) {
		return ServerUtil.makeGlobal(getRuntime(), new Path(path)).toString();
	}
	
	private String makeRelative(String path) {
		if (getRuntime() == null) {
			return path;
		}
		return ServerUtil.makeRelative(getRuntime(), new Path(path)).toString();
	}

	private IJBossServerRuntime getRuntime() {
		IRuntime r = server.getRuntime();
		IJBossServerRuntime ajbsrt = null;
		if (r != null) {
			ajbsrt = (IJBossServerRuntime) r
					.loadAdapter(IJBossServerRuntime.class,
							new NullProgressMonitor());
		}
		return ajbsrt;
	}
	
	
	private class ChangePropertyCommand extends ServerCommand {
		private DeploymentModulePrefs p;
		private String key;
		private String oldVal;
		private String newVal;
		public ChangePropertyCommand(DeploymentModulePrefs p, String key, String val) {
			super(ModuleDeploymentPage.this.server, "command text");
			this.p = p;
			this.key = key;
			this.newVal = val;
			this.oldVal = p.getProperty(key);
		}
		public void execute() {
			p.setProperty(key, newVal);
			viewer.refresh();
		}
		public void undo() {
			p.setProperty(key, oldVal);
			viewer.refresh();
		}
	}
	
	private class ModulePageContentProvider implements ITreeContentProvider {
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object inputElement) {
			return (IModule[]) possibleChildren.toArray(new IModule[possibleChildren.size()]);
		}
		public boolean hasChildren(Object element) {
			return false;
		}
		public Object getParent(Object element) {
			return null;
		}
		public Object[] getChildren(Object parentElement) {
			return null;
		}
	}
	
	private class ModulePageLabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			if( element instanceof IModule ) {
				IModule m = (IModule)element;
				if( columnIndex == 0 )
					return m.getName();
				if( columnIndex == 1 ) {
					DeploymentModulePrefs modPref = preferences.getOrCreatePreferences("local").getOrCreateModulePrefs(m);
					String result = modPref.getProperty(LOCAL_COLUMN_LOC);
					if( result != null)
						return result;
					modPref.setProperty(LOCAL_COLUMN_LOC, defaultLocation());
					return defaultLocation();
				}
				if( columnIndex == 2 ) {
					DeploymentModulePrefs modPref = preferences.getOrCreatePreferences("local").getOrCreateModulePrefs(m);
					String result = modPref.getProperty(LOCAL_COLUMN_TEMP_LOC);
					if( result != null )
						return result;
					modPref.setProperty(LOCAL_COLUMN_TEMP_LOC, defaultTempLocation());
					return defaultTempLocation();
				}
			}
			return element.toString();
		}
		public void addListener(ILabelProviderListener listener) {
		}
		public void dispose() {
		}
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}
		public void removeListener(ILabelProviderListener listener) {
		}
	}
	
	protected String defaultLocation() {
		//return "server/${jboss_config}/deploy";
		return "";
	}
	protected String defaultTempLocation() {
		//return "server/${jboss_config}/tmp/jbosstoolsTemp";
		return "";
	}
	
	public void setFocus() {
	}

	public void doSave(IProgressMonitor monitor) {
		try {
			DeploymentPreferenceLoader.savePreferences(server.getOriginal(), preferences);
		} catch( IOException ioe ) {
			// TODO eh?
		}
	}
}
