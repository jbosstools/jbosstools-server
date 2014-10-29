/******************************************************************************* 
* Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.editor.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.core.server.IDeploymentScannerModifier;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.UIUtil;
import org.jboss.ide.eclipse.as.ui.editor.DeploymentPage;
import org.jboss.ide.eclipse.as.ui.editor.IDeploymentPageUIController;
import org.jboss.ide.eclipse.as.ui.editor.ModuleDeploymentOptionsComposite;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;

/**
 * The standard page for showing deployment options 
 * for filesystem-based deployment
 * 
 */
public class StandardDeploymentPageController extends
		AbstractSubsystemController implements IDeploymentPageUIController, PropertyChangeListener {

	protected DeploymentPage page;
	protected JBossDeploymentOptionsComposite standardOptions;
	protected ModuleDeploymentOptionsComposite perModuleOptions;
	protected Label errorLabel;
	protected Label errorImage;
	
	public DeploymentPage getPage() {
		return page;
	}
	
	@Override
	public void serverChanged(ServerEvent event) {
		setDeploymentTabEnablement();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		if( standardOptions != null ) 
			standardOptions.updateListeners();
		if( perModuleOptions != null )
			perModuleOptions.updateListeners();
		
		IServer s = getServer();
		if( s.getServerState() == IServer.STATE_STARTED ) {
			JBossExtendedProperties properties = (JBossExtendedProperties)s.loadAdapter(JBossExtendedProperties.class, null);
			if( properties != null ) {
				IDeploymentScannerModifier modifier = properties.getDeploymentScannerModifier();
				if( modifier != null ) {
					Job scannerJob = modifier.getUpdateDeploymentScannerJob(s);
					if( scannerJob != null )
						scannerJob.schedule();
				}
			}
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input, DeploymentPage page) {
		this.page = page;
	}
	
	private void setDeploymentTabEnablement() {
		// This is a big hack due to https://bugs.eclipse.org/bugs/show_bug.cgi?id=386718
		// IT seems getting the NEW module list from the event is not possible,
		// and figuring out if a module was added also does not seem to be possible
		new Thread() {
			public void run() {
				try {
					Thread.sleep(300);
				} catch(InterruptedException ie) {}
				
				Display.getDefault().asyncExec(new Runnable() { 
					public void run() {
						updateWidgetEnablement();
					}
				});
			}
		}.start();
	}
	
	
	/**
	 * Update the enablement for the pages' widgets based on the most recent changes to the server. 
	 */
	protected void updateWidgetEnablement() {
		final boolean enabled = shouldAllowModifications();
		if( standardOptions != null && !standardOptions.isDisposed())
			standardOptions.setEnabled(enabled);
		if( perModuleOptions != null && !perModuleOptions.isDisposed())
			perModuleOptions.setEnabled(enabled);
	}
	

	
	/**
	 * Whether or not the widgets should be editable in the server's current state
	 * @return
	 */
	protected boolean shouldAllowModifications() {
		IModule[] deployed = getServer().getModules();
		final boolean hasNoModules = deployed == null || deployed.length == 0;
		final boolean enabled =  hasNoModules && 
				(getServer().getServerPublishState() == IServer.PUBLISH_STATE_NONE
				|| getServer().getServerPublishState() == IServer.PUBLISH_STATE_UNKNOWN);
		return enabled;
	}
	
	public void createPartControl(Composite parent) {
		try {
			ScrolledForm innerContent = createPageStructure(parent);
			addDeploymentLocationControls(innerContent.getBody(), null);
			innerContent.reflow(true);
			validatePage();
			getPage().getServer().addPropertyChangeListener(this);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private ScrolledForm createPageStructure(Composite parent) {
		FormToolkit toolkit = getFormToolkit(parent);
		ScrolledForm allContent = toolkit.createScrolledForm(parent);
		toolkit.decorateFormHeading(allContent.getForm());
		allContent.setText(Messages.EditorDeployment);
		allContent.getBody().setLayout(new FormLayout());
		return allContent;
	}
	
	/**
	 * Clients are expected to override this method if they 
	 * require a custom layout with various composites
	 * @param parent
	 * @param top
	 */
	protected void addDeploymentLocationControls(Composite parent, Control top) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		Label l1 = toolkit.createLabel(parent, Messages.EditorDeploymentPageWarning);
		
		FormData fd = new FormData();
		fd.left = new FormAttachment(0, 5);
		fd.top = top == null ? new FormAttachment(0, 5) : new FormAttachment(top, 5); 
		fd.right = new FormAttachment(100, -5);
		l1.setLayoutData(fd);
		
		errorImage = toolkit.createLabel(parent, "");
		fd = new FormData();
		fd.top = new FormAttachment(l1, 5); 
		fd.left = new FormAttachment(0, 0);
		errorImage.setLayoutData(fd);


		errorLabel = toolkit.createLabel(parent, "");
		fd = new FormData();
		fd.top = new FormAttachment(l1, 5); 
		fd.left = new FormAttachment(0, 20);
		fd.right = new FormAttachment(100, -5);
		errorLabel.setLayoutData(fd);

		
		// First section is deployment mode (server / custom / metadata) etc. 
		standardOptions = createServerDeploymentOptions(parent);
		standardOptions.setLayoutData(UIUtil.createFormData2(errorLabel, 5, null,0,0,5,100,-5));
		
		// Simply create a composite to show the per-module customizations
		perModuleOptions = createModuleDeploymentOptions(parent);
		fd = new FormData();
		fd.left = new FormAttachment(0, 5);
		fd.top = new FormAttachment(standardOptions, 5);
		fd.right = new FormAttachment(100, -5);
		fd.bottom = new FormAttachment(100, -5);
		perModuleOptions.setLayoutData(fd);
	}
	
	private void validatePage() {
		IStatus[] all = standardOptions.validate();
		IStatus[] all2 = perModuleOptions.validate();
		// show highest status error
		IStatus highest = null;
		for( int i = 0; i < all.length; i++ ) {
			if( highest == null || all[i].getSeverity() > highest.getSeverity())
				highest = all[i];
		}
		for( int i = 0; i < all2.length; i++ ) {
			if( highest == null || all2[i].getSeverity() > highest.getSeverity())
				highest = all2[i];
		}
		setStatus(highest);
	}
	
	protected void setStatus(IStatus status) {
		if( status == null || status.isOK()) {
			errorLabel.setText("");
			errorImage.setImage(null);
		} else {
			errorLabel.setText(status.getMessage());
			errorLabel.setToolTipText(status.getMessage());
			Image toUse = null;
			switch(status.getSeverity()) {
				case IStatus.WARNING: toUse = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK); break;
				case IStatus.ERROR: toUse = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK); break;
				case IStatus.INFO: toUse = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK); break;
			}
			errorImage.setImage(toUse);
		}
	}
	
	protected JBossDeploymentOptionsComposite createServerDeploymentOptions(Composite parent) {
		return new JBossDeploymentOptionsComposite(parent, this);
	}
	
	protected ModuleDeploymentOptionsComposite createModuleDeploymentOptions(Composite parent) {
		return new ModuleDeploymentOptionsComposite(parent, getPage(), getFormToolkit(parent), getPage().getPreferences());
	}

	
	public FormToolkit getFormToolkit(Composite parent) {
		return page.getFormToolkit(parent);
	}

	@Override
	public void dispose() {
		getPage().getServer().removePropertyChangeListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		validatePage();
	}
}
