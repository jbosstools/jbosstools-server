package org.jboss.ide.eclipse.archives.core.ant;

import org.eclipse.core.runtime.IStatus;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.AbstractBuildListener;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;

public class AntBuildListener extends AbstractBuildListener {

	public void buildFailed(IArchive pkg, IStatus status) {
		ArchivesCore.getInstance().getLogger().log(status);
	}
	public void error(IArchiveNode node, IStatus[] multi) {
		for( int i = 0; i < multi.length; i++ ) {
			ArchivesCore.getInstance().getLogger().log(multi[i]);
		}
	}
}
