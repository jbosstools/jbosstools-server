package org.jboss.ide.eclipse.as.test.publishing.v2;

import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.util.ModuleFile;
import org.eclipse.wst.server.core.util.ModuleFolder;
import org.jboss.ide.eclipse.as.core.publishers.patterns.PublishFilterDirectoryScanner;
import org.jboss.ide.eclipse.as.test.publishing.AbstractDeploymentTest;

public class PublishFilterDirectoryScannerTest extends TestCase {
	private IModuleResource[] members;
	
	/*        
        logout.html
        index.html
        login.html
        resources/
        resources/js/
        resources/js/script.js
        resources/css/
        resources/css/two.css
        resources/css/one.css
	    META-INF/
        META-INF/MANIFEST.MF
        META-INF/maven/
        META-INF/maven/foo.bar/
        META-INF/maven/foo.bar/webapp/
        META-INF/maven/foo.bar/webapp/pom.xml
        META-INF/maven/foo.bar/webapp/pom.properties
        WEB-INF/
        WEB-INF/web.xml
        WEB-INF/classes/
        WEB-INF/classes/foo/
        WEB-INF/classes/foo/custom/
        WEB-INF/classes/foo/custom/some.properties
        WEB-INF/classes/foo/bar/
        WEB-INF/classes/foo/bar/Dummy.class
        WEB-INF/pages/
        WEB-INF/pages/navbar.html
        WEB-INF/pages/common.html
	 */
	protected void setUp() throws Exception {
		// Dummy file... just need one to make IModuleFile objects... 
		File srcFile = AbstractDeploymentTest.getFileLocation("projectPieces/EJB3NoDescriptor.jar");
		
		// Create a fake module tree
		ArrayList<IModuleResource> resources = new ArrayList<IModuleResource>();
		// Add some root files
		resources.add(new ModuleFile(srcFile, "logout.html", new Path("/")));
		resources.add(new ModuleFile(srcFile, "login.html", new Path("/")));
		resources.add(new ModuleFile(srcFile, "index.html", new Path("/")));
		
		
		ModuleFolder resourcesF   = new ModuleFolder(null, "resources", new Path("/"));
		ModuleFolder resourcesJS  = new ModuleFolder(null, "js", new Path("resources"));
		ModuleFile   jsScript     = new ModuleFile(srcFile, "script.js", new Path("resources/js"));
		ModuleFolder resourcesCSS = new ModuleFolder(null, "css", new Path("resources"));
		ModuleFile   onecss       = new ModuleFile(srcFile, "one.css", new Path("resources/css"));
		ModuleFile   twocss       = new ModuleFile(srcFile, "two.css", new Path("resources/css"));

		resourcesCSS.setMembers(new IModuleResource[]{onecss, twocss});
		resourcesJS.setMembers(new IModuleResource[]{jsScript});
		resourcesF.setMembers(new IModuleResource[]{resourcesCSS, resourcesJS});
		
		
		ModuleFolder metainf = new ModuleFolder(null, "META-INF", new Path("/"));
		ModuleFile manifest = new ModuleFile(srcFile, "MANIFEST.MF", new Path("META-INF"));
		ModuleFolder metainfMaven = new ModuleFolder(null, "maven", new Path("META-INF"));
		metainf.setMembers(new IModuleResource[]{manifest, metainfMaven});
//        META-INF/maven/foo.bar/
//        META-INF/maven/foo.bar/webapp/
//        META-INF/maven/foo.bar/webapp/pom.xml
//        META-INF/maven/foo.bar/webapp/pom.properties
		ModuleFolder fooBar = new ModuleFolder(null, "foo.bar", new Path("META-INF/maven"));
		metainfMaven.setMembers(new IModuleResource[]{fooBar});
		ModuleFolder webapp = new ModuleFolder(null, "webapp", new Path("META-INF/maven/foo.bar"));
		fooBar.setMembers(new IModuleResource[]{webapp});
		ModuleFile pom = new ModuleFile(srcFile, "pom.xml", new Path("META-INF/maven/foo.bar/webapp"));
		ModuleFile pomProp = new ModuleFile(srcFile, "pom.properties", new Path("META-INF/maven/foo.bar/webapp"));
		webapp.setMembers(new IModuleResource[]{pom, pomProp});
		
//        WEB-INF/
//        WEB-INF/web.xml
//        WEB-INF/classes/
//        WEB-INF/pages/
		ModuleFolder webinf = new ModuleFolder(null, "WEB-INF", new Path("/"));
		ModuleFile webxml = new ModuleFile(srcFile, "web.xml", new Path("WEB-INF"));
		ModuleFolder webinfClasses = new ModuleFolder(null, "classes", new Path("WEB-INF"));
		ModuleFolder webinfPages = new ModuleFolder(null, "pages", new Path("WEB-INF"));
		webinf.setMembers(new IModuleResource[]{webxml, webinfClasses, webinfPages});
		
//        WEB-INF/classes/foo/
//        WEB-INF/classes/foo/custom/
//        WEB-INF/classes/foo/custom/some.properties
//        WEB-INF/classes/foo/bar/
//        WEB-INF/classes/foo/bar/Dummy.class
		ModuleFolder classesFoo = new ModuleFolder(null, "foo", new Path("WEB-INF/classes"));
		webinfClasses.setMembers(new IModuleResource[]{classesFoo});

		ModuleFolder fooCustom = new ModuleFolder(null, "custom", new Path("WEB-INF/classes/foo"));
		ModuleFile customProps = new ModuleFile(srcFile, "some.properties", new Path("WEB-INF/classes/foo/custom"));
		fooCustom.setMembers(new IModuleResource[]{customProps});
		ModuleFolder fooBar2 = new ModuleFolder(null, "bar", new Path("WEB-INF/classes/foo"));
		classesFoo.setMembers(new IModuleResource[]{fooCustom, fooBar2});
		ModuleFile dummyClass = new ModuleFile(srcFile, "Dummy.class", new Path("WEB-INF/classes/foo/bar"));
		fooBar2.setMembers(new IModuleResource[]{dummyClass});

//      WEB-INF/pages/navbar.html
//      WEB-INF/pages/common.html
		ModuleFile navbar = new ModuleFile(srcFile, "navbar.html", new Path("WEB-INF/pages"));
		ModuleFile commonhtml = new ModuleFile(srcFile, "common.html", new Path("WEB-INF/pages"));
		webinfPages.setMembers(new IModuleResource[]{navbar, commonhtml});
		
		resources.add(resourcesF);
		resources.add(webinf);
		resources.add(metainf);
		members = (IModuleResource[]) resources.toArray(new IModuleResource[resources.size()]);
	}
	
