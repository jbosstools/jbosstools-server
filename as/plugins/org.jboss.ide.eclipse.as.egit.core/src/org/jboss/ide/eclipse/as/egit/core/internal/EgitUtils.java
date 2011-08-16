package org.jboss.ide.eclipse.as.egit.core.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.egit.core.op.CommitOperation;

public class EgitUtils {

	public static void commit(IProject project) throws CoreException {

		IFile[] commitables = new IFile[]{};
		Collection<IFile> untracked = Collections.emptyList();
		CommitOperation op = new CommitOperation(
				commitables,
				new ArrayList<IFile>(),
				untracked,
				"dummyAuthor", 
				"dummyCommitter", 
				"Initial commit");
		op.execute(null);
	}
}
