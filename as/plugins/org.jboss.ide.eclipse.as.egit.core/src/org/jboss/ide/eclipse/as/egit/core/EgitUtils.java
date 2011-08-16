package org.jboss.ide.eclipse.as.egit.core;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.egit.core.op.CommitOperation;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.UserConfig;

public class EgitUtils {

	public static void commit(IProject project) throws CoreException {

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
		op.setRepository(repository );
		op.execute(null);
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
