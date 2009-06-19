/**
 * JBoss by Red Hat
 * Copyright 2006-2009, Red Hat Middleware, LLC, and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.ui.console;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IPatternMatchListenerDelegate;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;
import org.jboss.ide.eclipse.as.ui.Messages;

/**
 * Pattern matcher to provide linking for 
 * Caused by: Exception: /login.xhtml @23,66 value="#{identity.usernamedoesnotexists}": Property 'usernamedoesnotexists' not found on type org.jboss.seam.security.RuleBasedIdentity

 * @author max
 *
 */
// TODO: add logging, but AS plugins has no logging support ;(
public class ELExceptionsMatcher implements IPatternMatchListenerDelegate {

	private TextConsole console;

	static final Pattern resourceLocationPattern = Pattern.compile("Exception: (.*) @(\\d+),(\\d+)"); //$NON-NLS-1$
	
	public void connect(TextConsole console) {
		this.console = console;
	}

	public void disconnect() {
		console = null;
	}

	
	public void matchFound(PatternMatchEvent event) {

		String line = null;
		try {
			line = console.getDocument().get(event.getOffset(),
					event.getLength());
		} catch (BadLocationException e1) {
			return;
		}

		

		Matcher matcher;
		synchronized (resourceLocationPattern) {
			matcher = resourceLocationPattern.matcher(line);
		}

		String resource = null, lineNum, columnNum = null;
		int resourceStart = -1, resourceEnd = -1, columnEnd = -1;
		if (matcher.find()) {
			resource = matcher.group(1);
			resourceStart = matcher.start(1);
			resourceEnd = matcher.end(1);

			lineNum = matcher.group(2);
			columnNum = matcher.group(3);
            columnEnd = matcher.end(3);
            
            int space = resource.lastIndexOf(' ');
			if(space!=-1) {
            	resource = resource.substring(space).trim();
            	resourceStart = resourceEnd - resource.length();
            }
			CustomFileLink customFileLink = new CustomFileLink(console,
					resource, lineNum);
			try {
				console.addHyperlink(customFileLink, event.getOffset()+resourceStart, columnEnd
						- resourceStart);
			} catch (BadLocationException e) {
				// Can't do anything
				return;
			}
		}
				
	}

	/**
	 * Custom link to only do possible expensive lookups when it is actually requested by the user.
	 * @author max
	 *
	 */
	static class CustomFileLink implements IHyperlink {

		private TextConsole console;
		private final String resource;
		private final String lineNum;

		CustomFileLink(TextConsole console, String resourceName, String lineNum) {
			this.console = console;
			this.resource = resourceName;
			this.lineNum = lineNum;
		}

		private ILaunch getLaunch() {
			IProcess process = (IProcess) console
					.getAttribute(IDebugUIConstants.ATTR_CONSOLE_PROCESS);
			if (process != null) {
				return process.getLaunch();
			}
			return null;
		}

		public void linkActivated() {
			ILaunch launch = getLaunch();
			if (launch != null) {
				FileLink launchSpecificFile = getLaunchSpecificFile(launch);
				if (launchSpecificFile != null) {
					launchSpecificFile.linkActivated();
					return;
				}
			} else { // no launch associated, search recursively all
						// projects.
				FileLink launchSpecificFile = findFileInWorkspace();
				if (launchSpecificFile != null) {
					launchSpecificFile.linkActivated();
					return;
				}				
			}
			
			// Nothing found, lets inform the user about that.
			MessageDialog.openInformation(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
					Messages.ConsoleResourceNotFound, 
					NLS.bind(Messages.ConsoleCouldNotLocateInWorkspace, resource));

		}

		private FileLink findFileInWorkspace() {
			final String simpleName = resource.substring(resource
					.lastIndexOf("/") + 1); //$NON-NLS-1$

			IPath path = new Path(resource);
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
					.getProjects();

			final String finalResource = resource;

			final List<IFile> files = new ArrayList<IFile>();
			for (int i = 0; i < projects.length && files.isEmpty(); i++) {
				IProject project = projects[i];
				try {
					project.accept(new IResourceProxyVisitor() {

						public boolean visit(IResourceProxy proxy)
								throws CoreException {
							if (proxy.requestResource().getType() != IResource.FILE) {
								return true;
							}
							String n = proxy.getName();

							if (n.equals(simpleName)) {
								IPath requestFullPath = proxy.requestFullPath();
								if (requestFullPath.toOSString().endsWith(
										finalResource)) {
									files.add((IFile) proxy.requestResource());
								}
							}
							return true;
						}

					}, 0);
				} catch (CoreException e1) {
					//

				}
			}

			if (files.size() != 0) {
				IFile file = (IFile) files.get(0);
				if (file != null && file.exists()) {
					FileLink link = new FileLink(file, null, -1, -1, Integer
							.parseInt(lineNum));
					return link;
				}
			}

			return null;
		}

		private FileLink getLaunchSpecificFile(ILaunch launch) {
			try {
				Object resolveSourceElement = resolveSourceElement(resource,
						launch);
				if (resolveSourceElement != null
						&& resolveSourceElement instanceof IFile) {
					IFile file = (IFile) resolveSourceElement;
					FileLink link = new FileLink(file, null, -1, -1, Integer
							.parseInt(lineNum));
					return link;
				}
			} catch (CoreException e) {
				// resolveSourceElement somehow failed
			}

			return null;
		}

		/** Try and locate a file via the launch support for sourcelookup */
		private static Object resolveSourceElement(Object object, ILaunch launch)
				throws CoreException {
			ISourceLocator sourceLocator = launch.getSourceLocator();
			if (sourceLocator instanceof ISourceLookupDirector) {
				ISourceLookupDirector director = (ISourceLookupDirector) sourceLocator;
				Object[] objects = director.findSourceElements(object);
				if (objects.length > 0) {
					return objects[0];
				}
			}
			return null;
		}

		public void linkEntered() {
			 // noop
		}

		public void linkExited() {
			// noop
		}

	}
}
