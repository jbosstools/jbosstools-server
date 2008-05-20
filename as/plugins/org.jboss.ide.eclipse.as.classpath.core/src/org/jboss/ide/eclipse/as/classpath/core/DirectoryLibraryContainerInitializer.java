package org.jboss.ide.eclipse.as.classpath.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

// Can't extend abstractclasspathcontainer since it assumes libraries are within our plugins
// TODO: need to implement resource change listeners like done in FlexibleProjectContainer to be usefull.
public class DirectoryLibraryContainerInitializer extends ClasspathContainerInitializer {

	public static final String CONTAINER_ID = "org.jboss.ide.eclipse.as.classpath.core.DirectoryLibraryContainer";

	@Override
	public void initialize(IPath containerPath, IJavaProject project)
			throws CoreException {
		int size = containerPath.segmentCount();
	      if (size > 0)
	      {
	         if (containerPath.segment(0).equals(this.getClasspathContainerID()))
	         {
	            IClasspathContainer container = this.createClasspathContainer(project, containerPath.removeFirstSegments(1));
	            JavaCore.setClasspathContainer(containerPath, new IJavaProject[]
	            {project}, new IClasspathContainer[]
	            {container}, null);
	         }
	      }		
	}

	private IClasspathContainer createClasspathContainer(IJavaProject project, IPath containerPath) {
		return new DirectoryLibraryContainer(project, containerPath);		
	}

	private String getClasspathContainerID() {
		return CONTAINER_ID;
	}

	static class DirectoryLibraryContainer implements IClasspathContainer {

		private final IResource file;

		final IClasspathEntry[] entries;

		private final IPath containerPath;

		private final IJavaProject project;
		
		public DirectoryLibraryContainer(IJavaProject project, IPath containerPath) {
			this.project = project;
			this.containerPath = containerPath;
			
			this.file = project.getProject().getWorkspace().getRoot().getFolder(containerPath);
			
			
			final List<IClasspathEntry> libraries = new ArrayList<IClasspathEntry>();
			
			try {
				if(file!=null && file.exists()) {
					file.accept(new IResourceProxyVisitor() {
						public boolean visit(IResourceProxy proxy) /* throws CoreException */{
							switch(proxy.getType()) {
							case IResource.FILE :
								if (proxy.getName().endsWith(".jar") || proxy.getName().endsWith(".zip")) {
									libraries.add(JavaCore.newLibraryEntry(proxy.requestFullPath(), null, null));									
								}
								return false;
							case IResource.FOLDER :
								//TODO: recursive by default or ?
								return true;
							}
							return true;
						}
					}
					, IResource.NONE);
				}
			} catch (CoreException e) {
				// TODO: log
				e.printStackTrace();
			}
			
			entries = libraries.toArray(new IClasspathEntry[0]);
		}
		
		public IClasspathEntry[] getClasspathEntries() {
			return entries;
		}

		public String getDescription() {
			return "Libraries found in " + ((file==null)?"[No directory specified]":file.getProjectRelativePath().toString());
		}

		public int getKind() {
			return K_APPLICATION;
		}

		public IPath getPath() {
			return file.getProjectRelativePath();
		}		
	}
	
}
