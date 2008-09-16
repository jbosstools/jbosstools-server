/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.archives.test.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XMLBinding;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbFileSet;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackage;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackages;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XMLBinding.XbException;
import org.jboss.ide.eclipse.archives.test.ArchivesTest;
import org.jboss.ide.eclipse.archives.test.util.FileIOUtil;
import org.osgi.framework.Bundle;

/**
 * Tests unmarshalling and validating packaging files. 
 * The tests expect XB to throw exceptions when improper 
 * nonconformant files are parsed. 
 * 
 * @author rob.stryker <rob.stryker@redhat.com>
 *
 */
public class XBUnmarshallTest extends TestCase {
	private Bundle bundle;
	private IPath bundlePath;
	private IPath archiveDescriptors;
	protected void setUp() {
		if( bundlePath == null ) {
			try {
				bundle = ArchivesTest.getDefault().getBundle();
				URL bundleURL = FileLocator.toFileURL(bundle.getEntry(""));
				bundlePath = new Path(bundleURL.getFile());
				archiveDescriptors = bundlePath.append("inputs").append("archiveDescriptors").append("validation");
			} catch( IOException ioe) {
				fail("Failed to set up " + getClass().getName());
			}
		}
	}

	public void tearDown() {
		FileIOUtil.clearFolder(bundlePath.append("tmp").toFile().listFiles());
	}
	
	/*
	 * The tests
	 */
	
	public void testAbsoluteSchemaFailure() {
		parse("AbsoluteSchemaFailure.xml", false, "Broken schema successfully parsed.");
	}
	
	public void testAttributeSchemaFailure() {
		parse("AttributeSchemaFailure.xml", false, "Schema with random attribute successfully parsed.");
	}
	
	
	public void testCorrectPackage() {
		parse("CorrectPackage.xml", true, shouldParse("CorrectPackage.xml"));
	}

	public void testPackageMissingPackageName() {
		parse("MissingPackageName.xml", false, failedMissingRequiredAtt("name"));
	}

	// Currently the schema is written that this will pass
//	public void testPackageMissingDirectory() {
//		parse("MissingPackageDir.xml", false, failedMissingRequiredAtt("todir"));
//	}
	
	public void testPackageMissingOptionalType() {
		parse("MissingPackageType.xml", true, failedFalsePositiveRequiredAtt("type"));
	}
	
	public void testPackageMissingOptionalExploded() {
		XbPackages packs = parse("MissingPackageExploded.xml", true, failedFalsePositiveRequiredAtt("exploded"));
		XbPackage pack = (XbPackage)packs.getAllChildren().get(0);
		assertFalse(pack.isExploded());
	}
	
	public void testPackageMissingOptionalInWorkspace() {
		XbPackages packs = parse("MissingPackageInWorkspace.xml", true, failedFalsePositiveRequiredAtt("inWorkspace"));
		XbPackage pack = (XbPackage)packs.getAllChildren().get(0);
		assertTrue(pack.isInWorkspace());
	}
	
	public void testPackageMissingOptionalId() {
		XbPackages packs = parse("MissingPackageID.xml", true, failedMissingRequiredAtt("id"));
		XbPackage pack = (XbPackage)packs.getAllChildren().get(0);
		assertNull(pack.getId());
	}
	
	public void testCorrectFolder() {
		parse("CorrectFolder.xml", true, shouldParse("CorrectFolder.xml"));
	}
	
	public void testFolderMissingName() {
		parse("MissingFolderName.xml", false, failedMissingRequiredAtt("name"));
	}
	
	public void testCorrectProperties() {
		parse("CorrectProperties.xml", true, shouldParse("CorrectProperties.xml"));
	}
	
	public void testPropertiesMissingKey() {
		parse("MissingPropertiesName.xml", false, failedMissingRequiredAtt("name"));
	}
	
	public void testPropertiesMissingValue() {
		parse("MissingPropertiesValue.xml", false, failedMissingRequiredAtt("value"));
	}
	
	
	public void testCorrectFileset() {
		parse("CorrectFileset.xml", true, shouldParse("CorrectFileset.xml"));
	}
	
	public void testFilesetMissingDir() {
		parse("MissingFilesetDir.xml", false, failedMissingRequiredAtt("todir"));
	}

	public void testFilesetMissingIncludes() {
		parse("MissingFilesetIncludes.xml", false, failedMissingRequiredAtt("includes"));
	}
	
	public void testFilesetMissingOptionalExcludes() {
		parse("MissingFilesetExcludes.xml", true, failedFalsePositiveRequiredAtt("excludes"));
		// no default
	}
	
	public void testFilesetMissingOptionalInWorkspace() {
		XbPackages packs = parse("MissingFilesetInWorkspace.xml", true, failedFalsePositiveRequiredAtt("inWorkspace"));
		// no default
		XbPackage pack = (XbPackage)packs.getAllChildren().get(0);
		List l = pack.getChildren(XbFileSet.class);
		assertNotNull(l);
		assertEquals(1, l.size());
		XbFileSet fs = (XbFileSet)l.get(0);
		assertTrue(fs.isInWorkspace());
	}

	public void testFilesetMissingOptionalFlattened() {
		XbPackages packs = parse("MissingFilesetFlattened.xml", true, failedFalsePositiveRequiredAtt("flattened"));
		// no default
		XbPackage pack = (XbPackage)packs.getAllChildren().get(0);
		List l = pack.getChildren(XbFileSet.class);
		assertNotNull(l);
		assertEquals(1, l.size());
		XbFileSet fs = (XbFileSet)l.get(0);
		assertFalse(fs.isFlattened());
	}

	
	public void testCorrectAction() {
		parse("CorrectAction.xml", true, shouldParse("CorrectAction.xml"));
	}
	
	public void testActionMissingTime() {
		parse("MissingActionTime.xml", false, failedMissingRequiredAtt("time"));
	}
	
	public void testActionMissingType() {
		parse("MissingActionType.xml", false, failedMissingRequiredAtt("type"));
	}
	
	public void testReadPackagesVersion() {
		XbPackages packs = parse("ReadVersion.xml", true, failedFalsePositiveRequiredAtt("exploded"));
		assertEquals(new Float(packs.getVersion()), new Float(1.2));
	}

	
	/*
	 * Utility
	 */
	
	protected XbPackages parse(String file, boolean shouldSucceed, String failMsg) {
		XbPackages packs = null;
		try {
			packs = XMLBinding.unmarshal(archiveDescriptors.append(file).toFile(), new NullProgressMonitor());
		} catch( XbException e ) {
			if( shouldSucceed )
				fail(failMsg + " - " + e.getMessage());
			return packs;
		}
		if( !shouldSucceed )
			fail(failMsg);
		return packs;
	}
	
	protected String failedMissingRequiredAtt(String att) {
		return "File parsed while missing a required attribute: " + att;
	}
	
	protected String failedFalsePositiveRequiredAtt(String attribute) {
		return "File failed to parse even though \"" + attribute + "\" is not required";
	}
	
	protected String shouldParse(String file) {
		return file + " should parse correctly.";
	}
}
