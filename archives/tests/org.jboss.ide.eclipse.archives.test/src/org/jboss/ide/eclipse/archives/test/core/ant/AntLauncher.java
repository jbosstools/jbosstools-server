package org.jboss.ide.eclipse.archives.test.core.ant;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import junit.framework.Assert;

import org.eclipse.ant.internal.ui.launchConfigurations.AntLaunchShortcut;
import org.eclipse.ant.internal.ui.launchConfigurations.IAntLaunchConfigurationConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.jboss.ide.eclipse.archives.test.ArchivesTest;
import org.jboss.ide.eclipse.archives.test.util.FileIOUtil;
import org.osgi.framework.Bundle;

public class AntLauncher {
	protected String projectName;
	protected String fileName;
	protected String target;
	protected IFile antFile;
	protected IProject project;
	protected ILaunch launch;
	protected ILaunchConfiguration configuration;
	protected IProcessListener listener;
	
	public interface IProcessListener {
		public void out(String text);
		public void err(String text);
	}

	public AntLauncher(String projectName, String fileName, String target) {
		this.projectName = projectName;
		this.fileName = fileName;
		this.target = target;
	}
	
	protected void createProjectData(IPath template, HashMap<String, String> replacements) throws CoreException {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			project = root.getProject(projectName);
			if( project.exists() ) 
				fail("project already exists");
			
			project.create(new NullProgressMonitor());
			if( !project.exists() )
				fail("project not created");
			
			project.open(new NullProgressMonitor());
			if( !project.isOpen() )
				fail("project is not open");
			
			antFile = project.getFile(fileName);
			if( antFile.exists())
				fail("build file already exists");
			
			String s = getBuildXmlContents(template, replacements);
			InputStream is = new ByteArrayInputStream(s.getBytes("UTF-8"));
			antFile.create(is, true, new NullProgressMonitor());
		} catch(CoreException ce ) {
			ce.printStackTrace();
			fail(ce.getMessage());
		} catch( UnsupportedEncodingException uee) {
			uee.printStackTrace();
			fail(uee.getMessage());
		} catch( IOException ioe) {
			ioe.printStackTrace();
			fail(ioe.getMessage());
		}
	}
	
	public void deleteProject() throws CoreException {
		project.delete(true, new NullProgressMonitor());
	}
	
	protected void launch() throws CoreException {
		try {
			ILaunchConfiguration config = getLaunchConfiguration(antFile.getFullPath(), project, "run", target);
			launch = config.launch("run", new NullProgressMonitor());
			IProcess[] processes = launch.getProcesses();
			Assert.assertNotNull(processes);
			Assert.assertTrue(processes.length == 1);
			
			processes[0].getStreamsProxy().getOutputStreamMonitor().addListener(
					new IStreamListener() {
						public void streamAppended(String text,
								IStreamMonitor monitor) {
							if( listener != null )
								listener.out(text);
						}
					});
			processes[0].getStreamsProxy().getErrorStreamMonitor().addListener(
					new IStreamListener() {
						public void streamAppended(String text,
								IStreamMonitor monitor) {
							if( listener != null )
								listener.err(text);
						}
					});
		} catch( CoreException ce ) {
			ce.printStackTrace();
			fail(ce.getMessage());
		}
	}
	
	private String getBuildXmlContents(IPath template, HashMap<String, String> map) throws IOException {
		Bundle bundle = ArchivesTest.getDefault().getBundle();
		URL bundleURL = FileLocator.toFileURL(bundle.getEntry(""));
		IPath bundlePath = new Path(bundleURL.getFile());
		IPath templateFile = bundlePath.append(template);
		String result = FileIOUtil.getFileContents(templateFile.toFile());
		String key, val;
		if( result != null ) {
			Iterator<String> i = map.keySet().iterator();
			while(i.hasNext()) {
				key = i.next();
				val = map.get(key);
				while(result.indexOf(key) != -1)
					result = result.replace(key, val);
			}
		}
		
		return result;
	}
	
	private ILaunchConfiguration getLaunchConfiguration(IPath filePath, IProject project, String mode, String targetAttribute) throws CoreException {
		ILaunchConfiguration configuration = null;
		configuration = AntLaunchShortcut.createDefaultLaunchConfiguration(filePath, (project != null && project.exists() ? project : null));
		try {
			if (targetAttribute != null && ! targetAttribute.equals(configuration.getAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_TARGETS, ""))) { //$NON-NLS-1$
				ILaunchConfigurationWorkingCopy copy = configuration.getWorkingCopy();
				copy.setAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_TARGETS, targetAttribute);
				copy.doSave();
			}
		} catch (CoreException exception) {
			exception.printStackTrace();
			fail(exception.getMessage());
		}
		return configuration;
	}
	
	private void fail(String msg) throws CoreException {
		Assert.fail(msg);
	}
}
