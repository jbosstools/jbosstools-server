/*******************************************************************************
 * Copyright (c) 2014-2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.catalog;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.wst.validation.ValidationResult;
import org.eclipse.wst.xml.core.internal.XMLCorePlugin;
import org.eclipse.wst.xml.core.internal.catalog.provisional.ICatalog;
import org.eclipse.wst.xml.core.internal.catalog.provisional.ICatalogEntry;
import org.eclipse.wst.xml.core.internal.catalog.provisional.INextCatalog;
import org.eclipse.wst.xml.core.internal.contentmodel.CMDocument;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNamedNodeMap;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNode;
import org.eclipse.wst.xml.core.internal.contentmodel.ContentModelManager;
import org.eclipse.wst.xml.core.internal.contentmodel.util.DOMContentBuilder;
import org.eclipse.wst.xml.core.internal.preferences.XMLCorePreferenceNames;
import org.eclipse.wst.xml.core.internal.validation.XMLValidationConfiguration;
import org.eclipse.wst.xml.core.internal.validation.XMLValidationReport;
import org.eclipse.wst.xml.core.internal.validation.core.NestedValidatorContext;
import org.eclipse.wst.xml.core.internal.validation.core.ValidationMessage;
import org.eclipse.wst.xml.core.internal.validation.core.ValidationReport;
import org.eclipse.wst.xml.core.internal.validation.eclipse.XMLValidator;
import org.eclipse.wst.xml.ui.internal.wizards.NewXMLGenerator;
import org.eclipse.wst.xml.ui.internal.wizards.XMLSchemaValidationChecker;
import org.jboss.tools.as.catalog.ServerCatalogCorePlugin;
import org.jboss.tools.as.test.core.internal.utils.MatrixUtils;
import org.jboss.tools.test.util.JobUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import junit.framework.TestCase;

/**
 * Test that all xsd's in the catalog can create a proper
 * file that validates properly. 
 */
@RunWith(value = Parameterized.class)
public class CatalogValidationTest extends TestCase {
	
	private static IProject project;
	private static HashMap<String, Integer> expectedErrors;
	
	// Some xsd have errors that we are currently ignoring. 
	// Until they get fixed upstream, we account for these validation errors here. 
	static {
		expectedErrors = new HashMap<String, Integer>();
		expectedErrors.put("module-1_0.xsd", 2);
		expectedErrors.put("module-1_1.xsd", 4);
		expectedErrors.put("module-1_2.xsd", 4);
		expectedErrors.put("module-1_3.xsd", 4);
		expectedErrors.put("module-1_5.xsd", 4);
		expectedErrors.put("xmldsig-core-schema.xsd", 4);
		// wildfly-client_1_0.xml requires additional namespaces to have functional child elements ootb
		expectedErrors.put("wildfly-client_1_0.xsd", 1);
		// seems the generator does not support groups
		expectedErrors.put("wildfly-distributable-web_1_0.xsd", 1);
		expectedErrors.put("wildfly-distributable-web_2_0.xsd", 1);
		// Generation doesn't create required child elements sometimes. 
		expectedErrors.put("jboss-ejb-iiop_1_0.xsd", 1);
	}
	
	private static ArrayList<String> noRootElement = new ArrayList<String>();
	static {
		// Some schema have no root elements and thus can't be generated. Maybe this is a bug? Idk. 
		noRootElement.add("jboss-common_5_1.xsd");
		noRootElement.add("jboss-common_6_0.xsd");
		noRootElement.add("jboss-common_7_0.xsd");
		noRootElement.add("jboss-common_7_1.xsd");
		noRootElement.add("jboss-common_8_0.xsd");
		noRootElement.add("jboss-common_8_1.xsd");
		noRootElement.add("xml.xsd");
		noRootElement.add("wildfly-credential-reference_1_0.xsd");
		noRootElement.add("wildfly-credential-reference_1_1.xsd");
	}
	
	@Parameters
	public static Collection<Object[]> data() {
		ICatalog xmlCatalog = XMLCorePlugin.getDefault().getDefaultXMLCatalog();
		ArrayList<ICatalogEntry> list = new ArrayList<ICatalogEntry>();
		fillCatalogEntryList(xmlCatalog, list);
		return MatrixUtils.toMatrix(new Object[][]{
				(ICatalogEntry[]) list.toArray(new ICatalogEntry[list.size()])
		});
	}
	
