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

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.URIish;
import org.jboss.ide.eclipse.as.egit.core.EGitUtils;
import org.jboss.tools.common.ui.databinding.ObservableUIPojo;
import org.jboss.tools.openshift.express.client.IApplication;
import org.jboss.tools.openshift.express.client.IUser;
import org.jboss.tools.openshift.express.client.OpenshiftException;

/**
 * @author André Dietisheim
 */
public class ServerAdapterWizardModel extends ObservableUIPojo {

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

	public void setupProject() throws OpenshiftException, URISyntaxException, InvocationTargetException, InterruptedException, IOException {
		String applicationWorkingdir = "openshift-" + application.getName();
//		File workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
//		File workDir = new File(workspace, applicationWorkingdir);
//		URIish gitUri = new URIish(application.getGitUri());
//		new CloneOperation(gitUri, true, null, workDir, "refs/heads/*", "master", 10 * 1024).run(null);
		Repository repository = createRepository(applicationWorkingdir);
		// TODO replace remote name by user setting
		EGitUtils.addRemoteTo("openshift", new URIish(application.getGitUri()), repository);
		
		createServerAdapterIfRequired();
	}
	
	private void createServerAdapterIfRequired() {
		// TODO
	}

	private Repository createRepository(String name) throws IOException {
		InitCommand init = Git.init();
		IPath workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		IPath gitRepoProject = workspace.append(name);
		File repositoryFile = new File(gitRepoProject.toFile(), Constants.DOT_GIT);
		init.setDirectory(repositoryFile);
		init.setBare(false);
		Git git = init.call();
		return git.getRepository();
	}
	
}
