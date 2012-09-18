package org.jboss.tools.as.test.core.parametized.server.publishing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.ServerPreferences;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.validation.ValidationFramework;
import org.jboss.ide.eclipse.archives.core.util.internal.TrueZipUtil;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServer;
import org.jboss.ide.eclipse.as.core.server.internal.v7.DeploymentMarkerUtils;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentModulePrefs;
import org.jboss.ide.eclipse.as.core.util.DeploymentPreferenceLoader.DeploymentPreferences;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.jboss.tools.as.test.core.internal.MockPublishMethod4;
import org.jboss.tools.as.test.core.internal.utils.IOUtil;
import org.jboss.tools.as.test.core.internal.utils.MatrixUtils;
import org.jboss.tools.as.test.core.internal.utils.ResourceUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.jboss.tools.as.test.core.parametized.server.ServerParameterUtils;
import org.jboss.tools.test.util.JobUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.schlichtherle.io.ArchiveDetector;

@RunWith(value = Parameterized.class)
public abstract class AbstractPublishingTest extends TestCase {
	public static ArrayList<Object[]> defaultData() {
		Object[] servers = ServerParameterUtils.getPublishServerTypes();
		Object[] zipOption = ServerParameterUtils.getServerZipOptions();
		Object[] defaultDeployLoc = ServerParameterUtils.getDefaultDeployOptions();
		Object[] perModOverrides = ServerParameterUtils.getPerModuleOverrideOptions();
		Object[][] allOptions = new Object[][] {
				servers, zipOption, defaultDeployLoc, perModOverrides
		};
		return MatrixUtils.toMatrix(allOptions);
	}

	protected String param_serverType;
	protected String param_zip;
	protected String param_deployLoc;
	protected String param_perModOverride;
	protected IServer server;
	protected IServerWorkingCopy wc;
	protected String serverDeployPath;
	protected String serverTempDeployPath;
	protected IModule primaryModule;
	
	public AbstractPublishingTest(String serverType, String zip, String deployLoc, String perMod) { 
		this.param_serverType = serverType;
		this.param_zip = zip;
		this.param_deployLoc = deployLoc;
		this.param_perModOverride = perMod;
	}
	
	protected void printConstructor() {
		System.out.println(getClass().getName() + ":  " + param_serverType + ", " + param_zip + ", " + param_deployLoc + ", " + param_perModOverride);
	}
	

	@Before
	public void setUp() throws Exception {
		printConstructor();
		ServerPreferences.getInstance().setAutoPublishing(false);
		ValidationFramework.getDefault().suspendAllValidation(true);
		JobUtils.waitForIdle();
		server = ServerCreationTestUtils.createMockServerWithRuntime(param_serverType, getClass().getName() + param_serverType);
		wc = server.createWorkingCopy();
		setMockPublishMethod4(wc);
		setupZipParam(wc);
		setupDeployTypeParam(wc);
		createProjects();
		completeSetUp();
		server = wc.save(false, new NullProgressMonitor());
	}
	
	protected void completeSetUp() {
	}
	
	@After 
	public void tearDown() throws Exception {
		try {
			ASMatrixTests.cleanup();
		} finally {
			ValidationFramework.getDefault().suspendAllValidation(false);
		}
	}
	
	protected void setMockPublishMethod4(IServerWorkingCopy wc) {
		MockPublishMethod4.reset();
		wc.setAttribute(IDeployableServer.SERVER_MODE, "mock4");
	}
	
	protected void createProjects() throws Exception {}
	
	public void addModuleToServer(IModule module) throws CoreException  {
		assertNotNull(module);
		primaryModule = module;
		wc.modifyModules(new IModule[]{module}, new IModule[0], new NullProgressMonitor());
		setCustomDeployOverride(module);
	}

	protected void setCustomDeployOverride(IModule module) {
		if( param_perModOverride.equals(ServerParameterUtils.DEPLOY_PERMOD_DEFAULT))
			return;
		String overrideFolder = null;
		String overrideTemp = null;
		
		if( param_perModOverride.equals(ServerParameterUtils.DEPLOY_PERMOD_ABS)) {
			overrideFolder = getAbsoluteOverrideFolder();
			overrideTemp = getAbsoluteTempOverrideFolder();
		} else if( param_perModOverride.equals(ServerParameterUtils.DEPLOY_PERMOD_REL)) {
			overrideFolder = getRelativeOverrideFolder();
			overrideTemp = getRelativeTempOverrideFolder();
		}
		setCustomDeployOverride(module, null, overrideFolder, overrideTemp);
		MockPublishMethod4.setExpectedRoot(overrideFolder);
		MockPublishMethod4.setExpectedTempRoot(overrideTemp);
	}
	
