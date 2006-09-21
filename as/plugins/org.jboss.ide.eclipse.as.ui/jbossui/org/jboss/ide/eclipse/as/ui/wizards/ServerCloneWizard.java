package org.jboss.ide.eclipse.as.ui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.util.ASDebug;
import org.jboss.ide.eclipse.as.ui.Messages;

public class ServerCloneWizard extends Wizard {

	private JBossServer server;
	private ServerAttributeHelper helper;
	private Page1 page1;
	private ArrayList minimalList;
	private File[] selectedFiles;
	private String name, config;
	
	public ServerCloneWizard(JBossServer server) {
		this.server = server;
		helper = new ServerAttributeHelper(server, server.getServer().createWorkingCopy());
		page1 = new Page1();
		addPage(page1);
		minimalList = new ArrayList();
		minimalList.addAll(Arrays.asList(server.getAttributeHelper().getMinimalConfig()));
	}
		
    public void setContainer(IWizardContainer wizardContainer) {
    	super.setContainer(wizardContainer);
    	setWindowTitle(Messages.CloneWizardWindowTitle + server.getServer().getName());
    }


	public boolean performFinish() {
		selectedFiles = page1.viewerToFileArray();
		return true;
	}

	public File[] getSelectedFiles() {
		return selectedFiles;
	}



	public String getConfig() {
		return config;
	}

	public String getName() {
		return name;
	}
	
	class Page1 extends WizardPage {

		private Text serverNameText, configNameText;
		private Tree fileTree;
		private FileTreeCheckboxViewer fileTreeViewer;
		private Label requiredCheckedLabel;
		
		protected Page1() {
			super("");
		}

		public boolean isPageComplete() {
			if( !isServerNameValid(serverNameText.getText()) && serverNameText.getText().length() != 0) {
				setErrorMessage( Messages.CloneWizardServerNameInUse + serverNameText.getText());
				return false;
			} 
			if( serverNameText.getText().length() == 0 ) {
				//setErrorMessage("Server Name may not be empty.");
				setErrorMessage(null);
				return false;
			}
			
			if( !isConfigNameValid(configNameText.getText()) && configNameText.getText().length() != 0) {
				setErrorMessage( Messages.CloneWizardConfigNameInUse + configNameText.getText());
				return false;
			}
			if( configNameText.getText().length() == 0 ) {
				//setErrorMessage("Configuration name may not be empty.");
				setErrorMessage(null);
				return false;
			}
			
			setErrorMessage(null);
			return true;
		}
		
		public void createControl(Composite parent) {
			Composite c = new Composite(parent, SWT.NONE);
			c.setLayout(new FormLayout());
			
			addNameFields(c);
			addCheckBoxViewer(c);
			addBottomLabels(c);
			
			setTitle(Messages.CloneWizardTitle);
			setDescription(Messages.CloneWizardDescription);
			setControl(c);
		}
		
