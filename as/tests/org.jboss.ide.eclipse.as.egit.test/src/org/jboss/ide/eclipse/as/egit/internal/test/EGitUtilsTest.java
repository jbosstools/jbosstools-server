package org.jboss.ide.eclipse.as.egit.internal.test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.egit.core.Activator;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.SystemReader;
import org.jboss.ide.eclipse.as.egit.core.EGitUtils;
import org.jboss.ide.eclipse.as.egit.internal.test.util.MockSystemReader;
import org.jboss.ide.eclipse.as.egit.internal.test.util.TestProject;
import org.jboss.ide.eclipse.as.egit.internal.test.util.TestRepository;
import org.jboss.ide.eclipse.as.egit.internal.test.util.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EGitUtilsTest {

	private static final String GIT_EMAIL = "dummyUser@redhat.com";

	private static final String GIT_USER = "dummyUser";

	protected final TestUtils testUtils = new TestUtils();

	private File gitDir;
	private TestRepository testRepository;
	private TestProject testProject;
	private TestRepository clonedTestRepository;

	@Before
	public void setUp() throws Exception {
		Activator.getDefault().getRepositoryCache().clear();

		this.testProject = new TestProject(true);

		this.gitDir = TestUtils.createGitDir(testProject);
		this.testRepository = new TestRepository(gitDir);
		testRepository.createMockSystemReader(ResourcesPlugin.getWorkspace().getRoot().getLocation());
		testRepository.setUserAndEmail(GIT_USER, GIT_EMAIL);
		testRepository.connect(testProject.getProject());

		this.clonedTestRepository = cloneRepository(testRepository);
	}

	private TestRepository cloneRepository(TestRepository repository) throws URISyntaxException, InvocationTargetException, InterruptedException, IOException {
		File workspaceDir = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
		File clonedRepositoryFile =
				new File(workspaceDir, "clonedRepository-" + String.valueOf(System.currentTimeMillis()));
		return testRepository.cloneRepository(clonedRepositoryFile);
	}

	@After
	public void tearDown() throws Exception {
		testRepository.dispose();
		clonedTestRepository.dispose();

		testProject.dispose();
		Activator.getDefault().getRepositoryCache().clear();
	}

	@Test
	public void canCommitFileInProject() throws Exception {
		String fileName = "a.txt";
		String fileContent = "adietish@redhat.com";
		
		IFile file = testUtils.addFileToProject(
				testProject.getProject(),
				fileName, 
				fileContent);
		testRepository.track(file);

		EGitUtils.commit(testProject.getProject(), null);

		testUtils.assertRepositoryContainsFilesWithContent(
				testRepository.getRepository(),
				new String[] { testUtils.getPathInRepository(file), fileContent });
	}

	@Test
	public void fileAddedToCloneIsInOriginAfterPush() throws Exception {
		String fileName = "b.txt";
		String fileContent = "adietish@redhat.com";
		
		clonedTestRepository.createFile(fileName, fileContent);
		Repository clonedRepository = clonedTestRepository.getRepository();
		Git git = new Git(clonedRepository);
		git.add().addFilepattern(fileName).call();
		git.commit().setCommitter(GIT_USER, GIT_EMAIL).setMessage("adding a new file").call();

		EGitUtils.push(clonedRepository, null);

		// does origin contain file added to clone?
		testUtils.assertRepositoryContainsFilesWithContent(
				clonedRepository,
				fileName,
				fileContent);
	}
}
