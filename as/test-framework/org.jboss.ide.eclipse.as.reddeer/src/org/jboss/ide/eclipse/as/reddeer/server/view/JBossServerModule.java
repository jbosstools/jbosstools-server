package org.jboss.ide.eclipse.as.reddeer.server.view;

import org.jboss.ide.eclipse.as.reddeer.server.editor.ServerModuleWebPageEditor;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.ServerModule;
import org.eclipse.reddeer.eclipse.wst.server.ui.cnf.ServersView2;
import org.eclipse.reddeer.swt.api.TreeItem;
import org.eclipse.reddeer.swt.impl.menu.ContextMenuItem;

/**
 * Represents a module assigned to JBoss server {@link JBossServer} and contains
 * operations specific for this kind of server. 
 * 
 * @author Lucia Jelinkova
 *
 */
public class JBossServerModule extends ServerModule {
	
	protected JBossServerModule(TreeItem item, ServersView2 view) {
		super(item, view);
	}

	/**
	 * Open web page of the module. The webpage is represented by {@link ServerModuleWebPageEditor}
	 * but needs to be looked up by the client of this method since web page title can vary. 
	 * @return
	 */
	public void openWebPage(){
		activate();
		new ContextMenuItem("Show In", "Web Browser").select();
	}
}