	private static void fillCatalogEntryList(ICatalog c, ArrayList<ICatalogEntry> list) {
		ICatalogEntry[] all = c.getCatalogEntries();
		for( int i = 0; i < all.length; i++ ) {
			String uri = all[i].getURI();
			if( uri.contains(ServerCatalogCorePlugin.PLUGIN_ID))
				list.add(all[i]);
		}
		INextCatalog[] nextCatalogs = c.getNextCatalogs();
		for( int i = 0; i < nextCatalogs.length; i++ ) {
			fillCatalogEntryList(nextCatalogs[i].getReferencedCatalog(), list);
		}
	}
	

	private ICatalogEntry entry;
	public CatalogValidationTest(ICatalogEntry entry) {
		this.entry = entry;
	}
	
	
	@BeforeClass
	public static void createProject() {
		IProject t1 = ResourcesPlugin.getWorkspace().getRoot().getProject("catalogtest");
		try {
			// Let's just create a project
			t1.create(new NullProgressMonitor());
			JobUtils.waitForIdle();
			t1.open(new NullProgressMonitor());
			JobUtils.waitForIdle();
			project = t1;
		} catch(CoreException ce) {
			fail(ce.getMessage());
		}
	}
	
	@AfterClass
	public static void deleteProject() {
		try {
			project.delete(true, true, new NullProgressMonitor());
		} catch(CoreException ce) {
			ce.printStackTrace();
		}
	}

	@Test
	public void testValidateXLFiles() {
		testOneSchema(project, entry);
	}
	
