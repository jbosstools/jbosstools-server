package org.jboss.tools.as.test.core.parametized.server.publishing;

import java.util.Collection;

import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.tools.as.test.core.internal.utils.MatrixUtils;
import org.jboss.tools.as.test.core.internal.utils.ResourceUtils;
import org.jboss.tools.as.test.core.parametized.server.ServerParameterUtils;

public class AbstractComponentPublishingTest extends AbstractPublishingTest {
	public static Collection<Object[]> componentJarData() {
		return componentJarData(false);
	}
	public static Collection<Object[]> fullComponentJarData() {
		return componentJarData(true);
	}
	public static Collection<Object[]> componentJarData(boolean complete) {
		Object[] servers = ServerParameterUtils.getPublishServerTypes();
		Object[] zipOption = ServerParameterUtils.getServerZipOptions();
		Object[] defaultDeployLoc = ServerParameterUtils.getDefaultDeployOptions();
		Object[] perModOverrides = ServerParameterUtils.getPerModuleOverrideOptions();
		if( !complete ) {
			// simplify these options
			defaultDeployLoc = new Object[]{defaultDeployLoc[0]};
			perModOverrides = new Object[]{perModOverrides[0]};
		}
		Object[] junitName = new String[] { "junit.jar", "otherOut.jar"};
		Object[] outFolder = new String[] { "lib", "otherFolder", "deep/nested", ""};
		Object[][] allOptions = new Object[][] {
				servers, zipOption, defaultDeployLoc, perModOverrides,
				junitName, outFolder
		};
		return MatrixUtils.toMatrix(allOptions);
	}

	protected String jarFolder;
	protected String jarName;

	public AbstractComponentPublishingTest(String serverType, 
			String zip, String deployLoc, String perMod,
			String refName, String refFolder) {
		super(serverType, zip, deployLoc, perMod);
		jarName = refName;
		jarFolder = refFolder;
	}
	
	protected void printConstructor() {
		System.out.println(getClass().getName() + ":  " + param_serverType + ", " + param_zip + ", " + param_deployLoc + ", " + param_perModOverride + ", " + jarName + ", " + jarFolder);
	}

	protected void completeSetUp() {
		// Keep it local for REAL publishes
		wc.setAttribute(IDeployableServer.SERVER_MODE, "local");
	}
	
	protected void verifyFileFoundInModule(String folder, String name, int expectedFileCount) throws Exception {
		verifyFileFoundInModule(folder, name, expectedFileCount, primaryModule);
	}
	
	protected void verifyFileFoundInModule(String folder, String name, int expectedFileCount, IModule module) throws Exception {
		// now verify
		IModuleFile[] allFiles = ResourceUtils.findAllIModuleFiles(module);
		assertEquals(allFiles.length,expectedFileCount);
		IModuleFile selected = null;
		for( int i = 0; i < allFiles.length; i++ ) {
			if( allFiles[i].getName().equals(name)) {
				selected = allFiles[i];
				break;
			}
		}
		IModuleFile mf = selected;
		assertNotNull(mf);
		assertEquals(mf.getName(), name);
		assertEquals(mf.getModuleRelativePath().makeRelative(), new Path(folder).makeRelative());
	}


}
