package org.jboss.ide.eclipse.as.wtp.core.vcf;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualFolder;

/**
 * Currently does nothing at all, which is awesome.
 * Virtual components in jbt should be super dumb.
 * Stick with the model damnit. 
 * @author rob
 *
 */
public class JBTVirtualFolder extends VirtualFolder {
	private JBTVirtualComponent component;
	public JBTVirtualFolder(IProject aComponentProject, 
			IPath aRuntimePath, JBTVirtualComponent component) {
		super(aComponentProject, aRuntimePath);
		this.component = component;
	}
}
