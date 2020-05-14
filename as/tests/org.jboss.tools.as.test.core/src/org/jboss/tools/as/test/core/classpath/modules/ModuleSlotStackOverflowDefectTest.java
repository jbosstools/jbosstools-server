/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.classpath.modules;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal.ModuleSlot;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.junit.After;

import junit.framework.TestCase;

public class ModuleSlotStackOverflowDefectTest extends TestCase {
	@After
	public void tearDown() throws Exception {
		ServerCreationTestUtils.deleteAllServersAndRuntimes();
	} 
	
	public void testStackOverflow() throws IOException {
		Path tmpDir = Files.createTempDirectory(getClass().getName());
		Path moduleRoot = tmpDir.resolve("modules").resolve("system").resolve("layers")
				.resolve("base").resolve("test").resolve("modulename");
		moduleRoot.toFile().mkdirs();
		
		Path main = moduleRoot.resolve("main");
		Path alt = moduleRoot.resolve("alternative");
		main.toFile().mkdirs();
		alt.toFile().mkdirs();
		Files.write(alt.resolve("module.xml"), getAlternativeModuleXml().getBytes());
		Files.write(main.resolve("module.xml"), getMainModuleXml().getBytes());
		
		
		TestModuleSlot ms = new TestModuleSlot("test.modulename", "main");
		IPath[] ret = ms.getJars(new org.eclipse.core.runtime.Path(tmpDir.resolve("modules").toString()));
		assertNotNull(ret);
	}
	
	private class TestModuleSlot extends ModuleSlot {
		private ArrayList<Parameters> seen = new ArrayList<>();
		public TestModuleSlot(String module, String slot) {
			super(module, slot);
		}
		
		protected IPath[] getJarsFromLayer(File layeredPath, IPath modulesFolder, ModuleSlot ms) {
			Parameters p = new Parameters(layeredPath, modulesFolder, ms);
			if( seen.contains(p))
				return null;
			return super.getJarsFromLayer(layeredPath, modulesFolder, ms);
		}
	}
	
	private static class Parameters {
		private String layerPath;
		private String modulesFolder;
		private String msName;
		private String msSlot;
		public Parameters(File layeredPath, IPath modulesFolder, ModuleSlot ms) {
			this.layerPath = layeredPath.getAbsolutePath();
			this.modulesFolder = modulesFolder.toOSString();
			this.msName = ms.getModule();
			this.msSlot = ms.getSlot();
		}
		@Override
		public boolean equals(Object other) {
			return other instanceof Parameters &&
					isEq(layerPath, ((Parameters)other).layerPath) && 
					isEq(modulesFolder, ((Parameters)other).modulesFolder) && 
					isEq(msName, ((Parameters)other).msName) && 
					isEq(msSlot, ((Parameters)other).msSlot); 
		}
		
		@Override
	    public int hashCode() {
			return layerPath.hashCode() + modulesFolder.hashCode() + msName.hashCode() + msSlot.hashCode();
	    }
		
		private boolean isEq(String s1, String s2) {
			return s1 == null ? s2 == null : s1.equals(s2);
		}
	}
	
	private String getAlternativeModuleXml() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<module xmlns=\"urn:jboss:module:1:1\" name=\"test.modulename\" slot=\"alternative\">\n" + 
				"   <resources>\n" + 
				"     <resource-root path=\"some.jar\"/>\n" + 
				"   </resources>\n" + 
				"</module>\n"; 
				
	}
	
	private String getMainModuleXml() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<module-alias xmlns=\"urn:jboss:module:1:1\" name=\"test.modulename\" target-name=\"test.modulename\" target-slot=\"alternative\"/>";
	}
}
