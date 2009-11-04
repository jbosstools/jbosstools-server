package org.jboss.ide.eclipse.as.archives.integration.test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModelRootNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveStandardFileSet;
import org.jboss.ide.eclipse.archives.ui.actions.BuildAction;
import org.jboss.ide.eclipse.archives.webtools.modules.ArchivesModuleModelListener;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServer;
import org.jboss.tools.test.util.JobUtils;

@SuppressWarnings("nls")
public class BuildDeployTest extends TestCase {
	private static final String VALUE_PREFIX = "value";
	private static final String OUT_JAR = "out.jar";
	private static final String FILE_NAME = "a.txt";
	
	private int index = 0;
	private IProject project;
	private IJavaProject javaProject;
	private IRuntime runtime;
	private IServer server;
	private IPath deploy, tempDeploy;
	private IPath pluginMetadata;
	private IFile textFile;
	private IFile textOutputFile;
	private IFile textBuildArchiveFile;
	private File textDeployedFile;
	private IArchive rootArchive;
	
	
	public void testAll() throws Exception {
		// Builder is off, auto-deploy is off
		enableAutomaticBuilder(false);
		createProject();
		setupServer();
		addArchives();
		buildArchive();
		assertBuiltArchiveContents(index);
		changeArchivesDeployPrefs(false, server.getId());
		deploy();
		assertDeployContents(index);
		
		// Builder is on, auto-deploy is off
		enableAutomaticBuilder(true);
		int count = ++index;
		setContents(count);
		callBuild(); // just to speed the test
		assertSourceContents(count);
		assertBinContents(count);
		assertBuiltArchiveContents(count);
		try {
			assertDeployContents(count);
			assertTrue("Deployed File should not have changed", false);
		} catch( AssertionFailedError ae) {}
		
		
		// Builder is on, auto-deploy is on
		changeArchivesDeployPrefs(true, server.getId());
		count = ++index;
		try {
			Thread.sleep(3000);
		} catch( InterruptedException ie) {}
		JobUtils.waitForIdle();
		
		setContents(count);
		callBuild(); // just to speed the test
		assertSourceContents(count);
		assertBinContents(count);
		assertBuiltArchiveContents(count);
		assertDeployContents(count);
		
		// Builder is off, autodeploy is on
		count = ++index;
		setContents(count);
		assertSourceContents(count);
		try {
			assertBinContents(count);
			assertTrue("Bin File should not have changed", false);			
		} catch( AssertionFailedError ae ) {}
		try {
			assertBuiltArchiveContents(count);
			assertTrue("Built Archive File should not have changed", false);			
		} catch( AssertionFailedError ae ) {}
		try {
			assertDeployContents(count);
			assertTrue("Deployed File should not have changed", false);			
		} catch( AssertionFailedError ae ) {}
		
		// Builder is off, autodeploy is on, manually call build!
		callBuild();
		assertBinContents(count);
		assertBuiltArchiveContents(count);
		assertDeployContents(count);
		
	}
	
	protected void enableAutomaticBuilder(boolean enabled) throws CoreException {
		IWorkspaceDescription desc = ResourcesPlugin.getWorkspace().getDescription();
		desc.setAutoBuilding(enabled);
		ResourcesPlugin.getWorkspace().setDescription(desc);
	}
	
	protected void createProject() throws CoreException, IOException {
		javaProject = createJavaProject("P1");
		project = javaProject.getProject();
		textFile = project.getFile(new Path(FILE_NAME));
		textOutputFile = project.getFile(new Path("bin").append(FILE_NAME));
		String initial = VALUE_PREFIX + index;
		
		assertFalse(textFile.getLocation().toFile().exists());
		textFile.create(new ByteArrayInputStream(initial.getBytes()), true, new NullProgressMonitor());
		assertTrue(textFile.getLocation().toFile().exists());
		
		assertFalse(textOutputFile.getLocation().toFile().exists());
		callBuild();
		assertTrue(textOutputFile.getLocation().toFile().exists());
		
		assertContents(textOutputFile.getContents(), index);
		
		setContents(++index);
		assertContents(textFile.getContents(), index);

		assertContents(textOutputFile.getContents(), 0);
		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
		assertContents(textOutputFile.getContents(), 1);
	}
	
