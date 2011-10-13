/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.openshift.express.internal.ui.wizard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.egit.core.RepositoryUtil;
import org.eclipse.egit.core.op.CloneOperation;
import org.eclipse.egit.core.op.ConnectProviderOperation;
import org.eclipse.egit.ui.Activator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.tools.common.ui.databinding.ObservableUIPojo;
import org.jboss.tools.openshift.express.client.IApplication;
import org.jboss.tools.openshift.express.client.IUser;
import org.jboss.tools.openshift.express.client.OpenshiftException;
import org.jboss.tools.openshift.express.internal.core.behaviour.ExpressServerUtils;
import org.jboss.tools.openshift.express.internal.ui.OpenshiftUIActivator;
import org.jboss.tools.openshift.express.internal.ui.wizard.projectimport.GeneralProjectImportOperation;
import org.jboss.tools.openshift.express.internal.ui.wizard.projectimport.MavenProjectImportOperation;

/**
 * @author André Dietisheim <adietish@redhat.com>
 */
public class ImportProjectWizardModel extends ObservableUIPojo {

	private static final int CLONE_TIMEOUT = 10 * 1024;

	private HashMap<String, Object> dataModel = new HashMap<String, Object>();

	public static final String USER = "user";
	public static final String APPLICATION = "application";
	public static final String REMOTE_NAME = "remoteName";
	public static final String REPOSITORY_PATH = "repositoryPath";

	public void setProperty(String key, Object value) {
		Object oldVal = dataModel.get(key);
		dataModel.put(key, value);
		firePropertyChange(key, oldVal, value);
	}

	public Object getProperty(String key) {
		return dataModel.get(key);
	}

	public void setUser(IUser user) {
		setProperty(USER, user);
	}

	public IUser getUser() {
		return (IUser)getProperty(USER);
	}

	public IApplication getApplication() {
		return (IApplication) getProperty(APPLICATION);
	}

	public void setApplication(IApplication application) {
		setProperty(APPLICATION, application);
	}

	public String setRemoteName(String remoteName) {
		setProperty(REMOTE_NAME, remoteName);
		return remoteName;
	}

	public String getRemoteName() {
		return (String) getProperty(REMOTE_NAME);
	}

	public String setRepositoryPath(String repositoryPath) {
		setProperty(REPOSITORY_PATH, repositoryPath);
		return repositoryPath;
	}

	public String getRepositoryPath() {
		return (String) getProperty(REPOSITORY_PATH);
	}

	public void importProject(final File projectFolder, IProgressMonitor monitor) throws OpenshiftException,
			CoreException,
			InterruptedException {
		new WorkspaceJob(NLS.bind("Importing projects from {0}", projectFolder.getAbsolutePath())) {

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				try {
					MavenProjectImportOperation mavenImport = new MavenProjectImportOperation(projectFolder);
					List<IProject> importedProjects = Collections.emptyList();
					if (mavenImport.isMavenProject()) {
						importedProjects = mavenImport.importToWorkspace(monitor);
					} else {
						importedProjects = new GeneralProjectImportOperation(projectFolder).importToWorkspace(monitor);
					}

					File gitFolder = new File(projectFolder, Constants.DOT_GIT);
					connectToGitRepo(importedProjects, gitFolder, monitor);

					createServerAdapterIfRequired();
					return Status.OK_STATUS;
				} catch (Exception e) {
					IStatus status = new Status(IStatus.ERROR, OpenshiftUIActivator.PLUGIN_ID,
							NLS.bind("Could not import projects from {0}", projectFolder.getAbsolutePath()), e);
					OpenshiftUIActivator.log(status);
					return status;
				}
			}
		}.schedule();
	}

	private void connectToGitRepo(List<IProject> projects, File gitFolder, IProgressMonitor monitor)
			throws CoreException {
		for (IProject project : projects) {
			connectToGitRepo(project, gitFolder, monitor);
		}
	}

	private void connectToGitRepo(IProject project, File gitFolder, IProgressMonitor monitor) throws CoreException {
		new ConnectProviderOperation(project, gitFolder).execute(monitor);
	}

	public File cloneRepository(IProgressMonitor monitor) throws URISyntaxException, OpenshiftException,
			InvocationTargetException,
			InterruptedException {
		IApplication application = getApplication();
		File destination = new File(getRepositoryPath(), application.getName());
		cloneRepository(application.getGitUri(), destination, monitor);
		return destination;
	}

	private void cloneRepository(String uri, File destination, IProgressMonitor monitor) throws URISyntaxException,
			OpenshiftException,
			InvocationTargetException,
			InterruptedException {
		if (destination.exists()) {
			FileUtil.completeDelete(destination);
		}
		ensureEgitUIIsStarted();
		URIish gitUri = new URIish(uri);
		CloneOperation cloneOperation =
				new CloneOperation(gitUri, true, null, destination, Constants.HEAD, getRemoteName(), CLONE_TIMEOUT);
		cloneOperation.run(monitor);
		RepositoryUtil repositoryUtil = Activator.getDefault().getRepositoryUtil();
		repositoryUtil.addConfiguredRepository(new File(destination, Constants.DOT_GIT));
	}

	/**
	 * The EGit UI plugin initializes the ssh factory to present the user a
	 * passphrase prompt if the ssh key was not read yet. If this initialization
	 * is not executed, the ssh connection to the git repo would just fail with
	 * an authentication error. We therefore have to make sure that the EGit UI
	 * plugin is started and initializes the JSchConfigSessionFactory.
	 * <p>
	 * EGit initializes the SshSessionFactory with the EclipseSshSessionFactory.
	 * The EclipseSshSessionFactory overrides JschConfigSessionFactory#configure
	 * to present a UserInfoPrompter if the key passphrase was not entered
	 * before.
	 * 
	 * @see Activator#start(org.osgi.framework.BundleContext)
	 * @see Activator#setupSSH
	 * @see JschConfigSessionFactory#configure
	 * @see EclipseSshSessionFactory#configure
	 */
	private void ensureEgitUIIsStarted() {
		Activator.getDefault();
	}

	private void createServerAdapterIfRequired() {
		// TODO
		Boolean b = (Boolean)getProperty(AdapterWizardPageModel.CREATE_SERVER);
		if( b != null && b.booleanValue() ) {
			IServerType type = (IServerType)getProperty(AdapterWizardPageModel.SERVER_TYPE);
			IRuntime rt = (IRuntime)getProperty(AdapterWizardPageModel.RUNTIME_DELEGATE);
			String mode = (String)getProperty(AdapterWizardPageModel.MODE);
			
			try {
				IServer server = ExpressServerUtils.createServer(rt, type, "Openshift Server1");
				ExpressServerUtils.fillServerWithOpenshiftDetails(server, getApplication().getApplicationUrl(), 
						getUser().getRhlogin(), getUser().getPassword(), 
						getUser().getDomain().getRhcDomain(), getApplication().getName(), mode);
			} catch(CoreException ce) {
				OpenshiftUIActivator.getDefault().getLog().log(ce.getStatus());
			} catch( OpenshiftException ose) {
				IStatus s = new Status(IStatus.ERROR, OpenshiftUIActivator.PLUGIN_ID, "Cannot create openshift server adapter", ose);
				OpenshiftUIActivator.getDefault().getLog().log(s);
			}
		}
	}
}
