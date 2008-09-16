package org.jboss.ide.eclipse.archives.core.ant;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchivesVFS;
import org.jboss.ide.eclipse.archives.core.model.IVariableManager;
import org.jboss.ide.eclipse.archives.core.xpl.StringSubstitutionEngineClone;

public class AntVFS implements IArchivesVFS, IVariableManager {
	private String currentProject;
	public String performStringSubstitution(String expression,
			boolean reportUndefinedVariables) throws CoreException {
		return new StringSubstitutionEngineClone().performStringSubstitution(expression, reportUndefinedVariables, this);
	}
	public synchronized String performStringSubstitution(String expression,
			String projectName, boolean reportUndefinedVariables)
			throws CoreException {
		currentProject = projectName;
		String result = new StringSubstitutionEngineClone().performStringSubstitution(expression, reportUndefinedVariables, this);
		currentProject = null;
		return result;
	}
	public boolean containsVariable(String variable) {
		if(  IVariableManager.CURRENT_PROJECT.equals(variable) ) return true;
		if( ((AntArchivesCore)ArchivesCore.getInstance()).getProject().getProperty(variable) != null ) return true;
		return false;
	}
	
	public String getVariableValue(String variable) {
		if( IVariableManager.CURRENT_PROJECT.equals(variable))
			return currentProject;
		return ((AntArchivesCore)ArchivesCore.getInstance()).getProject().getProperty(variable);
	}

	public IPath[] getWorkspaceChildren(IPath path) {
		ArrayList<IPath> list = new ArrayList<IPath>();
		IPath pathAbsolute = workspacePathToAbsolutePath(path);
		
		if( pathAbsolute != null && pathAbsolute.toFile().exists() ) {
			String[] children = pathAbsolute.toFile().list();
			for( int i = 0; i < children.length; i++ ) {
				IPath tmp = path.append(children[i]);
				list.add(tmp);
			}
		}
		return (IPath[]) list.toArray(new IPath[list.size()]);
	}
	public IPath workspacePathToAbsolutePath(IPath path) {
		if( path.segmentCount() > 0 && path.segment(0) != null ) {
			String projNameProperty = path.segment(0) + ".dir";
			Object result = ((AntArchivesCore)ArchivesCore.getInstance()).getProject().getProperties().get(projNameProperty);
			if( result != null && result instanceof String) 
				return new Path((String)result).append(path.removeFirstSegments(1));
		}
		return null;
	}

	public String getProjectName(IPath absolutePath) {
		for (Iterator iter = ((AntArchivesCore)ArchivesCore.getInstance()).getProject().getProperties().keySet().iterator(); iter.hasNext(); ) {
			String property = (String) iter.next();
			if (property.endsWith(".dir")) {
				String val = ((AntArchivesCore)ArchivesCore.getInstance()).getProject().getProperty(property);
				if( val != null && new Path(val).equals(absolutePath)) {
					return property.substring(0, property.lastIndexOf("."));
				}
			}
		}
		return null;
	}

}
