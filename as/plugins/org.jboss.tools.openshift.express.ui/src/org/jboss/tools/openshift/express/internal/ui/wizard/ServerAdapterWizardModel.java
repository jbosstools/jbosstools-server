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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.egit.core.op.CloneOperation;
import org.eclipse.egit.ui.Activator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.InvalidMergeHeadsException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.tools.common.ui.databinding.ObservableUIPojo;
import org.jboss.tools.openshift.express.client.IApplication;
import org.jboss.tools.openshift.express.client.IUser;
import org.jboss.tools.openshift.express.client.OpenshiftException;

/**
 * @author André Dietisheim
 */
public class ServerAdapterWizardModel extends ObservableUIPojo {

	private static final String REMOTE_NAME = "openshift";
	private IUser user;
	private IApplication application;

	public void setUser(IUser user) {
		this.user = user;
	}

	public IUser getUser() {
		return user;
	}

	public IApplication getApplication() {
		return application;
	}

	public void setApplication(IApplication application) {
		this.application = application;
	}

	public void importProject(File projectDirectory, IProgressMonitor monitor) throws OpenshiftException,
			URISyntaxException,
			InvocationTargetException, InterruptedException, IOException, NoHeadException,
			ConcurrentRefUpdateException, CheckoutConflictException, InvalidMergeHeadsException,
			WrongRepositoryStateException, NoMessageException, CoreException {
		final String projectName = projectDirectory.getName();

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = workspaceRoot.getProject(projectName);
		overwriteExistingProject(project, monitor);

		importToNewProject(projectDirectory, monitor, project);

		createServerAdapterIfRequired();
	}

	private void importToNewProject(File projectDirectory, IProgressMonitor monitor, IProject project)
			throws CoreException, InvocationTargetException, InterruptedException {
		project.create(monitor);
		project.open(monitor);
		ImportOperation operation =
				new ImportOperation(
						project.getFullPath()
						, projectDirectory
						, FileSystemStructureProvider.INSTANCE
						, new IOverwriteQuery() {
							public String queryOverwrite(String file) {
								return IOverwriteQuery.ALL;
							}
						});
		operation.setCreateContainerStructure(false);
		operation.run(monitor);
	}

	private void overwriteExistingProject(final IProject project, IProgressMonitor monitor)
			throws CoreException {
		if (project == null
				|| !project.exists()) {
			return;
		}

		final boolean[] overwrite = new boolean[1];
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				overwrite[0] = MessageDialog.openQuestion(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						"Overwrite project?",
						NLS.bind(
								"A project \"{0}\" already exists in the workspace.\n"
										+ "If you want to import the OpenShift \"{0}\", the project in your workspace will "
										+ "get overwritten and may not be recovered.\n\n"
										+ "Are you sure that you want to overwrite the project \"{0}\" in your workspace?",
								project.getName()));
			}

		});
		if (overwrite[0]) {
			project.delete(true, true, monitor);
		}
	}

	public File cloneRepository(IProgressMonitor monitor) throws URISyntaxException, OpenshiftException,
			InvocationTargetException,
			InterruptedException {
		File destination = getDestinationDirectory(application);
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
		URIish gitUri = new URIish(uri);
		CloneOperation cloneOperation =
				new CloneOperation(gitUri, true, null, destination, Constants.HEAD, "origin", 10 * 1024);
		cloneOperation.run(null);
		File gitDirectory = new File(destination, Constants.DOT_GIT);
		Activator.getDefault().getRepositoryUtil().addConfiguredRepository(gitDirectory);
	}

	private boolean isEclipseProject(File destination) {
		if (isReadable(destination)) {
			return false;
		}

		return isReadable(new File(destination, ".project"));

	}

	private boolean isMavenProject(File destination) {
		if (isReadable(destination)) {
			return false;
		}

		return isReadable(new File(destination, "pom.xml"));
	}

	private boolean isReadable(File destination) {
		return destination == null
				|| !destination.exists()
				|| !destination.canRead();
	}

	private File getDestinationDirectory(IApplication application) {
		String applicationDirectory = "openshift-" + application.getName();
		// File workspace =
		// ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
		String userHome = System.getProperty("java.io.tmpdir");
		// File workDir = new File(workspace, applicationWorkingdir);
		return new File(userHome, applicationDirectory);
	}

	private void mergeWithRemote(Git git, String remoteName) throws CoreException, NoHeadException,
			ConcurrentRefUpdateException, CheckoutConflictException, InvalidMergeHeadsException,
			WrongRepositoryStateException, NoMessageException, IOException {
		Repository repository = git.getRepository();
		ObjectId objectId = repository.resolve("HEAD");
		git.merge().include(objectId).setStrategy(MergeStrategy.OURS).call();
	}

	private void createServerAdapterIfRequired() {
		// TODO
	}

	private Git createGit(File repositoryFile) throws IOException {
		InitCommand init = Git.init();
		init.setDirectory(repositoryFile);
		init.setBare(false);
		return init.call();
	}

	private File createRepositoryFile(String name) {
		IPath workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		IPath gitRepoProject = workspace.append(name);
		File repositoryFile = new File(gitRepoProject.toFile(), Constants.DOT_GIT);
		return repositoryFile;
	}

}
