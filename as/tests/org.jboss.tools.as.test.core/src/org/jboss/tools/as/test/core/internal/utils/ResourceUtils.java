package org.jboss.tools.as.test.core.internal.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.jboss.tools.test.util.JobUtils;

public class ResourceUtils {
	public static IFile createFile(IProject p, String filename, String contents) throws CoreException, IOException  {
		IFile resource = p.getFile(filename);
		setContents(resource, contents);
		return resource;
	}

	public static String getContents(IFile file) throws IOException, CoreException  {
		byte[] b = IOUtil.getBytesFromInputStream(file.getContents());
		return new String(b);
	}
	public static String getContents(IModuleFile file) throws IOException, CoreException  {
		File f1 = (File)file.getAdapter(File.class);
		IFile f2 = (IFile)file.getAdapter((IFile.class));
		InputStream is = f2 != null ? f2.getContents() : f1 != null ? new FileInputStream(f1) : null;
		byte[] b = IOUtil.getBytesFromInputStream(is);
		return b == null ? null : new String(b);
	}

	public static void setContents(IFile file, int val) throws IOException , CoreException{
		setContents(file, "" + val);
	}
	
	public static void setContents(IFile file, String val) throws IOException , CoreException{
		if( !file.exists()) 
			file.create(new ByteArrayInputStream((val).getBytes()), false, null);
		else
			file.setContents(new ByteArrayInputStream((val).getBytes()), false, false, new NullProgressMonitor());
		try {
			Thread.sleep(600);
		} catch( InterruptedException ie) {}
		JobUtils.waitForIdle(); 
	}
	
	public static void setContents(IProject project, IPath path, String val) throws IOException , CoreException{
		if( project.exists() ) {
			IResource member = project.findMember(path);
			if( member.exists() && member instanceof IFile ) {
				setContents((IFile)member, val);
			}
		}
	}
	
	/**
	 * This this method forwards request to IWorkspaceRoot.getProject(String name) and it 
	 * never returns null
	 * @see IWorkspaceRoot.getProject(String name) 
	 * @param name - name for the requested project
	 * @return a handler for project with <code>name</code>
	 */
	public static IProject findProject(String name) {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
	}
	
	public static void deleteProject(String name) {
		final IProject projectA = findProject("d1");
		Job deleteJob = new Job("delete d1") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					projectA.delete(true, new NullProgressMonitor());
				} catch(CoreException ce) {
					return ce.getStatus();
				}
				return Status.OK_STATUS;
			}
		};
		deleteJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
		deleteJob.schedule();
		JobUtils.delay(1000);
		JobUtils.waitForIdle();
	}


	public static IFile createJavaType(IProject p, IPath projectRelativePath, String packageName, String className) throws CoreException {
		IFolder folder = p.getFolder(projectRelativePath);
		createFolder(folder);
		IFile f = folder.getFile(className + ".java");
		String s = "package " + packageName + ";\n\npublic class " + className + "{\n\n}";
		f.create(new ByteArrayInputStream(s.getBytes()), true, new NullProgressMonitor());
		return f;
	}
	
	public static boolean createFolder(IFolder c) throws CoreException {
		if( c.exists())
			return true;
		if( !c.getParent().exists()) {
			createFolder((IFolder)c.getParent());
		}
		c.create(true, true, null);
		return true;
	}
	
	
	public static IModuleFile[] findAllIModuleFiles(IModule module) throws CoreException {
		ModuleDelegate md = (ModuleDelegate) module.loadAdapter(
				ModuleDelegate.class, new NullProgressMonitor());
		ArrayList<IModuleFile> list = new ArrayList<IModuleFile>();
		IModuleResource[] all = md.members();
		for(int i = 0; i < all.length; i++ ) {
			if( all[i] instanceof IModuleFile ) {
				list.add((IModuleFile)all[i]);
			} else if( all[i] instanceof IModuleFolder) {
				findAllIModuleFiles(list, (IModuleFolder) all[i]);
			}
		}
		return (IModuleFile[]) list.toArray(new IModuleFile[list.size()]);
	}
	public static void findAllIModuleFiles(ArrayList<IModuleFile> collector, IModuleFolder folder) {
		IModuleResource[] all = folder.members();
		for(int i = 0; i < all.length; i++ ) {
			if( all[i] instanceof IModuleFile ) {
				collector.add((IModuleFile)all[i]);
			} else if( all[i] instanceof IModuleFolder) {
				findAllIModuleFiles(collector, (IModuleFolder) all[i]);
			}
		}
	}
}