	private void testOneSchema(IProject project, ICatalogEntry n) {
		String lastSegment = new Path(n.getURI()).lastSegment();
		if(noRootElement.contains(lastSegment)) {
			// ignore this test, fail gracefully
			return;
		}
		
		IFile file = null;
		try {
			project.refreshLocal(10, new NullProgressMonitor());
			NewXMLGenerator gen = createGeneratorForCatalogEntry(n);
			String fname = lastSegment.replace(".xsd", ".xml");
			file = project.getFile(fname);
			file.delete(true, new NullProgressMonitor());
			file.create(new ByteArrayInputStream("".getBytes()), true, new NullProgressMonitor());
			gen.createXMLDocument(file, file.getLocation().toOSString());
			System.out.println(file.getLocation().toOSString());
			project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			ValidationReport report = validate(file.getLocation().toFile().toURI().toString(), file.getContents(), project, null);
			
			// The validator will return invalid for any xml file
			// based on a schema with errors. We can't allow 20 files to fail
			// when we don't allow the schema, so, we'll just fail if 
			// there's a non-zero column number attached to the error. 
			int tangibleErrorCount = 0;
			
			boolean generatedFileIsValid = report.isValid();
			StringBuilder errorMessage = new StringBuilder();
			errorMessage.append("XML file generated from schema " + n.getURI() + " is invalid.\n");
			if( !generatedFileIsValid) {
				ValidationMessage[] msg = report.getValidationMessages();
				for( int k = 0; k < msg.length; k++ ) {
					if( msg[k].getColumnNumber() != 0 ) {
						errorMessage.append(msg[k].getMessage());
						errorMessage.append("\n");
						tangibleErrorCount++;
					}
				}
			}
			
			String key = new Path(n.getURI()).lastSegment();
			Integer expected = expectedErrors.get(key);
			int eCount = expected == null ? 0 : expected.intValue();
			assertFalse("Failure validating catalog entry " + new Path(n.getURI()).lastSegment() + ",  " + errorMessage.toString(), tangibleErrorCount > eCount);
		} catch(Exception e ) {
			e.printStackTrace();
			System.out.println("Failure validating catalog entry " + new Path(n.getURI()).lastSegment() + ",  " + e.getMessage());
			fail(e.getMessage());
		} finally {
			if( file != null ) {
				try {
					file.delete(true, new NullProgressMonitor());
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	  public ValidationReport validate(String uri, InputStream inputstream, IProject context, ValidationResult result) {
		  try {
		    XMLValidator validator = XMLValidator.getInstance();
		    IScopeContext[] fPreferenceScopes = createPreferenceScopes(context);
		    XMLValidationConfiguration configuration = new XMLValidationConfiguration();
		    try
		    {
		      //Preferences pluginPreferences = XMLCorePlugin.getDefault().getPluginPreferences();
		      configuration.setFeature(XMLValidationConfiguration.INDICATE_NO_GRAMMAR, 1);
		      final IPreferencesService preferencesService = Platform.getPreferencesService();
		      configuration.setFeature(XMLValidationConfiguration.INDICATE_NO_DOCUMENT_ELEMENT, preferencesService.getInt(XMLCorePlugin.getDefault().getBundle().getSymbolicName(), XMLCorePreferenceNames.INDICATE_NO_DOCUMENT_ELEMENT, -1, fPreferenceScopes));
		      configuration.setFeature(XMLValidationConfiguration.USE_XINCLUDE, preferencesService.getBoolean(XMLCorePlugin.getDefault().getBundle().getSymbolicName(), XMLCorePreferenceNames.USE_XINCLUDE, false, fPreferenceScopes));
		      configuration.setFeature(XMLValidationConfiguration.HONOUR_ALL_SCHEMA_LOCATIONS, preferencesService.getBoolean(XMLCorePlugin.getDefault().getBundle().getSymbolicName(), XMLCorePreferenceNames.HONOUR_ALL_SCHEMA_LOCATIONS, true, fPreferenceScopes));
		    }
		    catch(Exception e)
		    {
		      // TODO: Unable to set the preference. Log this problem.
		    }
		    
		    XMLValidationReport valreport = validator.validate(uri, inputstream, configuration, result, new NestedValidatorContext());
		    return valreport;
		  } finally {
			  if( inputstream != null ) {
				  try {
					  inputstream.close();
				  } catch(IOException ioe) {
					  // ignore
				  }
			  }
		  }
	  }
	  
	  protected IScopeContext[] createPreferenceScopes(IProject project) {
		  if (project != null && project.isAccessible()) {
			  final ProjectScope projectScope = new ProjectScope(project);
			  if (projectScope.getNode(XMLCorePlugin.getDefault().getBundle().getSymbolicName()).getBoolean(XMLCorePreferenceNames.USE_PROJECT_SETTINGS, false))
				return new IScopeContext[]{projectScope, new InstanceScope(), new DefaultScope()};
		  }
		  return new IScopeContext[]{new InstanceScope(), new DefaultScope()};
	  }

	private NewXMLGenerator createGeneratorForCatalogEntry(ICatalogEntry n) {
		NewXMLGenerator gen = new NewXMLGenerator();
		String uri = n.getURI();
		XMLSchemaValidationChecker validator = new XMLSchemaValidationChecker();
		validator.isValid(uri);
		CMDocument doc = ContentModelManager.getInstance().createCMDocument(uri, null);
		gen.setCMDocument(doc);
		gen.setDefaultSystemId(uri);
		int buildPolicy = 0;
		buildPolicy = buildPolicy | DOMContentBuilder.BUILD_FIRST_CHOICE | DOMContentBuilder.BUILD_FIRST_SUBSTITUTION;
		buildPolicy = buildPolicy | DOMContentBuilder.BUILD_TEXT_NODES;
		gen.setBuildPolicy(buildPolicy);
		// Find the default root element
		String defaultRootName = (String) (gen.getCMDocument()).getProperty("http://org.eclipse.wst/cm/properties/defaultRootName"); //$NON-NLS-1$
		CMNamedNodeMap nameNodeMap = gen.getCMDocument().getElements();
		Iterator<CMNode> it = nameNodeMap.iterator();
		String rootNode = null;
		while(it.hasNext()) {
			CMNode test = it.next();
			rootNode = (rootNode == null ? test.getNodeName() : rootNode);
			if( test.getNodeName().equals(defaultRootName)) {
				rootNode = test.getNodeName();
				break;
			}
		}
		gen.setRootElementName(rootNode);
		gen.setXMLCatalogEntry(n);
		try {
			gen.createNamespaceInfoList();
		} catch(Exception e) {
			// Ignore. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=459013
			e.printStackTrace();
		}
		return gen;
	}
	
}
