/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.egit.core;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.egit.core.op.CommitOperation;
import org.eclipse.egit.core.op.PushOperation;
import org.eclipse.egit.core.op.PushOperationSpecification;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.UserConfig;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.osgi.util.NLS;
import org.jboss.ide.eclipse.as.egit.core.internal.EGitCoreActivator;

/**
 * @author André Dietisheim
 */
public class EgitUtils {

	public static void commit(IProject project) throws CoreException {
		commit(project, null);
	}

	public static void commit(IProject project, IProgressMonitor monitor) throws CoreException {

		IFile[] commitables = new IFile[] {};
		Collection<IFile> untracked = Collections.emptyList();
		Repository repository = getRepository(project);
		UserConfig userConfig = getUserConfig(repository);
		CommitOperation op = new CommitOperation(
				commitables,
				null, // committables
				null, // untracked
				getSubject(userConfig.getAuthorName(), userConfig.getAuthorEmail()),
				getSubject(userConfig.getCommitterName(), userConfig.getCommitterEmail()),
				"Initial commit");
		op.setCommitAll(true);
		op.setRepository(repository);
		op.execute(monitor);
	}

	public static void push(IProject project, Repository destinationRepository) throws CoreException {
		push(project, destinationRepository, null);
	}

	public static void push(IProject project, Repository destinationRepository, IProgressMonitor monitor)
			throws CoreException {
		push(getRepository(project), destinationRepository, monitor);
	}

	public static void push(Repository sourceRepository, Repository destinationRepository, IProgressMonitor monitor)
			throws CoreException {
		try {
			PushOperationSpecification spec = new PushOperationSpecification();
			URIish destinationURIisch = getPushUri(destinationRepository);
			List<RemoteRefUpdate> refUpdates = new ArrayList<RemoteRefUpdate>();
			RemoteRefUpdate update =
					new RemoteRefUpdate(destinationRepository, "HEAD", "refs/heads/test", false, null, null);
			refUpdates.add(update);
			spec.addURIRefUpdates(destinationURIisch, refUpdates);
			PushOperation pop =
					new PushOperation(sourceRepository, spec, false, 0);
			pop.run(monitor);
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, EGitCoreActivator.PLUGIN_ID,
					NLS.bind("Could not push repo {0} to {1}", sourceRepository.toString(),
							destinationRepository.toString()), e);
			throw new CoreException(status);
		}
	}

	/**
	 * Gets the first push uri configured for the given repository or <code>null</code>.
	 *
	 * @param repository the repository
	 * @return the push uri
	 * @throws URISyntaxException the uRI syntax exception
	 */
	private static URIish getPushUri(Repository repository) throws URISyntaxException {
//		return new URIish("file:///" + destinationRepository.getDirectory().toString());
		RemoteConfig remoteConfig = new RemoteConfig(repository.getConfig(), "");
		List<URIish> pushURIs = remoteConfig.getPushURIs();
		if (pushURIs == null || pushURIs.isEmpty()) {
			return null;
		}
		return pushURIs.get(0);
	}

	private static Repository getRepository(IProject project) {
		RepositoryMapping repositoryMapping = RepositoryMapping.getMapping(project);
		if (repositoryMapping == null) {
			return null;
		}
		return repositoryMapping.getRepository();
	}

	private static UserConfig getUserConfig(Repository repository) {
		return repository.getConfig().get(UserConfig.KEY);
	}

	private static String getSubject(String name, String email) {
		return new StringBuilder().append(name).append(" <").append(email).append('>').toString();
	}
}
