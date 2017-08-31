package org.jboss.tools.as.ui.bot.itests.reddeer.util;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.hamcrest.Matcher;
import org.jboss.ide.eclipse.as.reddeer.matcher.ServerConsoleContainsNoExceptionMatcher;
import org.jboss.ide.eclipse.as.reddeer.server.editor.ServerModuleWebPageEditor;
import org.jboss.ide.eclipse.as.reddeer.server.view.JBossServer;
import org.jboss.ide.eclipse.as.reddeer.server.view.JBossServerModule;
import org.eclipse.reddeer.common.exception.WaitTimeoutExpiredException;
import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.eclipse.condition.ConsoleHasNoChange;
import org.eclipse.reddeer.eclipse.condition.ConsoleHasText;
import org.eclipse.reddeer.eclipse.core.resources.Project;
import org.eclipse.reddeer.eclipse.jdt.ui.packageview.PackageExplorerPart;
import org.eclipse.reddeer.eclipse.ui.console.ConsoleView;
import org.eclipse.reddeer.eclipse.ui.dialogs.PropertyDialog;
import org.eclipse.reddeer.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.reddeer.eclipse.ui.views.markers.ProblemsView;
import org.eclipse.reddeer.eclipse.ui.views.markers.ProblemsView.ProblemType;
import org.eclipse.reddeer.eclipse.ui.wizards.datatransfer.ExternalProjectImportWizardDialog;
import org.eclipse.reddeer.eclipse.ui.wizards.datatransfer.WizardProjectsImportPage;
import org.eclipse.reddeer.eclipse.ui.wizards.newresource.BasicNewFileResourceWizard;
import org.eclipse.reddeer.eclipse.wst.common.project.facet.ui.RuntimesPropertyPage;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.ServersView2;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.ServersViewEnums.ServerPublishState;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.ServersViewEnums.ServerState;
import org.eclipse.reddeer.eclipse.wst.server.ui.wizard.ModifyModulesDialog;
import org.eclipse.reddeer.eclipse.wst.server.ui.wizard.ModifyModulesPage;
import org.eclipse.reddeer.workbench.impl.editor.TextEditor;
import org.jboss.tools.as.ui.bot.itests.Activator;
import org.junit.Test;


/**
 * Imports pre-prepared JSP project and adds it into the server. Checks:
 * 
 * <ul>
 * 	<li>the console output</li>
 * 	<li>server's label</li>
 * 	<li>project is listed under the server</li>
 * 	<li>the index.jsp of the project</li>
 * </ul>
 * @author Lucia Jelinkova
 *
 */
public class DeployJSPProjectTemplate {

	private static final Logger log = Logger.getLogger(DeployJSPProjectTemplate.class);
	
	
	public void clearConsole() {
		ConsoleView consoleView = new ConsoleView();
		consoleView.open();
		consoleView.clearConsole();		
	}
	
