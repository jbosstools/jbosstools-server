/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.ide.eclipse.as.core.eap.xp;

import org.apache.maven.model.Plugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.jboss.tools.common.jdt.core.buildpath.ClasspathContainersHelper;

public class ProjectUtils {
	/*
	public static Set<?> findInstalledExtensions(Object currentProject) {
		try {
			if (currentProject != null && currentProject instanceof IProject) {
				IResource resource = ((IProject)currentProject).findMember("pom.xml");
				if (resource != null) {
				    IPath path = resource.getRawLocation().removeLastSegments(1);
				    File file = new File(path.toOSString());
				    ProjectWriter projectWriter = new FileProjectWriter(file);
				    BuildFile buildFile = new MavenBuildFile(projectWriter);
				    ListExtensions listExtensions = new ListExtensions(buildFile);
				    Map<?,?> extensions = (Map<?,?>)getFindInstalledMethod().invoke(listExtensions);
				    return extensions.keySet(); 
				}
			}
		} catch (IOException | InvocationTargetException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return new HashSet<Object>();
	}
	
	public static void createProject(			
			String name, 
			String location,
			String groupId, 
			String artefactId, 
			String version, 
			String className,
			HashMap<String, Object> context) {
		try {
			if (context == null) {
				context = new HashMap<String, Object>();
			}
			File workspaceFolder = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
			File projectFolder = new File(location);
			ProjectWriter projectWriter = new FileProjectWriter(projectFolder);
			new CreateProject(projectWriter)
					.groupId(groupId)
					.artifactId(artefactId)
					.version(version)
					.className(className)
					.doCreateProject(context);
			Set<MavenProjectInfo> projectSet = null;
			IProjectConfigurationManager projectConfigurationManager = MavenPlugin.getProjectConfigurationManager();
			MavenModelManager mavenModelManager = MavenPlugin.getMavenModelManager();
			LocalProjectScanner scanner = new LocalProjectScanner(
					workspaceFolder, //
					projectFolder.getCanonicalPath(), 
					false, 
					mavenModelManager);
			scanner.run(new NullProgressMonitor());
			projectSet = projectConfigurationManager.collectProjects(scanner.getProjects());
			ProjectImportConfiguration configuration = new ProjectImportConfiguration();
			projectConfigurationManager.importProjects(projectSet,
					configuration, new NullProgressMonitor());
		} catch (IOException | InterruptedException | CoreException e) {
			e.printStackTrace();
		}
	}

	private static Method getFindInstalledMethod() {	
		Method result = null;
		try {
			result = ListExtensions.class.getDeclaredMethod("findInstalled");
			result.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	public static void installExtension(Object currentProject, Extension extension) {
		try {
			if (currentProject instanceof IProject) {
				IProject project = (IProject) currentProject;
				ToolSupport support = getToolSupport(project);
				ToolContext context = new DefaultToolContext(project.getName() + "__installExtension", project, Collections.emptyMap(), Collections.singletonList(extension.getArtifactId()));
				support.addExtension(context);
				project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			}		
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static boolean isApplicationYAML(IFile file) {
		return isQuarkusProject(file.getProject()) &&
				("application.yaml".equals(file.getName()) || "application.yml".equals(file.getName()));
	}
*/
	public static Object getSelectedProject(Object selectedElement) {
		Object result = null;
		if (selectedElement instanceof IResource) {
			result = ((IResource) selectedElement).getProject();
		}
		return result;
	}
	
	public static boolean projectExists(String name) {
		return (name != null) 
				&& !"".equals(name) 
				&& ResourcesPlugin.getWorkspace().getRoot().getProject(name).exists();
	}
	
	public static String getProjectLocationDefault() {
		return ResourcesPlugin.getWorkspace().getRoot().getRawLocation().toOSString();
	}
	
	public static boolean isJavaProject(IProject project) {
		try {
			return project.hasNature(JavaCore.NATURE_ID);
		} catch (CoreException e) {
			return false;
		}
	}
	
	public static boolean isMavenProject(IProject project) {
		try {
			return project.hasNature("org.eclipse.m2e.core.maven2Nature");
		} catch (CoreException e) {
			return false;
		}
	}
	
	public static boolean isJBossXpProject(IProject project) {
		return isJavaProject(project) && isJBossXpProject(JavaCore.create(project));
	}

	public static boolean isJBossXpProject(IJavaProject javaProject) {
		IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().create( javaProject.getProject(), new NullProgressMonitor() );
	    try {
			Plugin plugin = facade.getMavenProject(new NullProgressMonitor()).getPlugin("org.wildfly.plugins:wildfly-jar-maven-plugin");
			if (plugin != null) {
	    		//JBossServerUIPlugin.log("Found wf plugin " + plugin, null);
	    		return true;
			} else {
	    		//JBossServerUIPlugin.log("No wf plugin " + plugin, null);
			}
	    } catch( Exception e ) {
	    	
	    }
	    return false;
	}

	/**
	 * @param project the Eclipse project
	 * @return the path to the JRE/JDK attached to the project
	 * @throws CoreException 
	 */
	public static String getJREEntry(IProject project) throws CoreException {
		IJavaProject javaProject = JavaCore.create(project);
		for(IClasspathEntry cpe : javaProject.getRawClasspath()) {
			if (cpe.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
				IClasspathContainer container = JavaCore.getClasspathContainer(cpe.getPath(), javaProject);
				if (ClasspathContainersHelper.applies(container, ClasspathContainersHelper.JRE_CONTAINER_ID)) {
					return cpe.getPath().toPortableString();
					
				}
			}
		}
		return null;
	}
}