	protected void setCustomDeployOverride(IModule rootModule, String outputName, String outputDir, String temporaryDir) {
		DeploymentPreferences prefs = DeploymentPreferenceLoader.loadPreferencesFromServer(wc);
		DeploymentModulePrefs modPrefs = prefs.getOrCreatePreferences().getOrCreateModulePrefs(rootModule);
		modPrefs.setProperty(IJBossToolingConstants.LOCAL_DEPLOYMENT_TEMP_LOC, temporaryDir);
		modPrefs.setProperty(IJBossToolingConstants.LOCAL_DEPLOYMENT_LOC, outputDir);
		modPrefs.setProperty(IJBossToolingConstants.LOCAL_DEPLOYMENT_OUTPUT_NAME, outputName);
		DeploymentPreferenceLoader.savePreferencesToServerWorkingCopy(wc, prefs);
	}

	protected void setupZipParam(IServerWorkingCopy wc) {
		if( isZipped())
			ServerConverter.getDeployableServer(wc).setZipWTPDeployments(true);
	}
	
	protected boolean isZipped() {
		return param_zip.equals(ServerParameterUtils.ZIPPED);
	}

	protected void setupDeployTypeParam(IServerWorkingCopy wc) {
		IDeployableServer ds = ServerConverter.getDeployableServer(wc);
		if( !server.getServerType().getId().equals(IJBossToolingConstants.DEPLOY_ONLY_SERVER)) {
			if( param_deployLoc.equals(ServerParameterUtils.DEPLOY_META))
				ds.setDeployLocationType(IDeployableServer.DEPLOY_METADATA);
			if( param_deployLoc.equals(ServerParameterUtils.DEPLOY_SERVER))
				ds.setDeployLocationType(IDeployableServer.DEPLOY_SERVER);
			if( param_deployLoc.equals(ServerParameterUtils.DEPLOY_CUSTOM_NULL) || 
					param_deployLoc.equals(ServerParameterUtils.DEPLOY_CUSTOM_ABS) ||
					param_deployLoc.equals(ServerParameterUtils.DEPLOY_CUSTOM_REL)) {
				ds.setDeployLocationType(IDeployableServer.DEPLOY_SERVER);
			}
			if( param_deployLoc.equals(ServerParameterUtils.DEPLOY_CUSTOM_ABS)) {
				String f = getCustomAbsoluteFolder();
				new File(f).mkdirs();
				ds.setDeployFolder(f);
				String f2 = getCustomAbsoluteTempFolder();
				new File(f2).mkdirs();
				ds.setTempDeployFolder(f2);
			} else if(param_deployLoc.equals(ServerParameterUtils.DEPLOY_CUSTOM_REL) ) {
				String f = getCustomRelativeFolder();
				server.getRuntime().getLocation().append(f).toFile().mkdirs();
				ds.setDeployFolder(f);
				String f2 = getCustomRelativeTempFolder();
				server.getRuntime().getLocation().append(f2).toFile().mkdirs();
				ds.setTempDeployFolder(f2);
			}
		}
		serverDeployPath = ds.getDeployFolder();
		serverTempDeployPath = ds.getTempDeployFolder();
		MockPublishMethod4.setExpectedRoot(serverDeployPath);
		MockPublishMethod4.setExpectedTempRoot(serverTempDeployPath);

	}
	
	private String getCustomAbsoluteFolder(){
		return ServerCreationTestUtils.getRandomAbsoluteFolder().append("final").toOSString();
	}
	private String getCustomAbsoluteTempFolder(){
		return ServerCreationTestUtils.getRandomAbsoluteFolder().append("tmp").toOSString();
	}

	private String getAbsoluteOverrideFolder(){
		return ServerCreationTestUtils.getRandomAbsoluteFolder().append("finalOverride").toOSString();
	}
	private String getAbsoluteTempOverrideFolder(){
		return ServerCreationTestUtils.getRandomAbsoluteFolder().append("tmpOverride").toOSString();
	}

	private String getRelativeOverrideFolder(){
		return ServerCreationTestUtils.getRandomAbsoluteFolder().append("finalOverride").toOSString();
	}
	private String getRelativeTempOverrideFolder(){
		return ServerCreationTestUtils.getRandomAbsoluteFolder().append("tmpOverride").toOSString();
	}

	private String getCustomRelativeFolder(){
		if( ServerUtil.isJBoss7(server)) {
			return "myDeploy/final";
		}
		return "server/myDeploy/final";
	}
	private String getCustomRelativeTempFolder(){
		if( ServerUtil.isJBoss7(server)) {
			return "myDeploy/temp";
		}
		return "server/myDeploy/temp";
	}
	
	
	
	/* Utilities */
	/* Should add a .deploy marker */
	protected int getFullPublishChangedResourceCountModifier() {
		if( DeploymentMarkerUtils.supportsJBoss7MarkerDeployment(server))
			return 1;
		return 0;
	}
	
