package org.jboss.ide.eclipse.as.ui.packages;

import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.ui.internal.ImageResource;
import org.jboss.ide.eclipse.archives.core.build.SaveArchivesJob;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.as.core.modules.ArchivesBuildListener;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public class ArchivePublishWizard extends Wizard {

	private ArchivePublishWizardPage page;
	private IArchive pack;
	public ArchivePublishWizard(IArchive pack) {
		this.pack = pack;
		setWindowTitle("Archive Publish Settings");
	}
	public boolean performFinish() {
		boolean alwaysPublish = new Boolean(page.getAlwaysPublish()).booleanValue();
		pack.setProperty(ArchivesBuildListener.DEPLOY_SERVERS, alwaysPublish ? getServers() : null);
		pack.setProperty(ArchivesBuildListener.DEPLOY_AFTER_BUILD, getAutoDeploy());
		final IPath p = pack.getProjectPath();
		new SaveArchivesJob(p).schedule();
		return true;
	}
	public void addPages() {
		page = new ArchivePublishWizardPage(pack);
		addPage(page);
	}

	protected String getServers() {
		return page.getServers();
	}
	protected String getAutoDeploy() {
		return page.getAutoDeploy();
	}
	protected String getAlwaysPublish() {
		return page.getAlwaysPublish();
	}

	
	public class ArchivePublishWizardPage extends WizardPage {
		protected IArchive pack;
		protected TableViewer viewer;
		protected Button autoDeploy, alwaysPublish;
		protected String viewerResult = "";
		protected String deployResult = Boolean.toString(false);
		protected String alwaysPublishResult = Boolean.toString(false);
		
		protected ArchivePublishWizardPage(IArchive pack) {
			super("Select Server Wizard");
			setDescription("Select the server to publish the archive to.");
			setTitle("Publish archive to a server");
			this.pack = pack;
		}

		public void createControl(Composite parent) {
			Composite mainComposite = new Composite(parent, SWT.NONE);
			mainComposite.setLayout(new FormLayout());
			mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			fillComposite(mainComposite);
			addListeners();
			setControl(mainComposite);
			getContainer().updateTitleBar();
			setPackageDefaults();
		}

		protected void setPackageDefaults() {
			String servers = pack.getProperty(ArchivesBuildListener.DEPLOY_SERVERS);
			viewerResult = servers;
			String deployAfterBuild = pack.getProperty(ArchivesBuildListener.DEPLOY_AFTER_BUILD);
			if( servers != null ) {
				alwaysPublish.setSelection(true);
				alwaysPublishSelected();
				boolean depAfterBld = !(deployAfterBuild == null || new Boolean(deployAfterBuild).booleanValue() == false); 
				autoDeploy.setSelection(depAfterBld);
				autoDeploySelected();
				
				IDeployableServer[] depServers = ServerConverter.getAllDeployableServers();
				String[] serverList = servers.split(",");
				final ArrayList<IDeployableServer> selected = new ArrayList<IDeployableServer>();
				for(int i = 0; i < serverList.length; i++ ) {
					for( int j = 0; j < depServers.length; j++ ) {
						if( serverList[i].equals(depServers[j].getServer().getId())) 
							selected.add(depServers[j]);
					}
				}
				viewer.setSelection(new StructuredSelection(selected.toArray()));
			}
		}
		
		protected void fillComposite(Composite mainComposite) {
			viewer = new TableViewer(mainComposite);
			FormData viewerData = new FormData();
			viewerData.left = new FormAttachment(15,0);
			viewerData.right = new FormAttachment(85,0);
			viewerData.top = new FormAttachment(0,10);
			viewerData.bottom = new FormAttachment(80,0);
			viewer.getTable().setLayoutData(viewerData);
			
			this.alwaysPublish = new Button(mainComposite, SWT.CHECK);
			FormData always = new FormData();
			always.left = new FormAttachment(15,0);
			always.top = new FormAttachment(viewer.getTable(), 5);
			alwaysPublish.setLayoutData(always);
			alwaysPublish.setText("Always publish to these servers");
			
			autoDeploy = new Button(mainComposite, SWT.CHECK);
			FormData add = new FormData();
			add.left = new FormAttachment(15,0);
			add.top = new FormAttachment(alwaysPublish, 5);
			autoDeploy.setLayoutData(add);
			autoDeploy.setText("Auto-deploy to selected servers after builds");
		}

		protected void addListeners() {
			autoDeploy.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
				public void widgetSelected(SelectionEvent e) {
					autoDeploySelected();
				}
			});
			viewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					viewerSelected();
				} 
			} );
			
			alwaysPublish.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
				public void widgetSelected(SelectionEvent e) {
					alwaysPublishSelected();
				} 
			});
			
			viewer.setContentProvider(new ArrayContentProvider());
			viewer.setLabelProvider(new ArchivePublishLabelProvider());
			viewer.setInput(ServerConverter.getAllDeployableServers());
			autoDeploy.setEnabled(false);
		}
		
		protected void autoDeploySelected() {
			deployResult = Boolean.toString(autoDeploy.getSelection() && autoDeploy.getEnabled());
		}
		
		protected void alwaysPublishSelected() {
			autoDeploy.setEnabled(alwaysPublish.getSelection());
			deployResult = Boolean.toString(autoDeploy.getSelection() && autoDeploy.getEnabled());
			alwaysPublishResult = Boolean.toString(alwaysPublish.getSelection());
		}
		
		protected void viewerSelected() {
			ISelection sel = viewer.getSelection();
			if( sel instanceof IStructuredSelection ) {
				IStructuredSelection sel2 = (IStructuredSelection)sel;
				Object[] os = sel2.toArray();
				String tmp = "";
				for( int i = 0; i < os.length; i++ ) {
					tmp += ((IDeployableServer)os[i]).getServer().getId() + ",";
				}
				viewerResult = tmp;
			}
		}
		protected String getServers() {
			return viewerResult;
		}
		protected String getAutoDeploy() {
			return deployResult;
		}
		protected String getAlwaysPublish() {
			return alwaysPublishResult;
		}
	}
	
	protected class ArchivePublishLabelProvider extends LabelProvider {
	    public Image getImage(Object element) {
	    	if( element instanceof IDeployableServer ) {
	    		IServerType type = ((IDeployableServer)element).getServer().getServerType();
	    		return ImageResource.getImage(type.getId());
	    	}
	        return null;
	    }
	    public String getText(Object element) {
	    	if( element instanceof IDeployableServer ) {
	    		return ((IDeployableServer)element).getServer().getName();
	    	}
	        return element == null ? "" : element.toString();//$NON-NLS-1$
	    }

	}
}