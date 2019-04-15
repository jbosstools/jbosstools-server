package org.jboss.tools.as.ui.bot.itests.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.jar.Manifest;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This test is trying to optimize and clean up the huge number of tests that
 * were here.
 * 
 * Pre-reqs: 1)
 * -Djbosstools.test.single.runtime.location=/path/to/some/server/unzipped/loc
 */

@RunWith(RedDeerSuite.class)
public class SingleServerDirectoryStructureTest {
	private String location;
	private String productSlot;
	private String productReleaseName;

	public SingleServerDirectoryStructureTest() {
		location = System.getProperty("jbosstools.test.single.runtime.location");
		productSlot = System.getProperty("jbosstools.test.single.runtime.product.slot");
		productReleaseName = System.getProperty("jbosstools.test.single.runtime.product.release.name");
	}
	
	/*
	  1) bin/product.conf exists
	  2) bin/product.conf has slot = jbosstools.test.single.runtime.product.slot
	  3) jboss-modules.jar must exist
	  4) standalone/configuration/standalone.xml must exist
	  5) modules/system/layers/base/org/jboss/as/product/+jbosstools.test.single.runtime.product.slot+/dir/META-INF/MANIFEST.MF exists
	  6) JBoss-Product-Release-Name key in manifest.mf above has value jbosstools.test.single.runtime.product.release.name (+ " CD")
	  7) JBoss-Product-Release-Version key in manifest.mf is not null
	 */
	
	@Test
	public void verifyFolderStructure() {
		assertNotNull(productSlot);
		assertNotNull(productReleaseName);
		assertNotNull(location);
		File f = new File(location);
		assertTrue(f.exists());
		assertTrue(f.isDirectory());
		File prodConf = new File(new File(f, "bin"), "product.conf");
		assertTrue(prodConf.exists());
		try {
			Properties p = new Properties();
			p.load(new FileInputStream(prodConf));
			assertEquals(productSlot, p.getProperty("slot"));
		} catch(IOException ioe) {
			fail(ioe.getMessage());		
		}
		
		File jbModules = new File(location, "jboss-modules.jar");
		assertTrue(jbModules.exists());
		
		IPath standaloneXML = new Path(location).append("standalone/configuration/standalone.xml");
		assertTrue(standaloneXML.toFile().exists());
		
		
		Path p2 = new Path(location);
		IPath prodManifest = p2.append("modules/system/layers/base/org/jboss/as/product/" + productSlot + "/dir/META-INF/MANIFEST.MF");
		assertTrue(prodManifest.toFile().exists());
	
		try {
			Manifest man = new Manifest(new FileInputStream(prodManifest.toFile()));
			String o3 = man.getMainAttributes().getValue("JBoss-Product-Release-Name");
			assertTrue(productReleaseName.equals(o3) || (productReleaseName + " CD").equals(o3));
			String o4 = man.getMainAttributes().getValue("JBoss-Product-Release-Version");
			assertNotNull(o4);
		} catch(IOException ioe) {
			fail(ioe.getMessage());
		}
	}
}