	public void testBasicNoFail() {
		PublishFilterDirectoryScanner scanner = new PublishFilterDirectoryScanner(members);
		scanner.scan();
	}

	public void testUtilCount() {
		assertEquals(27, countAllResources(members));
	}

	public void testIncludesSpecificFolder() {
		PublishFilterDirectoryScanner scanner = new PublishFilterDirectoryScanner(members);
		scanner.setIncludes(new String[]{"WEB-INF/pages/**"});
		scanner.setExcludes(new String[]{""});
		scanner.scan();
		
		assertTrue(scanner.isRequiredMember("WEB-INF"));
		assertTrue(scanner.isNotIncludedButRequired("WEB-INF"));
		assertTrue(scanner.isRequiredMember("WEB-INF/pages"));
		assertTrue(scanner.isIncludedMember("WEB-INF/pages"));
		assertTrue(scanner.isIncludedMember("WEB-INF/pages/navbar.html"));
		assertFalse(scanner.isIncludedFile("resources/css/two.css"));
		assertFalse(scanner.isRequiredMember("resources"));
		
		IModuleResource[] cleaned = scanner.getCleanedMembers();
		assertEquals(27, countAllResources(members));
		int postTotal = countAllResources(cleaned);
		assertEquals(4, postTotal);
	}

	public void testExcludesDeepFolder() {
		PublishFilterDirectoryScanner scanner = new PublishFilterDirectoryScanner(members);
		scanner.setIncludes(new String[]{"**/*"});
		scanner.setExcludes(new String[]{"WEB-INF/pages/**"});
		scanner.scan();
		
		assertTrue(scanner.isRequiredMember("resources"));
		assertTrue(scanner.isIncludedFile("resources/css/two.css"));
		assertTrue(scanner.isRequiredMember("WEB-INF"));
		assertFalse(scanner.isRequiredMember("WEB-INF/pages"));
		assertFalse(scanner.isIncludedMember("WEB-INF/pages/navbar.html"));
		assertFalse(scanner.isRequiredMember("WEB-INF/pages/navbar.html"));
		assertEquals(24, countAllResources(scanner.getCleanedMembers()));
	}
	
	public void testExcludesTrailingSlash() {
		PublishFilterDirectoryScanner scanner = new PublishFilterDirectoryScanner(members);
		scanner.setIncludes(new String[]{"**/*"});
		scanner.setExcludes(new String[]{"WEB-INF/pages"});
		scanner.scan();
		assertEquals(27, countAllResources(scanner.getCleanedMembers()));
	}
	public void testExcludesTrailingSlash2() {
		PublishFilterDirectoryScanner scanner = new PublishFilterDirectoryScanner(members);
		scanner.setIncludes(new String[]{"**/*"});
		scanner.setExcludes(new String[]{"WEB-INF/pages/"});
		scanner.scan();
		assertEquals(24, countAllResources(scanner.getCleanedMembers()));
	}
	
	public void testCommaSeparatedExcludes() {
		PublishFilterDirectoryScanner scanner = new PublishFilterDirectoryScanner(members);
		scanner.setIncludes(new String[]{"**/*"});
		scanner.setExcludes(new String[]{ "WEB-INF/pages/","WEB-INF/classes/foo/bar/"});
		scanner.scan();
		assertEquals(22, countAllResources(scanner.getCleanedMembers()));
	}

	public void testCommaSeparatedExcludesAutoSplit() {
		PublishFilterDirectoryScanner scanner = new PublishFilterDirectoryScanner(members);
		scanner.setIncludes(new String[]{"**/*"});
		scanner.setExcludes("WEB-INF/pages/,WEB-INF/classes/foo/bar/");
		scanner.scan();
		assertEquals(22, countAllResources(scanner.getCleanedMembers()));
	}
	public void testExcludesMorePowerful() {
		PublishFilterDirectoryScanner scanner = new PublishFilterDirectoryScanner(members);
		scanner.setIncludes("**/*,WEB-INF/classes/foo/bar/");
		scanner.setExcludes("WEB-INF/classes/foo/");
		scanner.scan();
		assertEquals(22, countAllResources(scanner.getCleanedMembers()));
	}
	
	// deep count
	public static int countAllResources(IModuleResource[] members) {
		int total = 0;
		for( int i = 0; i < members.length; i++ ) {
			total++;
			if( members[i] instanceof IModuleFolder ) {
				total += countAllResources(((IModuleFolder)members[i]).members());
			}
		}
		return total;
	}

}
