package org.jboss.ide.eclipse.as.ui.packages;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.model.PackagesBuildListener;
import org.jboss.ide.eclipse.as.core.server.attributes.IDeployableServer;
import org.jboss.ide.eclipse.packages.core.model.IPackage;
import org.jboss.ide.eclipse.packages.core.model.IPackageNode;
import org.jboss.ide.eclipse.packages.ui.actions.AbstractNodeActionDelegate;

public class PublishAction extends AbstractNodeActionDelegate {

	
	public PublishAction() {
	}

	public void run() {
		Object o = getSelection().getFirstElement();
		if( o instanceof IPackage ) {
			IPackage node = (IPackage)o;
			String servers = node.getProperty(PackagesBuildListener.DEPLOY_SERVERS);
			if( servers == null || "".equals(servers)) {
				servers = showSelectServersDialog(node);
			}
			PackagesBuildListener.publish(node);
		}
	}
	
	public boolean isEnabledFor(IPackageNode node) {
		if (node.getNodeType() == IPackageNode.TYPE_PACKAGE
			|| node.getNodeType() == IPackageNode.TYPE_PACKAGE_REFERENCE)
		{
			IPackage pkg = (IPackage) node;
			if (pkg.isTopLevel())
			{
				return true;
			}
		}
		return false;
	}
	
	protected String showSelectServersDialog(IPackage node) {
		String servers = null;
		SelectServerWizard wiz = new SelectServerWizard(node);
		new WizardDialog(new Shell(), wiz).open();
		return node.getProperty(PackagesBuildListener.DEPLOY_SERVERS);
	}

	
	protected class SelectServerWizard extends Wizard {
		private SelectServerWizardPage page;
		private IPackage pack;
		protected SelectServerWizard(IPackage pack) {
			this.pack = pack;
		}
		public boolean performFinish() {
			pack.setProperty(PackagesBuildListener.DEPLOY_SERVERS, getServers());
			pack.setProperty(PackagesBuildListener.AUTO_DEPLOY, getAutoDeploy());
			return true;
		}
		public void addPages() {
			page = new SelectServerWizardPage(pack);
			addPage(page);
		}

		protected String getServers() {
			return page.getServers();
		}
		protected String getAutoDeploy() {
			return page.getAutoDeploy();
		}
	}
	
	protected class SelectServerWizardPage extends WizardPage {
		protected IPackage pack;
		protected ListViewer viewer;
		protected Button autoDeploy;
		protected String viewerResult = "";
		protected String deployResult = Boolean.toString(false);
		
		protected SelectServerWizardPage(IPackage pack) {
			super("Wizard Page Name");
			setDescription("Wizard Page Description");
			setTitle("Wizard Page Title");
			this.pack = pack;
		}

		public void createControl(Composite parent) {
			Composite mainComposite = new Composite(parent, SWT.NONE);
			mainComposite.setLayout(new FormLayout());
			mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			fillComposite(mainComposite);
			addListeners();
			setControl(mainComposite);
		}

		protected void fillComposite(Composite mainComposite) {
			viewer = new ListViewer(mainComposite);
			FormData viewerData = new FormData();
			viewerData.left = new FormAttachment(15,0);
			viewerData.right = new FormAttachment(85,0);
			viewerData.top = new FormAttachment(0,10);
			viewerData.bottom = new FormAttachment(80,0);
			viewer.getList().setLayoutData(viewerData);
			
			
			
			autoDeploy = new Button(mainComposite, SWT.CHECK);
			FormData add = new FormData();
			add.left = new FormAttachment(15,0);
			add.top = new FormAttachment(viewer.getList(), 5);
			autoDeploy.setLayoutData(add);
			
			Label autoDeployLabel = new Label(mainComposite, SWT.NONE);
			FormData adl = new FormData();
			adl.top = new FormAttachment(viewer.getList(), 5);
			adl.left = new FormAttachment(autoDeploy, 5);
			autoDeployLabel.setLayoutData(adl);
			autoDeployLabel.setText("Label Text");
		}
		protected void addListeners() {
			autoDeploy.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
				public void widgetSelected(SelectionEvent e) {
					deployResult = Boolean.toString(autoDeploy.getSelection());
				}
			});
			viewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
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
			} );
			
			viewer.setContentProvider(new ArrayContentProvider());
			viewer.setLabelProvider(new PublishServerLabelProvider());
			viewer.setInput(JBossServerCore.getAllDeployableServers());
		}
			
		protected String getServers() {
			return viewerResult;
		}
		protected String getAutoDeploy() {
			return deployResult;
		}
	}
	protected class PublishServerLabelProvider extends LabelProvider {
	    public Image getImage(Object element) {
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