	public void importProject(String projectName, String projectPath, String runtimeLabel) {
		log.step("Import " + projectName);
		ExternalProjectImportWizardDialog dialog = new ExternalProjectImportWizardDialog();
		dialog.open();
		
		WizardProjectsImportPage page = new WizardProjectsImportPage(dialog);
		page.setArchiveFile(Activator.getPathToFileWithinPlugin(projectPath));
		page.selectProjects(projectName);
		
		dialog.finish();
		
		Project project = getProject(projectName);
		
		openPropertyDialog(projectName, project, runtimeLabel);
	}
	
	
	protected void openPropertyDialog(String projectName, Project project, String runtimeLabel) {
		try {
			log.step("Set targeted runtime for " + projectName);
			PropertyDialog propertyDialog = project.openProperties();
			RuntimesPropertyPage targetedRuntimes = new RuntimesPropertyPage(propertyDialog);
			propertyDialog.select(targetedRuntimes);
			targetedRuntimes.selectRuntime(runtimeLabel);
	
			propertyDialog.ok();
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
	
	public JBossServer getServer(String name) {
		return new ServersView2().getServer(JBossServer.class, name);
	}
	
	@Test
	public void deployProject(String projectName, String serverName, String expectedConsoleMessage){
		log.step("Add " + projectName + " to the server (Add module dialog)");
		JBossServer server = getServer(serverName);
		addModule(server, projectName);

		// view
		log.step("Assert module is visible on Servers view");
		assertNotNull("Server contains project", server.getModule(projectName));
		// console
		log.step("Assert console has deployment notification");
		new WaitUntil(new ConsoleHasText(expectedConsoleMessage));
		assertNoException("Error in console after deploy");
		// web
		log.step("Open module's web page");
		JBossServerModule module = server.getModule(projectName);
		module.openWebPage();
		ServerModuleWebPageEditor editor = new ServerModuleWebPageEditor(module.getLabel().getName()); 

		assertProjectIsBuilt(projectName);
		try {
			new WaitUntil(new EditorWithBrowserContainsTextCondition(editor, "Hello tests"));
		} catch (WaitTimeoutExpiredException e) {
			log.step("Deploy failed, assert project is built");
			try {
				assertProjectIsBuilt(projectName);
			} catch (Exception ee){
				log.step("*** Refresh project");
				getProject(projectName).refresh();
				log.step("Re-assert project is built");
				assertProjectIsBuilt(projectName);
				log.step("Re-throw exception");
				throw ee;
			}
			throw e;
		}
		log.step("Assert web page text");
		assertThat(editor.getText(), containsString("Hello tests!"));
		// view
		log.step("Assert server's states");
		assertThat(getServer(serverName).getLabel().getState(), is(ServerState.STARTED));
		assertThat(getServer(serverName).getLabel().getPublishState(), is(ServerPublishState.SYNCHRONIZED));
		// problems
		log.step("Assert no error on Problems view");
		ProblemsView problemsView = new ProblemsView();
		problemsView.open();
		assertThat(problemsView.getProblems(ProblemType.ERROR).size(), is(0));
	}

	private void assertProjectIsBuilt(String projectName) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		log.debug("Workspace root is: " + root.getLocation().toFile());

		IProject project = root.getProject(projectName);
		log.debug("Project location is: " + project.getLocation().toFile());
		
		IFolder buildFolder = project.getFolder("build");
		log.debug("Build folder location is: " + buildFolder.getLocation().toFile());
		log.debug("Build folder exists: " + buildFolder.exists());
		
		IFolder classesFolder = project.getFolder("build/classes/org");
		log.debug("Classes folder location is: " + classesFolder.getLocation().toFile());
		log.debug("Classes folder exists: " + classesFolder.exists());
		
		File buildFolderFilesystem = project.getFile("build").getLocation().toFile();
		log.debug("Build folder on filesystem exists: " + buildFolderFilesystem.exists());

		if (buildFolderFilesystem.exists()){
			log.debug("Children of build folder");
			for (String fileName : buildFolderFilesystem.list()){
				log.debug(fileName);
				if ("classes".equals(fileName)){
					log.debug("Children of classes folder");
					for (String fileName2 : new File(buildFolderFilesystem, fileName).list()){
						log.debug(fileName2);
					}
				}
			}
		}
		assertTrue(classesFolder.exists());
	}

	private void addModule(JBossServer server, String projectName) {
		ModifyModulesDialog modifyModulesDialog = server.addAndRemoveModules();
		ModifyModulesPage modifyModulesPage = new ModifyModulesPage(modifyModulesDialog);
		modifyModulesPage.add(projectName);
		modifyModulesDialog.finish();
	}
	
	private Project getProject(String projectName){
		PackageExplorerPart explorer = new PackageExplorerPart();
		explorer.open();
		return explorer.getProject(projectName);
	}
	

	protected void assertNoServerException(String message) {
		assertException(message, new ServerConsoleContainsNoExceptionMatcher());
	}
	
	protected boolean ignoreExceptionInConsole() {
		return false;
	}

	protected void assertNoException(String message) {
		assertException(message, not(new ConsoleContainsTextMatcher("Exception")));
	}
	
	private void assertException(String message, Matcher<ConsoleView> consoleViewMatcher){
		if (ignoreExceptionInConsole()){
			log.step("Ignore any exception in console");
			return;
		}
		
		log.step("Check exception in console");
		ConsoleView consoleView = new ConsoleView();
		consoleView.open();
		consoleView.toggleShowConsoleOnStandardOutChange(false);
		
		new WaitUntil(new ConsoleHasNoChange(), TimePeriod.LONG);
		assertThat(message, consoleView, consoleViewMatcher);

		consoleView.close();
	}
	
	
	private static final String HOT_JSP_FILE_NAME = "hot.jsp";

	public static final String JSP_CONTENT = 
				"<%@ page language=\"java\" contentType=\"text/html; charset=UTF-8\" pageEncoding=\"UTF-8\"%> \n" + 
				"<html> <body> Hot deployment </body> </html>";
	
	public void hotDeployment(String projectName){
		log.step("Create " + HOT_JSP_FILE_NAME + " file");
		BasicNewFileResourceWizard newFileDialog = new BasicNewFileResourceWizard();
		newFileDialog.open();
		WizardNewFileCreationPage page = new WizardNewFileCreationPage(newFileDialog);
		page.setFileName(HOT_JSP_FILE_NAME);
		page.setFolderPath(projectName, "WebContent");
		newFileDialog.finish();
		
		log.step("Set content of " + HOT_JSP_FILE_NAME + " file");
		TextEditor editor = new TextEditor();
		editor.setText(JSP_CONTENT);
		editor.save();
		editor.close();
		
		log.step("Show " + HOT_JSP_FILE_NAME + " file in browser");
		
		BrowserContainsTextCondition bctc = new BrowserContainsTextCondition("http://localhost:8080/" + 
				projectName + "/hot.jsp", "Hot deployment", true); 
		try {
			new WaitUntil(bctc, TimePeriod.LONG);
		} finally {
			bctc.cleanup();
		}
	}
	
	
	public void undeployProject(String serverName, String projectName, String expectedConsole){
		log.step("Undeploy " + projectName);
		JBossServer server = getServer(serverName);
		undeploy(server, projectName);
		
		// console
		log.step("Assert console has undeployment notification");
		new WaitUntil(new ConsoleHasText(expectedConsole));
		assertNoException("Error in console after undeploy");
		// view
		log.step("Assert module is not visible on Servers view");
		assertTrue("Server contains no project", server.getModules().isEmpty());	
		
		log.step("Assert server's states");
		assertThat(server.getLabel().getState(), is(ServerState.STARTED));
		assertThat(server.getLabel().getPublishState(), is(ServerPublishState.SYNCHRONIZED));
	}

	private void undeploy(JBossServer server, String projectName) {
		ModifyModulesDialog modifyModulesDialog = server.addAndRemoveModules();
		ModifyModulesPage modifyModulesPage = new ModifyModulesPage(modifyModulesDialog);
		modifyModulesPage.remove(projectName);;
		modifyModulesDialog.finish();
	}
	
}
