package org.jboss.ide.eclipse.as.wtp.core.util;

import org.eclipse.core.resources.IResource;

public interface ResourceFilter {
	public boolean accepts(IResource resource);
}