	/* Should remove a .failed marker */
	protected int getFullPublishRemovedResourceCountModifier() {
		if( DeploymentMarkerUtils.supportsJBoss7MarkerDeployment(server))
			return 1;
		return 0;
	}

	
	/* Util methods to do the checking */
	protected static void publishAndCheckError(IServer server, int pubType) {
		MockPublishMethod4.resetPublish();
		server.publish(pubType, new NullProgressMonitor());
		JobUtils.waitForIdle();
		if( MockPublishMethod4.getError() != null ) {
			Throwable t = MockPublishMethod4.getError();
			System.out.println("   " + t.getMessage());
			fail("Error when publishing: " + t.getMessage());
		}
	}
	protected void verifyPublishMethodResults(int changed, int removed, int temp) {
		IPath[] changed2 = MockPublishMethod4.getChanged();
		IPath[] removed2 = MockPublishMethod4.getRemoved();
		IPath[] temp2 = MockPublishMethod4.getTempPaths();
		System.out.println("   " + changed2.length + ", " + removed2.length + ", " + temp2.length);
		assertEquals(changed2.length, changed);
		assertEquals(removed2.length, removed);
		assertEquals(temp2.length,    temp);
	}
	
	protected IModuleFile findModuleFile(IModuleFile[] files, IPath path) {
		for( int j = 0; j < files.length; j++ ) {
			IPath p1 = files[j].getModuleRelativePath().append(files[j].getName());
			if( p1.makeRelative().equals(path.makeRelative()))
				return files[j];
		}
		return null;
	}
	
	protected void verifyZipContents(IModuleFile[] files, IPath[] paths, String[] contents) throws CoreException, IOException {
		File foundZip = findZip(files);
		
		for( int i = 0; i < paths.length; i++ ) {
			IPath relative = paths[i].removeFirstSegments(1);
			de.schlichtherle.io.File zipRoot = new de.schlichtherle.io.File(foundZip, new TrueZipUtil.JarArchiveDetector());
			de.schlichtherle.io.File truezipFile = new de.schlichtherle.io.File(zipRoot, relative.toString(), ArchiveDetector.ALL);
			de.schlichtherle.io.FileInputStream fis = new de.schlichtherle.io.FileInputStream(truezipFile);
			byte[] b = IOUtil.getBytesFromInputStream(fis);
			String b2 = new String(b);
			assertEquals(b2, contents[i]);
		}
	}
	protected void verifyWorkspaceContents(IModuleFile[] files, IPath[] paths, String[] contents) throws CoreException, IOException {
		for( int i = 0; i < paths.length; i++ ) {
			IModuleFile mf = findModuleFile(files, paths[i].removeFirstSegments(1));
			assertNotNull("File not found: " + paths[i], mf);
			String s = ResourceUtils.getContents(mf);
			assertNotNull("File contents are null", s);
			assertEquals("File contents does not match expected contents", s, contents[i]);
		}
	}

	protected File findZip(IModuleFile[] files) {
		File foundZip = null;
		for( int i = 0; i < files.length; i++ ) {
			File f = (File)files[i].getAdapter(File.class);
			assertNotNull(f);
			boolean isZip = IOUtil.isZip(f);
			if( isZip && foundZip == null ) {
				foundZip = f;
			} else if( isZip ) {
				fail("Multiple top level zips found in zipped deployment");
			}
		}
		assertNotNull("Could not find a zipped file in the deployment", foundZip);
		return foundZip;
	}
	
	

	protected IModule[] getModule(IProject p) {
		return new IModule[]{org.eclipse.wst.server.core.ServerUtil.getModule(p)};
	}
	protected IProject findProject(String name) {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
	}
	protected IPath getLocalPublishMethodDeployRoot() {
		DeployableServer ds = (DeployableServer)server.loadAdapter(DeployableServer.class, new NullProgressMonitor());
		IPath path = ds.getDeploymentLocation(new IModule[]{primaryModule}, true);
		return path;
	}
	
	protected void verifyList(IPath root, ArrayList<IPath> list, boolean exists) {
		Iterator<IPath> iterator = list.iterator();
		ArchiveDetector detector = isZipped() ? new TrueZipUtil.JarArchiveDetector() : ArchiveDetector.DEFAULT;
		while(iterator.hasNext()) {
			de.schlichtherle.io.File f = new de.schlichtherle.io.File(root.toFile(), detector);
			IPath next = iterator.next();
			IPath nextTrimmed = next.removeFirstSegments(root.segmentCount());
			de.schlichtherle.io.File toCheck = new de.schlichtherle.io.File(f, nextTrimmed.toString(), ArchiveDetector.DEFAULT);
			assertEquals(toCheck.exists(), exists);
		}
	}

}
