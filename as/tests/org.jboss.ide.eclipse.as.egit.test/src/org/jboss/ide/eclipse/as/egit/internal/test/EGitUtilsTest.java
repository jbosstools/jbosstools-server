package org.jboss.ide.eclipse.as.egit.internal.test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.egit.core.Activator;
import org.eclipse.egit.core.op.AddToIndexOperation;
import org.eclipse.egit.core.op.CloneOperation;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FileUtils;
import org.eclipse.jgit.util.SystemReader;
import org.jboss.ide.eclipse.as.egit.core.EgitUtils;
import org.jboss.ide.eclipse.as.egit.internal.test.util.MockSystemReader;
import org.jboss.ide.eclipse.as.egit.internal.test.util.TestProject;
import org.jboss.ide.eclipse.as.egit.internal.test.util.TestRepository;
import org.jboss.ide.eclipse.as.egit.internal.test.util.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EGitUtilsTest {

	protected final TestUtils testUtils = new TestUtils();
	private List<IResource> resources = new ArrayList<IResource>();

	private File gitDir;
	private TestRepository testRepository;
	private Repository repository;
	private TestProject testProject;
	private IProject project;
	private File clonedRepositoryFile;

	@Before
	public void setUp() throws Exception {
		Activator.getDefault().getRepositoryCache().clear();

		this.testProject = new TestProject(true);
		this.project = testProject.getProject();

		createMockSystemReader();

		this.gitDir = createGitDir(testProject);
		this.testRepository = new TestRepository(gitDir);
		this.repository = testRepository.getRepository();
		testRepository.connect(project);

		TestProject testProject2 = new TestProject(true);
		// File gitDir2 = createGitDir(testProject2);
		// TestRepository testRepository2 = new TestRepository(gitDir2);
		// Repository repository2 = testRepository.getRepository();

		this.clonedRepositoryFile = cloneRepository(repository);
	}

	private File cloneRepository(Repository repository) throws URISyntaxException, InvocationTargetException,
			InterruptedException {
		URIish uri = new URIish("file:///" + repository.getDirectory().toString());
		String repoParent = repository.getDirectory().getParent();
		File clonedRepository = new File("clonedRepository" + String.valueOf(System.currentTimeMillis()), repoParent);
		CloneOperation clop = new CloneOperation(uri, true, null, clonedRepository, "refs/heads/master", "origin", 0);
		clop.run(null);
		return clonedRepository;
	}

	private void createMockSystemReader() {
		MockSystemReader mockSystemReader = new MockSystemReader();
		SystemReader.setInstance(mockSystemReader);
		mockSystemReader.setProperty(Constants.GIT_CEILING_DIRECTORIES_KEY,
				ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile()
						.getAbsoluteFile().toString());
	}

	private File createGitDir(TestProject testProject) throws IOException {
		IPath workspaceRoot = project.getWorkspace().getRoot().getRawLocation();
		// IPath randomFolder =
		// workspaceRoot.append(String.valueOf(System.currentTimeMillis()));
		// File gitDir = new File(randomFolder.toFile(), Constants.DOT_GIT);
		File gitDir = new File(workspaceRoot.toFile(), Constants.DOT_GIT);
		if (gitDir.exists()) {
			// guard
			FileUtils.delete(gitDir, FileUtils.RECURSIVE | FileUtils.RETRY);
		}
		return gitDir;
	}

	@After
	public void tearDown() throws Exception {
		testRepository.dispose();
		repository = null;

		testProject.dispose();
		project = null;
		Activator.getDefault().getRepositoryCache().clear();
		if (gitDir.exists()) {
			FileUtils.delete(gitDir, FileUtils.RECURSIVE | FileUtils.RETRY);
		}
		
		if (clonedRepositoryFile.exists()) {
			FileUtils.delete(clonedRepositoryFile, FileUtils.RECURSIVE | FileUtils.RETRY);
		}
	}

	@Test
	public void canCommitFileInProject() throws Exception {
		IFile file = testUtils.addFileToProject(
				testProject.getProject(),
				"a.txt", "some text");
		resources.add(file);
		new AddToIndexOperation(resources).execute(null);

		EgitUtils.commit(testProject.getProject(), null);

		testUtils.assertRepositoryContainsFiles(repository, new String[] { testUtils.getRepositoryPath(file) });
	}

	@Test
	public void canPushRepoToAntoherRepo() throws Exception {
		IFile file = testUtils.addFileToProject(
				testProject.getProject(),
				"a.txt", "some text");
		resources.add(file);
		new AddToIndexOperation(resources).execute(null);

		EgitUtils.push(testProject.getProject(), null);

		testUtils.assertRepositoryContainsFiles(repository, new String[] { testUtils.getRepositoryPath(file) });
	}

}