		private void addNameFields(Composite c) {
			
			// create the widgets
			Label serverNameLabel, configNameLabel;
			serverNameLabel = new Label(c, SWT.NONE);
			configNameLabel = new Label(c, SWT.NONE);
			serverNameText = new Text(c, SWT.BORDER);
			configNameText = new Text(c, SWT.BORDER);
			
			// set the texts
			serverNameLabel.setText(Messages.CloneWizardNewServerName);
			configNameLabel.setText(Messages.CloneWizardNewConfigName);
			
			
			// lay them OUT
			FormData serverNameLabelData = new FormData();
			FormData serverNameTextData = new FormData();
			FormData configNameLabelData = new FormData();
			FormData configNameTextData = new FormData();
			
			serverNameLabelData.left = new FormAttachment(0,5);
			serverNameLabelData.top = new FormAttachment(0,6);
			configNameLabelData.left = new FormAttachment(0,5);
			configNameLabelData.top = new FormAttachment(serverNameText, 6);
			serverNameTextData.top = new FormAttachment(0,4);
			serverNameTextData.left = new FormAttachment(40, 5);
			serverNameTextData.right = new FormAttachment(100, -5);
			configNameTextData.left = new FormAttachment(40, 5);
			configNameTextData.top = new FormAttachment(serverNameText, 5);
			configNameTextData.right = new FormAttachment(100, -5);
			
			
			serverNameLabel.setLayoutData(serverNameLabelData);
			serverNameText.setLayoutData(serverNameTextData);
			configNameLabel.setLayoutData(configNameLabelData);
			configNameText.setLayoutData(configNameTextData);
			
			// Add some text listeners
			serverNameText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					name = serverNameText.getText();
					getContainer().updateButtons();
				} 
			} );
			
			configNameText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					config = configNameText.getText();
					getContainer().updateButtons();
				} 
			});
		}
		
		protected boolean isServerNameValid(String name) {
			if( name == null || name.equals("")) return false;
			JBossServer[] servers = JBossServerCore.getAllJBossServers();
			for( int i = 0; i < servers.length; i++ ) {
				if( servers[i].getServer().getName().equals(name)) {
					return false;
				}
			}
			return true;
		}
		protected boolean isConfigNameValid(String name) {
			if( name == null || name.equals("")) return false;
			String homeDir = server.getAttributeHelper().getServerHome();
			String[] configNames = getConfigNames(homeDir);
			for( int i = 0; i < configNames.length; i++ ) {
				if( configNames[i].equals(name)) {
					return false;
				}
			}
			return true;
		}
		
		
		protected void addCheckBoxViewer(Composite c) {
			fileTree = new Tree(c, SWT.CHECK | SWT.BORDER);
			fileTreeViewer = new FileTreeCheckboxViewer(fileTree);
			fileTreeViewer.setContentProvider(new ITreeContentProvider() {

				public Object[] getChildren(Object parentElement) {
					if( parentElement instanceof File ) {
						return ((File)parentElement).listFiles();
					}
					return new Object[0];
				}

				public Object getParent(Object element) {
					return null;
				}

				public boolean hasChildren(Object element) {
					Object[] children = getChildren(element);
					if( children == null || children.length == 0 ) return false;
					return true;
				}

				public Object[] getElements(Object inputElement) {
					String configHome = helper.getConfigurationPath();
					File configDir = new File(configHome);
					return configDir.listFiles();
				}

				public void dispose() {
				}

				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				} 
				
			});
			fileTreeViewer.setLabelProvider(new LabelProvider() {
					public String getText(Object o) {
						if( o instanceof File ) {
							return ((File)o).getName();
						}
						return "";
					}
					public Image getImage(Object o) {
						return null;
					}
			});
			
			
			FormData treeData = new FormData();
			treeData.left = new FormAttachment(0,5);
			treeData.top = new FormAttachment(configNameText, 10);
			treeData.right = new FormAttachment(100, -5);
			treeData.bottom = new FormAttachment(100, -20);
			fileTree.setLayoutData(treeData);
			fileTreeViewer.setInput(server);
			fileTreeViewer.setAllChecked(true);
		}
		
		protected void addBottomLabels(Composite c) {
			requiredCheckedLabel = new Label(c, SWT.NONE);
			requiredCheckedLabel.setText(Messages.CloneWizardRequiredCheckedLabel);
			FormData data = new FormData();
			data.left = new FormAttachment(0,5);
			data.top = new FormAttachment(fileTreeViewer.getTree(), 5);
			requiredCheckedLabel.setLayoutData(data);
		}
		
		
		public String[] getConfigNames(String serverHome) {
			ArrayList configList = new ArrayList();
			File serverDirectory = new File(serverHome + File.separator + "server");
			
			if (serverDirectory.exists()) {
				File types[] = serverDirectory.listFiles();
				for (int i = 0; i < types.length; i++) {
					File serviceDescriptor = new File(
						types[i].getAbsolutePath() + File.separator +
						"conf" + File.separator + "jboss-service.xml");
					
					if (types[i].isDirectory() && serviceDescriptor.exists()) {
						configList.add(types[i].getName());
					}
				}
			}

			String[] asStrings = new String[configList.size()];
			configList.toArray(asStrings);
			return asStrings;
		}
		

		public File[] viewerToFileArray() {
			return fileTreeViewer.toFileArray();
		}
		
	}


	
	protected class FileTreeCheckboxViewer extends CheckboxTreeViewer {
	    public FileTreeCheckboxViewer(Tree tree) {
			super(tree);
		}

		protected void createTreeItem(Widget parent, Object element, int index) {
	    	// super 
	        Item item = newItem(parent, SWT.NULL, index);
	        updateItem(item, element);
	        updatePlus(item, element);

	        if( parent instanceof TreeItem ) {
	        	boolean checked = getChecked(((TreeItem)parent).getData());
	        	boolean grayed = getGrayed(((TreeItem)parent).getData());
	        	((TreeItem)item).setChecked(checked);
	        	((TreeItem)item).setGrayed(!checked || grayed);
	        }
	    }
	    
	    protected void handleTreeExpand(TreeEvent event) {
	    	super.handleTreeExpand(event);
	    	if( event.item instanceof TreeItem ) {
	    		Object expanded = ((TreeItem)event.item).getData();
	    		if( getGrayed(expanded) || !getChecked(expanded)) {
	    			TreeItem[] items = ((TreeItem)event.item).getItems();
	    			for( int i = 0; i < items.length; i++ ) {
	    				setGrayRecurse(items[i], true);
	    			}
	    		}
	    	}
	    }
	    
	    protected void handleSelect(SelectionEvent event) { 
	    	//super.handleSelect(event);
	    	TreeItem item = ((TreeItem)event.item);

	    	if( !childrenAreLoaded(item)) {
	    		if( isRequired(item)) {
	    			item.setChecked(true);
	    		}
	    		return;
	    	}
	    	
	    	// three states:  checked -> required,   checked -> full, unchecked -> whatever (gray)
	    	
	    	// get current state
	    	boolean beforeChecked = !item.getChecked(); // status before it was clicked
	    	boolean found = false;
	    	TreeItem[] kids = item.getItems();
	    	for( int i = 0; i < kids.length; i++ ) {
	    		if( kids[i].getChecked() && !isRequired(kids[i])) 
	    			found = true;
	    	}
	    	
	    	
	    	
	    	if( beforeChecked && found ) {
	    		// it was checked and minimal++ was found. Set to minimal
	    		item.setChecked(true);
	    		setRequiredCheckedRecurse(item);
	    	} else if( beforeChecked && !found ) {
	    		// it was checked, and minimal was found. Set to unchecked ONLY IF NOT REQUIRED
	    		if( isRequired(item)) {
	    			item.setChecked(true);
		    		setCheckedRecurse(item, true);
		    		setGrayRecurse(item, false);
	    		} else {
		    		item.setChecked(false);
		    		setGrayRecurse(item, true);
	    		}
	    	} else {
	    		// it was not checked.
	    		item.setChecked(true);
	    		setCheckedRecurse(item, true);
	    		setGrayRecurse(item, false);
	    	}

	    	getContainer().updateButtons();

	    }
	    
	    private boolean isRequired(TreeItem item) {
	    	if( item.getData() == null ) { 
	    		ASDebug.p("Null", this); 
	    		return false; 
	    	}
	    	return isRequired((File)item.getData());
	    }
	    
	    private boolean isRequired(File file) {
	    	String path = file.getAbsolutePath();
	    	String configPath = server.getAttributeHelper().getConfigurationPath();
    		String remainder = path.substring(configPath.length()+1);
    		if(minimalList.contains(remainder)) {
    			return true;
    		}
    		return false;
	    }
	    private void setGrayRecurse(TreeItem item, boolean grayed) {
	    	TreeItem[] items = item.getItems();
	    	for( int i = 0; i < items.length; i++ ) {
	    		items[i].setGrayed(grayed);
    			setGrayRecurse(items[i], grayed);
	    	}
	    }
	    private void setCheckedRecurse(TreeItem item, boolean checked) {
	    	TreeItem[] items = item.getItems();
	    	for( int i = 0; i < items.length; i++ ) {
	    		items[i].setChecked(checked);
    			setCheckedRecurse(items[i], checked);
	    	}
	    }
	    private void setRequiredCheckedRecurse(TreeItem item) {
	    	TreeItem[] items = item.getItems();
	    	for( int i = 0; i < items.length; i++ ) {
	    		if( isRequired(items[i])) {
	    			items[i].setChecked(true);
	    			setRequiredCheckedRecurse(items[i]);
	    		} else {
	    			items[i].setChecked(false);
	    			setCheckedRecurse(items[i], false);
	    		}
	    	}
	    }
	    
	    public File[] toFileArray() {
	    	ArrayList list = new ArrayList();
	    	Tree t = (Tree)getControl();
	    	TreeItem[] items = t.getItems();
	    	for( int i = 0; i < items.length; i++ ) {
	    		if( getChecked(items[i].getData())) {
	    			File f = (File)items[i].getData();
	    			list.add(f);
	    			if( childrenAreLoaded(items[i])) {
	    				addCheckedChildrenToList(list, items[i]);
	    			} else {
	    				// needs fixing.
	    				addAllChildrenToList(list, items[i].getData());
	    			}
	    		}
	    	}
	    	File ret[] = new File[list.size()];
	    	list.toArray(ret);
	    	return ret;
	    }
	    
	    public void addCheckedChildrenToList(ArrayList list, TreeItem item) {
	    	Item[] items = getChildren(item);
	    	
	    	// for this directory, are ONLY required checked?
	    	boolean onlyRequired = true;
	    	for( int i = 0; i < items.length; i++ ) {
	    		if( ((TreeItem)items[i]).getChecked() && !isRequired((TreeItem)items[i])) 
	    			onlyRequired = false;
	    	}

	    	
	    	for( int i = 0; i < items.length; i++ ) {
		    	if( items[i] instanceof TreeItem ) {
		    		if( getChecked(items[i].getData())) {
		    			File f = (File)items[i].getData();
		    			list.add(f);
		    			if( childrenAreLoaded((TreeItem)items[i])) {
		    				addCheckedChildrenToList(list, (TreeItem)items[i]);
		    			} else {
		    				// If things other than the required are selected, load all children
		    				if( !onlyRequired ) { 
		    					addAllChildrenToList(list, ((TreeItem)items[i]).getData());
		    				} else {
		    					addAllRequiredChildrenToList(list, ((TreeItem)items[i]).getData());
		    				}
		    			}
		    		} else {
		    			//ASDebug.p("NON CHECKED ITEM *** " + items[i].getData(), this);
		    		}

		    	}
	    	}
	    }
	    
	    
	    private void addAllRequiredChildrenToList(ArrayList list, Object item) {
	    	if( item != null && item instanceof File && ((File)item).isDirectory()) {
	    		File[] files = ((File)item).listFiles();
	    		for( int i = 0; i < files.length; i++ ) {
	    			if( isRequired(files[i])) {
	    				list.add(files[i]);
	    				addAllRequiredChildrenToList(list, files[i]);
	    			}
	    		}
	    	}
	    }
	    
	    private void addAllChildrenToList(ArrayList list, Object item) {
	    	if( item != null && item instanceof File && ((File)item).isDirectory()) {
	    		File[] files = ((File)item).listFiles();
	    		for( int i = 0; i < files.length; i++ ) {
	    			list.add(files[i]);
	    			addAllChildrenToList(list, files[i]);
	    		}
	    	}
	    }
	    
	    
	    private boolean childrenAreLoaded(TreeItem item) {
	        final Item[] tis = getChildren(item);
	        if (tis != null && tis.length > 0) {
	            Object data = tis[0].getData();
	            if (data != null) {
					return true; // children already there!
				}
	        }
	        return false;
	    }
	}


	
}