	protected String getContents(InputStream is) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(is);
        int l = (int)textOutputFile.getLocation().toFile().length();
        byte[] bs = new byte[l];
        l = bis.read(bs, 0, l);
        bis.close();
        return new String(bs);
	}
	
	protected IJavaProject createJavaProject(String name) {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject project = root.getProject(name);
			project.create(new NullProgressMonitor());
			project.open(new NullProgressMonitor());
			
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();
			String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = JavaCore.NATURE_ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, new NullProgressMonitor());
			IJavaProject javaProject = JavaCore.create(project);
			Set<IClasspathEntry> entries = new HashSet<IClasspathEntry>();
			entries.addAll(Arrays.asList(javaProject.getRawClasspath()));
			entries.add(JavaRuntime.getDefaultJREContainerEntry());
			IClasspathEntry[] entries2 = 
				entries.toArray(new IClasspathEntry[entries.size()]);
			javaProject.setRawClasspath(entries2, new NullProgressMonitor());
			return javaProject;
		} catch( CoreException ce ) {
		}
		return null;
	}
	
	protected void setupServer() throws CoreException {
		pluginMetadata = ASArchivesIntegrationTest.getDefault().getStateLocation();
		pluginMetadata.toFile().mkdirs();
		pluginMetadata.append("server1").toFile().mkdirs();
		deploy = pluginMetadata.append("server1").append("1");
		deploy.toFile().mkdirs();
		tempDeploy = pluginMetadata.append("server1").append("2");
		tempDeploy.toFile().mkdirs();

		
		IRuntimeType[] rtTypes = ServerCore.getRuntimeTypes();
		IRuntimeType rtType = null;
		for( int i = 0; i < rtTypes.length; i++ ) {
			if( rtTypes[i].getId().equals("org.jboss.ide.eclipse.as.runtime.stripped"))
				rtType = rtTypes[i];
		}
		assertNotNull(rtType);
		IRuntimeWorkingCopy rtwc = rtType.createRuntime("temp.rt.id", new NullProgressMonitor());
		runtime = rtwc.save(true, new NullProgressMonitor());
		
		IServerType[] serverTypes = ServerCore.getServerTypes();
		IServerType serverType = null;
		for( int i = 0; i < serverTypes.length; i++ ) {
			if( serverTypes[i].getId().equals("org.jboss.ide.eclipse.as.systemCopyServer"))
				serverType = serverTypes[i];
		}
		assertNotNull(serverType);
		IServerWorkingCopy wc = serverType.createServer("tmp.server.id", null, new NullProgressMonitor());
		if( wc instanceof ServerWorkingCopy ) {
			ServerWorkingCopy swc = (ServerWorkingCopy)wc;
			swc.setName("server1");
			swc.setAttribute(DeployableServer.DEPLOY_DIRECTORY, deploy.toOSString());
			swc.setAttribute(DeployableServer.TEMP_DEPLOY_DIRECTORY, tempDeploy.toOSString());
			swc.setRuntime(runtime);
			swc.setAutoPublishTime(1);
			swc.setAutoPublishSetting(Server.AUTO_PUBLISH_ENABLE);
		}
		server = wc.save(true, new NullProgressMonitor());
		
		// some asserts
		assertTrue(ServerCore.getRuntimes().length == 1);
		assertTrue(ServerCore.getServers().length == 1);
		
		textDeployedFile = deploy.append(OUT_JAR).append(FILE_NAME).toFile();
	}
	
	protected void addArchives() {
		ArchivesModel.instance().registerProject(project.getLocation(), new NullProgressMonitor());
		IArchiveModelRootNode root = ArchivesModel.instance().getRoot(project.getLocation());
		rootArchive = ArchivesCore.getInstance().getNodeFactory().createArchive();
		rootArchive.setExploded(true);
		rootArchive.setInWorkspace(true);
		rootArchive.setName(OUT_JAR);
		rootArchive.setDestinationPath(null);
		root.addChild(rootArchive);
		
		IArchiveStandardFileSet fs = ArchivesCore.getInstance().getNodeFactory().createFileset();
		fs.setIncludesPattern("**/*txt");
		fs.setExcludesPattern("**/bin/**, **/*jar*");
		fs.setInWorkspace(true);
		fs.setRawSourcePath("/P1");
		rootArchive.addChild(fs);
		root.save(new NullProgressMonitor());
		
		textBuildArchiveFile = project.getFolder(OUT_JAR).getFile(FILE_NAME);
	}
	
	protected void changeArchivesDeployPrefs(boolean alwaysPublish, String servers) {
		rootArchive.setProperty(ArchivesModuleModelListener.DEPLOY_SERVERS, servers);
		rootArchive.setProperty(ArchivesModuleModelListener.DEPLOY_AFTER_BUILD, new Boolean(alwaysPublish).toString());
		final IPath projectPath = rootArchive.getProjectPath();
		ArchivesModel.instance().save(projectPath, new NullProgressMonitor());
	}
	
	protected void buildArchive()  {
		Job j = new BuildAction().run(rootArchive);
		JobUtils.waitForIdle();
		assertTrue(j.getResult().isOK());
	}
	
	protected void deploy() {
		ArchivesModuleModelListener.getInstance().publish(rootArchive, server.getId(), IServer.PUBLISH_FULL);
	}
	
	
	protected void setContents(int val) throws IOException , CoreException{
		textFile.setContents(new ByteArrayInputStream(new String(VALUE_PREFIX + (val)).getBytes()), false, false, new NullProgressMonitor());
		try {
			Thread.sleep(2000);
		} catch( InterruptedException ie) {}
		JobUtils.waitForIdle(); 
	}
	
	protected void assertContents(InputStream is, int val) throws IOException {
		String contents = getContents(is);
		assertEquals(VALUE_PREFIX + val, contents);
	}
	
	protected void assertSourceContents(int val) throws IOException, CoreException {
		assertContents(textFile.getContents(), val);
	}
	
	protected void assertBinContents(int val) throws IOException, CoreException {
		assertContents(textOutputFile.getContents(), val);
	}
	
	protected void assertBuiltArchiveContents(int val) throws IOException, CoreException {
		assertContents(textBuildArchiveFile.getContents(), val);
	}
	protected void assertDeployContents(int val) throws IOException, CoreException {
		assertContents(new FileInputStream(textDeployedFile), val);
	}
	
	protected void callBuild() throws CoreException {
		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
		JobUtils.waitForIdle(); 
	}
}
