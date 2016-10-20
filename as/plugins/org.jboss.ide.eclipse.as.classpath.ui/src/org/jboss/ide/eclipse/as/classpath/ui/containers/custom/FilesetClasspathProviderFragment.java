/*******************************************************************************
 * Copyright (c) 2011-2104 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.ui.containers.custom;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.server.core.IRuntimeType;
import org.jboss.ide.eclipse.archives.webtools.filesets.FilesetComposite;
import org.jboss.ide.eclipse.as.classpath.core.runtime.path.internal.Fileset;
import org.jboss.ide.eclipse.as.classpath.core.runtime.path.internal.RuntimePathProviderFileset;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.tools.foundation.ui.xpl.taskwizard.IWizardHandle;
import org.jboss.tools.foundation.ui.xpl.taskwizard.WizardFragment;

public class FilesetClasspathProviderFragment extends WizardFragment implements FilesetComposite.IFilesetCompositeErrorDisplay{
	private IWizardHandle handle;
	private FilesetComposite composite;
	public boolean hasComposite() {
		return true;
	}

	/**
	 * Creates the composite associated with this fragment.
	 * This method is only called when hasComposite() returns true.
	 * 
	 * @param parent a parent composite
	 * @param handle a wizard handle
	 * @return the created composite
	 */
	public Composite createComposite(Composite parent, IWizardHandle handle) {
		this.handle = handle;
		handle.setTitle("Create a fileset classpath entry.");
		handle.setDescription("Create a new classpath fileset which can be added to all projects targeting this runtime-type");
		
		IRuntimeType rtt = (IRuntimeType)getTaskModel().getObject(RuntimeClasspathProviderWizard.RUNTIME_TYPE);
		ServerExtendedProperties props = rtt == null ? null : 
			(ServerExtendedProperties)Platform.getAdapterManager().getAdapter(rtt, ServerExtendedProperties.class);
		String defaultLocation = props == null ? "" : props.getNewClasspathFilesetDefaultRootFolder();

		Fileset fs = new Fileset();
        fs.setFolder(defaultLocation);
        fs.setIncludesPattern("**/*.jar");

		composite = new FilesetComposite(parent, fs, false, false);
		composite.setErrorDisplay(this);
		return composite;
	}
	

	public void performFinish(IProgressMonitor monitor) throws CoreException {
		Fileset fs = composite.getFileset();
		RuntimePathProviderFileset rppf = new RuntimePathProviderFileset(fs);
		getTaskModel().putObject(RuntimeClasspathProviderWizard.CREATED_PATH_PROVIDER, rppf);
	}

	@Override
	public void updateError(String msg) {
		handle.setMessage(msg, IMessageProvider.ERROR);
	}
	
}
