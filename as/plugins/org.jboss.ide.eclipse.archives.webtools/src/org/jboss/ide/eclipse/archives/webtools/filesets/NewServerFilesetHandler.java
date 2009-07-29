package org.jboss.ide.eclipse.archives.webtools.filesets;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;

public class NewServerFilesetHandler extends UnitedServerListener {
	private static NewServerFilesetHandler instance;
	public static NewServerFilesetHandler getDefault() {
		if( instance == null )
			instance = new NewServerFilesetHandler();
		return instance;
	}
	
	public void serverAdded(IServer server) {
		IPath fileToRead = FilesetUtil.DEFAULT_FS_ROOT.append(FilesetUtil.DEFAULT_FS_ALL_SERVERS);
		Fileset[] sets = FilesetUtil.loadFilesets(fileToRead.toFile(), null);
		ArrayList<Fileset> list = new ArrayList<Fileset>();
		list.addAll(Arrays.asList(sets));
		
		String typeId = server.getServerType().getId();
		fileToRead = FilesetUtil.DEFAULT_FS_ROOT.append(typeId);
		sets = FilesetUtil.loadFilesets(fileToRead.toFile(), null);
		list.addAll(Arrays.asList(sets));
		Fileset[] finalSets = (Fileset[]) list.toArray(new Fileset[list.size()]);
		FilesetUtil.saveFilesets(server, finalSets);
	}
}
