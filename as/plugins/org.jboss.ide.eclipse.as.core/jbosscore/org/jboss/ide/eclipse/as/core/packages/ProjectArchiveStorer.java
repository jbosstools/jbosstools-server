package org.jboss.ide.eclipse.as.core.packages;

import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XMLBinding;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackage;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackages;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;


public class ProjectArchiveStorer {
	public static final String JST_ARCHIVE_CONFIGURATION = "jst.archive.configuration";
	private static HashMap cache = new HashMap();
	
	public static IArchive getArchiveFor(IProject project) {
		if( cache.containsKey(project))
			return (IArchive)cache.get(project);
		return loadArchiveFrom(project);
	}

	public static IArchive loadArchiveFrom(IProject project) {
		try {
			String s = project.getPersistentProperty(new QualifiedName(JBossServerCorePlugin.PLUGIN_ID, JST_ARCHIVE_CONFIGURATION));
			if( s != null ) {
				XbPackages packs = XMLBinding.unmarshal(s, new NullProgressMonitor());
				if( packs != null && packs.getAllChildren().size() == 1 ) {
					XbPackage pack = (XbPackage)packs.getAllChildren().get(0);
					IArchive node = (IArchive)ArchivesModel.createPackageNodeImpl(project.getLocation(), pack, null);
					if( node != null ) {
						cache.put(project, node);
					}
				}
			}
		} catch( CoreException ce ) {
			ce.printStackTrace();
		}
		return (IArchive)cache.get(project);
	}
	
	public static void storeArchive(IProject project, IArchive archive) {
		try {
			String s = XMLBinding.marshall(archive, new NullProgressMonitor());
			if( s != null )
				project.setPersistentProperty(new QualifiedName(JBossServerCorePlugin.PLUGIN_ID, JST_ARCHIVE_CONFIGURATION), s);
		} catch( CoreException ce) {
			ce.printStackTrace();
		}
	}
}
