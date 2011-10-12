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
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.tools.common.ui.databinding.ObservableUIPojo;
import org.jboss.tools.openshift.express.client.IApplication;
import org.jboss.tools.openshift.express.client.IUser;
import org.jboss.tools.openshift.express.client.OpenshiftException;
import org.jboss.tools.openshift.express.internal.ui.OpenshiftUIActivator;
import org.jboss.tools.openshift.express.internal.ui.wizard.projectimport.GeneralProjectImportOperation;
import org.jboss.tools.openshift.express.internal.ui.wizard.projectimport.MavenProjectImportOperation;

/**
 * @author André Dietisheim <adietish@redhat.com>
 */
public class ApplicationWizardModel extends ObservableUIPojo {

	private HashMap<String, Object> dataModel = new HashMap<String, Object>();

	private static final String USER = "user";
	private static final String APPLICATION = "application";

	public void setProperty(String key, Object value) {
		dataModel.put(key, value);
	}

	public Object getProperty(String key) {
		return dataModel.get(key);
	}

	public void setUser(IUser user) {
		dataModel.put(USER, user);
	}

	public IUser getUser() {
		return (IUser) dataModel.get(USER);
	}

	public IApplication getApplication() {
		return (IApplication) dataModel.get(APPLICATION);
	}

	public void setApplication(IApplication application) {
		dataModel.put(APPLICATION, application);
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
		new ConnectProviderOperation(project).execute(monitor);
	}

	public File cloneRepository(IProgressMonitor monitor) throws URISyntaxException, OpenshiftException,
			InvocationTargetException,
			InterruptedException {
		File destination = getDestinationDirectory(getApplication());
		cloneRepository(getApplication().getGitUri(), destination, monitor);
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
		RepositoryUtil repositoryUtil = Activator.getDefault().getRepositoryUtil();
		CloneOperation cloneOperation =
				new CloneOperation(gitUri, true, null, destination, Constants.HEAD, "origin", 10 * 1024);
		cloneOperation.run(null);
		File gitDirectory = new File(destination, Constants.DOT_GIT);
		repositoryUtil.addConfiguredRepository(gitDirectory);
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

	private File getDestinationDirectory(IApplication application) {
		String applicationDirectory = application.getName();
		// File workspace =
		// ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
		String userHome = System.getProperty("java.io.tmpdir");
		// File workDir = new File(workspace, applicationWorkingdir);
		return new File(userHome, applicationDirectory);
	}

	private void createServerAdapterIfRequired() {
		// TODO
	}
}
